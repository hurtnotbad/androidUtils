package com.example.lammy.androidutils.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.lammy.androidutils.R;


public class MainActivity extends AppCompatActivity {
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = findViewById(R.id.grid_view);mGridView.setColumnWidth(200);
        final FunctionAdapter functionAdapter = new FunctionAdapter(this);
        mGridView.setAdapter( functionAdapter);
        System.out.println("点击");
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String functionName = functionAdapter.getFunctionName(position);
                functionAdapter.gotoFunction(functionName);
            }
        });

    }


}
