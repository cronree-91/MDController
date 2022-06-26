package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.server.ServerCommand;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ServerCreateCommand  extends SlashCommand {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServerRepository serverRepository;
    @Autowired
    DockerClient dockerClient;
    public ServerCreateCommand() {
        this.name = "create";
        this.help = "サーバーを作成します。";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(
                EmbedUtil.generateErrorEmbed("このコマンドはDiscordの方針により使えません。\nスラッシュコマンド/serverを使用してください。")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        List<Image> images = dockerClient.listImagesCmd().exec();
        Image image = images.stream().filter(i -> String.join("",i.getRepoTags()).equals("itzg/minecraft-server:latest")).findFirst().orElse(null);
        if (image == null){
            StringBuilder builder = new StringBuilder();
            builder.append("Minecraft Serverがインストールされていません。\nインストールの準備をしています...\n");
            InteractionHook hook = event.reply(builder.toString()).complete();
            final int[] step = {1};
            dockerClient.pullImageCmd("itzg/minecraft-server")
                    .withTag("latest")
                    .exec(new ResultCallback<PullResponseItem>() {
                        @Override
                        public void onStart(Closeable closeable) {
                            builder.append("インストールを開始します...\n");
                            hook.editOriginal(builder.toString()).complete();
                        }

                        @Override
                        public void onNext(PullResponseItem object) {
                            hook.editOriginal("ステップ"+ step[0] +"を処理中...\n"+object.getStatus()+"\n"+object.getStream()).complete();
                            step[0]++;
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            hook.editOriginal(":x: エラーが発生しました。\n\n"+throwable.getClass().getCanonicalName()+"\n"+throwable.getMessage()).complete();
                        }

                        @Override
                        public void onComplete() {
                            hook.editOriginal(":o: インストールが完了しました。").complete();
                        }

                        @Override
                        public void close() throws IOException {
                        }
                    });
            return;
        }


        UserEntity user = userRepository.findById(event.getUser().getIdLong()).orElse(null);
        if (user==null) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("あなたはデータベース上に登録がありません。"));
            event.reply(messageBuilder.build()).complete();
        } else if (!user.permissions.contains(UserEntity.Permission.CREATE_SERVER)) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("このコマンドを実行するには以下の権限が必要です。\nCREATE_SERVER"));
            event.reply(messageBuilder.build()).complete();
        } else {
            List<ServerEntity> servers = serverRepository.findServersByOwner(user.id);
            if (servers.size() > user.server_limit) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("あなたのサーバー数が上限に達しています。"));
                event.reply(messageBuilder.build()).complete();
            } else {
                ServerEntity server = new ServerEntity();
                server.id = UUID.randomUUID().toString();
                server.owner = user.id;
                server.version = "1.12.2";
                server.serverType = ServerEntity.ServerType.PAPER;

                MessageBuilder b = new MessageBuilder();
                b.setEmbeds(EmbedUtil.generateServerSettingEmbed(server));

                List<SelectOption> options = new ArrayList<>();
                options.add(SelectOption.of("変更したい項目を選んでください。", "please_select").withDefault(true));
                options.add(SelectOption.of("バージョンを変更する", "version"));
                options.add(SelectOption.of("サーバータイプを変更する", "type"));
                SelectionMenu menu = SelectionMenu.create("server-setting")
                        .addOptions(options)
                        .build();
                Button submitButton = Button.primary("submit", "作成");
                Button cancelButton = Button.danger("cancel", "キャンセル");


                InteractionHook hook = event.reply(b.build())
                        .addActionRow(menu)
                        .addActionRow(submitButton)
                        .addActionRow(cancelButton)
                        .complete();
                Message msg = hook.retrieveOriginal().complete();

                event.getJDA().addEventListener(new ListenerAdapter() {
                    @Override
                    public void onButtonClick(@NotNull ButtonClickEvent event) {
                        if (event.getMessage().getIdLong()!=msg.getIdLong()) return;
                        if (event.getUser().getIdLong()!=user.id) return;
                        if (event.getButton().getId().equals("submit")) {
                            hook.editOriginal(new MessageBuilder().setContent("構築中です...\nこの操作には時間がかかる場合があります。").build()).complete();
                            InteractionHook action = event.deferReply().complete();
                            server.setupPort(serverRepository);
                            server.createContainer(dockerClient);
                            serverRepository.save(server);
                            action.sendMessage("サーバーを作成しました。\nサーバーを起動するには/server startコマンドを実行してください。\nサーバーID: "+server.id).complete();
                            event.getJDA().removeEventListener(this);
                        } else if (event.getButton().getId().equals("cancel")) {
                            MessageBuilder messageBuilder = new MessageBuilder();
                            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("サーバーを作成をキャンセルしました。"));
                            event.reply(messageBuilder.build()).complete();
                            event.getJDA().removeEventListener(this);
                        }
                    }

                    @Override
                    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
                        if (event.getMessage().getIdLong()!=msg.getIdLong()) return;
                        if (event.getUser().getIdLong()!=user.id) return;
                        SelectOption option = event.getSelectedOptions().get(0);
                        if (option.getValue().equals("version")) {
                            MessageBuilder builder = new MessageBuilder();
                            builder.setContent("このメッセージに返信する形でバージョンを指定してください。");
                            InteractionHook hook = event.reply(builder.build()).complete();
                            Message msg2 = hook.retrieveOriginal().complete();
                            event.getJDA().addEventListener(new ListenerAdapter() {
                                @Override
                                public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                                    if ( event.getAuthor().getIdLong()!=user.id ||  event.getMessage().getType()!= MessageType.INLINE_REPLY || event.getMessage().getReferencedMessage().getIdLong()!=msg2.getIdLong())
                                        return;
                                    server.version = event.getMessage().getContentRaw();
                                    event.getMessage().reply("バージョンを"+event.getMessage().getContentRaw()+"変更しました。").complete();
                                    msg2.delete().complete();
                                    msg.editMessageEmbeds(EmbedUtil.generateServerSettingEmbed(server)).complete();
                                    event.getJDA().removeEventListener(this);
                                }
                            });
                        } else if (option.getValue().equals("type")) {
                            MessageBuilder builder = new MessageBuilder();
                            builder.setContent("以下の中からタイプを選んでください。");
                            List<SelectOption> options = new ArrayList<>();
                            for (ServerEntity.ServerType val : ServerEntity.ServerType.values()) {
                                options.add(SelectOption.of(val.name(), val.name()));
                            }
                            SelectionMenu menu = SelectionMenu.create("type-setting")
                                    .addOptions(options)
                                    .build();
                            InteractionHook hook = event.reply(builder.build())
                                    .addActionRow(menu)
                                    .complete();
                            Message msg2 = hook.retrieveOriginal().complete();
                            event.getJDA().addEventListener(new ListenerAdapter() {
                                @Override
                                public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
                                    if (event.getMessage().getIdLong()!=msg2.getIdLong()) return;
                                    if (event.getUser().getIdLong()!=user.id) return;
                                    SelectOption option = event.getSelectedOptions().get(0);
                                    ServerEntity.ServerType type = ServerEntity.ServerType.valueOf(option.getValue());
                                    server.serverType = type;
                                    event.reply("サーバータイプを"+type.name()+"に変更しました。").complete();
                                    msg2.delete().complete();
                                    msg.editMessageEmbeds(EmbedUtil.generateServerSettingEmbed(server)).complete();
                                    event.getJDA().removeEventListener(this);
                                }
                            });
                        }
                    }
                });
            }
        }
    }
}
