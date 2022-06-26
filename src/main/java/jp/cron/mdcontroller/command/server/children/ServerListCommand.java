package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ServerListCommand extends SlashCommand {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServerRepository serverRepository;
    @Autowired
    DockerClient dockerClient;
    public ServerListCommand() {
        this.name = "list";
        this.help = "サーバーの一覧を表示します。";
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(
                EmbedUtil.generateErrorEmbed("このコマンドはDiscordの方針により使えません。\nスラッシュコマンド/serverを使用してください。")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        UserEntity user = userRepository.findById(event.getUser().getIdLong()).orElse(null);
        if (user==null) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("あなたはデータベース上に登録がありません。"));
            event.reply(messageBuilder.build()).complete();
        } else if (!user.permissions.contains(UserEntity.Permission.SHOW_SERVER_LIST)) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("このコマンドを実行するには以下の権限が必要です。\nSHOW_SERVER_LIST"));
            event.reply(messageBuilder.build()).complete();
        } else {
            MessageBuilder messageBuilder = new MessageBuilder();
            List<MessageEmbed> embeds = new ArrayList<>();
            List<ServerEntity> servers = serverRepository.findAll();
            for (ServerEntity server : servers) {
                embeds.add(
                        new EmbedBuilder()
                                .setTitle("サーバー: "+server.id)
                                .addField("サーバーバージョン", server.version, true)
                                .addField("サーバータイプ", server.serverType.name(), true)
                                .addField("ポート", String.valueOf(server.port), true)
                                .build()
                );
            }
            messageBuilder.setEmbeds(embeds);
            event.reply(messageBuilder.build()).complete();
        }
    }
}
