package jp.test.servercreate.util;


public class FormatUtil {

    public static String formatUsername(String username, Integer discriminator) {
        return formatUsername(username, String.valueOf(discriminator));
    }

    public static String formatUsername(String username, String discriminator) {
        if (username==null)
            username = "null";
        if (discriminator==null)
            discriminator = "null";
        return username+"#"+discriminator;
    }

    public static String getInviteLink(Long server_id) {
        return "https://discord.com/api/oauth2/authorize?client_id=989773676385816586&permissions=0&scope=bot%20applications.commands" + "&guild_id=" + server_id;
    }

    public static String getIconLink(Long discord_id, String icon) {
        return "https://cdn.discordapp.com/icons/"+discord_id+"/"+icon;
    }
}
