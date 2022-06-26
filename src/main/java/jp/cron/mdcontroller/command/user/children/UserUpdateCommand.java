package jp.cron.mdcontroller.command.user.children;

import jp.cron.mdcontroller.api.data.entity.UserEntity;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.command.ChildrenCommandImpl;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserUpdateCommand extends ChildrenCommandImpl {
    UserRepository userRepository;
    @Autowired
    public UserUpdateCommand(UserRepository userRepository) {
        super(userRepository, "update", "ユーザを更新します。", UserEntity.Permission.UPDATE_USER);
        this.userRepository = userRepository;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "target", "ユーザを指定してください。", true));
        options.add(new OptionData(OptionType.STRING, "name", "ユーザ名を指定してください。"));
        options.add(new OptionData(OptionType.INTEGER, "server_limit", "サーバーの作成数制限を指定してください。"));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity authorEntity, InteractionHook hook) {
        User user = event.getOptionsByName("user").get(0).getAsUser();
        String name = event.getOptionsByName("name").isEmpty() ? null : event.getOptionsByName("name").get(0).getAsString();
        long server_limit = event.getOptionsByName("server_limit").isEmpty() ? -1 : event.getOptionsByName("server_limit").get(0).getAsLong();
        UserEntity userEntity = userRepository.findById(user.getIdLong()).orElse(null);
        if (userEntity == null) {
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateErrorEmbed("ユーザが見つかりませんでした。"))).complete();
        } else {
            if (name != null) {
                userEntity.name = name;
            }
            if (server_limit != -1) {
                userEntity.server_limit = Math.toIntExact(server_limit);
            }
            userRepository.save(userEntity);
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":white_check_mark:", EmbedUtil.generateSuccessEmbed("ユーザを更新しました。"))).complete();
        }
    }
}
