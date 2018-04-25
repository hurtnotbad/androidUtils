package com.example.lammy.androidutils.appIn;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.net.DownloadActivity;
import com.example.lammy.androidutils.share.ShareActivity;


/**
 * Created by zhangpeng30 on 2018/4/23.
 */

public class FunctionAdapter extends BaseAdapter {

    private Context mContext;
    private  static String[] functions;

    public FunctionAdapter(Context context){
        this.mContext = context;
        Resources resources = mContext.getResources();
        functions = new String[]{
                resources.getString(R.string.function_share),
                resources.getString(R.string.function_download)
        };
    }

    @Override
    public int getCount() {
        return functions.length;
    }

    @Override
    public Object getItem(int position) {
        return functions[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //用button 会和gridview抢占监听事件，导致无法监听到gridview 的item点击事件
        //        Button button = new Button(mContext);

        TextView textView = new TextView(mContext);
//        textView.setWidth(50);
//        textView.setHeight(40);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(0Xffffff00);
        textView.setText(functions[position]);
        //不添加下面中的一个布局，则会发现textView 无论怎么设置宽高都无效
//        textView.setLayoutParams(new GridView.LayoutParams(200,160));
        textView.setLayoutParams(new RelativeLayout.LayoutParams(200,120));
        return textView;
    }

    public String getFunctionName(int position){
        return  functions[position];
    }
    public void gotoFunction(String functionName){
        Resources resources = mContext.getResources();
        if(functionName.equals(resources.getString(R.string.function_share))){
            openShare();
            return;
        }
        if(functionName.equals(resources.getString(R.string.function_download))){
            openHttp();
            return;
        }


    }

    public void openShare (){
        Intent shareIntent = new Intent(mContext , ShareActivity.class);
        mContext.startActivity(shareIntent);
    }

    public void openHttp(){
        Intent downloadIntent = new Intent(mContext , DownloadActivity.class);
        mContext.startActivity(downloadIntent);
    }

}
