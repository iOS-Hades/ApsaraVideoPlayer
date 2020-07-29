package com.aliyun.sdk.liveshift;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aliyun.sdk.liveshift.bean.TimeLineContent;
import com.aliyun.sdk.liveshift.bean.TimeLineInfo;
import com.aliyun.sdk.liveshift.request.GetTimeShiftRequest;
import com.aliyun.sdk.player.AliLiveShiftPlayer;
import com.aliyun.player.source.LiveShift;
import com.aliyun.sdk.utils.BaseRequest;

import java.lang.ref.WeakReference;
import java.util.List;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public class LiveTimeUpdater {
    private static final String TAG = "LiveTimeUpdater";


    //////时移更新相关。、、、、

    //更新消息WHAT
    private static int WHAT_UPDATE_LIVE_TIME = 0;
    private static int WHAT_UPDATE_PLAY_TIME = 1;


    private final LiveShift mTimeShift;

    private WeakReference<Context>                        contextWeak;
    private AliLiveShiftPlayer.OnTimeShiftUpdaterListener timeShiftUpdaterListener;
    private long                                          playTime;
    private long                                          liveTime;

    private Handler timer = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == WHAT_UPDATE_LIVE_TIME) {
                updateLiveTimer();
                //，60s一次
                startUpdateLiveTimerDelay(60);
            } else if (msg.what == WHAT_UPDATE_PLAY_TIME) {
                //暂停的时候不更新播放进度，但是当前直播时间进度还是需要更新的。
                if (needPause) {

                } else {
                    playTime++;
                }

                liveTime++;

                startUpdatePlayTimerDelay(1);
            }
        }
    };

    public LiveTimeUpdater(Context context, LiveShift timeShift) {
        contextWeak = new WeakReference<Context>(context);
        mTimeShift = timeShift;
    }

    public void setStartPlayTime(long startPlayTime) {
        playTime = startPlayTime;
    }

    private void updateLiveTimer() {
        final GetTimeShiftRequest request = new GetTimeShiftRequest(contextWeak.get(), mTimeShift, new BaseRequest.OnRequestListener<TimeLineContent>() {
            @Override
            public void onSuccess(TimeLineContent requestInfo, String requestId) {
//                VcPlayerLog.d(TAG, "GetTimeShiftRequest success ...");
                if (timeShiftUpdaterListener != null) {
                    long currentTime = requestInfo.current;
                    long shiftStartTime = getStartTime(requestInfo);
                    long shiftEndTime = getEndTime(requestInfo);

                    liveTime = currentTime;
                    if(playTime < 0){
                        playTime = liveTime;
                    }
                    //这里启动播放的时间更新
                    startUpdatePlayTimerDelay(0);
                    timeShiftUpdaterListener.onUpdater(currentTime, shiftStartTime, shiftEndTime);
                }
            }

            @Override
            public void onFail(int code, String msg, String requestId) {
//                VcPlayerLog.d(TAG, "GetTimeShiftRequest onFail ..." + msg);
            }
        });
        request.getAsync();
    }

    private long getEndTime(TimeLineContent requestInfo) {
        List<TimeLineInfo> lines = requestInfo.timelines;
        if (lines != null && lines.size() > 0) {
            //取最后一个值
            return lines.get(lines.size() - 1).end;
        }
        return 0;
    }

    private long getStartTime(TimeLineContent requestInfo) {
        List<TimeLineInfo> lines = requestInfo.timelines;
        if (lines != null && lines.size() > 0) {
            //取第一个值
            return lines.get(0).start;
        }
        return 0;
    }

    //开始刷新时间轴
    public void setUpdaterListener(AliLiveShiftPlayer.OnTimeShiftUpdaterListener listener) {
        timeShiftUpdaterListener = listener;

    }

    private void startUpdatePlayTimerDelay(int second) {
        stopUpdatePlayTimer();
        timer.sendEmptyMessageDelayed(WHAT_UPDATE_PLAY_TIME, second * 1000);
    }

    private void stopUpdatePlayTimer() {
        timer.removeMessages(WHAT_UPDATE_PLAY_TIME);
    }

    private void startUpdateLiveTimerDelay(int seconds) {
        stopUpdateLiveTimer();
        timer.sendEmptyMessageDelayed(WHAT_UPDATE_LIVE_TIME, seconds * 1000);
    }

    private void stopUpdateLiveTimer() {
        timer.removeMessages(WHAT_UPDATE_LIVE_TIME);
    }


    public void startUpdater() {
        stopUpdater();
        startUpdateLiveTimerDelay(0);
    }

    public void pauseUpdater() {
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for (StackTraceElement ele : elements) {
//            VcPlayerLog.e("lfj0103pause" + TAG, ele.toString());
//        }
        needPause = true;
    }

    private boolean needPause = false;

    public void resumeUpdater() {
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for (StackTraceElement ele : elements) {
//            VcPlayerLog.w("lfj0103resume" + TAG, ele.toString());
//        }
        needPause = false;
    }

    public void stopUpdater() {

//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        for (StackTraceElement ele : elements) {
//            VcPlayerLog.d("lfj0103stop" + TAG, ele.toString());
//        }

        stopUpdateLiveTimer();
        stopUpdatePlayTimer();
    }


    public long getPlayTime() {
        return playTime;
    }

    public long getLiveTime() {
        return liveTime;
    }
}
