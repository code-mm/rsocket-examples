package org.ms.app;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.ms.module.base.view.BaseAppCompatActivity;
import org.ms.module.supper.client.Modules;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.function.Consumer;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

public class MainActivity extends BaseAppCompatActivity implements View.OnClickListener {


    private Button buttonLogin;
    private EditText editTextUsername;
    private EditText editTextPassword;

    private RSocket mRSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connect();
    }

    private void connect() {
        showDialog();
        RSocketManager.getInstance().getRSocketWebSocket().connect("192.168.1.6", 9000, new RSocketCallBack() {
            @Override
            public void onSuccess(Object o) {
                Modules.getUtilsModule().getToastUtils().show("连接成功");
                hideDialog();
                mRSocket = (RSocket) o;
            }

            @Override
            public void onException(Object o) {
                Modules.getUtilsModule().getToastUtils().show("连接失败");
                hideDialog();
            }
        });
    }

    @Override
    protected void initView() {
        buttonLogin = findViewById(R.id.buttonLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        buttonLogin.setOnClickListener(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean isFullScreen() {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLogin:

                if (mRSocket == null) {

                    connect();
                    return;
                }

                mRSocket.requestChannel(new Publisher<Payload>() {
                    @Override
                    public void subscribe(Subscriber<? super Payload> s) {
                        showDialog();
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("username", editTextUsername.getText().toString().trim());
                            jsonObject.put("password", editTextPassword.getText().toString().trim());
                            s.onNext(DefaultPayload.create("login", jsonObject.toString()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).subscribe(new Consumer<Payload>() {
                    @Override
                    public void accept(Payload payload) {
                        hideDialog();
                        Modules.getUtilsModule().getToastUtils().show(payload.getMetadataUtf8());


                    }
                });
                break;
        }
    }
}