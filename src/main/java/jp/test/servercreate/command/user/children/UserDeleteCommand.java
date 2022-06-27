package jp.test.servercreate.command.user.children;

import jp.test.servercreate.api.data.entity.UserEntity;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.command.ChildrenCommandImpl;
import jp.test.servercreate.util.EmbedUtil;
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
public class UserDeleteCommand extends ChildrenCommandImpl {
    UserRepository userRepository;
    @Autowired
    public UserDeleteCommand(UserRepository userRepository) {
        super(userRepository, "delete", "ユーザを削除します。", UserEntity.Permission.DELETE_USER);
        this.userRepository = userRepository;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user_to_delete", "ユーザを指定してください。", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity authorEntity, InteractionHook hook) {
        User user = event.getOptionsByName("user_to_delete").get(0).getAsUser();
        UserEntity userEntity = userRepository.findById(user.getIdLong()).orElse(null);
        if (userEntity == null) {
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateErrorEmbed("ユーザが見つかりませんでした。"))).complete();
        } else {
            userRepository.delete(userEntity);
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":white_check_mark:", EmbedUtil.generateSuccessEmbed("ユーザを削除しました。"))).complete();
        }
    }
}
