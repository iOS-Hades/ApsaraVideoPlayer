package com.aliyun.sdk.liveshift.bean;


import com.aliyun.sdk.utils.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public class TimeLineContent {

    private static final String TAG = TimeLineContent.class.getSimpleName();
    public long current;

    public List<TimeLineInfo> timelines;

    public static TimeLineContent getInfoFromJson(JSONObject json) {
        TimeLineContent content = new TimeLineContent();
        if (json == null) {
            return content;
        }

        content.current = JsonUtil.getLong(json, "current");

        try {
            JSONArray jsonArray = json.getJSONArray("timeline");
            content.timelines = TimeLineInfo.getInfoArrayFromJson(jsonArray);
        } catch (JSONException e) {
//            VcPlayerLog.e(TAG , "e : " + e.getMessage());
        }

        return content;
    }
}
