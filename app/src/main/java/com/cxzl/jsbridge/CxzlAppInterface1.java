package com.cxzl.jsbridge;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.cxzl.cxzljsbridge.jsbridge.ClientInterface;
import com.cxzl.cxzljsbridge.jsbridge.CxzlCallback;
import com.cxzl.cxzljsbridge.jsbridge.CxzlWebView;

public class CxzlAppInterface1 implements ClientInterface {

    @Override
    public void clientProcessing(CxzlWebView webView, String data, CxzlCallback callback) {
        Context context = webView.getContext();
        if(context instanceof Activity){
            Toast.makeText(context,"客户端Interface1收到web端消息" + data,Toast.LENGTH_SHORT).show();
        }
        callback.onCallback(0,"客户端Interface1已处理消息");
    }
}
