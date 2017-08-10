package com.shakuro.skylocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class JavaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java);
    }

    public void onClick(View v) {
        Intent i = new Intent(this, KotlinActivity.class);
        startActivity(i);
    }
}
