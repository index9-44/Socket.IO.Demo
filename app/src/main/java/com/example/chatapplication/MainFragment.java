package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainFragment extends AppCompatActivity {
    private EditText mInputMessageView;
    private String mUsername="ert";
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //监听事件
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on("login", onLogin);
        mSocket.on("new message", onNewMessage);
        mSocket.connect();
        mSocket.emit("add user", mUsername);
    }
    //信息接收监控
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            String username;
            String message;
            try {
                username = data.getString("username");
                message = data.getString("message");

                Log.e("Tag","username:"+username+"     message:"+message);
            } catch (JSONException e) {
                Log.e("Tag", e.getMessage());
                return;
            }
        }

    };

    //连接监控
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
                if(null!=mUsername){
                    mSocket.emit("add user", mUsername);
                    Log.e("Tag",mUsername+"开始尝试连接");
                }else {
                    Log.e("Tag","您的登录ID为空，请检查您的变量mUsername是否为空");
                }
        }
    };

    //登录监控
    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("Tag","收到账号登录成功反馈");
            JSONObject data = (JSONObject) args[0];
            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            Log.e("Tag","当前房间人数："+numUsers);
        }
    };

    public void btn_click(View view) {
        mInputMessageView = (EditText)findViewById(R.id.message_input);
        if (null == mUsername) return;
        if (!mSocket.connected()) return;
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Log.e("Tag","null message:");
            mInputMessageView.requestFocus();
            return;
        }
        mInputMessageView.setText("");
        Log.e("Tag","message:"+message);
        //把信息放在ui上，这里官方给的demo就是把信息放在RecyclerView上
        //addMessage(mUsername, message);

        // 执行发送消息尝试。
        mSocket.emit("new message", message);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        //分别关闭onConnect，onNewMessage，onLogin
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off("new message", onNewMessage);
        mSocket.off("login", onLogin);
    }
}
