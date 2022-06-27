package jp.test.servercreate.command;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import jp.test.servercreate.api.data.entity.UserEntity;
import jp.test.servercreate.api.data.repo.UserRepository;
import jp.test.servercreate.util.EmbedUtil;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public abstract class ChildrenCommandImpl extends SlashCommand {
    protected UserEntity.Permission permission;
    protected UserRepository userRepository;
    public ChildrenCommandImpl(UserRepository userRepository, String name, String help, UserEntity.Permission permission) {
        this.userRepository = userRepository;
        this.name = name;
        this.help = help;
        this.permission = permission;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(
                EmbedUtil.generateErrorEmbed("スラッシュコマンド/userを使用してください。")
        );
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        InteractionHook hook = event.deferReply().complete();
        try {
            UserEntity user = userRepository.findById(event.getUser().getIdLong()).orElse(null);
            if (user == null) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("あなたはデータベース上に登録がありません。"));
                hook.sendMessage(messageBuilder.build()).complete();
            } else if (!user.permissions.contains(this.permission)) {
                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed("このコマンドを実行するには以下の権限が必要です。\n" + this.permission.toString()));
                hook.sendMessage(messageBuilder.build()).complete();
            } else {
                execute(event, user, hook);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(EmbedUtil.generateErrorEmbed(e.getClass().getCanonicalName()+"\n"+e.getMessage()));
            hook.sendMessage(messageBuilder.build()).complete();
        }
    }

    protected abstract void execute(SlashCommandEvent event, UserEntity user, InteractionHook hook);
}
