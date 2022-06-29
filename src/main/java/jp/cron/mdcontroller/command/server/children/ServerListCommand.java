package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.PullResponseItem;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.server.ServerChildrenCommandImpl;
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
public class ServerListCommand extends ServerChildrenCommandImpl {
    UserRepository userRepository;
    DockerClient dockerClient;
    @Autowired
    ServerRepository serverRepository;

    @Autowired
    public ServerListCommand(UserRepository userRepository, DockerClient dockerClient) {
        super(dockerClient, userRepository, "list", "サーバ一覧を表示します。", UserEntity.Permission.SHOW_SERVER_LIST);
        this.userRepository = userRepository;
        this.dockerClient = dockerClient;
    }

    @Override
    protected void invoke(SlashCommandEvent event, UserEntity user, InteractionHook hook) {
        MessageBuilder messageBuilder = new MessageBuilder();
        List<MessageEmbed> embeds = new ArrayList<>();
        List<ServerEntity> servers = serverRepository.findAll();
        for (ServerEntity server : servers) {
            String status;
            try {
                InspectContainerResponse container
                        = dockerClient.inspectContainerCmd(server.containerId).exec();
                InspectContainerResponse.ContainerState containerState = container.getState();
                status = containerState.getStatus();
            } catch (NotFoundException e) {
                status = "not found";
            }
            UserEntity owner = userRepository.findById(server.owner).orElse(null);
            embeds.add(
                    new EmbedBuilder()
                            .setTitle("サーバー: "+server.id)
                            .addField("サーバーバージョン", server.version, true)
                            .addField("サーバータイプ", server.serverType.name(), true)
                            .addField("ポート", String.valueOf(server.port), true)
                            .addField("オーナー", owner.name+" ( "+owner.id+" )", true)
                            .addField("ステータス", status, false)
                            .build()
            );
        }
        messageBuilder.setContent("**サーバーの一覧を表示します**");
        messageBuilder.setEmbeds(embeds);
        hook.sendMessage(messageBuilder.build()).complete();
    }

}
