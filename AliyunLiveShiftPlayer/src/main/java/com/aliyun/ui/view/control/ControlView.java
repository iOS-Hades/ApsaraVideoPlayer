package com.aliyun.ui.view.control;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.sdk.player.aliyunliveshiftplayer.R;
import com.aliyun.player.IPlayer;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.aliyunplayerbase.util.Formatter;
import com.aliyun.ui.view.seekbar.LiveSeekBar;

public class ControlView extends RelativeLayout {

    /**
     * 标题
     */
    private TextView mTitleTextView;
    /**
     * 返回按钮
     */
    private ImageView mTitleBackImageView;
    /**
     * 播放状态
     */
    private ImageView mPlayStateImagView;
    /**
     * 小屏状态下，直播seekBar
     */
    private LiveSeekBar mSmallLiveSeekBar;
    /**
     * 标题栏 rootView
     */
    private LinearLayout mTitleBarLinearLayout;
    /**
     * 控制栏 rootView
     */
    private LinearLayout mControlRootLinearLayout;
    /**
     * 小屏状态下，控制栏 rootView
     */
    private LinearLayout mSmallBarRootLinearLayout;

    private boolean mInSeek = false;

    /**
     * 一系列监听
     */
    private OnBackIconClickListener mOnBackIconClickListener;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private OnPlayStateClickListener mOnPlayStateClickListener;
    private OnScreenModeBtnClickListener mOnScreenModeBtnClickListener;

    //视频播放状态
    private int mPlayState = IPlayer.started;
    //切换大小屏相关
    private AliyunScreenMode mAliyunScreenMode = AliyunScreenMode.Small;
    private ImageView mScreenModeImageView;
    /**
     * 可时移的结束时间
     */
    private TextView mEndTimeTextView;
    /**
     * 当前现实时间
     */
    private TextView mCurrentPlayTimeTextView;
    /**
     * 可时移的起始时间
     */
    private long mShiftStartTime;

    public ControlView(Context context) {
        super(context);
        init();
    }

    public ControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_live_shift_control, this, true);
        initView();
        initListener();

        updateSeekBarTheme();
        updateAllViews();

    }

    private void initView() {
        mTitleTextView = findViewById(R.id.alivc_tv_title);
        mEndTimeTextView = findViewById(R.id.tv_endTime);
        mTitleBarLinearLayout = findViewById(R.id.titlebar);
        mTitleBackImageView = findViewById(R.id.alivc_title_back);
        mPlayStateImagView = findViewById(R.id.alivc_player_state);
        mScreenModeImageView = findViewById(R.id.alivc_screen_mode);
        mControlRootLinearLayout = findViewById(R.id.ll_control_root);
        mCurrentPlayTimeTextView = findViewById(R.id.tv_current_play_time);
        mSmallBarRootLinearLayout = findViewById(R.id.alivc_info_small_bar);
        mSmallLiveSeekBar = findViewById(R.id.alivc_info_live_small_seekbar);
    }

    private void initListener() {
        //返回icon
        mTitleBackImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnBackIconClickListener != null) {
                    mOnBackIconClickListener.onBackClickListener();
                }
            }
        });

        mPlayStateImagView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnPlayStateClickListener != null){
                    if(mPlayState == IPlayer.started){
                        mPlayState = IPlayer.paused;
                    }else if(mPlayState == IPlayer.paused){
                        mPlayState = IPlayer.started;
                    }
                    mOnPlayStateClickListener.onPlayStateClickListener(mPlayState);
                    updatePlayStateBtn();
                }
            }
        });

        //seek监听
        mSmallLiveSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mInSeek = true;
                    long seekTime = seekBar.getProgress() + mShiftStartTime;
                    mCurrentPlayTimeTextView.setText(Formatter.formatDate(seekTime));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mInSeek = false;
                if (mOnSeekBarChangeListener != null) {
                    long seekTime = mSmallLiveSeekBar.getProgress();
                    mOnSeekBarChangeListener.onStopTrackingTouch(seekTime);
                }
            }
        });

        //切换屏幕
        mScreenModeImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnScreenModeBtnClickListener != null){
                    mOnScreenModeBtnClickListener.onScreenModeClick();
                }
            }
        });
    }


    /**
     * 更新当前主题色
     */
    private void updateSeekBarTheme() {
        //获取不同主题的图片
        int progressDrawableResId = R.drawable.alivc_info_seekbar_bg_blue;
        int thumbResId = R.drawable.alivc_info_seekbar_thumb_blue;


        //这个很有意思。。哈哈。不同的seekbar不能用同一个drawable，不然会出问题。
        // https://stackoverflow.com/questions/12579910/seekbar-thumb-position-not-equals-progress

        //设置到对应控件中
        Resources resources = getResources();
        Drawable smallProgressDrawable = ContextCompat.getDrawable(getContext(), progressDrawableResId);
        Drawable smallThumb = ContextCompat.getDrawable(getContext(), thumbResId);
        mSmallLiveSeekBar.setProgressDrawable(smallProgressDrawable);
        mSmallLiveSeekBar.setThumb(smallThumb);

    }

    /**
     * 设置播放状态
     */
    public void setPlayState(int playState) {
        mPlayState = playState;
        updatePlayStateBtn();
        updateScreenModeBtn();
    }

    /**
     * 设置屏幕状态
     * @param mode
     */
    public void setScreenModeStatus(AliyunScreenMode mode) {
        mAliyunScreenMode = mode;
        updateScreenModeBtn();
    }

    /**
     * 设置视频标题
     */
    public void setVideoTitle(String title) {

    }

    private void updateAllViews() {
        updatePlayStateBtn();
    }

    /**
     * 更新播放按钮的状态
     */
    private void updatePlayStateBtn() {
        if (mPlayState == IPlayer.started) {
            mPlayStateImagView.setImageResource(R.drawable.alivc_playstate_pause);
        } else {
            mPlayStateImagView.setImageResource(R.drawable.alivc_playstate_play);
        }
    }

    /**
     * 更新切换大小屏按钮的信息
     */
    private void updateScreenModeBtn() {
        if (mAliyunScreenMode == AliyunScreenMode.Full) {
            mScreenModeImageView.setImageResource(R.drawable.alivc_screen_mode_small);
        } else {
            mScreenModeImageView.setImageResource(R.drawable.alivc_screen_mode_large);
        }
    }

    /**
     * 设置progress为当前直播的播放时间
     */
    public void setPlayProgress(long mCurrentTime) {
        if (mSmallLiveSeekBar != null) {
            mSmallLiveSeekBar.setPlayProgress(mCurrentTime);
        }
        if(mCurrentPlayTimeTextView != null && !mInSeek){
            mCurrentPlayTimeTextView.setText(Formatter.formatDate(mCurrentTime));
        }
    }

    /**
     * 设置当前直播的现实时间
     */
    public void setLiveTime(long mCurrentLiveTime) {
        if (mSmallLiveSeekBar != null && !mInSeek) {
            mSmallLiveSeekBar.setLiveTime(mCurrentLiveTime);
        }
    }

    /**
     * 更新
     */
    public void updateRange(long mShiftStartTime, long mEndTime) {
        this.mShiftStartTime = mShiftStartTime;
        if (mSmallLiveSeekBar != null) {
            mSmallLiveSeekBar.updateRange(mShiftStartTime, mEndTime);
        }
        if(mEndTimeTextView != null){
            mEndTimeTextView.setText(Formatter.formatDate(mEndTime));
        }
    }

    /**
     * 获取当前状态
     */
    public int getCurrentPlayState(){
        return mPlayState;
    }

    /**
     * seek完成通知
     */
    public void seekLiveCompletion() {
        mInSeek = false;
    }

    /**
     * 隐藏ControlView
     */
    public void hide() {
        setVisibility(View.GONE);
    }

    /**
     * 显示ControlView
     */
    public void show(){
        setVisibility(View.VISIBLE);
    }

    public interface OnBackIconClickListener {
        void onBackClickListener();
    }

    public void setOnBackIconClickListener(OnBackIconClickListener listener) {
        this.mOnBackIconClickListener = listener;
    }

    public interface OnSeekBarChangeListener {
        void onStopTrackingTouch(long seekTime);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.mOnSeekBarChangeListener = listener;
    }

    public interface OnPlayStateClickListener{
        void onPlayStateClickListener(int currentPlayState);
    }

    public void setOnPlayStateClickListener(OnPlayStateClickListener listener){
        this.mOnPlayStateClickListener = listener;
    }

    public interface OnScreenModeBtnClickListener{
        void onScreenModeClick();
    }

    public void setOnScreenModeBtnClickListener(OnScreenModeBtnClickListener listener){
        this.mOnScreenModeBtnClickListener = listener;
    }

}
