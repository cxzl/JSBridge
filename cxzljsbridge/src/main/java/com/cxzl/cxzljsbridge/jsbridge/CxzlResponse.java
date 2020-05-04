package com.cxzl.cxzljsbridge.jsbridge;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CxzlResponse {
    //应答id，回复数据时，如果有callbackId，将callbackId赋值给responseId，接收方就知道是哪个接口的回复
    private String responseId;
    //应答状态码
    private int responseCode;
    //应答数据
    private String responseData;

    private final static String RESPONSE_ID = "responseId";
    private final static String RESPONSE_CODE = "responseCode";
    private final static String RESPONSE_DATA = "responseData";

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public static List<CxzlResponse> toList(String jsonStr){
        //web端通过evaluateJavascript返回的字符会多一层引号，这里message是json数组，去掉数组外层引号
        String removeQuotes = jsonStr.replace("\"[", "[")
                .replace("]\"", "]");
        //去掉json中的转义字符
        String escape = StringEscapeUtils.unescapeJson(removeQuotes);
        List<CxzlResponse> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(escape);
            for(int i = 0; i < jsonArray.length(); i++){
                CxzlResponse m = new CxzlResponse();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                m.setResponseId(jsonObject.has(RESPONSE_ID) ? jsonObject.getString(RESPONSE_ID):null);
                m.setResponseCode(jsonObject.has(RESPONSE_CODE) ? jsonObject.getInt(RESPONSE_CODE):0);
                m.setResponseData(jsonObject.has(RESPONSE_DATA) ? jsonObject.getString(RESPONSE_DATA):null);
                list.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
