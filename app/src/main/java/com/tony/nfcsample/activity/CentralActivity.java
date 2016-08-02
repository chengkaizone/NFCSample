package com.tony.nfcsample.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tony.nfcsample.R;

/**
 * Created by lance on 16/8/2.
 * 作为控制中心
 */
public class CentralActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_central);


    }
}
