package com.example.lammy.androidutils.File;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class CopyFileTask extends AsyncTask<Void,Void,Void> {


    // copy file task file
    public   static String cube_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/lammy/lammy.jpg";

    private Context context;
    private TaskListener taskListener;
   public CopyFileTask(Context context, TaskListener taskListener){
        this.context = context;
        this.taskListener = taskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        File file = new File(cube_path);
        if(!file.exists() && !file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }else{
            try {
                copyBigDataToSD(context,file.getName(),cube_path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
       if(taskListener != null){
           taskListener.onTaskDone();
       }

    }


    private void copyBigDataToSD(Context context, String fileName, String strOutFileName) throws Exception {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(fileName);

        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }



    public void setTaskDoneLiteneter(TaskListener taskListener) {
        this.taskListener = taskListener;
    }


    public interface TaskListener{
        void onTaskStart();
        void onTaskDone();
    }
}

