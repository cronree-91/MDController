package jp.cron.mdcontroller.command;

import com.jagrosh.jdautilities.command.SlashCommand;
import jp.cron.mdcontroller.api.data.repo.ServerRepository;
import jp.cron.mdcontroller.api.data.repo.UserRepository;
import jp.cron.mdcontroller.bot.MainBot;
import jp.cron.mdcontroller.util.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResetCommand extends SlashCommand {
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServerRepository serverRepository;

    public ResetCommand() {
        this.name = "reset";
        this.help = "すべてのデータをリセットします。";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (event.getUser().getIdLong()!= MainBot.OWNER_ID) return;
        userRepository.deleteAll();
        serverRepository.deleteAll();
        event.replyEmbeds(EmbedUtil.generateSuccessEmbed("すべてのデータをリセットしました。")).complete();
    }
}
