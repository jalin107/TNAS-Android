package com.terramaster;

import android.app.Activity;
import android.os.Bundle;

import com.terramaster.utils.LogM;

import java.net.Socket;

public class TestingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        int startPortRange = 0;
        int stopPortRange = 25;

        for (int i = startPortRange; i <= stopPortRange; i++) {
            try {
                Socket ServerSok = new Socket("192.168.0.100", i);

                LogM.e("Port in use: " + i);


                ServerSok.close();
            } catch (Exception e) {
            }
            LogM.e("Port not in use: " + i);
        }
    }
}
