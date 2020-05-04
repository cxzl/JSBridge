package com.cxzl.cxzljsbridge.jsbridge;

public class Constants {
    public static final String JSBRIDGE_JS_FILE = "JsBridge.js";
    public static final String SCHEME = "cxzlscheme";
    public static final String MESSAGE = "://QUEUE_MESSAGE";
    public static final String RESPONSE_MESSAGE = "://QUEUE_RESPONSE_MESSAGE";
    public static final String BRIDGE_LOADED = "://BRIDGE_LOADED";

    public static final String CALL_WEB = "javascript:WebViewJavascriptBridge.clientSendMessageToWeb('%s');";
    public static final String FETCH_QUEUE = "javascript:WebViewJavascriptBridge.clientFetchQueue()";
    public static final String FETCH_RESPONSE_QUEUE = "javascript:WebViewJavascriptBridge.clientFetchResponseQueue()";

    public static final String URL = "url";
    public static final String CALLBACK_ID_FORMAT = "JAVA_CB_%s";

}
