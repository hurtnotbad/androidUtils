package com.example.lammy.androidutils.appIn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.permission.PermissionActivity;
import com.example.lammy.androidutils.permission.Permissions;


public class MainActivity extends PermissionActivity {
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = findViewById(R.id.grid_view);mGridView.setColumnWidth(200);
        final FunctionAdapter functionAdapter = new FunctionAdapter(this);
        mGridView.setAdapter( functionAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String functionName = functionAdapter.getFunctionName(position);
                functionAdapter.gotoFunction(functionName);
            }
        });

    }


}
