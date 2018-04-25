package com.example.lammy.androidutils.File;

import android.os.Environment;


import com.example.lammy.androidutils.log.LogUtil;

import java.io.File;

/**
 * Created by mali18 on 17-8-30.
 */

public class FileUtil {
    /**
     *
     * @param subDir 相对与内存卡的子目录
     * @return 返回 根目录 + 子目录的文件夹
     */
    public static String getSdSunFolderPath(String subDir) {
        String sdPath = getSDPath();
        if (sdPath == null) {
            return null;
        } else {
            String folderPath = sdPath + "/" + subDir;
            File destDir = new File(folderPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            return folderPath;
        }
    }
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        return sdDir.toString();
    }

    /**
     * 删除根目录下的所有文件
     * @param root
     */
    public static void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }



    public static void deleteFileByPath(String path){
        File file = new File(path);
        if(file.exists() && file.isFile()){
            try {
                file.delete();
            } catch (Exception e) {
            }
        }
    }

    public static boolean renameFile(File oldFile , File newFile, boolean overWritten){

        if(newFile.exists()&&overWritten){
            LogUtil.e("Http" , "newFile is exist");
            if(!newFile.delete()){
                return false;
            }
        }
        boolean flag = oldFile.renameTo(newFile);
        if (flag) {
            return true;
        } else {
            return false;
        }


    }

}
