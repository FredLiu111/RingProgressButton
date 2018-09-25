package com.fredliu.ringprogressview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ring.fredliu.com.library.CircleProgressButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CircleProgressButton circleProgressButton = (CircleProgressButton)findViewById(R.id.circle_progress_btn);
        circleProgressButton.setCircleProcessListener(new CircleProgressButton.CircleProcessListener() {
            @Override
            public void onFinished() {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onCancelOk() {

            }

            @Override
            public void onReStart() {

            }

            @Override
            public void onStarting() {

            }
        });
    }
}
