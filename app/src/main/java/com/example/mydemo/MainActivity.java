package com.example.mydemo;
/**
 * @Author LPSH
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button button;
    //I did not found a better solution to solve memory leak
    static TextView textView;
    static String msgstr;

    public static class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                MainActivity.msgstr = (String) msg.obj;
                MainActivity.textView.setText(msgstr);
            }
        }
    }

    MyHandler myhandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.button = findViewById(R.id.button);
        button.setOnClickListener(this);
        textView = findViewById(R.id.text);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                new Thread(new MyThread(this.myhandler,"USD","CNY")).start();
                break;
            case R.id.text:
                break;
        }
    }
}

class MyThread implements Runnable {

    private MainActivity.MyHandler handler;
    private String scur;
    private String tcur;

    MyThread(MainActivity.MyHandler handler,String scur,String tcur) {
        this.handler = handler;
        this.scur = scur;
        this.tcur = tcur;
    }
    @Override
    public void run() {
        try {
            URL url = new URL("https://sapi.k780.com/?app=finance.rate&"+"scur="+scur+"&tcur="+tcur+"&appkey=48354&sign=d230033184e2441d03e0e5dc572fbc57&format=json");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder responseData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseData.append(line);
            }
            String response = responseData.toString();
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonObject1 = (JSONObject) jsonObject.get("result");
            String string = (String) jsonObject1.get("rate");

            Message message = this.handler.obtainMessage();
            message.obj = string;
            message.what = 1;
            this.handler.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
            Message msg = this.handler.obtainMessage();
            msg.obj = "ERROR";
            msg.what = 1;
            this.handler.sendMessage(msg);
        }
    }
}