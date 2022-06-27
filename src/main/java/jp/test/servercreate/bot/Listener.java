package jp.test.servercreate.bot;

import jp.test.servercreate.api.data.repo.UserRepository;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

public class Listener extends ListenerAdapter {


    @Autowired
    UserRepository userRepository;
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("BOT NAME: "+event.getJDA().getSelfUser().getName());
        MainBot.INSTANCE.reloadStatus();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        MainBot.INSTANCE.reloadStatus();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        MainBot.INSTANCE.reloadStatus();
    }
}
