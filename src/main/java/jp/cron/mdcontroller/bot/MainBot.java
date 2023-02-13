package jp.cron.mdcontroller.bot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.vdurmont.emoji.EmojiParser;
import jp.cron.mdcontroller.command.ResetCommand;
import jp.cron.mdcontroller.command.server.ServerCommand;
import jp.cron.mdcontroller.command.user.UserCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginException;
import java.net.UnknownHostException;
import java.util.Arrays;

@Service
public class MainBot {
    public static MainBot INSTANCE;

    public JDA bot;
    public CommandClient client;

    @Autowired
    ServerCommand serverCommand;
    @Autowired
    UserCommand userCommand;
    @Autowired
    ResetCommand resetCommand;

    @Value("${bot.setting.prefix}")
    String prefix;
    @Value("${bot.setting.token}")
    String token;
    @Value("${bot.setting.ownerId}")
    String ownerId;

    @PostConstruct
    public void start() throws LoginException, UnknownHostException {
        MainBot.INSTANCE = this;

        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(prefix)
                .setOwnerId(ownerId)
                .setActivity(null)
                .setEmojis(EmojiParser.parseToUnicode(":o:"), EmojiParser.parseToUnicode(":bulb:"), EmojiParser.parseToUnicode(":x:"))
                .setLinkedCacheSize(200);

        cb.addCommands(
                serverCommand, userCommand, resetCommand
        );

        cb.addSlashCommands(
                serverCommand, userCommand, resetCommand
        );

        try {
            client = cb.build();
            bot = JDABuilder.create(token, Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES))
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
