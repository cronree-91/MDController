package jp.cron.mdcontroller.bot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.vdurmont.emoji.EmojiParser;
import jp.cron.mdcontroller.command.server.ServerCommand;
import jp.cron.mdcontroller.command.user.UserCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.util.Arrays;

@Service
public class MainBot {
    public static MainBot INSTANCE;
    public static Long OWNER_ID = 731503872098697226L;

    public JDA bot;
    public CommandClient client;

    @Autowired
    public MainBot(ServerCommand serverCommand, UserCommand userCommand) {
        MainBot.INSTANCE = this;

        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix("$")
                .setOwnerId(String.valueOf(OWNER_ID))
                .setActivity(null)
                .setEmojis(EmojiParser.parseToUnicode(":o:"), EmojiParser.parseToUnicode(":bulb:"), EmojiParser.parseToUnicode(":x:"))
                .setLinkedCacheSize(200);

        cb.addCommands(
                serverCommand, userCommand
        );

        cb.addSlashCommands(
                serverCommand, userCommand
        );

        try {
            client = cb.build();
            bot = JDABuilder.create(System.getenv("BOT_TOKEN"), Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.ONLINE_STATUS)
                    .setActivity(Activity.playing("ロード中..."))
                    .addEventListeners(
                            client,
                            new Listener()
                    )
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
        } catch (LoginException e) {
            System.out.println("ログインに失敗しました。トークンが間違っています。");
            System.exit(1);
        }
    }

    public void reloadStatus() {
        bot.getPresence().setActivity(
                Activity.playing("$serverでMCサーバーを構築 | "+"0"+"サーバーを起動中")
        );
    }

}
