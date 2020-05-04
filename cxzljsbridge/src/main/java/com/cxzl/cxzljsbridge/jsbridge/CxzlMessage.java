package com.cxzl.cxzljsbridge.jsbridge;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * web和客户端双向交互的message
 */
public class CxzlMessage {
    //接口名，根据接口处理客户端调用web端的具体功能或web端调用客户端具体功能
    private String interfaceName;
    //发送的数据
    private String data;
    //回调id，客户端发起的请求由客户端提供，web端发起的请求由web端提供
    private String callbackId;

    private final static String INTERFACE_NAME = "interfaceName";
    private final static String DATA = "data";
    private final static String CALLBACK_ID = "callbackId";

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public static List<CxzlMessage> toList(String jsonStr){
        //web端通过evaluateJavascript返回的字符会多一层引号，这里message是json数组，去掉数组外层引号
        String removeQuotes = jsonStr.replace("\"[", "[")
                .replace("]\"", "]");
        //去掉json中的转义字符
        String escape = StringEscapeUtils.unescapeJson(removeQuotes);
        List<CxzlMessage> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(escape);
            for(int i = 0; i < jsonArray.length(); i++){
                CxzlMessage m = new CxzlMessage();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                m.setInterfaceName(jsonObject.has(INTERFACE_NAME) ? jsonObject.getString(INTERFACE_NAME):null);
                m.setData(jsonObject.has(DATA) ? jsonObject.getString(DATA):null);
                m.setCallbackId(jsonObject.has(CALLBACK_ID) ? jsonObject.getString(CALLBACK_ID):null);
                list.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
