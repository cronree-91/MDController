package jp.test.servercreate.command.server;

import com.github.dockerjava.api.DockerClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.test.servercreate.api.data.repo.ServerRepository;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.command.server.children.ServerCreateCommand;
import jp.test.servercreate.command.server.children.ServerListCommand;
import jp.test.servercreate.command.server.children.ServerStartCommand;
import jp.test.servercreate.command.server.children.ServerStopCommand;
import jp.test.servercreate.util.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerCommand extends SlashCommand {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    DockerClient dockerClient;
    @Autowired
    public ServerCommand(ServerCreateCommand serverCreateCommand, ServerStartCommand serverStartCommand, ServerStopCommand serverStopCommand, ServerListCommand serverListCommand) {
        this.name = "server";
        this.help =  "MCサーバーを管理します。";
        this.guildOnly = false;
        this.children = new SlashCommand[]{serverCreateCommand, serverStartCommand, serverStopCommand, serverListCommand};
    }

    @Override
    protected void execute(SlashCommandEvent event) {

    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(
                EmbedUtil.generateErrorEmbed("スラッシュコマンド/serverを使用してください。")
        );
    }



}
