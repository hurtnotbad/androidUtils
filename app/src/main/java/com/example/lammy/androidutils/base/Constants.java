package com.example.lammy.androidutils.base;

import android.os.Environment;

import java.io.File;

/**
 * Created by zhangpeng30 on 2018/4/19.
 */

public class Constants {
    public static final String lammy_utils = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "lammy-utils" + File.separator;
    public static final long REQUEST_TIME_OUT =10000;
}
