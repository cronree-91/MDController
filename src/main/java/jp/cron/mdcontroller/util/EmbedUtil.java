package jp.cron.mdcontroller.util;

import jp.cron.mdcontroller.api.data.entity.ServerEntity;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class EmbedUtil {
    public static MessageEmbed generateErrorEmbed(String message) {
        EmbedBuilder b = new EmbedBuilder();
        b.setColor(Color.RED);
        b.setTitle(":x: エラーが発生しました。");
        b.setDescription(message);
        b.addField("お困りですか？", "公式サポートサーバーまでご連絡ください。\n[公式サポートサーバー](https://discord.gg/KZkgXMJ34D)", false);
        return b.build();
    }

    public static MessageEmbed generateServerSettingEmbed(ServerEntity server) {
        EmbedBuilder b = new EmbedBuilder();
        b.setTitle("サーバー"+server.id);
//        b.setDescription(server.running ? "起動中" : "停止中");
        b.addField("オーナー", "<@"+server.owner+"> ("+server.owner+")", true);
        b.addField("タイプ", server.serverType.name(), true);
        b.addField("バージョン", server.version, true);
        return b.build();
    }
}
