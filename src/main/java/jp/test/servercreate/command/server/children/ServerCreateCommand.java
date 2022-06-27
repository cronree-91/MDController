package jp.test.servercreate.command.server.children;

import com.github.dockerjava.api.DockerClient;
import jp.test.servercreate.api.data.entity.ServerEntity;
import jp.test.servercreate.api.data.entity.UserEntity;
import jp.test.servercreate.api.data.repo.ServerRepository;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.command.server.ServerChildrenCommandImpl;
import jp.test.servercreate.util.EmbedUtil;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ServerCreateCommand  extends ServerChildrenCommandImpl {
    UserRepository userRepository;
    DockerClient dockerClient;
    @Autowired
    ServerRepository serverRepository;

    @Autowired
    public ServerCreateCommand(UserRepository userRepository, DockerClient dockerClient) {
        super(dockerClient, userRepository, "create", "サーバを作成します。", UserEntity.Permission.CREATE_SERVER);
        this.userRepository = userRepository;
        this.dockerClient = dockerClient;
    }
    @Override
    protected void invoke(SlashCommandEvent event, UserEntity user, InteractionHook hook) {
        List<ServerEntity> servers = serverRepository.findServersByOwner(user.id);
        if (servers.size() > user.server_limit) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("あなたのサーバー数が上限に達しています。"));
            event.reply(messageBuilder.build()).complete();
        } else {
            ServerEntity server = new ServerEntity();
            server.id = UUID.randomUUID().toString();
            server.owner = user.id;
            server.version = "1.18.2";
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


            hook.sendMessage(b.build())
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
