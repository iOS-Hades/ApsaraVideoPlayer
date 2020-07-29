package com.aliyun.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.aliyun.sdk.player.AliLiveShiftPlayer;
import com.aliyun.sdk.player.ApsaraLiveShiftPlayer;
import com.aliyun.player.IPlayer;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.aliyunplayerbase.util.NetWatchdog;
import com.aliyun.player.aliyunplayerbase.util.OrientationWatchDog;
import com.aliyun.player.aliyunplayerbase.view.tipsview.TipsView;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.source.LiveShift;
import com.aliyun.ui.view.control.ControlView;

import java.lang.ref.WeakReference;


/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */

/**
 * 通过调用setUrl(String)设置直播地址。通过setTimeLineUrl(String)设置时移时间地址。
 * TimeLineUrl是用于获取时移时间段的地址。播放器SDK会一分钟请求一次这个地址，更新当前时移时间段等信息
 */
public class AliyunLiveShiftPlayerView extends RelativeLayout {

    private static final String URL = "http://qt1.alivecdn.com/timeline/testshift.m3u8?auth_key=1594730859-0-0-b71fd57c57a62a3c2b014f24ca2b9da3";
    private static final int HANDLER_MSG_WHAT = 0x0000;

    private SurfaceView mSurfaceView;
    private AliLiveShiftPlayer mAliLiveShiftPlayer;
    /**
     * 控制栏
     */
    private ControlView mControlView;
    /**
     * TipsView
     */
    private TipsView mTipsView;

    /**
     * 网络监听
     */
    private NetWatchdog mNetWatchdog;

    /**
     * 当前直播的现实时间
     */
    private long mCurrentLiveTime;
    /**
     * 当前直播的播放时间
     */
    private long mCurrentTime;
    /**
     * 可时移的起始时间
     */
    private long mShiftStartTime;
    /**
     * 可时移的结束时间
     */
    private long mShiftEndTime;
    private long mEndTime = -1;
    private ProgressUpdateTimerHandler mProgressUpdateTimerHandler = new ProgressUpdateTimerHandler(this);
    private int mPlayerState;
    private LiveShift mAliyunLiveTimeShift;

    //返回按钮点击监听
    private ControlView.OnBackIconClickListener mOutOnBackIconClickListener;
    //屏幕方向监听
    private OrientationWatchDog mOrientationWatchDog;
    //屏幕方向改变监听
    private OnOrientationChangeListener orientationChangeListener;
    //直播时移seek完成监听
    private AliLiveShiftPlayer.OnSeekLiveCompletionListener mOutOnSeekLiveCompletionListener;
    //直播时移时间更新监听
    private AliLiveShiftPlayer.OnTimeShiftUpdaterListener mOutOnTimeShiftUpdaterListener;

    //当前屏幕模式
    private AliyunScreenMode mCurrentScreenMode = AliyunScreenMode.Small;

    public AliyunLiveShiftPlayerView(Context context) {
        super(context);
        initVideoView();
    }

    public AliyunLiveShiftPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView();
    }

    public AliyunLiveShiftPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    /**
     * 初始化view
     */
    private void initVideoView() {
        //初始化SurfaceView
        initSurfaceView();
        //初始化直播时移播放器
        initLiveShiftPlayer();
        //初始化控制栏
        initControlView();
        //初始化提示view
        initTipsView();
        //初始化网络监听
        initNetWatchDog();
        //初始化屏幕方向监听
        initOrientationWatchdog();
    }

    private void setPlaySource() {
        mAliyunLiveTimeShift = new LiveShift();

        long currentSeconds = System.currentTimeMillis() / 1000;

        mAliyunLiveTimeShift.setUrl(URL);
        mAliyunLiveTimeShift.setTimeLineUrl("http://qt1.alivecdn.com/openapi/timeline/query?auth_key=1594731135-0-0-61c9bd253b29ef4c8017ce05c0953083&app=timeline&stream=testshift&format=ts&lhs_start_unix_s_0="
                + (currentSeconds - 5 * 60) + "&lhs_end_unix_s_0=" + (currentSeconds + 5 * 60));
        mAliyunLiveTimeShift.setTitle("喷出彩带!庆祝抗战胜利70周年");
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //添加到布局中
        addView(view, params);
    }


    /**
     * 初始化SurfaceView
     */
    private void initSurfaceView() {
        mSurfaceView = new SurfaceView(getContext());
        addSubView(mSurfaceView);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // Important: surfaceView changed from background to front, we need reset surface to mediaplayer.
                // 对于从后台切换到前台,需要重设surface;部分手机锁屏也会做前后台切换的处理
                if (mAliLiveShiftPlayer != null) {
                    mAliLiveShiftPlayer.setDisplay(surfaceHolder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
                if (mAliLiveShiftPlayer != null) {
                    mAliLiveShiftPlayer.redraw();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (mAliLiveShiftPlayer != null) {
                    mAliLiveShiftPlayer.setSurface(null);
                }
            }
        });
    }

    private void initLiveShiftPlayer() {
//        mAliLiveShiftPlayer = AliPlayerFactory.createAliLiveShiftPlayer(getContext().getApplicationContext());
        mAliLiveShiftPlayer = new ApsaraLiveShiftPlayer(getContext().getApplicationContext());
        //默认开启自动播放
        mAliLiveShiftPlayer.setAutoPlay(true);
        mAliLiveShiftPlayer.setOnPreparedListener(new IPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {
                if(mTipsView != null){
                    mTipsView.hideNetLoadingTipView();
                }
                mAliLiveShiftPlayer.start();
            }
        });

        mAliLiveShiftPlayer.setOnStateChangedListener(new IPlayer.OnStateChangedListener() {
            @Override
            public void onStateChanged(int newState) {
                mPlayerState = newState;
                if (mControlView != null) {
                    mControlView.setPlayState(newState);
                }
            }
        });

        mAliLiveShiftPlayer.setOnRenderingStartListener(new IPlayer.OnRenderingStartListener() {
            @Override
            public void onRenderingStart() {
                if (mControlView != null) {
                    mControlView.show();
                }
            }
        });

        mAliLiveShiftPlayer.setOnErrorListener(new IPlayer.OnErrorListener() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                if (mTipsView != null) {
                    mTipsView.hideAll();
                }
                showErrorTipView(errorInfo.getCode().getValue(), Integer.toHexString(errorInfo.getCode().getValue()), errorInfo.getMsg());
            }
        });

        mAliLiveShiftPlayer.setOnLoadingStatusListener(new IPlayer.OnLoadingStatusListener() {
            @Override
            public void onLoadingBegin() {
                if(mTipsView != null){
                    mTipsView.showBufferLoadingTipView();
                }
            }

            @Override
            public void onLoadingProgress(int percent, float v) {
                if(mTipsView != null){
                    mTipsView.updateLoadingPercent(percent);
                }
            }

            @Override
            public void onLoadingEnd() {
                if(mTipsView != null){
                    mTipsView.hideBufferLoadingTipView();
                }
            }
        });

        mAliLiveShiftPlayer.setOnSeekCompleteListener(new IPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                if (mControlView != null) {
                    mControlView.seekLiveCompletion();
                }
            }
        });

        //时移seek完成通知。
        mAliLiveShiftPlayer.setOnSeekLiveCompletionListener(new AliLiveShiftPlayer.OnSeekLiveCompletionListener() {
            @Override
            public void onSeekLiveCompletion(long playTime) {
                //playTime 实际播放的时间
                if(mOutOnSeekLiveCompletionListener != null){
                    mOutOnSeekLiveCompletionListener.onSeekLiveCompletion(playTime);
                }
            }
        });

        //时移时间更新监听事件
        mAliLiveShiftPlayer.setOnTimeShiftUpdaterListener(new AliLiveShiftPlayer.OnTimeShiftUpdaterListener() {
            @Override
            public void onUpdater(long currentLiveTime, long shiftStartTime, long shiftEndTime) {
                mCurrentLiveTime = currentLiveTime;
                mShiftStartTime = shiftStartTime;
                mShiftEndTime = shiftEndTime;
                if(mOutOnTimeShiftUpdaterListener != null){
                    mOutOnTimeShiftUpdaterListener.onUpdater(currentLiveTime,shiftStartTime,shiftEndTime);
                }
                updater(shiftStartTime,shiftEndTime);
            }
        });
    }

    /**
     * 时移时间更新
     */
    private void updater(long shiftStartTime, long shiftEndTime) {
        if (mAliLiveShiftPlayer == null) {
            return;
        }
        this.mCurrentLiveTime = mAliLiveShiftPlayer.getCurrentLiveTime();
        this.mCurrentTime = mAliLiveShiftPlayer.getCurrentTime();
        this.mShiftEndTime = shiftEndTime;
        this.mShiftStartTime = shiftStartTime;
        long offsetTimeLen = mShiftEndTime - mShiftStartTime;
        if (mEndTime - mCurrentLiveTime < offsetTimeLen * 0.05) {
            mEndTime = (long) (mCurrentLiveTime + offsetTimeLen * 0.1);
        }
        if (mControlView != null) {
            mControlView.setPlayProgress(mCurrentTime);
            mControlView.setLiveTime(mCurrentLiveTime);
            mControlView.updateRange(mShiftStartTime, mEndTime);
        }

        startUpdateTimer();
    }

    private static class ProgressUpdateTimerHandler extends Handler {

        private WeakReference<AliyunLiveShiftPlayerView> weakReference;

        public ProgressUpdateTimerHandler(AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView) {
            weakReference = new WeakReference<>(aliyunLiveShiftPlayerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null && msg.what == HANDLER_MSG_WHAT) {
                aliyunLiveShiftPlayerView.updater(aliyunLiveShiftPlayerView.mShiftStartTime, aliyunLiveShiftPlayerView.mShiftEndTime);
            }
        }
    }

    private void startUpdateTimer() {
        if (mProgressUpdateTimerHandler != null) {
            mProgressUpdateTimerHandler.removeMessages(HANDLER_MSG_WHAT);
            mProgressUpdateTimerHandler.sendEmptyMessageDelayed(HANDLER_MSG_WHAT, 1000);
        }

    }

    private void stopUpdateTimer() {
        if (mProgressUpdateTimerHandler != null) {
            mProgressUpdateTimerHandler.removeMessages(HANDLER_MSG_WHAT);
        }
    }

    /**
     * 初始化控制栏
     */
    private void initControlView() {
        mControlView = new ControlView(getContext());
        addSubView(mControlView);

        //返回按钮点击监听
        mControlView.setOnBackIconClickListener(new ControlView.OnBackIconClickListener() {
            @Override
            public void onBackClickListener() {
                if (mOutOnBackIconClickListener != null) {
                    mOutOnBackIconClickListener.onBackClickListener();
                }
            }
        });

        //播放状态监听
        mControlView.setOnPlayStateClickListener(new ControlView.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClickListener(int currentPlayState) {
                if (mAliLiveShiftPlayer != null) {
                    if (currentPlayState == IPlayer.started) {
                        mAliLiveShiftPlayer.start();
                    }
                    if (currentPlayState == IPlayer.paused) {
                        mAliLiveShiftPlayer.pause();
                    }
                }

            }
        });

        //seek监听
        mControlView.setOnSeekBarChangeListener(new ControlView.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(long seekTime) {
                if (mAliLiveShiftPlayer != null) {
                    mAliLiveShiftPlayer.seekToLiveTime(seekTime + mShiftStartTime);
                }
            }
        });

        //切换全屏
        mControlView.setOnScreenModeBtnClickListener(new ControlView.OnScreenModeBtnClickListener() {
            @Override
            public void onScreenModeClick() {
                if (mCurrentScreenMode == AliyunScreenMode.Small) {
                    changedToLandForwardScape(true);
                } else {
                    changedToPortrait(true);
                }
            }
        });
    }

    /**
     * 初始化提示view
     */
    private void initTipsView() {

        mTipsView = new TipsView(getContext());
        //设置tip中的点击监听事件
        mTipsView.setOnTipClickListener(new TipsView.OnTipClickListener() {
            @Override
            public void onContinuePlay() {
                mTipsView.hideAll();
                if(mAliLiveShiftPlayer != null){
                    mAliLiveShiftPlayer.setAutoPlay(true);
                }

            }

            @Override
            public void onStopPlay() {
                // 结束播放
                mTipsView.hideAll();
                stop();

                Context context = getContext();
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onRetryPlay(int errorCode) {
                //重新prepare
                if (mTipsView != null) {
                    mTipsView.hideAll();
                }
                if(mAliLiveShiftPlayer != null){
                    prepare();
                }
            }

            @Override
            public void onReplay() {
            }

            @Override
            public void onRefreshSts() {

            }

            @Override
            public void onWait() {

            }

            @Override
            public void onExit() {

            }
        });
        addSubView(mTipsView);
    }

    private void initNetWatchDog() {
        mNetWatchdog = new NetWatchdog(getContext());
        mNetWatchdog.setNetChangeListener(new MyNetChangeListener(this));
        mNetWatchdog.startWatch();
    }

    private void initOrientationWatchdog() {
        final Context context = getContext();
        mOrientationWatchDog = new OrientationWatchDog(context);
        mOrientationWatchDog.setOnOrientationListener(new InnerOrientationListener(this));
    }

    private static class InnerOrientationListener implements OrientationWatchDog.OnOrientationListener {

        private WeakReference<AliyunLiveShiftPlayerView> weakReference;

        public InnerOrientationListener(AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView) {
            weakReference = new WeakReference<>(aliyunLiveShiftPlayerView);
        }

        @Override
        public void changedToLandForwardScape(boolean fromPort) {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.changedToLandForwardScape(fromPort);
            }
        }

        @Override
        public void changedToLandReverseScape(boolean fromPort) {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.changedToLandReverseScape(fromPort);
            }
        }

        @Override
        public void changedToPortrait(boolean fromLand) {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.changedToPortrait(fromLand);
            }
        }
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private void changedToLandForwardScape(boolean fromPort) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return;
        }
        changeScreenMode(AliyunScreenMode.Full, false);
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromPort, mCurrentScreenMode);
        }
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private void changedToLandReverseScape(boolean fromPort) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return;
        }
        changeScreenMode(AliyunScreenMode.Full, true);
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromPort, mCurrentScreenMode);
        }
    }

    /**
     * 屏幕方向变为竖屏
     *
     * @param fromLand 是否从横屏转过来
     */
    private void changedToPortrait(boolean fromLand) {
        if (mCurrentScreenMode == AliyunScreenMode.Full) {
            //全屏情况转到了竖屏
            //没有固定竖屏，就变化mode
            if (fromLand) {
                changeScreenMode(AliyunScreenMode.Small, false);
            } else {
                //如果没有转到过横屏，就不让他转了。防止竖屏的时候点横屏之后，又立即转回来的现象
            }

        } else if (mCurrentScreenMode == AliyunScreenMode.Small) {
            //竖屏的情况转到了竖屏
        }
        if (orientationChangeListener != null) {
            orientationChangeListener.orientationChange(fromLand, mCurrentScreenMode);
        }
    }

    /**
     * 改变屏幕模式：小屏或者全屏。
     *
     * @param targetMode {@link AliyunScreenMode}
     */
    public void changeScreenMode(AliyunScreenMode targetMode, boolean isReverse) {
        mCurrentScreenMode = targetMode;
        Context context = getContext();
        if (context instanceof Activity) {
            if (targetMode == AliyunScreenMode.Full) {
                if (isReverse) {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else {
                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            } else if (targetMode == AliyunScreenMode.Small) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (mControlView != null) {
            mControlView.setScreenModeStatus(targetMode);
        }
    }

    /**
     * 屏幕方向改变监听接口
     */
    public interface OnOrientationChangeListener {
        /**
         * 屏幕方向改变
         *
         * @param from        从横屏切换为竖屏, 从竖屏切换为横屏
         * @param currentMode 当前屏幕类型
         */
        void orientationChange(boolean from, AliyunScreenMode currentMode);
    }

    public void setOrientationChangeListener(
            OnOrientationChangeListener listener) {
        this.orientationChangeListener = listener;
    }

    /**
     * 网络监听
     */
    private static class MyNetChangeListener implements NetWatchdog.NetChangeListener {

        private WeakReference<AliyunLiveShiftPlayerView> weakReference;

        public MyNetChangeListener(AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView) {
            weakReference = new WeakReference<>(aliyunLiveShiftPlayerView);
        }

        @Override
        public void onWifiTo4G() {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.onWifiTo4G();
            }
        }

        @Override
        public void on4GToWifi() {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.on4GToWifi();
            }
        }

        @Override
        public void onNetDisconnected() {
            AliyunLiveShiftPlayerView aliyunLiveShiftPlayerView = weakReference.get();
            if (aliyunLiveShiftPlayerView != null) {
                aliyunLiveShiftPlayerView.onNetDisconnected();
            }
        }
    }

    private void onWifiTo4G() {
        if (mControlView != null) {
            mPlayerState = mControlView.getCurrentPlayState();
        }
        if (mPlayerState == IPlayer.started) {
            pause();
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("网络切换到4g");
        alertDialog.setMessage("继续播放？");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replay();
            }
        });
        alertDialog.setNegativeButton("No", null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void on4GToWifi() {
//        ToastUtils.show(getContext(),R.string.net_change_to_wifi);

    }

    private void onNetDisconnected() {
//        ToastUtils.show(getContext(),R.string.net_disconnect);
    }

    /**
     * 是否自动播放
     */
    public void setAutoPlay(boolean autoPlay) {
        if (mAliLiveShiftPlayer != null) {
            mAliLiveShiftPlayer.setAutoPlay(autoPlay);
        }
    }

    /**
     * 显示错误提示
     *
     * @param errorCode  错误码
     * @param errorEvent 错误事件
     * @param errorMsg   错误描述
     */
    public void showErrorTipView(int errorCode, String errorEvent, String errorMsg) {
        stop();

        if (mControlView != null) {
            mControlView.setPlayState(IPlayer.paused);
        }

        if (mControlView != null) {
            mControlView.hide();
        }
        if(mTipsView != null){
            mTipsView.showErrorTipView(errorCode, errorEvent, errorMsg);
        }
    }

    /**
     * prepare
     */
    public void prepare() {
        if (mTipsView != null) {
            mTipsView.showNetLoadingTipView();
        }
        setPlaySource();
        if (mAliLiveShiftPlayer != null) {
            mAliLiveShiftPlayer.setAutoPlay(true);
            mAliLiveShiftPlayer.setDataSource(mAliyunLiveTimeShift);
            mAliLiveShiftPlayer.prepare();
        }
    }

    /**
     * replay
     */
    public void replay() {
        stop();

        prepare();
    }

    /**
     * start
     */
    public void start() {
        if (mAliLiveShiftPlayer != null && mPlayerState == IPlayer.paused) {
            mAliLiveShiftPlayer.start();
        }
    }

    /**
     * stop
     */
    public void stop() {
        stopUpdateTimer();
        if (mAliLiveShiftPlayer != null) {
            mAliLiveShiftPlayer.stop();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mAliLiveShiftPlayer != null && mPlayerState == IPlayer.started) {
            mAliLiveShiftPlayer.pause();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (mAliLiveShiftPlayer != null) {
            mAliLiveShiftPlayer.stop();
            mAliLiveShiftPlayer.release();
        }
        if (mNetWatchdog != null) {
            mNetWatchdog.stopWatch();
        }
    }

    public void setOutOnBackIconClickListener(ControlView.OnBackIconClickListener listener) {
        this.mOutOnBackIconClickListener = listener;
    }

    public void setmOutOnSeekLiveCompletionListener(AliLiveShiftPlayer.OnSeekLiveCompletionListener listener){
        this.mOutOnSeekLiveCompletionListener = listener;
    }

    public void setmOutOnTimeShiftUpdaterListener(AliLiveShiftPlayer.OnTimeShiftUpdaterListener listener){
        this.mOutOnTimeShiftUpdaterListener = listener;
    }
}
