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
public class UserPermissionCommand extends ChildrenCommandImpl {
    UserRepository userRepository;
    @Autowired
    public UserPermissionCommand(UserRepository userRepository) {
        super(userRepository, "permission", "ユーザのパーミッションを更新します。", UserEntity.Permission.UPDATE_USER);
        this.userRepository = userRepository;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "target", "ユーザを指定してください。", true));
        OptionData permissions = new OptionData(OptionType.STRING, "permissions", "パーミッションを指定してください。", true);
        for (UserEntity.Permission value : UserEntity.Permission.values()) {
            permissions.addChoice(value.name(), value.name());
        }
        options.add(permissions);
        options.add(new OptionData(OptionType.BOOLEAN, "enable", "有効にする場合はtrueを指定してください。", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity authorEntity, InteractionHook hook) {
        User user = event.getOptionsByName("target").get(0).getAsUser();
        UserEntity userEntity = userRepository.findById(user.getIdLong()).orElse(null);
        if (userEntity == null) {
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateErrorEmbed("ユーザが見つかりませんでした。"))).complete();
        } else {
            UserEntity.Permission permission = UserEntity.Permission.valueOf(event.getOptionsByName("permissions").get(0).getAsString());
            boolean enable = event.getOptionsByName("enable").get(0).getAsBoolean();
            if (enable) {
                if (!userEntity.permissions.contains(permission))
                    userEntity.permissions.add(permission);
            } else {
                userEntity.permissions.remove(permission);
            }
            userRepository.save(userEntity);
            hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateSuccessEmbed("ユーザのパーミッションを更新しました。"))).complete();
        }
    }
}
