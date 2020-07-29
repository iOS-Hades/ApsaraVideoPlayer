package com.aliyun.sdk.player;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.aliyun.player.AVPBase;
import com.aliyun.sdk.liveshift.LiveTimeUpdater;
import com.aliyun.player.nativeclass.JniSaasPlayer;
import com.aliyun.player.nativeclass.NativePlayerBase;
import com.aliyun.player.source.LiveShift;
import com.aliyun.player.source.UrlSource;

import java.lang.ref.WeakReference;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public class ApsaraLiveShiftPlayer extends AVPBase implements AliLiveShiftPlayer {

    public static final int SeekLive = 10;

    private int status;

    private int statusWhenSeek;


    private long liveSeekToTime = -1;
    private long liveSeekOffset = -1;

    private LiveShift liveShiftSource = null;
    private LiveTimeUpdater liveTimeUpdater = null;

    private InnerTimeShiftUpdaterListener timeShiftUpdaterListener = null;

    public ApsaraLiveShiftPlayer(Context context) {
        super(context, (String)null);
        timeShiftUpdaterListener = new InnerTimeShiftUpdaterListener(this);
    }

    public ApsaraLiveShiftPlayer(Context context, String traceID) {
        super(context, traceID);
        timeShiftUpdaterListener = new InnerTimeShiftUpdaterListener(this);
    }

    @Override
    protected NativePlayerBase createAlivcMediaPlayer(Context context, String traceID) {
        return new JniSaasPlayer(context);
    }

    @Override
    public void setDataSource(LiveShift liveShift) {
        liveShiftSource = liveShift;
        UrlSource urlSource = new UrlSource();
        urlSource.setUri(liveShift.getUrl());
        NativePlayerBase corePlayer = getCorePlayer();
        if (corePlayer instanceof JniSaasPlayer) {
            ((JniSaasPlayer) corePlayer).setDataSource(urlSource);
        }
    }

    @Override
    public long getCurrentLiveTime() {
        if (liveTimeUpdater != null) {
            return liveTimeUpdater.getLiveTime();
        }
        return 0;
    }

    @Override
    public long getCurrentTime() {
        if (liveTimeUpdater != null) {
            return liveTimeUpdater.getPlayTime();
        }
        return 0;
    }


    @Override
    public void seekToLiveTime(long liveTime) {
        //已经在seek中了，就不去seek了。
        //防止下面的liveSeekPlayerState被改变，导致连续seek不能播放的问题。
        if (status == SeekLive) {
            return;
        }

        if (liveShiftSource == null) {
            return;
        }
        statusWhenSeek = status;

        status = SeekLive;

        liveSeekToTime = liveTime;
        liveSeekOffset = getCurrentLiveTime() - liveSeekToTime;

        if (liveSeekOffset < 0) {
            liveSeekOffset = 0;
            liveSeekToTime = getCurrentLiveTime();
        }


        String finalPlayUrl = liveShiftSource.getUrl();
        if (liveSeekToTime > 0 && liveSeekOffset > 0) {
            String queryStr = Uri.parse(finalPlayUrl).getQuery();
            if (finalPlayUrl.endsWith("?") || finalPlayUrl.endsWith("&")) {
                finalPlayUrl = finalPlayUrl + "lhs_offset_unix_s_0=" + liveSeekOffset + "&lhs_start=1&aliyunols=on";
            } else {
                if (TextUtils.isEmpty(queryStr)) {
                    finalPlayUrl = finalPlayUrl + "?lhs_offset_unix_s_0=" + liveSeekOffset + "&lhs_start=1&aliyunols=on";
                } else {
                    finalPlayUrl = finalPlayUrl + "&lhs_offset_unix_s_0=" + liveSeekOffset + "&lhs_start=1&aliyunols=on";
                }
            }

        }

        UrlSource urlSource = new UrlSource();
        urlSource.setUri(finalPlayUrl);
        NativePlayerBase corePlayer = getCorePlayer();
        if (corePlayer instanceof JniSaasPlayer) {
            stopInner();
            ((JniSaasPlayer) corePlayer).setDataSource(urlSource);
            corePlayer.prepare();
        }
    }


    @Override
    public void setOnTimeShiftUpdaterListener(OnTimeShiftUpdaterListener l) {
        mOutTimeShiftUpdaterListener = l;
    }

    @Override
    public void setOnSeekLiveCompletionListener(OnSeekLiveCompletionListener l) {
        mOutSeekLiveCompletionListener = l;
    }

    private OnSeekLiveCompletionListener mOutSeekLiveCompletionListener = null;
    private OnPreparedListener mOnPreparedListener = null;

    private static class InnerPreparedListener implements OnPreparedListener {

        private WeakReference<ApsaraLiveShiftPlayer> playerWR;

        InnerPreparedListener(ApsaraLiveShiftPlayer player) {
            playerWR = new WeakReference<>(player);
        }

        @Override
        public void onPrepared() {

            ApsaraLiveShiftPlayer player = playerWR.get();
            if (player != null) {
                player.onPrepared();
            }

        }

    }

    private void onPrepared() {
        if (liveTimeUpdater != null) {
            liveTimeUpdater.stopUpdater();
        } else {
            liveTimeUpdater = new LiveTimeUpdater(mContext, liveShiftSource);
            liveTimeUpdater.setUpdaterListener(timeShiftUpdaterListener);
        }

        liveTimeUpdater.setStartPlayTime(liveSeekToTime);
        liveTimeUpdater.startUpdater();

        if (status == SeekLive) {
            status = prepared;

            if (statusWhenSeek == started) {
                start();
            } else {
                liveTimeUpdater.pauseUpdater();//暂停进度的更新
            }

            if (mOutSeekLiveCompletionListener != null) {
                mOutSeekLiveCompletionListener.onSeekLiveCompletion(liveSeekToTime);
            }
            liveSeekToTime = -1;

        } else {
            status = prepared;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared();
            }
        }
    }

    private OnStateChangedListener mOnStateChangedListener = null;

    private OnStateChangedListener innerOnStateChangedListener = new InnerStateChangedListener(this);

    private static class InnerStateChangedListener implements OnStateChangedListener {
        private WeakReference<ApsaraLiveShiftPlayer> playerWR;

        InnerStateChangedListener(ApsaraLiveShiftPlayer apsaraLiveShiftPlayer) {
            playerWR = new WeakReference<>(apsaraLiveShiftPlayer);
        }

        @Override
        public void onStateChanged(int newState) {
            ApsaraLiveShiftPlayer player = playerWR.get();
            if (player != null) {
                player.onStateChanged(newState);
            }
        }
    }

    private void onStateChanged(int newState) {
        if (newState != prepared) {
            status = newState;
        }

        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(newState);
        }
    }

    @Override
    public void setOnStateChangedListener(OnStateChangedListener l) {
        mOnStateChangedListener = l;
        super.setOnStateChangedListener(innerOnStateChangedListener);
    }

    private OnLoadingStatusListener mOnLoadingStatusListener = null;
    private OnLoadingStatusListener innerOnLoadingStatusListener = new InnerOnLoadingStatusListener(this);

    private static class InnerOnLoadingStatusListener implements OnLoadingStatusListener {
        private WeakReference<ApsaraLiveShiftPlayer> playerWR;

        InnerOnLoadingStatusListener(ApsaraLiveShiftPlayer apsaraLiveShiftPlayer) {
            playerWR = new WeakReference<>(apsaraLiveShiftPlayer);
        }

        @Override
        public void onLoadingBegin() {
            ApsaraLiveShiftPlayer player = playerWR.get();
            if (player != null) {
                player.onLoadingBegin();
            }
        }

        @Override
        public void onLoadingProgress(int percent, float netSpeed) {
            ApsaraLiveShiftPlayer player = playerWR.get();
            if (player != null) {
                player.onLoadingProgress(percent, netSpeed);
            }
        }

        @Override
        public void onLoadingEnd() {
            ApsaraLiveShiftPlayer player = playerWR.get();
            if (player != null) {
                player.onLoadingEnd();
            }
        }
    }


    private void onLoadingBegin() {
        if (liveTimeUpdater != null) {
            liveTimeUpdater.pauseUpdater();
        }

        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingBegin();
        }
    }

    private void onLoadingProgress(int percent, float netSpeed) {
        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingProgress(percent, netSpeed);
        }
    }

    private void onLoadingEnd() {
        if (liveTimeUpdater != null) {
            liveTimeUpdater.resumeUpdater();
        }
        if (mOnLoadingStatusListener != null) {
            mOnLoadingStatusListener.onLoadingEnd();
        }
    }


    @Override
    public void setOnLoadingStatusListener(OnLoadingStatusListener l) {
        mOnLoadingStatusListener = l;
        super.setOnLoadingStatusListener(innerOnLoadingStatusListener);
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;

        super.setOnPreparedListener(new InnerPreparedListener(this));
    }

    @Override
    public void start() {
        super.start();

        if (liveTimeUpdater != null) {
            liveTimeUpdater.resumeUpdater();
        }
    }

    @Override
    public void pause() {
        super.pause();

        if (liveTimeUpdater != null) {
            liveTimeUpdater.pauseUpdater();
        }
    }

    @Override
    public void stop() {
        super.stop();

        if (liveTimeUpdater != null) {
            liveTimeUpdater.stopUpdater();
        }
    }

    private static class InnerTimeShiftUpdaterListener implements OnTimeShiftUpdaterListener {

        private WeakReference<ApsaraLiveShiftPlayer> playerReference;

        public InnerTimeShiftUpdaterListener(ApsaraLiveShiftPlayer shiftPlayer) {
            playerReference = new WeakReference<ApsaraLiveShiftPlayer>(shiftPlayer);
        }

        @Override
        public void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime) {
            ApsaraLiveShiftPlayer playerProxy = playerReference.get();
            if (playerProxy != null) {
                playerProxy.onUpdater(currentTime, shiftStartTime, shiftEndTime);
            }
        }
    }


    private OnTimeShiftUpdaterListener mOutTimeShiftUpdaterListener = null;

    private void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime) {
        if (mOutTimeShiftUpdaterListener != null) {
            mOutTimeShiftUpdaterListener.onUpdater(currentTime, shiftStartTime, shiftEndTime);
        }
    }

}
