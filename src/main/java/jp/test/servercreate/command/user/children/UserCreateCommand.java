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
public class UserCreateCommand extends ChildrenCommandImpl {
    UserRepository userRepository;
    @Autowired
    public UserCreateCommand(UserRepository userRepository) {
        super(userRepository, "create", "ユーザを作成します。", UserEntity.Permission.CREATE_USER);
        this.userRepository = userRepository;
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.USER, "user", "ユーザを指定してください。", true));
        options.add(new OptionData(OptionType.STRING, "name", "ユーザ名を指定してください。", true));
        options.add(new OptionData(OptionType.INTEGER, "server_limit", "サーバーの作成数制限を指定してください。", true));
        this.options = options;
    }

    @Override
    protected void execute(SlashCommandEvent event, UserEntity authorEntity, InteractionHook hook) {
        User user = event.getOptionsByName("user").get(0).getAsUser();
        String name = event.getOptionsByName("name").get(0).getAsString();
        long server_limit = event.getOptionsByName("server_limit").get(0).getAsLong();
        UserEntity userEntity = new UserEntity();
        userEntity.id = user.getIdLong();
        userEntity.name = name;
        userEntity.server_limit = Math.toIntExact(server_limit);
        userRepository.save(userEntity);
        hook.sendMessage(EmbedUtil.generateMessageWithSingleEmbed(":o:", EmbedUtil.generateSuccessEmbed("ユーザを作成しました。"))).complete();
    }
}
