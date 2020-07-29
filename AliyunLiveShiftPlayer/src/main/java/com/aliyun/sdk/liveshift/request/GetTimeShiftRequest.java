package com.aliyun.sdk.liveshift.request;

import android.content.Context;
import android.text.TextUtils;

import com.aliyun.sdk.liveshift.bean.TimeLineContent;
import com.aliyun.player.source.LiveShift;
import com.aliyun.sdk.utils.BaseRequest;
import com.aliyun.sdk.utils.HttpClientHelper;
import com.aliyun.sdk.utils.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.aliyun.player.bean.ErrorCode.ERROR_SERVER_LIVESHIFT_DATA_PARSER_ERROR;
import static com.aliyun.player.bean.ErrorCode.ERROR_SERVER_LIVESHIFT_REQUEST_ERROR;
import static com.aliyun.player.bean.ErrorCode.ERROR_SERVER_LIVESHIFT_UNKNOWN;

/**
 * Created by lifujun on 2017/6/5.
 */

public class GetTimeShiftRequest extends BaseRequest {


    private static final String    TAG = "GetTimeShiftRequest";
    private              LiveShift mLiveShiftSource;

    private WeakReference<Context> mContextWeak;

    public GetTimeShiftRequest(Context context, LiveShift localSource, BaseRequest.OnRequestListener l) {
        super(context, l);
        mContextWeak = new WeakReference<Context>(context);
        mLiveShiftSource = localSource;
    }

    private HttpClientHelper httpClientHelper = null;

    @Override
    public void runInBackground() {
        String requestUrl = mLiveShiftSource.getTimeLineUrl();

        if (wantStop) {
            sendFailResult(-1, "", "");
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

            JSONObject responseJson = new JSONObject(responseStr);
            int        retCode      = JsonUtil.getInt(responseJson, "retCode");
            if (retCode != 0) {
                //错误信息
                sendFailResult(ERROR_SERVER_LIVESHIFT_REQUEST_ERROR.getValue(), "request fail", "");

            } else {
                TimeLineContent timeLineContent = TimeLineContent.getInfoFromJson(responseJson.getJSONObject("content"));
                sendSuccessResult(timeLineContent, "");

            }
        } catch (JSONException e) {
            sendFailResult(ERROR_SERVER_LIVESHIFT_DATA_PARSER_ERROR.getValue(), "response not json", "");
        } catch (Exception e) {
            sendFailResult(ERROR_SERVER_LIVESHIFT_UNKNOWN.getValue(), "unknow error", "");
        }
    }

    @Override
    public void stopInner() {
        if (httpClientHelper != null) {
            httpClientHelper.stop();
        }
    }


}
