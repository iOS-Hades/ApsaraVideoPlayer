package com.aliyun.vodplayerview;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.aliyun.player.IPlayer;
import com.aliyun.player.VidPlayerConfigGen;
import com.aliyun.player.alivcplayerexpand.constants.GlobalPlayerConfig;
import com.aliyun.player.alivcplayerexpand.listener.OnScreenCostingSingleTagListener;
import com.aliyun.player.alivcplayerexpand.listener.OnStoppedListener;
import com.aliyun.player.alivcplayerexpand.playlist.AlivcVideoInfo;
import com.aliyun.player.alivcplayerexpand.theme.Theme;
import com.aliyun.player.alivcplayerexpand.util.TimeFormater;
import com.aliyun.player.alivcplayerexpand.util.VidStsUtil;
import com.aliyun.player.alivcplayerexpand.view.choice.AlivcShowMoreDialog;
import com.aliyun.player.alivcplayerexpand.view.control.ControlView;
import com.aliyun.player.alivcplayerexpand.view.gesturedialog.BrightnessDialog;
import com.aliyun.player.alivcplayerexpand.view.more.AliyunShowMoreValue;
import com.aliyun.player.alivcplayerexpand.view.more.DanmakuSettingView;
import com.aliyun.player.alivcplayerexpand.view.more.ShowMoreView;
import com.aliyun.player.alivcplayerexpand.view.more.SpeedValue;
import com.aliyun.player.alivcplayerexpand.view.more.TrackInfoView;
import com.aliyun.player.alivcplayerexpand.view.softinput.SoftInputDialogFragment;
import com.aliyun.player.alivcplayerexpand.widget.AliyunVodPlayerView;
import com.aliyun.player.aliyunplayerbase.bean.AliyunMps;
import com.aliyun.player.aliyunplayerbase.bean.AliyunPlayAuth;
import com.aliyun.player.aliyunplayerbase.bean.AliyunSts;
import com.aliyun.player.aliyunplayerbase.net.GetAuthInformation;
import com.aliyun.player.aliyunplayerbase.net.ServiceCommon;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.aliyunplayerbase.view.tipsview.ErrorInfo;
import com.aliyun.player.aliyunplayerbase.view.tipsview.OnTipsViewBackClickListener;
import com.aliyun.player.aliyunplayerbase.view.tipsview.TipsView;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.nativeclass.TrackInfo;
import com.aliyun.player.source.Definition;
import com.aliyun.player.source.LiveSts;
import com.aliyun.player.source.StsInfo;
import com.aliyun.player.source.UrlSource;
import com.aliyun.player.source.VidAuth;
import com.aliyun.player.source.VidMps;
import com.aliyun.player.source.VidSts;
import com.aliyun.svideo.common.base.AlivcListSelectorDialogFragment;
import com.aliyun.svideo.common.okhttp.AlivcOkHttpClient;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.vodplayer.R;
import com.aliyun.vodplayerview.fragment.AliYunFragment;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by LiuHaiMing
 * on 2020/8/2.
 */
public class MyAliView extends AliyunVodPlayerView {
    private Context context;
    /**
     * 当前系统屏幕亮度
     */
    private int mCurrentBrightValue;
    /**
     * 本地视频播放地址
     */
    private String mLocalVideoPath;

    /**
     * 当前正在播放的videoId
     */
    private String mCurrentVideoId;
    private ErrorInfo currentError = ErrorInfo.Normal;
    /**
     * 弹幕设置Dialog
     */
    private AlivcShowMoreDialog danmakuShowMoreDialog;

    /**
     * 更多Dialog
     */
    private AlivcShowMoreDialog showMoreDialog;
    /**
     * 当前点击的视频列表的下标
     */
    private int currentVidItemPosition;
    /**
     * 播放列表资源
     */
    private ArrayList<AlivcVideoInfo.DataBean.VideoListBean> mVideoListBean;
    private AliyunScreenMode currentScreenMode = AliyunScreenMode.Small;
    /**
     * 下载清晰度Dialog
     */
    private AlivcListSelectorDialogFragment mAlivcListSelectorDialogFragment;
    /**
     * 是否鉴权过期
     */
    private boolean mIsTimeExpired = false;
    /**
     * 弹幕设置View
     */
    private DanmakuSettingView mDanmakuSettingView;
    //弹幕透明度、显示区域、速率progress
    private int mAlphProgress = 0, mRegionProgress = 0, mSpeedProgress = 30;

    /**
     * get StsToken stats
     */
    private boolean inRequest;
    /**
     * 点击发送弹幕的画笔弹出的dialog
     */
    private SoftInputDialogFragment mSoftInputDialogFragment;

    public MyAliView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public MyAliView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public MyAliView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(context);
    }

    private void init(Context context) {
        setManualBright();
        mCurrentBrightValue = getCurrentBrightValue();
//        mLocalVideoPath = getIntent().getStringExtra(GlobalPlayerConfig.Intent_Key.LOCAL_VIDEO_PATH);
//        mNeedOnlyFullScreen = context.getIntent().getBooleanExtra(GlobalPlayerConfig.Intent_Key.NEED_ONLY_FULL_SCREEN, false);
        initSoftDialogFragment();
        initAliyunPlayerView();
        initPlayerConfig();
        initDataSource();
    }

    /**
     * 播放方式
     */
    private void initDataSource() {
        GlobalPlayerConfig.PLAYTYPE mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType;
        if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            VidAuth vidAuth = getVidAuth(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            setAuthInfo(vidAuth);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS) {
            VidSts vidSts = getVidSts(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            setVidSts(vidSts);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL) {
            UrlSource urlSource = new UrlSource();
            mCurrentVideoId = "";
            if (TextUtils.isEmpty(mLocalVideoPath)) {
                urlSource.setUri(GlobalPlayerConfig.mUrlPath);
            } else {
                urlSource.setUri(mLocalVideoPath);
            }
            setLocalSource(urlSource);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            VidMps vidMps = getVidMps(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            setVidMps(vidMps);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.LIVE_STS) {
            LiveSts liveSts = getLiveSts(GlobalPlayerConfig.mLiveStsUrl);
            setLiveStsDataSource(liveSts);
        } else {
            //default
            currentVidItemPosition = 0;
            loadPlayList();
        }

//        if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS
//                || mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH
//                || mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.DEFAULT) {
//            mDownloadImageView.setVisibility(View.VISIBLE);
//        } else {
//            mDownloadImageView.setVisibility(View.GONE);
//        }
    }

    private void initSoftDialogFragment() {
        mSoftInputDialogFragment = SoftInputDialogFragment.newInstance();
        mSoftInputDialogFragment.setOnBarrageSendClickListener(new SoftInputDialogFragment.OnBarrageSendClickListener() {
            @Override
            public void onBarrageSendClick(String danmu) {
                setmDanmaku(danmu);
                mSoftInputDialogFragment.dismiss();
            }
        });
    }

    /**
     * 获取播放列表数据
     */
    private void loadPlayList() {
        AlivcOkHttpClient.getInstance().get(ServiceCommon.GET_VIDEO_DEFAULT_LIST, new AlivcOkHttpClient.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Request request, String result) {
                Gson gson = new Gson();
                AlivcVideoInfo alivcVideoInfo = gson.fromJson(result, AlivcVideoInfo.class);
                if (alivcVideoInfo != null && alivcVideoInfo.getData() != null) {
                    mVideoListBean = (ArrayList<AlivcVideoInfo.DataBean.VideoListBean>) alivcVideoInfo.getData().getVideoList();
//                    if (mAliyunPlayerVideoListAdapter != null) {
//                        mAliyunPlayerVideoListAdapter.setData(mVideoListBean);
//                        mAliyunPlayerVideoListAdapter.notifyDataSetChanged();
//                    }
                    if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.DEFAULT) {
                        mCurrentVideoId = mVideoListBean.get(currentVidItemPosition).getVideoId();
                        VidSts vidSts = getVidSts(mCurrentVideoId);
                        setVidSts(vidSts);
                    }
                }
            }
        });
    }

    /**
     * 获取LiveSts
     *
     * @param mUrlPath url地址
     */
    private LiveSts getLiveSts(String mUrlPath) {
        LiveSts liveSts = new LiveSts();
        liveSts.setUrl(mUrlPath);
        liveSts.setRegion(GlobalPlayerConfig.mRegion);
        liveSts.setAccessKeyId(GlobalPlayerConfig.mLiveStsAccessKeyId);
        liveSts.setAccessKeySecret(GlobalPlayerConfig.mLiveStsAccessKeySecret);
        liveSts.setSecurityToken(GlobalPlayerConfig.mLiveStsSecurityToken);
        liveSts.setDomain(GlobalPlayerConfig.mLiveStsDomain);
        liveSts.setApp(GlobalPlayerConfig.mLiveStsApp);
        liveSts.setStream(GlobalPlayerConfig.mLiveStsStream);
        return liveSts;
    }

    String TAG = MyAliView.class.getSimpleName();

    /**
     * 初始化播放配置
     */
    private void initPlayerConfig() {
        //界面设置
        setEnableHardwareDecoder(GlobalPlayerConfig.mEnableHardDecodeType);
        setRenderMirrorMode(GlobalPlayerConfig.mMirrorMode);
        setRenderRotate(GlobalPlayerConfig.mRotateMode);
        //播放配置设置
        PlayerConfig playerConfig = getPlayerConfig();
        playerConfig.mStartBufferDuration = GlobalPlayerConfig.PlayConfig.mStartBufferDuration;
        playerConfig.mHighBufferDuration = GlobalPlayerConfig.PlayConfig.mHighBufferDuration;
        playerConfig.mMaxBufferDuration = GlobalPlayerConfig.PlayConfig.mMaxBufferDuration;
        playerConfig.mMaxDelayTime = GlobalPlayerConfig.PlayConfig.mMaxDelayTime;
        playerConfig.mNetworkTimeout = GlobalPlayerConfig.PlayConfig.mNetworkTimeout;
        playerConfig.mMaxProbeSize = GlobalPlayerConfig.PlayConfig.mMaxProbeSize;
        playerConfig.mReferrer = GlobalPlayerConfig.PlayConfig.mReferrer;
        playerConfig.mHttpProxy = GlobalPlayerConfig.PlayConfig.mHttpProxy;
        playerConfig.mNetworkRetryCount = GlobalPlayerConfig.PlayConfig.mNetworkRetryCount;
        playerConfig.mEnableSEI = GlobalPlayerConfig.PlayConfig.mEnableSei;
        playerConfig.mClearFrameWhenStop = GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop;
        setPlayerConfig(playerConfig);
        //缓存设置
        initCacheConfig();
        Log.e(TAG, "cache dir : " + GlobalPlayerConfig.PlayCacheConfig.mDir
                + " startBufferDuration = " + GlobalPlayerConfig.PlayConfig.mStartBufferDuration
                + " highBufferDuration = " + GlobalPlayerConfig.PlayConfig.mHighBufferDuration
                + " maxBufferDuration = " + GlobalPlayerConfig.PlayConfig.mMaxBufferDuration
                + " maxDelayTime = " + GlobalPlayerConfig.PlayConfig.mMaxDelayTime
                + " enableCache = " + GlobalPlayerConfig.PlayCacheConfig.mEnableCache
                + " --- mMaxDurationS = " + GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS
                + " --- mMaxSizeMB = " + GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB);
    }

    /**
     * 判断是否有网络的监听
     */
    private class MyNetConnectedListener implements AliyunVodPlayerView.NetConnectedListener {
        WeakReference<MyAliView> weakReference;

        public MyNetConnectedListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onReNetConnected(boolean isReconnect) {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onReNetConnected(isReconnect);
            }
        }

        @Override
        public void onNetUnConnected() {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onNetUnConnected();
            }
        }
    }

    private void onReNetConnected(boolean isReconnect) {
        currentError = ErrorInfo.Normal;
    }

    private void onNetUnConnected() {
        currentError = ErrorInfo.UnConnectInternet;
    }

    private static class MyCompletionListener implements IPlayer.OnCompletionListener {

        private WeakReference<MyAliView> activityWeakReference;

        public MyCompletionListener(MyAliView skinActivity) {
            activityWeakReference = new WeakReference<MyAliView>(skinActivity);
        }

        @Override
        public void onCompletion() {

            MyAliView activity = activityWeakReference.get();
            if (activity != null) {
                activity.onCompletion();
            }
        }
    }

    /**
     * 隐藏所有Dialog
     */
    private void hideAllDialog() {
        if (danmakuShowMoreDialog != null && danmakuShowMoreDialog.isShowing()) {
            danmakuShowMoreDialog.dismiss();
        }
        if (showMoreDialog != null && showMoreDialog.isShowing()) {
            showMoreDialog.dismiss();
        }
    }

    /**
     * 播放下一个视频
     */
    private void onNext() {
        if (currentError == ErrorInfo.UnConnectInternet) {
            // 此处需要判断网络和播放类型
            // 网络资源, 播放完自动波下一个, 无网状态提示ErrorTipsView
            // 本地资源, 播放完需要重播, 显示Replay, 此处不需要处理
            if (GlobalPlayerConfig.mCurrentPlayType.equals(GlobalPlayerConfig.PLAYTYPE.STS)) {
                showErrorTipView(4014, "-1", getResources().getString(R.string.alivc_net_disable));
            }
            return;
        }

        currentVidItemPosition++;
        if (currentVidItemPosition > mVideoListBean.size() - 1) {
            //列表循环播放，如发现播放完成了从列表的第一个开始重新播放
            currentVidItemPosition = 0;
        }

        if (mVideoListBean.size() > 0) {
            AlivcVideoInfo.DataBean.VideoListBean videoListBean = mVideoListBean.get(currentVidItemPosition);
            if (videoListBean != null) {
                changePlayVidSource(videoListBean);
            }
        }

    }

    private void initCacheConfig() {
        CacheConfig cacheConfig = new CacheConfig();
        GlobalPlayerConfig.PlayCacheConfig.mDir = FileUtils.getDir(context) + GlobalPlayerConfig.CACHE_DIR_PATH;
        cacheConfig.mEnable = GlobalPlayerConfig.PlayCacheConfig.mEnableCache;
        cacheConfig.mDir = GlobalPlayerConfig.PlayCacheConfig.mDir;
        cacheConfig.mMaxDurationS = GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS;
        cacheConfig.mMaxSizeMB = GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB;

        setCacheConfig(cacheConfig);
    }

    /**
     * 获取VidSts
     *
     * @param vid videoId
     */
    private VidSts getVidSts(String vid) {
        VidSts vidSts = new VidSts();
        vidSts.setVid(vid);
        vidSts.setRegion(GlobalPlayerConfig.mRegion);
        vidSts.setAccessKeyId(GlobalPlayerConfig.mStsAccessKeyId);
        vidSts.setSecurityToken(GlobalPlayerConfig.mStsSecurityToken);
        vidSts.setAccessKeySecret(GlobalPlayerConfig.mStsAccessKeySecret);
        //试看
        if (GlobalPlayerConfig.mPreviewTime > 0) {
            VidPlayerConfigGen configGen = new VidPlayerConfigGen();
            configGen.setPreviewTime(GlobalPlayerConfig.mPreviewTime);
            vidSts.setPlayConfig(configGen);
        }

        if (GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen) {
            List<Definition> list = new ArrayList<>();
            list.add(Definition.DEFINITION_AUTO);
            vidSts.setDefinition(list);
        }
        return vidSts;
    }

    /**
     * 切换播放资源
     */
    private void changePlayVidSource(AlivcVideoInfo.DataBean.VideoListBean videoListItem) {
        initCacheConfig();
        mCurrentVideoId = videoListItem.getVideoId();
        VidSts vidSts = getVidSts(mCurrentVideoId);
        setVidSts(vidSts);
    }

    private void onCompletion() {
        Toast.makeText(context, R.string.toast_play_compleion, Toast.LENGTH_SHORT).show();

        hideAllDialog();

        // 当前视频播放结束, 播放下一个视频
        if (GlobalPlayerConfig.mCurrentPlayType.equals(GlobalPlayerConfig.PLAYTYPE.DEFAULT)) {
            onNext();
        } else {
            showReplay();
        }
    }

    private static class MyOrientationChangeListener implements AliyunVodPlayerView.OnOrientationChangeListener {

        private final WeakReference<MyAliView> weakReference;

        public MyOrientationChangeListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void orientationChange(boolean from, AliyunScreenMode currentMode) {
            MyAliView activity = weakReference.get();

            if (activity != null) {
                if (currentMode == AliyunScreenMode.Small
                        && GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL
                        && !TextUtils.isEmpty(activity.mLocalVideoPath)) {
                    //如果播放本地视频，切换到小屏后，直接关闭
//                    activity.finish();
                } else {
                    activity.hideDownloadDialog(from, currentMode);
                    activity.hideShowMoreDialog(from, currentMode);
                    activity.hideDanmakuSettingDialog(from, currentMode);
                    activity.hideScreenSostDialog(from, currentMode);
                }
            }
        }
    }

    private void hideDanmakuSettingDialog(boolean fromUser, AliyunScreenMode currentMode) {
        if (danmakuShowMoreDialog != null) {
            if (currentMode == AliyunScreenMode.Small) {
                danmakuShowMoreDialog.dismiss();
                currentScreenMode = currentMode;
            }
        }
    }

    private void hideScreenSostDialog(boolean fromUser, AliyunScreenMode currentMode) {//投屏dialog
//        if (screenShowMoreDialog != null) {
//            if (currentMode == AliyunScreenMode.Small) {
//                screenShowMoreDialog.dismiss();
//                currentScreenMode = currentMode;
//            }
//        }
    }

    private void hideShowMoreDialog(boolean from, AliyunScreenMode currentMode) {
        if (showMoreDialog != null) {
            if (currentMode == AliyunScreenMode.Small) {
                showMoreDialog.dismiss();
                currentScreenMode = currentMode;
            }
        }
    }

    private void hideDownloadDialog(boolean from, AliyunScreenMode currentMode) {

        if (mAlivcListSelectorDialogFragment != null) {
            if (currentScreenMode != currentMode) {
                mAlivcListSelectorDialogFragment.dismiss();
                currentScreenMode = currentMode;
            }
        }
    }

    private void initAliyunPlayerView() {
        {
            //保持屏幕敞亮
            setKeepScreenOn(true);
            setTheme(Theme.Blue);
            setAutoPlay(true);
//            needOnlyFullScreenPlay(mNeedOnlyFullScreen);

            setOnPreparedListener(new MyPrepareListener(this));
            setNetConnectedListener(new MyNetConnectedListener(this));
            setOnCompletionListener(new MyCompletionListener(this));
            setOnFirstFrameStartListener(new MyFrameInfoListener(this));
            setOnTrackChangedListener(new MyOnTrackChangedListener(this));
            setOnStoppedListener(new MyStoppedListener(this));
            setOrientationChangeListener(new MyOrientationChangeListener(this));
            setOnTimeExpiredErrorListener(new MyOnTimeExpiredErrorListener(this));
            setOnShowMoreClickListener(new MyShowMoreClickLisener(this));
            setOnPlayStateBtnClickListener(new MyPlayStateBtnClickListener(this));
            setOnSeekCompleteListener(new MySeekCompleteListener(this));
            setOnSeekStartListener(new MySeekStartListener(this));
//            setOnFinishListener(new MyOnFinishListener(this));
            setOnScreenCostingSingleTagListener(new MyOnScreenCostingSingleTagListener(this));
            setOnScreenBrightness(new MyOnScreenBrightnessListener(this));
//            setSoftKeyHideListener(new MyOnSoftKeyHideListener(this));
            setOnErrorListener(new MyOnErrorListener(this));
            if (context instanceof Activity)
                setScreenBrightness(BrightnessDialog.getActivityBrightness((Activity) context));
            setOnTrackInfoClickListener(new MyOnTrackInfoClickListener(this));
            setOnInfoListener(new MyOnInfoListener(this));
            setOutOnSeiDataListener(new MyOnSeiDataListener(this));
            setOnTipClickListener(new MyOnTipClickListener(this));
            setOnTipsViewBackClickListener(new MyOnTipsViewBackClickListener(this));
            setOutOnVerifyStsCallback(new MyOnVerifyStsCallback(this));
            enableNativeLog();
            setScreenBrightness(mCurrentBrightValue);
            startNetWatch();
        }
    }

    private static class MyOnVerifyStsCallback implements IPlayer.OnVerifyStsCallback {

        private WeakReference<MyAliView> weakReference;

        public MyOnVerifyStsCallback(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public IPlayer.StsStatus onVerifySts(StsInfo stsInfo) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                return aliyunPlayerSkinActivity.onVerifySts(stsInfo);
            }
            return IPlayer.StsStatus.Valid;
        }
    }

    private IPlayer.StsStatus onVerifySts(final StsInfo stsInfo) {
        String mLiveExpiration = GlobalPlayerConfig.mLiveExpiration;
        long expirationInGMTFormat = TimeFormater.getExpirationInGMTFormat(mLiveExpiration);
        //判断鉴权信息是否过期
        if (TextUtils.isEmpty(mLiveExpiration) || DateUtil.getFixedSkewedTimeMillis() / 1000 > expirationInGMTFormat - 5 * 60) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayLiveStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String msg) {
                    onStop();
                    ToastUtils.show(context, "Get Sts Info error : " + msg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mLiveStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mLiveStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mLiveStsAccessKeySecret = dataBean.getAccessKeySecret();
                        GlobalPlayerConfig.mLiveExpiration = dataBean.getExpiration();

                        stsInfo.setAccessKeyId(GlobalPlayerConfig.mLiveStsAccessKeyId);
                        stsInfo.setAccessKeySecret(GlobalPlayerConfig.mLiveStsAccessKeySecret);
                        stsInfo.setSecurityToken(GlobalPlayerConfig.mLiveStsSecurityToken);
                        updateStsInfo(stsInfo);
                    }
                }
            });
            return IPlayer.StsStatus.Pending;
        } else {
            return IPlayer.StsStatus.Valid;
        }
    }

    /**
     * TipsView返回按钮点击事件
     */
    private static class MyOnTipsViewBackClickListener implements OnTipsViewBackClickListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnTipsViewBackClickListener(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onBackClick() {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onTipsViewClick();
            }
        }
    }

    private void onTipsViewClick() {
//        finish();
    }

    /**
     * TipsView点击监听事件
     */
    private static class MyOnTipClickListener implements TipsView.OnTipClickListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnTipClickListener(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onContinuePlay() {

        }

        @Override
        public void onStopPlay() {

        }

        @Override
        public void onRetryPlay(int errorCode) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                if (errorCode == ErrorCode.ERROR_LOADING_TIMEOUT.getValue()) {
                    aliyunPlayerSkinActivity.reTry();
                } else {
                    aliyunPlayerSkinActivity.refresh(false);
                }
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
//            MyAliView aliyunPlayerSkinActivity = weakReference.get();
//            if (aliyunPlayerSkinActivity != null) {
//                aliyunPlayerSkinActivity.finish();
//            }
        }
    }

    /**
     * 重试
     */
    private void refresh(final boolean isDownload) {
        if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String errorMsg) {
                    ToastUtils.show(context, errorMsg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mStsAccessKeySecret = dataBean.getAccessKeySecret();

                        VidSts vidSts = getVidSts(mCurrentVideoId);
                        if (isDownload) {
//                            mAliyunDownloadManager.setmVidSts(vidSts);
//                            mAliyunDownloadManager.prepareDownload(vidSts);
                        } else {
                            setVidSts(vidSts);
                        }

                    }
                }
            });
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayAuthInfo(new GetAuthInformation.OnGetPlayAuthInfoListener() {
                @Override
                public void onGetPlayAuthError(String msg) {
                    ToastUtils.show(context, msg);
                }

                @Override
                public void onGetPlayAuthSuccess(AliyunPlayAuth.PlayAuthBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mPlayAuth = dataBean.getPlayAuth();

                        VidAuth vidAuth = getVidAuth(mCurrentVideoId);
                        if (isDownload) {
//                            mAliyunDownloadManager.setmVidAuth(vidAuth);
//                            mAliyunDownloadManager.prepareDownload(vidAuth);
                        } else {
                            setAuthInfo(vidAuth);
                        }

                    }
                }
            });
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayMpsInfo(new GetAuthInformation.OnGetMpsInfoListener() {
                @Override
                public void onGetMpsError(String msg) {
                    ToastUtils.show(context, msg);
                }

                @Override
                public void onGetMpsSuccess(AliyunMps.MpsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mMpsRegion = dataBean.getRegionId();
                        GlobalPlayerConfig.mMpsAuthInfo = dataBean.getAuthInfo();
                        GlobalPlayerConfig.mMpsHlsUriToken = dataBean.getHlsUriToken();
                        GlobalPlayerConfig.mMpsAccessKeyId = dataBean.getAkInfo().getAccessKeyId();
                        GlobalPlayerConfig.mMpsSecurityToken = dataBean.getAkInfo().getSecurityToken();
                        GlobalPlayerConfig.mMpsAccessKeySecret = dataBean.getAkInfo().getAccessKeySecret();

                        VidMps vidMps = getVidMps(mCurrentVideoId);
                        setVidMps(vidMps);
                    }
                }
            });
        } else {
            reTry();
        }
    }

    /**
     * 获取VidMps
     *
     * @param vid videoId
     */
    private VidMps getVidMps(String vid) {
        VidMps vidMps = new VidMps();
        vidMps.setMediaId(vid);
        vidMps.setRegion(GlobalPlayerConfig.mMpsRegion);
        vidMps.setAccessKeyId(GlobalPlayerConfig.mMpsAccessKeyId);
        vidMps.setAccessKeySecret(GlobalPlayerConfig.mMpsAccessKeySecret);
        vidMps.setSecurityToken(GlobalPlayerConfig.mMpsSecurityToken);
        vidMps.setAuthInfo(GlobalPlayerConfig.mMpsAuthInfo);
        vidMps.setHlsUriToken(GlobalPlayerConfig.mMpsHlsUriToken);
        //试看
        if (GlobalPlayerConfig.mPreviewTime > 0) {
            VidPlayerConfigGen configGen = new VidPlayerConfigGen();
            configGen.setPreviewTime(GlobalPlayerConfig.mPreviewTime);
            vidMps.setPlayConfig(configGen);
        }

        if (GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen) {
            List<Definition> list = new ArrayList<>();
            list.add(Definition.DEFINITION_AUTO);
            vidMps.setDefinition(list);
        }
        return vidMps;
    }

    /**
     * 获取VidAuth
     *
     * @param vid videoId
     */
    private VidAuth getVidAuth(String vid) {
        VidAuth vidAuth = new VidAuth();
        vidAuth.setVid(vid);
        vidAuth.setRegion(GlobalPlayerConfig.mRegion);
        vidAuth.setPlayAuth(GlobalPlayerConfig.mPlayAuth);
        //试看
        if (GlobalPlayerConfig.mPreviewTime > 0) {
            VidPlayerConfigGen configGen = new VidPlayerConfigGen();
            configGen.setPreviewTime(GlobalPlayerConfig.mPreviewTime);
            vidAuth.setPlayConfig(configGen);
        }

        if (GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen) {
            List<Definition> list = new ArrayList<>();
            list.add(Definition.DEFINITION_AUTO);
            vidAuth.setDefinition(list);
        }
        return vidAuth;
    }

    /**
     * sei监听事件
     */
    private static class MyOnSeiDataListener implements IPlayer.OnSeiDataListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnSeiDataListener(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onSeiData(int type, byte[] bytes) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSeiData(type, bytes);
            }
        }
    }

    private void onSeiData(int type, byte[] bytes) {
//        Log.e(TAG, "onSeiData: type = " + type + " data = " + new String(bytes));
    }

    private static class MyOnInfoListener implements IPlayer.OnInfoListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnInfoListener(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onInfo(InfoBean infoBean) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onInfo(infoBean);
            }
        }
    }

    private void onInfo(InfoBean infoBean) {
        if (infoBean.getCode() == InfoCode.CacheSuccess) {
            Toast.makeText(context, R.string.alivc_player_cache_success, Toast.LENGTH_SHORT).show();
        } else if (infoBean.getCode() == InfoCode.CacheError) {
            Toast.makeText(context, infoBean.getExtraMsg(), Toast.LENGTH_SHORT).show();
        } else if (infoBean.getCode() == InfoCode.SwitchToSoftwareVideoDecoder) {
            Toast.makeText(context, R.string.alivc_player_switch_to_software_video_decoder, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 字幕、清晰度、码率、音轨点击事件
     */
    private static class MyOnTrackInfoClickListener implements ControlView.OnTrackInfoClickListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnTrackInfoClickListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        //字幕
        @Override
        public void onSubtitleClick(List<TrackInfo> subtitleTrackInfoList) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSubtitleClick(subtitleTrackInfoList);
            }
        }

        //音轨
        @Override
        public void onAudioClick(List<TrackInfo> audioTrackInfoList) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onAudioClick(audioTrackInfoList);
            }
        }

        //码率
        @Override
        public void onBitrateClick(List<TrackInfo> bitrateTrackInfoList) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onBitrateClick(bitrateTrackInfoList);
            }
        }

        //清晰度
        @Override
        public void onDefinitionClick(List<TrackInfo> definitionTrackInfoList) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onDefinitionClick(definitionTrackInfoList);
            }
        }
    }

    /**
     * 清晰度改变事件
     */
    private void onDefinitionClick(List<TrackInfo> definitionTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(context);
        final TrackInfoView mTrackInfoView = new TrackInfoView(context);
        mTrackInfoView.setTrackInfoLists(definitionTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(currentTrack(TrackInfo.Type.TYPE_VOD));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnDefinitionChangedListener(new TrackInfoView.OnDefinitionChangedListrener() {
            @Override
            public void onDefinitionChanged(TrackInfo selectTrackInfo) {
                selectTrack(selectTrackInfo);
            }
        });
    }

    /**
     * 码率改变事件
     */
    private void onBitrateClick(List<TrackInfo> bitrateTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(context);
        final TrackInfoView mTrackInfoView = new TrackInfoView(context);
        mTrackInfoView.setTrackInfoLists(bitrateTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(currentTrack(TrackInfo.Type.TYPE_VIDEO));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnBitrateChangedListener(new TrackInfoView.OnBitrateChangedListener() {
            @Override
            public void onBitrateChanged(TrackInfo selectTrackInfo, int checkedId) {
                if (checkedId == R.id.auto_bitrate) {
                    selectAutoBitrateTrack();
                } else {
                    selectTrack(selectTrackInfo);
                }
            }
        });
    }

    /**
     * 音轨改变事件
     */
    private void onAudioClick(List<TrackInfo> audioTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(context);
        final TrackInfoView mTrackInfoView = new TrackInfoView(context);
        mTrackInfoView.setTrackInfoLists(audioTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(currentTrack(TrackInfo.Type.TYPE_AUDIO));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnAudioChangedListener(new TrackInfoView.OnAudioChangedListener() {
            @Override
            public void onAudioChanged(TrackInfo selectTrackInfo) {
                selectTrack(selectTrackInfo);
            }
        });
    }

    /**
     * 字幕改变事件
     */
    private void onSubtitleClick(List<TrackInfo> subtitleTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(context);
        final TrackInfoView mTrackInfoView = new TrackInfoView(context);
        mTrackInfoView.setTrackInfoLists(subtitleTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(currentTrack(TrackInfo.Type.TYPE_SUBTITLE));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnSubtitleChangedListener(new TrackInfoView.OnSubtitleChangedListener() {
            @Override
            public void onSubtitleChanged(TrackInfo selectTrackInfo) {
                selectTrack(selectTrackInfo);
            }

            @Override
            public void onSubtitleCancel() {
                Toast.makeText(context, R.string.alivc_player_cancel_subtitle, Toast.LENGTH_SHORT).show();
//                if (mAliyunVodPlayerView != null) {
//                    mAliyunVodPlayerView.cancelSubtitle();
//                }
            }
        });
    }


    /**
     * 播放器出错监听
     */
    private static class MyOnErrorListener implements IPlayer.OnErrorListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnErrorListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onError(com.aliyun.player.bean.ErrorInfo errorInfo) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onError(errorInfo);
            }
        }
    }

    private void onError(com.aliyun.player.bean.ErrorInfo errorInfo) {
        //鉴权过期
        if (errorInfo.getCode().getValue() == ErrorCode.ERROR_SERVER_POP_UNKNOWN.getValue()) {
            mIsTimeExpired = true;
        }
    }

    /**
     * 软键盘隐藏监听
     */
    private static class MyOnSoftKeyHideListener implements AliyunVodPlayerView.OnSoftKeyHideListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnSoftKeyHideListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void softKeyHide() {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
//                aliyunPlayerSkinActivity.hideSoftKeyBoard(aliyunPlayerSkinActivity);
            }
        }

        @Override
        public void onClickPaint() {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSoftKeyShow();

            }
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void onSoftKeyShow() {
//        if (mSoftInputDialogFragment != null) {
//            mSoftInputDialogFragment.show(getActivity().getSupportFragmentManager(), "SoftInputDialogFragment");
//        }
    }

    private static class MyOnScreenBrightnessListener implements AliyunVodPlayerView.OnScreenBrightnessListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnScreenBrightnessListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onScreenBrightness(int brightness) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.setWindowBrightness(brightness);
                aliyunPlayerSkinActivity.setScreenBrightness(brightness);
            }
        }
    }

    private static class MyOnScreenCostingSingleTagListener implements OnScreenCostingSingleTagListener {

        private WeakReference<MyAliView> weakReference;

        private MyOnScreenCostingSingleTagListener(MyAliView aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onScreenCostingSingleTag() {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.screenCostingSingleTag();
            }
        }
    }

    private void screenCostingSingleTag() {
//        if (screenShowMoreDialog != null && screenShowMoreDialog.isShowing()) {
//            screenShowMoreDialog.dismiss();
//        }
    }

    private static class MySeekStartListener implements AliyunVodPlayerView.OnSeekStartListener {
        WeakReference<MyAliView> weakReference;

        MySeekStartListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSeekStart(int position) {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onSeekStart(position);
            }
        }
    }

    private void onSeekStart(int position) {
    }

    private static class MySeekCompleteListener implements IPlayer.OnSeekCompleteListener {
        WeakReference<MyAliView> weakReference;

        MySeekCompleteListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSeekComplete() {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onSeekComplete();
            }
        }
    }

    private void onSeekComplete() {
    }

    private static class MyPlayStateBtnClickListener implements AliyunVodPlayerView.OnPlayStateBtnClickListener {
        WeakReference<MyAliView> weakReference;

        MyPlayStateBtnClickListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onPlayBtnClick(int playerState) {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onPlayStateSwitch(playerState);
            }
        }
    }

    /**
     * 播放状态切换
     */
    private void onPlayStateSwitch(int playerState) {
        if (playerState == IPlayer.started) {

        } else if (playerState == IPlayer.paused) {

        }

    }

    private static class MyShowMoreClickLisener implements ControlView.OnShowMoreClickListener {
        WeakReference<MyAliView> weakReference;

        MyShowMoreClickLisener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void showMore() {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                activity.showMore(activity);
            }
        }
    }

    private void showMore(final MyAliView activity) {
        showMoreDialog = new AlivcShowMoreDialog(context);
        AliyunShowMoreValue moreValue = new AliyunShowMoreValue();
        moreValue.setSpeed(getCurrentSpeed());
        moreValue.setVolume((int) getCurrentVolume());
        moreValue.setScaleMode(getScaleMode());
        moreValue.setLoop(isLoop());

        ShowMoreView showMoreView = new ShowMoreView(context, moreValue);
        showMoreDialog.setContentView(showMoreView);
        showMoreDialog.show();

        showMoreView.setOnScreenCastButtonClickListener(new ShowMoreView.OnScreenCastButtonClickListener() {
            @Override
            public void onScreenCastClick() {
            }
        });

        showMoreView.setOnBarrageButtonClickListener(new ShowMoreView.OnBarrageButtonClickListener() {
            @Override
            public void onBarrageClick() {
                if (showMoreDialog != null && showMoreDialog.isShowing()) {
                    showMoreDialog.dismiss();
                }
                showDanmakuSettingView();
            }
        });

        showMoreView.setOnSpeedCheckedChangedListener(new ShowMoreView.OnSpeedCheckedChangedListener() {
            @Override
            public void onSpeedChanged(RadioGroup group, int checkedId) {
                // 点击速度切换
                if (checkedId == R.id.rb_speed_normal) {
                    changeSpeed(SpeedValue.One);
                } else if (checkedId == R.id.rb_speed_onequartern) {
                    changeSpeed(SpeedValue.OneQuartern);
                } else if (checkedId == R.id.rb_speed_onehalf) {
                    changeSpeed(SpeedValue.OneHalf);
                } else if (checkedId == R.id.rb_speed_twice) {
                    changeSpeed(SpeedValue.Twice);
                }

            }
        });

        showMoreView.setOnScaleModeCheckedChangedListener(new ShowMoreView.OnScaleModeCheckedChangedListener() {
            @Override
            public void onScaleModeChanged(RadioGroup group, int checkedId) {
                //切换画面比例
                IPlayer.ScaleMode mScaleMode;
                if (checkedId == R.id.rb_scale_aspect_fit) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
                } else if (checkedId == R.id.rb_scale_aspect_fill) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FILL;
                } else if (checkedId == R.id.rb_scale_to_fill) {
                    mScaleMode = IPlayer.ScaleMode.SCALE_TO_FILL;
                } else {
                    mScaleMode = IPlayer.ScaleMode.SCALE_ASPECT_FIT;
                }
                setScaleMode(mScaleMode);
            }
        });

        showMoreView.setOnLoopCheckedChangedListener(new ShowMoreView.OnLoopCheckedChangedListener() {
            @Override
            public void onLoopChanged(RadioGroup group, int checkedId) {
                boolean isLoop;
                if (checkedId == R.id.rb_loop_open) {
                    isLoop = true;
                } else {
                    isLoop = false;
                }
                setLoop(isLoop);
            }
        });

        /**
         * 初始化亮度
         */
        showMoreView.setBrightness(getScreenBrightness());
        // 亮度seek
        showMoreView.setOnLightSeekChangeListener(new ShowMoreView.OnLightSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                setWindowBrightness(progress);
                setScreenBrightness(progress);
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

        /**
         * 初始化音量
         */
        showMoreView.setVoiceVolume(getCurrentVolume());
        showMoreView.setOnVoiceSeekChangeListener(new ShowMoreView.OnVoiceSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                setCurrentVolume(progress / 100.00f);
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

    }

    /**
     * 显示弹幕设置对话框
     */
    private void showDanmakuSettingView() {
        danmakuShowMoreDialog = new AlivcShowMoreDialog(context);
        mDanmakuSettingView = new DanmakuSettingView(context);
        mDanmakuSettingView.setAlphaProgress(mAlphProgress);
        mDanmakuSettingView.setSpeedProgress(mSpeedProgress);
        mDanmakuSettingView.setRegionProgress(mRegionProgress);
        danmakuShowMoreDialog.setContentView(mDanmakuSettingView);
        danmakuShowMoreDialog.show();

        //透明度
        mDanmakuSettingView.setOnAlphaSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAlphProgress = progress;
                setDanmakuAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //显示区域
        mDanmakuSettingView.setOnRegionSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mRegionProgress = progress;
                setDanmakuRegion(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //速率
        mDanmakuSettingView.setOnSpeedSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpeedProgress = progress;
                setDanmakuSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //恢复默认
        mDanmakuSettingView.setOnDefaultListener(new DanmakuSettingView.OnDefaultClickListener() {
            @Override
            public void onDefaultClick() {
                setDanmakuDefault();
            }
        });

    }

    /**
     * 设置屏幕亮度
     */
    private void setWindowBrightness(int brightness) {
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = brightness / 100.00f;
            window.setAttributes(lp);
//        aliYunPlayerUtil.setWindowBrightness(brightness, activity);
        }
    }

    /**
     * 因为鉴权过期,而去重新鉴权
     */
    private static class RetryExpiredSts implements VidStsUtil.OnStsResultListener {

        private WeakReference<MyAliView> weakReference;

        public RetryExpiredSts(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(String vid, String akid, String akSecret, String token) {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onStsRetrySuccess(vid, akid, akSecret, token);
            }
        }

        @Override
        public void onFail() {

        }
    }

    private void onStsRetrySuccess(String mVid, String akid, String akSecret, String token) {
        GlobalPlayerConfig.mVid = mVid;
        GlobalPlayerConfig.mStsAccessKeyId = akid;
        GlobalPlayerConfig.mStsAccessKeySecret = akSecret;
        GlobalPlayerConfig.mStsSecurityToken = token;

        inRequest = false;
        mIsTimeExpired = false;

        VidSts vidSts = getVidSts(mVid);
        setVidSts(vidSts);
    }

    /**
     * 鉴权过期
     */
    private void onTimExpiredError() {
        VidStsUtil.getVidSts(GlobalPlayerConfig.mVid, new MyAliView.RetryExpiredSts(this));
    }

    public static class MyOnTimeExpiredErrorListener implements AliyunVodPlayerView.OnTimeExpiredErrorListener {

        WeakReference<MyAliView> weakReference;

        public MyOnTimeExpiredErrorListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onTimeExpiredError() {
            MyAliView activity = weakReference.get();
            if (activity != null) {
                activity.onTimExpiredError();
            }
        }
    }

    private static class MyStoppedListener implements OnStoppedListener {

        private WeakReference<MyAliView> activityWeakReference;

        public MyStoppedListener(MyAliView skinActivity) {
            activityWeakReference = new WeakReference<>(skinActivity);
        }

        @Override
        public void onStop() {
            MyAliView activity = activityWeakReference.get();
            if (activity != null) {
                activity.onStopped();
            }
        }
    }

    private void onStopped() {
        Toast.makeText(context, R.string.log_play_stopped, Toast.LENGTH_SHORT).show();
    }

    //清晰度切换listener
    private static class MyOnTrackChangedListener implements IPlayer.OnTrackChangedListener {

        private WeakReference<MyAliView> weakReference;

        public MyOnTrackChangedListener(MyAliView activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onChangedSuccess(TrackInfo trackInfo) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.changeTrackSuccess(trackInfo);
            }
        }

        @Override
        public void onChangedFail(TrackInfo trackInfo, com.aliyun.player.bean.ErrorInfo errorInfo) {
            MyAliView aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.changeTrackFail(trackInfo, errorInfo);
            }
        }
    }

    private void changeTrackFail(TrackInfo trackInfo, com.aliyun.player.bean.ErrorInfo errorInfo) {
        if (showMoreDialog != null && showMoreDialog.isShowing()) {
            showMoreDialog.dismiss();
        }
        Toast.makeText(context, context.getString(R.string.alivc_player_track_change_error, errorInfo.getCode(), errorInfo.getMsg()), Toast.LENGTH_SHORT).show();
    }

    //清晰度切换成功
    private void changeTrackSuccess(TrackInfo trackInfo) {
        if (trackInfo == null) {
            return;
        }
        if (showMoreDialog != null && showMoreDialog.isShowing()) {
            showMoreDialog.dismiss();
        }
        if (trackInfo.getType() == TrackInfo.Type.TYPE_VIDEO) {
            //码率
            Toast.makeText(context, context.getString(R.string.alivc_player_track_bitrate_change_success, trackInfo.getVideoBitrate() + ""), Toast.LENGTH_SHORT).show();
        } else if (trackInfo.getType() == TrackInfo.Type.TYPE_VOD) {//清晰度切换成功
            Toast.makeText(context, context.getString(R.string.alivc_player_track_definition_change_success, trackInfo.getVodDefinition()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.alivc_player_track_change_success, trackInfo.getDescription()), Toast.LENGTH_SHORT).show();
        }
    }

    private static class MyFrameInfoListener implements IPlayer.OnRenderingStartListener {

        private WeakReference<MyAliView> activityWeakReference;

        public MyFrameInfoListener(MyAliView skinActivity) {
            activityWeakReference = new WeakReference<>(skinActivity);
        }

        @Override
        public void onRenderingStart() {
            MyAliView activity = activityWeakReference.get();
            if (activity != null) {
                activity.onFirstFrameStart();
            }
        }
    }

    private void onFirstFrameStart() {

    }

    private static class MyPrepareListener implements IPlayer.OnPreparedListener {

        private WeakReference<MyAliView> activityWeakReference;

        public MyPrepareListener(MyAliView skinActivity) {
            activityWeakReference = new WeakReference<>(skinActivity);
        }

        @Override
        public void onPrepared() {
            MyAliView activity = activityWeakReference.get();
            if (activity != null) {
                activity.onPrepared();
            }
        }

    }

    private void onPrepared() {
        Toast.makeText(context, R.string.toast_prepare_success, Toast.LENGTH_SHORT).show();
        MediaInfo mediaInfo = getMediaInfo();
        if (mediaInfo != null) {
            mCurrentVideoId = mediaInfo.getVideoId();
        }
    }


    public void setManualBright() {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 仅当系统的亮度模式是非自动模式的情况下，获取当前屏幕亮度值[0, 255].
     * 如果是自动模式，那么该方法获得的值不正确。
     */
    private int getCurrentBrightValue() {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS, 255);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
}
