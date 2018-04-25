package com.example.lammy.androidutils.layout;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by madl on 2017/9/5.
 */

public class DimenUtil {
    /**
     * dp 转成px
     * @param dp
     * @return
     */
    public static float dp2px(float dp) {
        Resources r = Resources.getSystem();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    /**
     * dp转成px
     * @param dp
     * @return
     */
    public static int dp2pxInt(float dp) {
        return (int) dp2px(dp);
    }

    /**
     * mm转成px
     */
    public static int mm2px(float mm, int dpi) {
        return Math.round(mm / 25.4f * dpi);
    }
}
