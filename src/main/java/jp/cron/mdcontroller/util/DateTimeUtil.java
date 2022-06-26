package jp.cron.mdcontroller.util;

import java.time.OffsetDateTime;

public class DateTimeUtil {
    public static String f(OffsetDateTime time) {
        return "<t:"+time.toEpochSecond()+":f>";
    }

    public static String R(OffsetDateTime time) {
        return "<t:"+time.toEpochSecond()+":R>";
    }
}
