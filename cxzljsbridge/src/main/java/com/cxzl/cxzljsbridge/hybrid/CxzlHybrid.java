package com.cxzl.cxzljsbridge.hybrid;

import android.support.annotation.NonNull;

import com.cxzl.cxzljsbridge.jsbridge.ClientInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * hybrid框架启动类
 */
public class CxzlHybrid {
    public static volatile CxzlHybrid cxzlHybrid;
    //存储客户端处理web消息的实现类
    private Map<String, ClientInterface> clientInterfaceMap = new HashMap<>();

    private CxzlHybrid(){}

    public static CxzlHybrid getInstance(){
        if(cxzlHybrid == null){
            synchronized (CxzlHybrid.class){
                if(cxzlHybrid == null){
                    cxzlHybrid = new CxzlHybrid();
                }
            }
        }
        return cxzlHybrid;
    }

    public CxzlWebFragment createWebFragmnt(@NonNull String url) {
        return createWebFragmnt(url,null);
    }

    public CxzlWebFragment createWebFragmnt(@NonNull String url, CxzlWebState cxzlWebState) {
        if(url == null){
            throw new IllegalArgumentException("url must not be null");
        }
        return CxzlWebFragment.newInstance(url);
    }

    public CxzlHybrid registerInterface(String InterfaceName,ClientInterface clientInterface){
        clientInterfaceMap.put(InterfaceName,clientInterface);
        return this;
    }

    public Map<String, ClientInterface> getClientInterfaceMap() {
        return clientInterfaceMap;
    }
}
