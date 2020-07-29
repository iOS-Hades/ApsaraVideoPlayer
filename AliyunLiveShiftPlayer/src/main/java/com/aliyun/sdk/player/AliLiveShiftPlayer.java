package com.aliyun.sdk.player;

import com.aliyun.player.IPlayer;
import com.aliyun.player.source.LiveShift;

/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */
public interface AliLiveShiftPlayer extends IPlayer {

    /**
     * 设置数据源
     *
     * @param liveShift 本机地址或网络地址。见{@link LiveShift}。
     */
    /****
     * Specify a timeshift playback source.
     *
     * @param liveShift The specified timeshift playback source: a local address or a URL. See {@link LiveShift}.
     */
    abstract public void setDataSource(LiveShift liveShift);

    /**
     * 获取当前直播的现实时间
     * @return 现实时间
     */
    /****
     * Query the current broadcasting time.
     * @return The current broadcasting time.
     */
    abstract public long getCurrentLiveTime();

    /**
     * 获取当前直播的播放时间
     * @return 播放时间
     */
    /****
     * Query the time that the player currently seeks to.
     * @return The time that the player currently seeks to.
     */
    abstract public long getCurrentTime();

    /**
     * 时移到某个时间
     * @param liveTime 时间。单位毫秒
     */
    /****
     * Seek to a specified time.
     * @param liveTime The specified time that the player will seek to. Unit: milliseconds.
     */
    abstract public void seekToLiveTime(long liveTime);

    /**
     * 设置时移时间更新监听事件
     * @param l 时移时间更新监听事件
     */
    /****
     * Set a timeshifting update callback.
     * @param l The timeshifting update callback.
     */
    abstract public void setOnTimeShiftUpdaterListener(OnTimeShiftUpdaterListener l);

    /**
     * 设置时移seek完成通知。
     * @param l seek完成通知。
     */
    /****
     * Set a timeshifting success callback.
     * @param l The timeshifting success callback.
     */
    abstract public void setOnSeekLiveCompletionListener(OnSeekLiveCompletionListener l);

    /**
     * 时移时间更新监听事件
     */
    /****
     * Timeshifting update callback.
     */
    public interface OnTimeShiftUpdaterListener {

    /**
     * 时移时间更新
     * @param currentTime 当前现实时间
     * @param shiftStartTime 可时移的起始时间
     * @param shiftEndTime 可时移的结束时间
     */
    /****
     * Timeshifting update notification.
     * @param currentTime The current broadcasting time.
     * @param shiftStartTime The start of the time window for timeshift.
     * @param shiftEndTime The end of the time window for timeshift.
     */
        void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime);
    }

    /**
     * 时移seek完成通知。
     */
    /****
     * Timeshifting success callback.
     */
    public interface OnSeekLiveCompletionListener {
        /**
         * 时移seek完成通知。
         * @param playTime 实际播放的时间。
         */
        /****
         * Timeshifting success notification.
         * @param playTime The time that the player seeks to.
         */
        void onSeekLiveCompletion(long playTime);
    }

}
