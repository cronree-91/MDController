package jp.cron.mdcontroller.command.server.children;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.server.ServerChildrenCommandImpl;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ServerStopCommand extends ServerChildrenCommandImpl {
    UserRepository userRepository;
    DockerClient dockerClient;
    @Autowired
    ServerRepository serverRepository;

    @Autowired
    public ServerStopCommand(DockerClient dockerClient, UserRepository userRepository) {
        super(dockerClient, userRepository, "stop", "サーバーを停止します。", UserEntity.Permission.STOP_SERVER);
        this.dockerClient = dockerClient;
        this.userRepository = userRepository;
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
            try {
                dockerClient.stopContainerCmd(server.containerId).exec();
                hook.sendMessage(":o: サーバーを停止しました。").complete();
            } catch (NotModifiedException e) {
                hook.sendMessage("サーバーはすでに停止しています。").complete();
            }
        }
    }

    @Override
    public List<OptionData> getOptions() {
        OptionData opt = new OptionData(OptionType.STRING, "id", "起動するサーバーのIDを指定してください。", true);
        return Collections.singletonList(opt);
    }


}
