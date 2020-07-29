package com.aliyun.sdk.liveshift.request;

import android.content.Context;
import android.text.TextUtils;


import com.aliyun.sdk.utils.BaseRequest;
import com.aliyun.sdk.utils.HttpClientHelper;
import com.aliyun.sdk.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.aliyun.player.bean.ErrorCode.*;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public class GetServerTimeRequest extends BaseRequest {

private static final String TAG = "GetServerTimeRequest";

    private WeakReference<Context> mContextWeak;
    private String mHost;

    GetServerTimeRequest(Context context, String host, BaseRequest.OnRequestListener<Long> l) {
        super(context, l);
        mHost = host;
        mContextWeak = new WeakReference<Context>(context);
    }

    private HttpClientHelper httpClientHelper = null;

    @Override
    public void runInBackground() {
        String requestUrl = "https://"+ mHost + "/openapi/getutc?lhs_start=1";
        if(wantStop){
            sendFailResult(-1, "" , "");
            return;
        }
        String responseStr = null;
        try {
            httpClientHelper = new HttpClientHelper(requestUrl);
            responseStr = httpClientHelper.doGet();
            if (TextUtils.isEmpty(responseStr)) {
                sendFailResult(ERROR_SERVER_LIVESHIFT_REQUEST_ERROR.getValue(), "request fail", "");
                return;
            }

            String[] values = responseStr.split("=");
            if(values.length == 2) {
                JSONObject responseJson = new JSONObject(responseStr);
                long serverTime = JsonUtil.getLong(responseJson, "GT");
                if (serverTime == 0) {
                    //错误信息
                    sendFailResult(ERROR_SERVER_LIVESHIFT_REQUEST_ERROR.getValue(), "request fail", "");
                } else {
                    sendSuccessResult(serverTime, "");
                }
            }else{
                sendFailResult(ERROR_SERVER_LIVESHIFT_REQUEST_ERROR.getValue(), "request fail", "");
            }

        } catch (JSONException e) {
            sendFailResult(ERROR_SERVER_LIVESHIFT_DATA_PARSER_ERROR.getValue(), "response not json", "");

        } catch (Exception e) {
            sendFailResult(ERROR_SERVER_LIVESHIFT_UNKNOWN.getValue(), "unknow error", "");

        }
    }

    @Override
    public void stopInner() {
        if(httpClientHelper != null){
            httpClientHelper.stop();
        }
    }
}
