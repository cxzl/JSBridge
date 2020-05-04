package com.cxzl.cxzljsbridge.jsbridge;

/**
 * 分发消息接口，具体的客户端业务类实现该接口，根据web端要求处理任务，并回调给web端
 */
public interface ClientInterface {
    void clientProcessing(CxzlWebView webView,String data, CxzlCallback callback);
}
