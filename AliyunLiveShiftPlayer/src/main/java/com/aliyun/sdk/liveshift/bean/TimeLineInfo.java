package com.aliyun.sdk.liveshift.bean;

import com.aliyun.sdk.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public class TimeLineInfo {

    private static final String TAG = "TimeLineInfo";
    public long start;
    public long end;

    public static List<TimeLineInfo> getInfoArrayFromJson(JSONArray jsonArray) {
        if (jsonArray == null) {
//            VcPlayerLog.w(TAG, "jsonArray == null ");
            return null;
        }
        List<TimeLineInfo> timeLineInfos = new ArrayList<TimeLineInfo>();
        int length = jsonArray.length();
//        VcPlayerLog.w(TAG, "getInfoArrayFromJson() length = " + length);
        for (int i = 0; i < length; i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TimeLineInfo timeLineInfo = getInfoFromJson(jsonObject);
                if (timeLineInfo != null) {
                    timeLineInfos.add(timeLineInfo);
                }
            } catch (JSONException e) {
//                VcPlayerLog.e(TAG , "e : " + e.getMessage());
            }

        }
//        VcPlayerLog.w(TAG, "getInfoArrayFromJson() retunr length = " + timeLineInfos.size());
        return timeLineInfos;
    }


    private static TimeLineInfo getInfoFromJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        TimeLineInfo timeLineInfo = new TimeLineInfo();
        timeLineInfo.start = JsonUtil.getLong(jsonObject, "start");
        timeLineInfo.end = JsonUtil.getLong(jsonObject, "end");

        return timeLineInfo;
    }

}
