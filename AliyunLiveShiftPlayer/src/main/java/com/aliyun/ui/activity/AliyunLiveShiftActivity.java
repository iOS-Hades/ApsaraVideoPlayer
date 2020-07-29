package com.aliyun.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.sdk.player.AliLiveShiftPlayer;
import com.aliyun.sdk.player.aliyunliveshiftplayer.R;
import com.aliyun.player.aliyunplayerbase.activity.BaseActivity;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.aliyunplayerbase.util.ScreenUtils;
import com.aliyun.ui.view.AliyunLiveShiftPlayerView;
import com.aliyun.ui.view.control.ControlView;

import java.lang.ref.WeakReference;

public class AliyunLiveShiftActivity extends BaseActivity {

    private AliyunLiveShiftPlayerView mLiveShiftView;
    private TextView mLiveShiftPlayerTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_aliyun_live_shift);
        initView();
        initListener();
    }

    private void initView(){
        mLiveShiftView = findViewById(R.id.live_shift_view);
        mLiveShiftPlayerTitleTextView = findViewById(R.id.live_shift_player_title);

        mLiveShiftView.prepare();
    }

    private void initListener(){
        mLiveShiftView.setOutOnBackIconClickListener(new MyOnBackIconClickListener(this));
        mLiveShiftView.setOrientationChangeListener(new MyOrientationChangeListener(this));
        mLiveShiftView.setmOutOnSeekLiveCompletionListener(new MyOnSeekLiveCompletionListener(this));
        mLiveShiftView.setmOutOnTimeShiftUpdaterListener(new MyOnTimeShiftUpdaterListener(this));
    }

    /**
     * 时间更新监听
     */
    private static class MyOnTimeShiftUpdaterListener implements AliLiveShiftPlayer.OnTimeShiftUpdaterListener{

        private WeakReference<AliyunLiveShiftActivity> weakReference;

        public MyOnTimeShiftUpdaterListener(AliyunLiveShiftActivity aliyunLiveShiftActivity){
            weakReference = new WeakReference<>(aliyunLiveShiftActivity);
        }

        @Override
        public void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime) {
            AliyunLiveShiftActivity aliyunLiveShiftActivity = weakReference.get();
            if(aliyunLiveShiftActivity != null){
                aliyunLiveShiftActivity.onUpdater(currentTime,shiftStartTime,shiftEndTime);
            }
        }
    }

    private void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime) {

    }

    /**
     * 直播时移seek完成监听
     */
    private static class MyOnSeekLiveCompletionListener implements AliLiveShiftPlayer.OnSeekLiveCompletionListener{

        private WeakReference<AliyunLiveShiftActivity> weakReference;

        public MyOnSeekLiveCompletionListener(AliyunLiveShiftActivity aliyunLiveShiftActivity){
            weakReference = new WeakReference<>(aliyunLiveShiftActivity);
        }

        @Override
        public void onSeekLiveCompletion(long playTime) {
            AliyunLiveShiftActivity aliyunLiveShiftActivity = weakReference.get();
            if(aliyunLiveShiftActivity != null){
                aliyunLiveShiftActivity.onSeekLiveCompletion(playTime);
            }
        }
    }

    private void onSeekLiveCompletion(long playTime) {

    }

    /**
     * 屏幕方向改变监听
     */
    private static class MyOrientationChangeListener implements AliyunLiveShiftPlayerView.OnOrientationChangeListener {

        private WeakReference<AliyunLiveShiftActivity> weakReference;

        public MyOrientationChangeListener(AliyunLiveShiftActivity aliyunLiveShiftActivity){
            weakReference = new WeakReference<>(aliyunLiveShiftActivity);
        }

        @Override
        public void orientationChange(boolean from, AliyunScreenMode currentMode) {
            AliyunLiveShiftActivity aliyunLiveShiftActivity = weakReference.get();
            if(aliyunLiveShiftActivity != null){

            }
        }
    }

    /**
     * 返回按钮点击监听事件
     */
    private static class MyOnBackIconClickListener implements ControlView.OnBackIconClickListener{

        private WeakReference<AliyunLiveShiftActivity> weakReference;

        public MyOnBackIconClickListener(AliyunLiveShiftActivity aliyunLiveShiftActivity){
            weakReference = new WeakReference<>(aliyunLiveShiftActivity);
        }

        @Override
        public void onBackClickListener() {
            AliyunLiveShiftActivity aliyunLiveShiftActivity = weakReference.get();
            if(aliyunLiveShiftActivity != null){
                aliyunLiveShiftActivity.onBackClick();
            }
        }
    }

    private void onBackClick(){
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mLiveShiftView != null){
            mLiveShiftView.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mLiveShiftView != null){
            mLiveShiftView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if(mLiveShiftView != null){
            mLiveShiftView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePlayerViewMode();
    }

    private void updatePlayerViewMode() {
        if (mLiveShiftView != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mLiveShiftView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                //设置view的布局，宽高之类
                LinearLayout.LayoutParams aliVcVideoViewLayoutParams = (LinearLayout.LayoutParams) mLiveShiftView
                        .getLayoutParams();
                aliVcVideoViewLayoutParams.height = (int) (ScreenUtils.getWidth(this) * 9.0f / 16);
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //转到横屏了。
                //隐藏状态栏
                if (!isStrangePhone()) {
                    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    mLiveShiftView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                //设置view的布局，宽高
                LinearLayout.LayoutParams aliVcVideoViewLayoutParams = (LinearLayout.LayoutParams) mLiveShiftView
                        .getLayoutParams();
                aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }
}
