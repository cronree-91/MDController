package jp.test.servercreate.command.user;

import com.github.dockerjava.api.DockerClient;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.test.servercreate.api.data.repo.ServerRepository;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.command.user.children.*;
import jp.test.servercreate.command.user.children.*;
import jp.test.servercreate.command.user.children.*;
import jp.test.servercreate.util.EmbedUtil;
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
    public UserCommand(UserCreateCommand userCreateCommand, UserDeleteCommand userDeleteCommand, UserPermissionCommand userPermissionCommand, UserUpdateCommand userUpdateCommand, UserListCommand userListCommand) {
        this.name = "user";
        this.help =  "ユーザを管理します。";
        this.guildOnly = false;
        this.children = new SlashCommand[]{userCreateCommand, userDeleteCommand, userPermissionCommand, userUpdateCommand, userListCommand};
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
