package com.cxzl.jsbridge;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.cxzl.cxzljsbridge.hybrid.CxzlAppInterfaceName;
import com.cxzl.cxzljsbridge.hybrid.CxzlHybrid;
import com.cxzl.cxzljsbridge.jsbridge.CxzlCallback;
import com.cxzl.cxzljsbridge.hybrid.CxzlWebFragment;
import com.cxzl.cxzljsbridge.jsbridge.CxzlMessage;

public class MainActivity extends AppCompatActivity {
    CxzlWebFragment cxzlWebFragment;
    public static final String DEMO_URL = "file:///android_asset/demo.html";
    EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMessage = findViewById(R.id.et_message);
        findViewById(R.id.btn_loadwebpage).setOnClickListener(l);
        findViewById(R.id.btn_callweb1).setOnClickListener(l);
        findViewById(R.id.btn_callweb2).setOnClickListener(l);
        loadWebFragment();

    }

    View.OnClickListener l = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.btn_loadwebpage:
                    if(cxzlWebFragment == null){
                        loadWebFragment();
                    }else{
                        cxzlWebFragment.reloadWebView();
                    }
                    break;
                case R.id.btn_callweb1:
                    if(cxzlWebFragment == null){
                        return;
                    }
                    CxzlMessage message = new CxzlMessage();
                    message.setInterfaceName("cxzl.web.interface1");
                    message.setData(etMessage.getText().toString());
                    cxzlWebFragment.callWeb(message,
                            new CxzlCallback() {
                                @Override
                                public void onCallback(int code,String data) {
                                    Toast.makeText(MainActivity.this,"客户端接口1收到web端回调"
                                                    + data, Toast.LENGTH_SHORT).show();
                                }
                    });
                    break;
                case R.id.btn_callweb2:
                    if(cxzlWebFragment == null){
                        return;
                    }
                    CxzlMessage message2 = new CxzlMessage();
                    message2.setInterfaceName("cxzl.web.interface2");
                    message2.setData(etMessage.getText().toString());
                    cxzlWebFragment.callWeb(message2,
                            new CxzlCallback() {
                                @Override
                                public void onCallback(int code,String data) {
                                    Toast.makeText(MainActivity.this,"客户端接口2收到web端回调"
                                            + data, Toast.LENGTH_SHORT).show();                                }
                            });
                    break;
            }
        }
    };

    private void loadWebFragment(){
        cxzlWebFragment = CxzlHybrid.getInstance()
                .registerInterface(CxzlAppInterfaceName.appInterface1,new CxzlAppInterface1())
                .registerInterface(CxzlAppInterfaceName.appInterface2,new CxzlAppInterface2())
                .createWebFragmnt(DEMO_URL);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_container,cxzlWebFragment);
        transaction.commit();
    }
}
