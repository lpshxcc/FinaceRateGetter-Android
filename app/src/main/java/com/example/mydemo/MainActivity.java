package com.example.mydemo;

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
    static TextView textView;
    static String msgstr;

    public static class MyHandler extends Handler {
        /**
         * Subclasses must implement this to receive messages.
         *
         * @param msg
         */
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                new Thread(new MyThread(this.myhandler)).start();
                break;
        }
    }
}

class MyThread implements Runnable {

    private MainActivity.MyHandler handler;

    MyThread(MainActivity.MyHandler handler) {
        this.handler = handler;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            URL url = new URL("https://sapi.k780.com/?app=finance.rate&scur=USD&tcur=CNY&appkey=48354&sign=d230033184e2441d03e0e5dc572fbc57&format=json");
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
            //将返回的数据分析

        } catch (Exception e) {
            e.printStackTrace();
            Message msg = this.handler.obtainMessage();
            msg.obj = "ERROR";
            msg.what = 1;
            this.handler.sendMessage(msg);
        }
    }
}