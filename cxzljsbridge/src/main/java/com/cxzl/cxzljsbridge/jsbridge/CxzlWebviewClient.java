package com.cxzl.cxzljsbridge.jsbridge;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CxzlWebviewClient extends WebViewClient {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if(jsBridgeUrlIntercept(view,url)){
            return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(jsBridgeUrlIntercept(view,url)){
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * 是否进入JsBridge拦截逻辑
     * @param view
     * @param url
     * @return
     */
    private boolean jsBridgeUrlIntercept(WebView view, String url){
        //判断是否是jsbridge的拦截协议头 是否是jsbridge的webview
        if (url.startsWith(Constants.SCHEME) && view instanceof CxzlWebView) {
            //拦截web端新消息提示，收到提示后主动去调js接口拉取消息，这个会频繁被调用，所以第一个判断
            if (url.indexOf(Constants.MESSAGE) > 0) {
                ((CxzlWebView)view).fetchWebMessage();
                return true;
            //拦截web端的消息回复
            } else if (url.indexOf(Constants.RESPONSE_MESSAGE) > 0) {
                ((CxzlWebView)view).fetchResponseMessage();
                return true;
            //拦截初始化web端初始化jsbridge的请求，一个页面只会执行一次
            //客户端不主动加载jsbridge，一定要web端发起请求才注入，是为了防止非hybrid页面也被注册jsbridge
            } else if (url.indexOf(Constants.BRIDGE_LOADED) > 0) {
                ((CxzlWebView)view).injectJsBridge();
                return true;
            }
        }
        return false;
    }
}
