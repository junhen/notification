package xx.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiaoxin on 17-4-27.
 */

public class DateTimeUtil {


    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();

    public static String getNowTime() {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    public static String time() {
        long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(time);
        return format.format(d1);
    }
}
