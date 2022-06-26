package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ServerStopCommand extends SlashCommand {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServerRepository serverRepository;
    @Autowired
    DockerClient dockerClient;
    public ServerStopCommand() {
        this.name = "stop";
        this.help = "サーバーを停止します。";
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
        } else if (!user.permissions.contains(UserEntity.Permission.STOP_SERVER)) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("このコマンドを実行するには以下の権限が必要です。\nSTOP_SERVER"));
            event.reply(messageBuilder.build()).complete();
        } else {
            String server_id = event.getOptionsByName("id").get(0).getAsString();
            System.out.println(server_id);
            ServerEntity server = serverRepository.findById(server_id).orElse(null);
            if (server==null) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("サーバーが見つかりませんでした。"));
                event.reply(messageBuilder.build()).complete();
            } else {
                try {
                    dockerClient.stopContainerCmd(server.containerId).exec();
                    event.reply(":o: サーバーを停止しました。").complete();
                } catch (NotModifiedException e) {
                    event.replyEmbeds(EmbedUtil.generateErrorEmbed("サーバーはすでに停止しています。")).complete();
                }
            }
        }
    }

    @Override
    public List<OptionData> getOptions() {
        OptionData opt = new OptionData(OptionType.STRING, "id", "起動するサーバーのIDを指定してください。", true);
        return Collections.singletonList(opt);
    }

}
