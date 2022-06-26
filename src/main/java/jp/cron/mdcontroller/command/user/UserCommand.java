package jp.cron.mdcontroller.command.user;

import com.github.dockerjava.api.DockerClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.server.children.ServerCreateCommand;
import jp.cron.mdcontroller.command.server.children.ServerListCommand;
import jp.cron.mdcontroller.command.server.children.ServerStartCommand;
import jp.cron.mdcontroller.command.server.children.ServerStopCommand;
import jp.cron.mdcontroller.command.user.children.UserCreateCommand;
import jp.cron.mdcontroller.command.user.children.UserDeleteCommand;
import jp.cron.mdcontroller.command.user.children.UserPermissionCommand;
import jp.cron.mdcontroller.command.user.children.UserUpdateCommand;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserCommand extends SlashCommand {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ServerRepository serverRepository;
    @Autowired
    DockerClient dockerClient;
    @Autowired
    public UserCommand(UserCreateCommand userCreateCommand, UserDeleteCommand userDeleteCommand, UserPermissionCommand userPermissionCommand, UserUpdateCommand userUpdateCommandu) {
        this.name = "user";
        this.help =  "ユーザを管理します。";
        this.guildOnly = false;
        this.children = new SlashCommand[]{userCreateCommand, userDeleteCommand, userPermissionCommand, userUpdateCommandu};
    }

    @Override
    protected void execute(SlashCommandEvent event) {
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(
                EmbedUtil.generateErrorEmbed("このコマンドはDiscordの方針により使えません。\nスラッシュコマンド/serverを使用してください。")
        );
    }



}
