package com.cxzl.cxzljsbridge.hybrid;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cxzl.cxzljsbridge.jsbridge.CxzlCallback;
import com.cxzl.cxzljsbridge.jsbridge.Constants;
import com.cxzl.cxzljsbridge.jsbridge.CxzlMessage;
import com.cxzl.cxzljsbridge.jsbridge.CxzlWebView;
import com.cxzl.cxzljsbridge.R;

/**
 * hybrid框架主要以fragment形式使用，也可以直接使用jsbridge的webview，很多功能需要自己实现
 */
public class CxzlWebFragment extends Fragment {
    private CxzlWebView cxzlWebView;

    public static CxzlWebFragment newInstance(String url){
        CxzlWebFragment cxzlWebFragment = new CxzlWebFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.URL,url);
        cxzlWebFragment.setArguments(bundle);
        return cxzlWebFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cxzl_web,null);
        cxzlWebView = view.findViewById(R.id.webview_cxzl);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initWebView();
    }

    private void initWebView() {
        String url = getArguments().getString(Constants.URL);
        cxzlWebView.loadUrl(url);
    }

    public void reloadWebView(){
        initWebView();
    }

    public void callWeb(CxzlMessage message, CxzlCallback callback){
        cxzlWebView.callWeb(message,callback);
    }
}
