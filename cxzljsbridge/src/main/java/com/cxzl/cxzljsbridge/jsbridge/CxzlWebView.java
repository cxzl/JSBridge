package com.cxzl.cxzljsbridge.jsbridge;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.cxzl.cxzljsbridge.hybrid.CxzlHybrid;
import com.google.gson.Gson;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CxzlWebView extends WebView {
    public static final String TAG = "CxzlWebView";
    //callback识别号
    private int uniqueId = 0;
    //存储收到消息时的callback，以便处理后再回调
    private Map<String, CxzlCallback> callbacks = new HashMap();
    //是否已加载jsbrige库
    private boolean injectJsbridge = false;

    public CxzlWebView(Context context) {
        super(context);
        init();
    }

    public CxzlWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getSettings().setJavaScriptEnabled(true);
        //web页面回调，因为jsbridge用shouldoverride回调进行通信，所以必须注册成专用的
        setWebViewClient(new CxzlWebviewClient());
        //web弹框ui相关的回调，jsbridge项目可以不处理，如果是hybrid项目也需要做一些通用处理
        setWebChromeClient(new WebChromeClient());
    }

    /**
     * 注入JsBridge的js库
     */
    public void injectJsBridge() {
        injectJsBridge(null);
    }

    public void injectJsBridge(CxzlMessage message) {
        if (getContext() == null || injectJsbridge) {
            return;
        }
        String script = getAssetsToString(Constants.JSBRIDGE_JS_FILE);
        evaluateJavascript("javascript:" + script, null);
        injectJsbridge = true;
        if(message != null){
            dispatchMessage(message);
        }
    }

    /**
     * java调用js的jsbridge方法
     *
     * @param message 发送的消息
     * @param callback 回调
     */
    public void callWeb(@NonNull CxzlMessage message, CxzlCallback callback) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        //保存callback到map中
        if (callback != null) {
            String callbackId = String.format(Constants.CALLBACK_ID_FORMAT, ++uniqueId + "_"
                    + System.currentTimeMillis());
            callbacks.put(callbackId, callback);
            message.setCallbackId(callbackId);
        }
        if(!injectJsbridge){
            injectJsBridge(message);
        }else{
            dispatchMessage(message);
        }
    }

    /**
     * 发送消息，把消息的java对象转成json字符串，调用jsbridge定义的js方法
     *
     * @param message
     */
    private void dispatchMessage(CxzlMessage message) {
        //用gson把消息从对象转为json
        String messageJson = new Gson().toJson(message);
        //对json消息的转义符进行处理
        String escape = StringEscapeUtils.unescapeJson(messageJson);
        //给messageJson包装上jsbridge定义的js调用方法
        String javascriptCode = String.format(Constants.CALL_WEB, escape);
        //一定要在主线程注入js
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            evaluateJavascript(javascriptCode, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    Log.i("cxzlresponse",value);
                }
            });
        }
    }

    /**
     * 回复消息，把消息的java对象转成json字符串，调用jsbridge定义的js方法
     *
     * @param message
     */
    private void responseMessage(CxzlResponse message) {
        //用gson把消息从对象转为json
        String messageJson = new Gson().toJson(message);
        //给messageJson包装上jsbridge定义的js调用方法
        String javascriptCode = String.format(Constants.CALL_WEB, messageJson);
        //一定要在主线程注入js
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            evaluateJavascript(javascriptCode, null);
        }
    }

    /**
     * 处理web端请求的消息
     */
    public void fetchWebMessage() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            return;
        }
        //通过js注入调用web方法，获取web端的fetchQueue方法回调的消息
        evaluateJavascript(Constants.FETCH_QUEUE, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                List<CxzlMessage> list = CxzlMessage.toList(value);
                if (list == null || list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    CxzlMessage message = list.get(i);
                    CxzlCallback callback = null;
                    final String callbackId = message.getCallbackId();
                    //如果有callbackId则设置回调给web
                    if (!TextUtils.isEmpty(callbackId)) {
                        callback = new CxzlCallback() {
                            @Override
                            public void onCallback(int code,String data) {
                                CxzlResponse responseMsg = new CxzlResponse();
                                responseMsg.setResponseId(callbackId);
                                responseMsg.setResponseData(data);
                                responseMessage(responseMsg);
                            }
                        };
                    }
                    ClientInterface clientImpl = null;
                    //查找客户端接口实现类，分发消息
                    if (!TextUtils.isEmpty(message.getInterfaceName())) {
                        clientImpl = CxzlHybrid.getInstance().getClientInterfaceMap().get(message.getInterfaceName());
                    }
                    if (clientImpl != null) {
                        clientImpl.clientProcessing(CxzlWebView.this, message.getData(), callback);
                    } else {
                        callback.onCallback(-1,"no clientInterface for message from JavaScript");
                        Log.i(TAG, "no clientInterface for message from JavaScript");
                    }
                }
            }
        });
    }

    /**
     * 处理web端回复的消息
     */
    public void fetchResponseMessage() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            return;
        }
        //通过js注入调用web方法，获取web端的fetchResponseQueue方法回调的消息
        evaluateJavascript(Constants.FETCH_RESPONSE_QUEUE, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                List<CxzlResponse> list = CxzlResponse.toList(value);
                if (list == null || list.size() == 0) {
                    return;
                }
                for (int i = 0; i < list.size(); i++) {
                    CxzlResponse message = list.get(i);
                    String responseId = message.getResponseId();
                    //根据responseId，发送给具体需要response的接口
                    if (!TextUtils.isEmpty(responseId)) {
                        CxzlCallback callback = callbacks.get(responseId);
                        callback.onCallback(message.getResponseCode(),message.getResponseData());
                        callbacks.remove(responseId);
                    }
                }
            }
        });
    }

    public String getAssetsToString(String fileName){
        InputStream in = null;
        try {
            in = getContext().getAssets().open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    sb.append(line);
                }
            } while (line != null);

            bufferedReader.close();
            in.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
