package jp.cron.mdcontroller.command.server;

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
import jp.cron.mdcontroller.command.server.children.ServerCreateCommand;
import jp.cron.mdcontroller.command.server.children.ServerListCommand;
import jp.cron.mdcontroller.command.server.children.ServerStartCommand;
import jp.cron.mdcontroller.command.server.children.ServerStopCommand;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
                EmbedUtil.generateErrorEmbed("このコマンドはDiscordの方針により使えません。\nスラッシュコマンド/serverを使用してください。")
        );
    }



}
