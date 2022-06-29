package jp.cron.mdcontroller.command.user.children;

import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.ChildrenCommandImpl;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserListCommand extends ChildrenCommandImpl {
    UserRepository userRepository;
    @Autowired
    public UserListCommand(UserRepository userRepository) {
        super(userRepository, "list", "ユーザの一覧を表示します。", UserEntity.Permission.SHOW_USER_LIST);
        this.userRepository = userRepository;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity authorEntity, InteractionHook hook) {
        List<UserEntity> userEntities = userRepository.findAll();
        if (userEntities.isEmpty()) {
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateErrorEmbed("ユーザが見つかりませんでした。"))).complete();
        } else {
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setContent("ユーザ一覧を表示します。");
            List<MessageEmbed> embeds = new ArrayList<>();
            for (UserEntity userEntity : userEntities) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(userEntity.name+" ("+userEntity.id+")");
                embedBuilder.addField("サーバー制限", String.valueOf(userEntity.server_limit), true);
                embedBuilder.addField("権限", userEntity.permissions.stream().map(p -> p.name()).collect(Collectors.joining(", ")), true);
                embeds.add(embedBuilder.build());
            }
            messageBuilder.setEmbeds(embeds);
            hook.sendMessage(messageBuilder.build()).complete();
        }
    }
}
