package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.server.ServerChildrenCommandImpl;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ServerDeleteCommand extends ServerChildrenCommandImpl {
    UserRepository userRepository;
    DockerClient dockerClient;
    @Autowired
    ServerRepository serverRepository;

    @Autowired
    public ServerDeleteCommand(UserRepository userRepository, DockerClient dockerClient) {
        super(dockerClient, userRepository, "delete", "サーバを削除します。", UserEntity.Permission.DELETE_SERVER);
        this.userRepository = userRepository;
        this.dockerClient = dockerClient;
    }

    @Override
    public List<OptionData> getOptions() {
        OptionData opt = new OptionData(OptionType.STRING, "id", "削除するサーバーのIDを指定してください。", true);
        return Collections.singletonList(opt);
    }

    @Override
    protected void invoke(SlashCommandEvent event, UserEntity user, InteractionHook hook) {
        String server_id = event.getOptionsByName("id").get(0).getAsString();
        System.out.println(server_id);
        ServerEntity server = serverRepository.findById(server_id).orElse(null);
        if (server==null) {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("サーバーが見つかりませんでした。"));
            hook.sendMessage(messageBuilder.build()).complete();
        } else {
            serverRepository.deleteById(server.id);
            try {
                dockerClient.killContainerCmd(server.containerId).exec();
                dockerClient.removeContainerCmd(server.containerId).exec();
            } catch (NotFoundException ignored) {}

        }
    }


}
