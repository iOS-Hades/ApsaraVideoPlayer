package com.aliyun.vodplayerview.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.aliyun.downloader.DownloaderConfig;
import com.aliyun.player.IPlayer;
import com.aliyun.player.VidPlayerConfigGen;
import com.aliyun.player.alivcplayerexpand.constants.GlobalPlayerConfig;
import com.aliyun.player.alivcplayerexpand.listener.OnScreenCostingSingleTagListener;
import com.aliyun.player.alivcplayerexpand.listener.OnStoppedListener;
import com.aliyun.player.alivcplayerexpand.listener.RefreshStsCallback;
import com.aliyun.player.alivcplayerexpand.playlist.AlivcVideoInfo;
import com.aliyun.player.alivcplayerexpand.theme.Theme;
import com.aliyun.player.alivcplayerexpand.util.Common;
import com.aliyun.player.alivcplayerexpand.util.TimeFormater;
import com.aliyun.player.alivcplayerexpand.util.VidStsUtil;
import com.aliyun.player.alivcplayerexpand.util.database.DatabaseManager;
import com.aliyun.player.alivcplayerexpand.util.database.LoadDbDatasListener;
import com.aliyun.player.alivcplayerexpand.util.download.AliyunDownloadManager;
import com.aliyun.player.alivcplayerexpand.util.download.AliyunDownloadMediaInfo;
import com.aliyun.player.alivcplayerexpand.view.choice.AlivcShowMoreDialog;
import com.aliyun.player.alivcplayerexpand.view.control.ControlView;
import com.aliyun.player.alivcplayerexpand.view.dlna.callback.OnDeviceItemClickListener;
import com.aliyun.player.alivcplayerexpand.view.gesturedialog.BrightnessDialog;
import com.aliyun.player.alivcplayerexpand.view.more.AliyunShowMoreValue;
import com.aliyun.player.alivcplayerexpand.view.more.DanmakuSettingView;
import com.aliyun.player.alivcplayerexpand.view.more.ScreenCostView;
import com.aliyun.player.alivcplayerexpand.view.more.ShowMoreView;
import com.aliyun.player.alivcplayerexpand.view.more.SpeedValue;
import com.aliyun.player.alivcplayerexpand.view.more.TrackInfoView;
import com.aliyun.player.alivcplayerexpand.view.softinput.SoftInputDialogFragment;
import com.aliyun.player.alivcplayerexpand.widget.AliyunVodPlayerView;
import com.aliyun.player.aliyunplayerbase.bean.AliyunMps;
import com.aliyun.player.aliyunplayerbase.bean.AliyunPlayAuth;
import com.aliyun.player.aliyunplayerbase.bean.AliyunSts;
import com.aliyun.player.aliyunplayerbase.bean.AliyunVideoList;
import com.aliyun.player.aliyunplayerbase.net.GetAuthInformation;
import com.aliyun.player.aliyunplayerbase.net.ServiceCommon;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.aliyunplayerbase.util.ScreenUtils;
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
import com.aliyun.private_service.PrivateService;
import com.aliyun.svideo.common.base.AlivcListSelectorDialogFragment;
import com.aliyun.svideo.common.okhttp.AlivcOkHttpClient;
import com.aliyun.svideo.common.utils.FastClickUtil;
import com.aliyun.svideo.common.utils.FileUtils;
import com.aliyun.svideo.common.utils.ToastUtils;
import com.aliyun.utils.VcPlayerLog;
import com.aliyun.vodplayer.R;
import com.aliyun.vodplayerview.adapter.AliyunPlayerVideoListAdapter;
import com.aliyun.vodplayerview.global.Global;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by LiuHaiMing
 * on 2020/7/31.
 */
public class AliYunFragment extends Fragment {

    /**
     * 播放器播放方式 -- 博播放方式编辑界面 requestCode
     */
    private static final int PLAY_TYPE_EDIT_ACTIVITY_REQUEST = 0x0001;
    /**
     * 开始播放
     */
//        private TextView mStartPlayTextView;
    /**
     * 其他设置
     */
//        private RadioGroup mDecodeRadioGroup, mMirrorRadioGroup, mAutoSwithRadioGroup, mSeekModuleRadioGroup, mEnableBackgroundRadioGroup;
//        private ImageView mPlayConfigSettingImageView;

    /**
     * 播放方式
     */
    private RadioButton mPlayTypeUrlRadioButton, mPlayTypeStsRadioButton, mPlayTypeMpsRadioButton, mPlayTypeAuthRadioButton, mPlayTypeDefaultRadioButton, mPlayTypeLiveStsRadioButton;
    /**
     * 镜像模式
     */
    private RadioButton mPlayTypeRotate0RadioButton, mPlayTypeRotate90RadioButton, mPlayTypeRotate180RadioButton, mPlayTypeRotate270RadioButton;
    /**
     * 编辑
     */
//        private TextView mPlayTypeEditTextView;
    /**
     * 播放方式ListView
     */
    private List<RadioButton> mPlayTypeRadioButtonList = new ArrayList<>();
    /**
     * 加载中ProgressBar
     */
//        private ProgressBar mLoadingProgressBar;
    /**
     * 返回
     */
//        private ImageView mBackImageView;
    private Common commenUtils;
    /**
     * 下载帮助类
     */
    private AliyunDownloadManager mAliyunDownloadManager;

    private static final String TAG = "AliyunPlayerSkinActivit";

    private AliyunScreenMode currentScreenMode = AliyunScreenMode.Small;
    private ScreenCostView mScreenCostView;

    private AliyunVodPlayerView mAliyunVodPlayerView = null;
    private ErrorInfo currentError = ErrorInfo.Normal;

    /**
     * get StsToken stats
     */
    private boolean inRequest;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    /**
     * 当前点击的视频列表的下标
     */
    private int currentVidItemPosition;
    /**
     * 弹幕设置Dialog
     */
    private AlivcShowMoreDialog danmakuShowMoreDialog;
    /**
     * 更多Dialog
     */
    private AlivcShowMoreDialog showMoreDialog;
    /**
     * 投屏选择Dialog
     */
    private AlivcShowMoreDialog screenShowMoreDialog;
    /**
     * 弹幕设置View
     */
    private DanmakuSettingView mDanmakuSettingView;
    /**
     * 下载管理类
     */
    //弹幕透明度、显示区域、速率progress
    private int mAlphProgress = 0, mRegionProgress = 0, mSpeedProgress = 30;

    /**
     * 是否鉴权过期
     */
    private boolean mIsTimeExpired = false;
    /**
     * 是否正在加载下载信息
     */
    private boolean mIsLoadDownloadInfo = false;
    /**
     * 播放列表RecyclerView
     */
    private RecyclerView mPlayerListRecyclerView;
    /**
     * 播放列表Adapter
     */
    private AliyunPlayerVideoListAdapter mAliyunPlayerVideoListAdapter;

    /**
     * 播放列表资源
     */
    private ArrayList<AlivcVideoInfo.DataBean.VideoListBean> mVideoListBean;
    /**
     * 下载ImageView
     */
    private ImageView mDownloadImageView;
    /**
     * 分享ImageView
     */
    private ImageView mShareImageView;
    /**
     * 下载监听类
     */
    /**
     * 下载清晰度Dialog
     */
    private AlivcListSelectorDialogFragment mAlivcListSelectorDialogFragment;
    /**
     * 点击发送弹幕的画笔弹出的dialog
     */
    private SoftInputDialogFragment mSoftInputDialogFragment;
    /**
     * 下载列表
     */
    private TextView mDownloadListTextView;
    /**
     * 本地视频播放地址
     */
    private String mLocalVideoPath;

    /**
     * 用于恢复原本的播放方式，如果跳转到下载界面，播放本地视频，会切换到url播放方式
     */
    private GlobalPlayerConfig.PLAYTYPE mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType;
    /**
     * 下载Progress
     */
    private ProgressBar mDownloadProgressBar;
    /**
     * 当前正在播放的videoId
     */
    private String mCurrentVideoId;
    private boolean mNeedOnlyFullScreen;
    /**
     * 当前系统屏幕亮度
     */
    private int mCurrentBrightValue;
    /**
     * 判断是否是从下载界面进入到播放界面的
     */
    private boolean mIsFromDownloadActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aliyun_first, null);

        if (isStrangePhone()) {
            //            setTheme(R.style.ActTheme);
        } else {
            getActivity().setTheme(R.style.NoActionTheme);
        }

        super.onCreate(savedInstanceState);
        restoreSaveInstance(savedInstanceState);
        setManualBright();
        mCurrentBrightValue = getCurrentBrightValue();
        mLocalVideoPath = activity.getIntent().getStringExtra(GlobalPlayerConfig.Intent_Key.LOCAL_VIDEO_PATH);
        mNeedOnlyFullScreen = activity.getIntent().getBooleanExtra(GlobalPlayerConfig.Intent_Key.NEED_ONLY_FULL_SCREEN, false);
        initDownloadManager();
        initView();
        initAliyunPlayerView();
        initPlayerConfig();
        initDataSource();
        initVideoList();
        return view;
    }

    protected boolean isStrangePhone() {
        boolean strangePhone = "mx5".equalsIgnoreCase(Build.DEVICE)
                || "Redmi Note2".equalsIgnoreCase(Build.DEVICE)
                || "Z00A_1".equalsIgnoreCase(Build.DEVICE)
                || "hwH60-L02".equalsIgnoreCase(Build.DEVICE)
                || "hermes".equalsIgnoreCase(Build.DEVICE)
                || ("V4".equalsIgnoreCase(Build.DEVICE) && "Meitu".equalsIgnoreCase(Build.MANUFACTURER))
                || ("m1metal".equalsIgnoreCase(Build.DEVICE) && "Meizu".equalsIgnoreCase(Build.MANUFACTURER));

        VcPlayerLog.e("lfj1115 ", " Build.Device = " + Build.DEVICE + " , isStrange = " + strangePhone);
        return strangePhone;
    }

    public void setManualBright() {
        try {
            Settings.System.putInt(getActivity().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        copyAssets();
        activity = getActivity();
        initDownloadInfo();
        initCacheDir();
        initDataBase();
        initGlobalConfig();
//
//        //开始播放
//        getCurrentPlayType();
//        checkedIsNeedNormalData();


    }


    private void initVideoList() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        mPlayerListRecyclerView.setLayoutManager(mLinearLayoutManager);

        mAliyunPlayerVideoListAdapter = new AliyunPlayerVideoListAdapter(activity);
        mPlayerListRecyclerView.setAdapter(mAliyunPlayerVideoListAdapter);
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
     * 播放方式
     */
    private void initDataSource() {
        GlobalPlayerConfig.PLAYTYPE mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType;
        if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            VidAuth vidAuth = getVidAuth(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            mAliyunVodPlayerView.setAuthInfo(vidAuth);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS) {
            VidSts vidSts = getVidSts(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            mAliyunVodPlayerView.setVidSts(vidSts);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL) {
            UrlSource urlSource = new UrlSource();
            mCurrentVideoId = "";
            if (TextUtils.isEmpty(mLocalVideoPath)) {
                urlSource.setUri(GlobalPlayerConfig.mUrlPath);
            } else {
                urlSource.setUri(mLocalVideoPath);
            }
            mAliyunVodPlayerView.setLocalSource(urlSource);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            VidMps vidMps = getVidMps(GlobalPlayerConfig.mVid);
            mCurrentVideoId = GlobalPlayerConfig.mVid;
            mAliyunVodPlayerView.setVidMps(vidMps);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.LIVE_STS) {
            LiveSts liveSts = getLiveSts(GlobalPlayerConfig.mLiveStsUrl);
            mAliyunVodPlayerView.setLiveStsDataSource(liveSts);
        } else {
            //default
            currentVidItemPosition = 0;
            loadPlayList();
        }

        if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS
                || mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH
                || mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.DEFAULT) {
            mDownloadImageView.setVisibility(View.VISIBLE);
        } else {
            mDownloadImageView.setVisibility(View.GONE);
        }
    }


    /**
     * 获取播放列表数据
     */
    private void loadPlayList() {
        AlivcOkHttpClient.getInstance().get(ServiceCommon.GET_VIDEO_DEFAULT_LIST, new AlivcOkHttpClient.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(Request request, String result) {
                Gson gson = new Gson();
                AlivcVideoInfo alivcVideoInfo = gson.fromJson(result, AlivcVideoInfo.class);
                if (alivcVideoInfo != null && alivcVideoInfo.getData() != null) {
                    mVideoListBean = (ArrayList<AlivcVideoInfo.DataBean.VideoListBean>) alivcVideoInfo.getData().getVideoList();
                    if (mAliyunPlayerVideoListAdapter != null) {
                        mAliyunPlayerVideoListAdapter.setData(mVideoListBean);
                        mAliyunPlayerVideoListAdapter.notifyDataSetChanged();
                    }
                    if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.DEFAULT) {
                        if (mAliyunVodPlayerView != null) {
                            mCurrentVideoId = mVideoListBean.get(currentVidItemPosition).getVideoId();
                            VidSts vidSts = getVidSts(mCurrentVideoId);
                            mAliyunVodPlayerView.setVidSts(vidSts);
                        }
                    }
                }
            }
        });
    }

    /**
     * 初始化播放配置
     */
    private void initPlayerConfig() {
        if (mAliyunVodPlayerView != null) {
            //界面设置
            mAliyunVodPlayerView.setEnableHardwareDecoder(GlobalPlayerConfig.mEnableHardDecodeType);
            mAliyunVodPlayerView.setRenderMirrorMode(GlobalPlayerConfig.mMirrorMode);
            mAliyunVodPlayerView.setRenderRotate(GlobalPlayerConfig.mRotateMode);
            //播放配置设置
            PlayerConfig playerConfig = mAliyunVodPlayerView.getPlayerConfig();
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
            mAliyunVodPlayerView.setPlayerConfig(playerConfig);
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
    }

    private void initCacheConfig() {
        CacheConfig cacheConfig = new CacheConfig();
        GlobalPlayerConfig.PlayCacheConfig.mDir = FileUtils.getDir(activity) + GlobalPlayerConfig.CACHE_DIR_PATH;
        cacheConfig.mEnable = GlobalPlayerConfig.PlayCacheConfig.mEnableCache;
        cacheConfig.mDir = GlobalPlayerConfig.PlayCacheConfig.mDir;
        cacheConfig.mMaxDurationS = GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS;
        cacheConfig.mMaxSizeMB = GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB;

        mAliyunVodPlayerView.setCacheConfig(cacheConfig);
    }

    private void onPrepared() {
        Toast.makeText(activity, R.string.toast_prepare_success, Toast.LENGTH_SHORT).show();
        if (mAliyunVodPlayerView != null) {
            MediaInfo mediaInfo = mAliyunVodPlayerView.getMediaInfo();
            if (mediaInfo != null) {
                mCurrentVideoId = mediaInfo.getVideoId();
            }
        }
    }

    private void onReNetConnected(boolean isReconnect) {
        currentError = ErrorInfo.Normal;
    }

    private void onNetUnConnected() {
        currentError = ErrorInfo.UnConnectInternet;
    }

    /**
     * 判断是否有网络的监听
     */
    private class MyNetConnectedListener implements AliyunVodPlayerView.NetConnectedListener {
        WeakReference<AliYunFragment> weakReference;

        public MyNetConnectedListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onReNetConnected(boolean isReconnect) {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                activity.onReNetConnected(isReconnect);
            }
        }

        @Override
        public void onNetUnConnected() {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                activity.onNetUnConnected();
            }
        }
    }

    private static class MyPrepareListener implements IPlayer.OnPreparedListener {


        public MyPrepareListener() {
        }

        @Override
        public void onPrepared() {
            onPrepared();
        }
    }

    private static class MyCompletionListener implements IPlayer.OnCompletionListener {

        private WeakReference<AliYunFragment> activityWeakReference;

        public MyCompletionListener(AliYunFragment skinActivity) {
            activityWeakReference = new WeakReference<AliYunFragment>(skinActivity);
        }

        @Override
        public void onCompletion() {

            AliYunFragment activity = activityWeakReference.get();
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
        if (screenShowMoreDialog != null && screenShowMoreDialog.isShowing()) {
            screenShowMoreDialog.dismiss();
        }
    }

    /**
     * 切换播放资源
     */
    private void changePlayVidSource(AlivcVideoInfo.DataBean.VideoListBean videoListItem) {
        if (mAliyunVodPlayerView != null) {
            initCacheConfig();
            mCurrentVideoId = videoListItem.getVideoId();
            VidSts vidSts = getVidSts(mCurrentVideoId);
            mAliyunVodPlayerView.setVidSts(vidSts);
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
                mAliyunVodPlayerView.showErrorTipView(4014, "-1", getResources().getString(R.string.alivc_net_disable));
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

    private void onCompletion() {
        Toast.makeText(activity, R.string.toast_play_compleion, Toast.LENGTH_SHORT).show();

        hideAllDialog();

        // 当前视频播放结束, 播放下一个视频
        if (GlobalPlayerConfig.mCurrentPlayType.equals(GlobalPlayerConfig.PLAYTYPE.DEFAULT)) {
            onNext();
        } else {
            if (mAliyunVodPlayerView != null) {
                mAliyunVodPlayerView.showReplay();
            }
        }
    }

    private static class MyFrameInfoListener implements IPlayer.OnRenderingStartListener {

        private WeakReference<AliYunFragment> activityWeakReference;

        public MyFrameInfoListener(AliYunFragment skinActivity) {
            activityWeakReference = new WeakReference<>(skinActivity);
        }

        @Override
        public void onRenderingStart() {
            AliYunFragment activity = activityWeakReference.get();
            if (activity != null) {
                activity.onFirstFrameStart();
            }
        }
    }

    private void onFirstFrameStart() {

    }

    //清晰度切换listener
    private static class MyOnTrackChangedListener implements IPlayer.OnTrackChangedListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnTrackChangedListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onChangedSuccess(TrackInfo trackInfo) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.changeTrackSuccess(trackInfo);
            }
        }

        @Override
        public void onChangedFail(TrackInfo trackInfo, com.aliyun.player.bean.ErrorInfo errorInfo) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.changeTrackFail(trackInfo, errorInfo);
            }
        }
    }

    private void changeTrackFail(TrackInfo trackInfo, com.aliyun.player.bean.ErrorInfo errorInfo) {
        if (showMoreDialog != null && showMoreDialog.isShowing()) {
            showMoreDialog.dismiss();
        }
        Toast.makeText(activity, getString(R.string.alivc_player_track_change_error, errorInfo.getCode(), errorInfo.getMsg()), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(activity, getString(R.string.alivc_player_track_bitrate_change_success, trackInfo.getVideoBitrate() + ""), Toast.LENGTH_SHORT).show();
        } else if (trackInfo.getType() == TrackInfo.Type.TYPE_VOD) {//清晰度切换成功
            Toast.makeText(activity, getString(R.string.alivc_player_track_definition_change_success, trackInfo.getVodDefinition()), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, getString(R.string.alivc_player_track_change_success, trackInfo.getDescription()), Toast.LENGTH_SHORT).show();
        }
    }

    private static class MyStoppedListener implements OnStoppedListener {

        private WeakReference<AliYunFragment> activityWeakReference;

        public MyStoppedListener(AliYunFragment skinActivity) {
            activityWeakReference = new WeakReference<>(skinActivity);
        }

        @Override
        public void onStop() {
            AliYunFragment activity = activityWeakReference.get();
            if (activity != null) {
                activity.onStopped();
            }
        }
    }

    private void onStopped() {
        Toast.makeText(activity, R.string.log_play_stopped, Toast.LENGTH_SHORT).show();
    }

    private void updatePlayerViewMode() {
        if (mAliyunVodPlayerView != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (mDownloadListTextView != null) {
                mDownloadListTextView.setVisibility(View.GONE);
//                mDownloadListTextView.setVisibility(orientation == Configuration.ORIENTATION_PORTRAIT ? View.VISIBLE : View.GONE);
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mAliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                //设置view的布局，宽高之类
                RelativeLayout.LayoutParams aliVcVideoViewLayoutParams = (RelativeLayout.LayoutParams) mAliyunVodPlayerView
                        .getLayoutParams();
                aliVcVideoViewLayoutParams.height = (int) (ScreenUtils.getWidth(activity) * 9.0f / 16);
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //转到横屏了。
                //隐藏状态栏
                if (!isStrangePhone()) {
                    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    mAliyunVodPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
                //设置view的布局，宽高
                RelativeLayout.LayoutParams aliVcVideoViewLayoutParams = (RelativeLayout.LayoutParams) mAliyunVodPlayerView
                        .getLayoutParams();
                aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlayerViewMode();
        if (!GlobalPlayerConfig.PlayConfig.mEnablePlayBackground || mIsFromDownloadActivity) {
            if (mAliyunVodPlayerView != null) {
                mAliyunVodPlayerView.setAutoPlay(true);
                mAliyunVodPlayerView.onResume();
            }
            GlobalPlayerConfig.mCurrentPlayType = mCurrentPlayType;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsFromDownloadActivity = false;
        if (!GlobalPlayerConfig.PlayConfig.mEnablePlayBackground) {
            if (mAliyunVodPlayerView != null) {
                mAliyunVodPlayerView.setAutoPlay(false);
                mAliyunVodPlayerView.onStop();
            }
            mCurrentPlayType = GlobalPlayerConfig.mCurrentPlayType;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updatePlayerViewMode();
    }

    private static class MyRefreshStsCallback implements RefreshStsCallback {

        @Override
        public VidSts refreshSts(String vid, String quality, String format, String title, boolean encript) {
            //NOTE: 注意：这个不能启动线程去请求。因为这个方法已经在线程中调用了。
            VidSts vidSts = VidStsUtil.getVidSts(vid);
            if (vidSts == null) {
                return null;
            } else {
                vidSts.setVid(vid);
                vidSts.setQuality(quality, true);
                vidSts.setTitle(title);
                return vidSts;
            }
        }
    }


    private void hideShowMoreDialog(boolean from, AliyunScreenMode currentMode) {
        if (showMoreDialog != null) {
            if (currentMode == AliyunScreenMode.Small) {
                showMoreDialog.dismiss();
                currentScreenMode = currentMode;
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

    private void hideScreenSostDialog(boolean fromUser, AliyunScreenMode currentMode) {
        if (screenShowMoreDialog != null) {
            if (currentMode == AliyunScreenMode.Small) {
                screenShowMoreDialog.dismiss();
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
        mDownloadListTextView.setVisibility(View.GONE);
//        mDownloadListTextView.setVisibility(currentMode == AliyunScreenMode.Small ? View.VISIBLE : View.GONE);
    }

    private static class MyOrientationChangeListener implements AliyunVodPlayerView.OnOrientationChangeListener {

        private final WeakReference<AliYunFragment> weakReference;

        public MyOrientationChangeListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void orientationChange(boolean from, AliyunScreenMode currentMode) {
            AliYunFragment activity = weakReference.get();

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

    /**
     * 因为鉴权过期,而去重新鉴权
     */
    private static class RetryExpiredSts implements VidStsUtil.OnStsResultListener {

        private WeakReference<AliYunFragment> weakReference;

        public RetryExpiredSts(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(String vid, String akid, String akSecret, String token) {
            AliYunFragment activity = weakReference.get();
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
        mAliyunVodPlayerView.setVidSts(vidSts);
    }

    /**
     * 鉴权过期
     */
    private void onTimExpiredError() {
        VidStsUtil.getVidSts(GlobalPlayerConfig.mVid, new RetryExpiredSts(this));
    }

    public static class MyOnTimeExpiredErrorListener implements AliyunVodPlayerView.OnTimeExpiredErrorListener {

        WeakReference<AliYunFragment> weakReference;

        public MyOnTimeExpiredErrorListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onTimeExpiredError() {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                activity.onTimExpiredError();
            }
        }
    }


    private static class MyShowMoreClickLisener implements ControlView.OnShowMoreClickListener {
        WeakReference<AliYunFragment> weakReference;

        MyShowMoreClickLisener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void showMore() {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                if (FastClickUtil.isFastClick()) {
                    return;
                }
                activity.showMore(activity);
            }
        }
    }

    private void showMore(final AliYunFragment activity) {
        showMoreDialog = new AlivcShowMoreDialog(getActivity());
        AliyunShowMoreValue moreValue = new AliyunShowMoreValue();
        moreValue.setSpeed(mAliyunVodPlayerView.getCurrentSpeed());
        moreValue.setVolume((int) mAliyunVodPlayerView.getCurrentVolume());
        moreValue.setScaleMode(mAliyunVodPlayerView.getScaleMode());
        moreValue.setLoop(mAliyunVodPlayerView.isLoop());

        ShowMoreView showMoreView = new ShowMoreView(getActivity(), moreValue);
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
                    mAliyunVodPlayerView.changeSpeed(SpeedValue.One);
                } else if (checkedId == R.id.rb_speed_onequartern) {
                    mAliyunVodPlayerView.changeSpeed(SpeedValue.OneQuartern);
                } else if (checkedId == R.id.rb_speed_onehalf) {
                    mAliyunVodPlayerView.changeSpeed(SpeedValue.OneHalf);
                } else if (checkedId == R.id.rb_speed_twice) {
                    mAliyunVodPlayerView.changeSpeed(SpeedValue.Twice);
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
                mAliyunVodPlayerView.setScaleMode(mScaleMode);
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
                mAliyunVodPlayerView.setLoop(isLoop);
            }
        });

        /**
         * 初始化亮度
         */
        if (mAliyunVodPlayerView != null) {
            showMoreView.setBrightness(mAliyunVodPlayerView.getScreenBrightness());
        }
        // 亮度seek
        showMoreView.setOnLightSeekChangeListener(new ShowMoreView.OnLightSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                setWindowBrightness(progress);
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setScreenBrightness(progress);
                }
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

        /**
         * 初始化音量
         */
        if (mAliyunVodPlayerView != null) {
            showMoreView.setVoiceVolume(mAliyunVodPlayerView.getCurrentVolume());
        }
        showMoreView.setOnVoiceSeekChangeListener(new ShowMoreView.OnVoiceSeekChangeListener() {
            @Override
            public void onStart(SeekBar seekBar) {

            }

            @Override
            public void onProgress(SeekBar seekBar, int progress, boolean fromUser) {
                mAliyunVodPlayerView.setCurrentVolume(progress / 100.00f);
            }

            @Override
            public void onStop(SeekBar seekBar) {

            }
        });

    }

    /**
     * 显示投屏对话框
     */
    private void showScreenCastView() {
        if (screenShowMoreDialog == null) {
            screenShowMoreDialog = new AlivcShowMoreDialog(activity);
        }
        if (mScreenCostView == null) {
            mScreenCostView = new ScreenCostView(activity);
        }
        screenShowMoreDialog.setContentView(mScreenCostView);
        screenShowMoreDialog.show();
        mScreenCostView.setOnDeviceItemClickListener(new OnDeviceItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.screenCostPlay();
                }
                if (screenShowMoreDialog != null) {
                    screenShowMoreDialog.dismiss();
                }
            }
        });

    }

    /**
     * 显示弹幕设置对话框
     */
    private void showDanmakuSettingView() {
        danmakuShowMoreDialog = new AlivcShowMoreDialog(activity);
        mDanmakuSettingView = new DanmakuSettingView(activity);
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
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setDanmakuAlpha(progress);
                }
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
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setDanmakuRegion(progress);
                }
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
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setDanmakuSpeed(progress);
                }
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
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.setDanmakuDefault();
                }
            }
        });

    }

    /**
     * 获取url的scheme
     *
     * @param url
     * @return
     */
    private String getUrlScheme(String url) {
        return Uri.parse(url).getScheme();
    }

    private static class MyPlayStateBtnClickListener implements AliyunVodPlayerView.OnPlayStateBtnClickListener {
        WeakReference<AliYunFragment> weakReference;

        MyPlayStateBtnClickListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onPlayBtnClick(int playerState) {
            AliYunFragment activity = weakReference.get();
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

    private static class MySeekCompleteListener implements IPlayer.OnSeekCompleteListener {
        WeakReference<AliYunFragment> weakReference;

        MySeekCompleteListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSeekComplete() {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                activity.onSeekComplete();
            }
        }
    }

    private void onSeekComplete() {
    }

    private static class MySeekStartListener implements AliyunVodPlayerView.OnSeekStartListener {
        WeakReference<AliYunFragment> weakReference;

        MySeekStartListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSeekStart(int position) {
            AliYunFragment activity = weakReference.get();
            if (activity != null) {
                activity.onSeekStart(position);
            }
        }
    }

    private static class MyOnFinishListener implements AliyunVodPlayerView.OnFinishListener {

        WeakReference<AliYunFragment> weakReference;

        public MyOnFinishListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onFinishClick() {
//            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
//            if (aliyunPlayerSkinActivity != null) {
//                aliyunPlayerSkinActivity.finish();
//            }
        }
    }

    private static class MyOnScreenBrightnessListener implements AliyunVodPlayerView.OnScreenBrightnessListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnScreenBrightnessListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onScreenBrightness(int brightness) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.setWindowBrightness(brightness);
                if (aliyunPlayerSkinActivity.mAliyunVodPlayerView != null) {
                    aliyunPlayerSkinActivity.mAliyunVodPlayerView.setScreenBrightness(brightness);
                }
            }
        }
    }

    /**
     * 软键盘隐藏监听
     */
    private static class MyOnSoftKeyHideListener implements AliyunVodPlayerView.OnSoftKeyHideListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnSoftKeyHideListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void softKeyHide() {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.hideSoftKeyBoard(aliyunPlayerSkinActivity);
            }
        }

        @Override
        public void onClickPaint() {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSoftKeyShow();

            }
        }
    }

    private void onSoftKeyShow() {
        if (mSoftInputDialogFragment != null) {
            mSoftInputDialogFragment.show(getActivity().getSupportFragmentManager(), "SoftInputDialogFragment");
        }
    }

    /**
     * 播放器出错监听
     */
    private static class MyOnErrorListener implements IPlayer.OnErrorListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnErrorListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void onError(com.aliyun.player.bean.ErrorInfo errorInfo) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
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
     * 字幕、清晰度、码率、音轨点击事件
     */
    private static class MyOnTrackInfoClickListener implements ControlView.OnTrackInfoClickListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnTrackInfoClickListener(AliYunFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        //字幕
        @Override
        public void onSubtitleClick(List<TrackInfo> subtitleTrackInfoList) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSubtitleClick(subtitleTrackInfoList);
            }
        }

        //音轨
        @Override
        public void onAudioClick(List<TrackInfo> audioTrackInfoList) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onAudioClick(audioTrackInfoList);
            }
        }

        //码率
        @Override
        public void onBitrateClick(List<TrackInfo> bitrateTrackInfoList) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onBitrateClick(bitrateTrackInfoList);
            }
        }

        //清晰度
        @Override
        public void onDefinitionClick(List<TrackInfo> definitionTrackInfoList) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onDefinitionClick(definitionTrackInfoList);
            }
        }
    }

    /**
     * 字幕改变事件
     */
    private void onSubtitleClick(List<TrackInfo> subtitleTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(activity);
        final TrackInfoView mTrackInfoView = new TrackInfoView(activity);
        mTrackInfoView.setTrackInfoLists(subtitleTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(mAliyunVodPlayerView.currentTrack(TrackInfo.Type.TYPE_SUBTITLE));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnSubtitleChangedListener(new TrackInfoView.OnSubtitleChangedListener() {
            @Override
            public void onSubtitleChanged(TrackInfo selectTrackInfo) {
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.selectTrack(selectTrackInfo);
                }
            }

            @Override
            public void onSubtitleCancel() {
                Toast.makeText(activity, R.string.alivc_player_cancel_subtitle, Toast.LENGTH_SHORT).show();
                if (mAliyunVodPlayerView != null) {
//                    mAliyunVodPlayerView.cancelSubtitle();
                }
            }
        });
    }

    /**
     * 音轨改变事件
     */
    private void onAudioClick(List<TrackInfo> audioTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(activity);
        final TrackInfoView mTrackInfoView = new TrackInfoView(activity);
        mTrackInfoView.setTrackInfoLists(audioTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(mAliyunVodPlayerView.currentTrack(TrackInfo.Type.TYPE_AUDIO));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnAudioChangedListener(new TrackInfoView.OnAudioChangedListener() {
            @Override
            public void onAudioChanged(TrackInfo selectTrackInfo) {
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.selectTrack(selectTrackInfo);
                }
            }
        });
    }

    /**
     * 码率改变事件
     */
    private void onBitrateClick(List<TrackInfo> bitrateTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(activity);
        final TrackInfoView mTrackInfoView = new TrackInfoView(activity);
        mTrackInfoView.setTrackInfoLists(bitrateTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(mAliyunVodPlayerView.currentTrack(TrackInfo.Type.TYPE_VIDEO));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnBitrateChangedListener(new TrackInfoView.OnBitrateChangedListener() {
            @Override
            public void onBitrateChanged(TrackInfo selectTrackInfo, int checkedId) {
                if (mAliyunVodPlayerView != null) {
                    if (checkedId == R.id.auto_bitrate) {
                        mAliyunVodPlayerView.selectAutoBitrateTrack();
                    } else {
                        mAliyunVodPlayerView.selectTrack(selectTrackInfo);
                    }
                }
            }
        });
    }

    /**
     * 清晰度改变事件
     */
    private void onDefinitionClick(List<TrackInfo> definitionTrackInfoList) {
        showMoreDialog = new AlivcShowMoreDialog(activity);
        final TrackInfoView mTrackInfoView = new TrackInfoView(activity);
        mTrackInfoView.setTrackInfoLists(definitionTrackInfoList);
        mTrackInfoView.setCurrentTrackInfo(mAliyunVodPlayerView.currentTrack(TrackInfo.Type.TYPE_VOD));
        showMoreDialog.setContentView(mTrackInfoView);
        showMoreDialog.show();

        mTrackInfoView.setOnDefinitionChangedListener(new TrackInfoView.OnDefinitionChangedListrener() {
            @Override
            public void onDefinitionChanged(TrackInfo selectTrackInfo) {
                if (mAliyunVodPlayerView != null) {
                    mAliyunVodPlayerView.selectTrack(selectTrackInfo);
                }
            }
        });
    }

    private static class MyOnInfoListener implements IPlayer.OnInfoListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnInfoListener(AliYunFragment aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onInfo(InfoBean infoBean) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onInfo(infoBean);
            }
        }
    }

    private void onInfo(InfoBean infoBean) {
        if (infoBean.getCode() == InfoCode.CacheSuccess) {
            Toast.makeText(activity, R.string.alivc_player_cache_success, Toast.LENGTH_SHORT).show();
        } else if (infoBean.getCode() == InfoCode.CacheError) {
            Toast.makeText(activity, infoBean.getExtraMsg(), Toast.LENGTH_SHORT).show();
        } else if (infoBean.getCode() == InfoCode.SwitchToSoftwareVideoDecoder) {
            Toast.makeText(activity, R.string.alivc_player_switch_to_software_video_decoder, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * sei监听事件
     */
    private static class MyOnSeiDataListener implements IPlayer.OnSeiDataListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnSeiDataListener(AliYunFragment aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onSeiData(int type, byte[] bytes) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onSeiData(type, bytes);
            }
        }
    }

    private void onSeiData(int type, byte[] bytes) {
        Log.e(TAG, "onSeiData: type = " + type + " data = " + new String(bytes));
    }

    /**
     * TipsView点击监听事件
     */
    private static class MyOnTipClickListener implements TipsView.OnTipClickListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnTipClickListener(AliYunFragment aliyunPlayerSkinActivity) {
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
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                if (errorCode == ErrorCode.ERROR_LOADING_TIMEOUT.getValue()) {
                    aliyunPlayerSkinActivity.mAliyunVodPlayerView.reTry();
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
//            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
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
                    ToastUtils.show(activity, errorMsg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mStsAccessKeySecret = dataBean.getAccessKeySecret();

                        VidSts vidSts = getVidSts(mCurrentVideoId);
                        if (isDownload) {
                            mAliyunDownloadManager.setmVidSts(vidSts);
                            mAliyunDownloadManager.prepareDownload(vidSts);
                        } else {
                            if (mAliyunVodPlayerView != null) {
                                mAliyunVodPlayerView.setVidSts(vidSts);
                            }
                        }

                    }
                }
            });
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayAuthInfo(new GetAuthInformation.OnGetPlayAuthInfoListener() {
                @Override
                public void onGetPlayAuthError(String msg) {
                    ToastUtils.show(activity, msg);
                }

                @Override
                public void onGetPlayAuthSuccess(AliyunPlayAuth.PlayAuthBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mPlayAuth = dataBean.getPlayAuth();

                        VidAuth vidAuth = getVidAuth(mCurrentVideoId);
                        if (isDownload) {
                            mAliyunDownloadManager.setmVidAuth(vidAuth);
                            mAliyunDownloadManager.prepareDownload(vidAuth);
                        } else {
                            if (mAliyunVodPlayerView != null) {
                                mAliyunVodPlayerView.setAuthInfo(vidAuth);
                            }
                        }

                    }
                }
            });
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayMpsInfo(new GetAuthInformation.OnGetMpsInfoListener() {
                @Override
                public void onGetMpsError(String msg) {
                    ToastUtils.show(activity, msg);
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
                        if (mAliyunVodPlayerView != null) {
                            mAliyunVodPlayerView.setVidMps(vidMps);
                        }
                    }
                }
            });
        } else {
            if (mAliyunVodPlayerView != null) {
                mAliyunVodPlayerView.reTry();
            }
        }
    }

    /**
     * TipsView返回按钮点击事件
     */
    private static class MyOnTipsViewBackClickListener implements OnTipsViewBackClickListener {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnTipsViewBackClickListener(AliYunFragment aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onBackClick() {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.onTipsViewClick();
            }
        }
    }

    private void onTipsViewClick() {
//        finish();
    }

    private static class MyOnVerifyStsCallback implements IPlayer.OnVerifyStsCallback {

        private WeakReference<AliYunFragment> weakReference;

        public MyOnVerifyStsCallback(AliYunFragment aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public IPlayer.StsStatus onVerifySts(StsInfo stsInfo) {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                return aliyunPlayerSkinActivity.onVerifySts(stsInfo);
            }
            return IPlayer.StsStatus.Valid;
        }
    }

    private IPlayer.StsStatus onVerifySts(final StsInfo stsInfo) {
        Log.e(TAG, "onVerifySts: ");
        String mLiveExpiration = GlobalPlayerConfig.mLiveExpiration;
        long expirationInGMTFormat = TimeFormater.getExpirationInGMTFormat(mLiveExpiration);
        //判断鉴权信息是否过期
        if (TextUtils.isEmpty(mLiveExpiration) || DateUtil.getFixedSkewedTimeMillis() / 1000 > expirationInGMTFormat - 5 * 60) {
            GetAuthInformation getAuthInformation = new GetAuthInformation();
            getAuthInformation.getVideoPlayLiveStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String msg) {
                    if (mAliyunVodPlayerView != null) {
                        mAliyunVodPlayerView.onStop();
                    }
                    ToastUtils.show(activity, "Get Sts Info error : " + msg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mLiveStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mLiveStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mLiveStsAccessKeySecret = dataBean.getAccessKeySecret();
                        GlobalPlayerConfig.mLiveExpiration = dataBean.getExpiration();

                        if (mAliyunVodPlayerView != null) {
                            stsInfo.setAccessKeyId(GlobalPlayerConfig.mLiveStsAccessKeyId);
                            stsInfo.setAccessKeySecret(GlobalPlayerConfig.mLiveStsAccessKeySecret);
                            stsInfo.setSecurityToken(GlobalPlayerConfig.mLiveStsSecurityToken);
                            mAliyunVodPlayerView.updateStsInfo(stsInfo);
                        }
                    }
                }
            });
            Log.e(TAG, "refreshSts: ");
            return IPlayer.StsStatus.Pending;
        } else {
            Log.e(TAG, "IPlayer.StsStatus.Valid: ");
            return IPlayer.StsStatus.Valid;
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyBoard(AliYunFragment activity) {
        View view = this.activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void onSeekStart(int position) {
    }


    /**
     * 设置屏幕亮度
     */
    private void setWindowBrightness(int brightness) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 100.00f;
        window.setAttributes(lp);
//        aliYunPlayerUtil.setWindowBrightness(brightness, activity);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mCurrentPlayType", mCurrentPlayType.ordinal());
        if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            outState.putString("mVid", GlobalPlayerConfig.mVid);
            outState.putString("mRegion", GlobalPlayerConfig.mRegion);
            outState.putString("mPlayAuth", GlobalPlayerConfig.mPlayAuth);
            outState.putInt("mPreviewTime", GlobalPlayerConfig.mPreviewTime);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS) {
            outState.putString("mVid", GlobalPlayerConfig.mVid);
            outState.putString("mRegion", GlobalPlayerConfig.mRegion);
            outState.putString("mStsAccessKeyId", GlobalPlayerConfig.mStsAccessKeyId);
            outState.putString("mStsAccessKeySecret", GlobalPlayerConfig.mStsAccessKeySecret);
            outState.putString("mStsSecurityToken", GlobalPlayerConfig.mStsSecurityToken);
            outState.putInt("mPreviewTime", GlobalPlayerConfig.mPreviewTime);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            outState.putString("mVid", GlobalPlayerConfig.mVid);
            outState.putString("mRegion", GlobalPlayerConfig.mRegion);
            outState.putString("mMpsAccessKeyId", GlobalPlayerConfig.mMpsAccessKeyId);
            outState.putString("mMpsAccessKeySecret", GlobalPlayerConfig.mMpsAccessKeySecret);
            outState.putString("mMpsSecurityToken", GlobalPlayerConfig.mMpsSecurityToken);
            outState.putString("mMpsHlsUriToken", GlobalPlayerConfig.mMpsHlsUriToken);
            outState.putString("mMpsAuthInfo", GlobalPlayerConfig.mMpsAuthInfo);
            outState.putInt("mPreviewTime", GlobalPlayerConfig.mPreviewTime);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.LIVE_STS) {
            outState.putString("mLiveStsUrl", GlobalPlayerConfig.mLiveStsUrl);
            outState.putString("mRegion", GlobalPlayerConfig.mRegion);
            outState.putString("mLiveStsAccessKeyId", GlobalPlayerConfig.mLiveStsAccessKeyId);
            outState.putString("mLiveStsAccessKeySecret", GlobalPlayerConfig.mLiveStsAccessKeySecret);
            outState.putString("mLiveStsSecurityToken", GlobalPlayerConfig.mLiveStsSecurityToken);
            outState.putString("mLiveStsDomain", GlobalPlayerConfig.mLiveStsDomain);
            outState.putString("mLiveStsApp", GlobalPlayerConfig.mLiveStsApp);
            outState.putString("mLiveStsStream", GlobalPlayerConfig.mLiveStsStream);
        } else if (mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL) {
            outState.putString("mUrlPath", GlobalPlayerConfig.mUrlPath);
        } else {
            //default
            outState.putString("mVid", GlobalPlayerConfig.mVid);
            outState.putString("mRegion", GlobalPlayerConfig.mRegion);
            outState.putString("mStsAccessKeyId", GlobalPlayerConfig.mStsAccessKeyId);
            outState.putString("mStsAccessKeySecret", GlobalPlayerConfig.mStsAccessKeySecret);
            outState.putString("mStsSecurityToken", GlobalPlayerConfig.mStsSecurityToken);
        }

        //PlayerConfig
        outState.putInt("mStartBufferDuration", GlobalPlayerConfig.PlayConfig.mStartBufferDuration);
        outState.putInt("mHighBufferDuration", GlobalPlayerConfig.PlayConfig.mHighBufferDuration);
        outState.putInt("mMaxBufferDuration", GlobalPlayerConfig.PlayConfig.mMaxBufferDuration);
        outState.putInt("mMaxDelayTime", GlobalPlayerConfig.PlayConfig.mMaxDelayTime);
        outState.putInt("mMaxProbeSize", GlobalPlayerConfig.PlayConfig.mMaxProbeSize);
        outState.putString("mReferrer", GlobalPlayerConfig.PlayConfig.mReferrer);
        outState.putString("mHttpProxy", GlobalPlayerConfig.PlayConfig.mHttpProxy);
        outState.putInt("mNetworkTimeout", GlobalPlayerConfig.PlayConfig.mNetworkTimeout);
        outState.putInt("mNetworkRetryCount", GlobalPlayerConfig.PlayConfig.mNetworkRetryCount);
        outState.putBoolean("mEnableSei", GlobalPlayerConfig.PlayConfig.mEnableSei);
        outState.putBoolean("mEnableClearWhenStop", GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop);
        outState.putBoolean("mAutoSwitchOpen", GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen);
        outState.putBoolean("mEnableAccurateSeekModule", GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule);
        outState.putBoolean("mEnablePlayBackground", GlobalPlayerConfig.PlayConfig.mEnablePlayBackground);
        outState.putBoolean("mEnableHardDecodeType", GlobalPlayerConfig.mEnableHardDecodeType);

        //CacheConfig
        outState.putBoolean("mEnableCache", GlobalPlayerConfig.PlayCacheConfig.mEnableCache);
        outState.putString("mDir", GlobalPlayerConfig.PlayCacheConfig.mDir);
        outState.putInt("mMaxDurationS", GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS);
        outState.putInt("mMaxSizeMB", GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB);
    }


    private static class MyOnScreenCostingSingleTagListener implements OnScreenCostingSingleTagListener {

        private WeakReference<AliYunFragment> weakReference;

        private MyOnScreenCostingSingleTagListener(AliYunFragment aliyunPlayerSkinActivity) {
            weakReference = new WeakReference<>(aliyunPlayerSkinActivity);
        }

        @Override
        public void onScreenCostingSingleTag() {
            AliYunFragment aliyunPlayerSkinActivity = weakReference.get();
            if (aliyunPlayerSkinActivity != null) {
                aliyunPlayerSkinActivity.screenCostingSingleTag();
            }
        }
    }

    private void screenCostingSingleTag() {
        if (screenShowMoreDialog != null && screenShowMoreDialog.isShowing()) {
            screenShowMoreDialog.dismiss();
        }
    }


    private void initAliyunPlayerView() {
        //保持屏幕敞亮
        mAliyunVodPlayerView.setKeepScreenOn(true);
        mAliyunVodPlayerView.setTheme(Theme.Blue);
        mAliyunVodPlayerView.setAutoPlay(true);
        mAliyunVodPlayerView.needOnlyFullScreenPlay(mNeedOnlyFullScreen);
//        aliYunPlayerUtil.changeSpeed(player);

        mAliyunVodPlayerView.setOnPreparedListener(new MyPrepareListener());
        mAliyunVodPlayerView.setNetConnectedListener(new MyNetConnectedListener(this));
        mAliyunVodPlayerView.setOnCompletionListener(new MyCompletionListener(this));
        mAliyunVodPlayerView.setOnFirstFrameStartListener(new MyFrameInfoListener(this));
        mAliyunVodPlayerView.setOnTrackChangedListener(new MyOnTrackChangedListener(this));
        mAliyunVodPlayerView.setOnStoppedListener(new MyStoppedListener(this));
        mAliyunVodPlayerView.setOrientationChangeListener(new MyOrientationChangeListener(this));
        mAliyunVodPlayerView.setOnTimeExpiredErrorListener(new MyOnTimeExpiredErrorListener(this));
        mAliyunVodPlayerView.setOnShowMoreClickListener(new MyShowMoreClickLisener(this));
        mAliyunVodPlayerView.setOnPlayStateBtnClickListener(new MyPlayStateBtnClickListener(this));
        mAliyunVodPlayerView.setOnSeekCompleteListener(new MySeekCompleteListener(this));
        mAliyunVodPlayerView.setOnSeekStartListener(new MySeekStartListener(this));
        mAliyunVodPlayerView.setOnFinishListener(new MyOnFinishListener(this));
        mAliyunVodPlayerView.setOnScreenCostingSingleTagListener(new MyOnScreenCostingSingleTagListener(this));
        mAliyunVodPlayerView.setOnScreenBrightness(new MyOnScreenBrightnessListener(this));
        mAliyunVodPlayerView.setSoftKeyHideListener(new MyOnSoftKeyHideListener(this));
        mAliyunVodPlayerView.setOnErrorListener(new MyOnErrorListener(this));
        mAliyunVodPlayerView.setScreenBrightness(BrightnessDialog.getActivityBrightness(activity));
        mAliyunVodPlayerView.setOnTrackInfoClickListener(new MyOnTrackInfoClickListener(this));
        mAliyunVodPlayerView.setOnInfoListener(new MyOnInfoListener(this));
        mAliyunVodPlayerView.setOutOnSeiDataListener(new MyOnSeiDataListener(this));
        mAliyunVodPlayerView.setOnTipClickListener(new MyOnTipClickListener(this));
        mAliyunVodPlayerView.setOnTipsViewBackClickListener(new MyOnTipsViewBackClickListener(this));
        mAliyunVodPlayerView.setOutOnVerifyStsCallback(new MyOnVerifyStsCallback(this));
        mAliyunVodPlayerView.enableNativeLog();
        mAliyunVodPlayerView.setScreenBrightness(mCurrentBrightValue);
        mAliyunVodPlayerView.startNetWatch();

    }

    private void initDownloadManager() {
        mAliyunDownloadManager = AliyunDownloadManager.getInstance(activity.getApplicationContext());
        DownloaderConfig downloaderConfig = new DownloaderConfig();
        downloaderConfig.mConnectTimeoutS = 3;
        downloaderConfig.mNetworkTimeoutMs = 5000;
        mAliyunDownloadManager.setDownloaderConfig(downloaderConfig);
    }

    /**
     * 仅当系统的亮度模式是非自动模式的情况下，获取当前屏幕亮度值[0, 255].
     * 如果是自动模式，那么该方法获得的值不正确。
     */
    private int getCurrentBrightValue() {
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS, 255);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    private void restoreSaveInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int mPlayType = savedInstanceState.getInt("mCurrentPlayType");
            int authType = GlobalPlayerConfig.PLAYTYPE.AUTH.ordinal();
            int stsType = GlobalPlayerConfig.PLAYTYPE.STS.ordinal();
            int mpsType = GlobalPlayerConfig.PLAYTYPE.MPS.ordinal();
            int urlType = GlobalPlayerConfig.PLAYTYPE.URL.ordinal();
            int liveStsType = GlobalPlayerConfig.PLAYTYPE.LIVE_STS.ordinal();
            if (mPlayType == authType) {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.AUTH;
                GlobalPlayerConfig.mVid = savedInstanceState.getString("mVid");
                GlobalPlayerConfig.mRegion = savedInstanceState.getString("mRegion");
                GlobalPlayerConfig.mPlayAuth = savedInstanceState.getString("mPlayAuth");
                GlobalPlayerConfig.mPreviewTime = savedInstanceState.getInt("mPreviewTime");
            } else if (mPlayType == stsType) {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.STS;
                GlobalPlayerConfig.mVid = savedInstanceState.getString("mVid");
                GlobalPlayerConfig.mRegion = savedInstanceState.getString("mRegion");
                GlobalPlayerConfig.mStsAccessKeyId = savedInstanceState.getString("mStsAccessKeyId");
                GlobalPlayerConfig.mStsAccessKeySecret = savedInstanceState.getString("mStsAccessKeySecret");
                GlobalPlayerConfig.mStsSecurityToken = savedInstanceState.getString("mStsSecurityToken");
                GlobalPlayerConfig.mPreviewTime = savedInstanceState.getInt("mPreviewTime");
            } else if (mPlayType == mpsType) {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.MPS;
                GlobalPlayerConfig.mVid = savedInstanceState.getString("mVid");
                GlobalPlayerConfig.mRegion = savedInstanceState.getString("mRegion");
                GlobalPlayerConfig.mMpsAccessKeyId = savedInstanceState.getString("mMpsAccessKeyId");
                GlobalPlayerConfig.mMpsAccessKeySecret = savedInstanceState.getString("mMpsAccessKeySecret");
                GlobalPlayerConfig.mMpsSecurityToken = savedInstanceState.getString("mMpsSecurityToken");
                GlobalPlayerConfig.mMpsHlsUriToken = savedInstanceState.getString("mMpsHlsUriToken");
                GlobalPlayerConfig.mMpsAuthInfo = savedInstanceState.getString("mMpsAuthInfo");
                GlobalPlayerConfig.mPreviewTime = savedInstanceState.getInt("mPreviewTime");
            } else if (mPlayType == urlType) {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.URL;
                GlobalPlayerConfig.mUrlPath = savedInstanceState.getString("mUrlPath");
            } else if (mPlayType == liveStsType) {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.LIVE_STS;
                GlobalPlayerConfig.mLiveStsUrl = savedInstanceState.getString("mLiveStsUrl");
                GlobalPlayerConfig.mRegion = savedInstanceState.getString("mRegion");
                GlobalPlayerConfig.mLiveStsAccessKeyId = savedInstanceState.getString("mLiveStsAccessKeyId");
                GlobalPlayerConfig.mLiveStsAccessKeySecret = savedInstanceState.getString("mLiveStsAccessKeySecret");
                GlobalPlayerConfig.mLiveStsSecurityToken = savedInstanceState.getString("mLiveStsSecurityToken");
                GlobalPlayerConfig.mLiveStsDomain = savedInstanceState.getString("mLiveStsDomain");
                GlobalPlayerConfig.mLiveStsApp = savedInstanceState.getString("mLiveStsApp");
                GlobalPlayerConfig.mLiveStsStream = savedInstanceState.getString("mLiveStsStream");
            } else {
                mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.DEFAULT;
                GlobalPlayerConfig.mVid = savedInstanceState.getString("mVid");
                GlobalPlayerConfig.mRegion = savedInstanceState.getString("mRegion");
                GlobalPlayerConfig.mStsAccessKeyId = savedInstanceState.getString("mStsAccessKeyId");
                GlobalPlayerConfig.mStsAccessKeySecret = savedInstanceState.getString("mStsAccessKeySecret");
                GlobalPlayerConfig.mStsSecurityToken = savedInstanceState.getString("mStsSecurityToken");
            }
            GlobalPlayerConfig.mCurrentPlayType = mCurrentPlayType;

            //PlayerConfig
            GlobalPlayerConfig.PlayConfig.mStartBufferDuration = savedInstanceState.getInt("mStartBufferDuration");
            GlobalPlayerConfig.PlayConfig.mHighBufferDuration = savedInstanceState.getInt("mHighBufferDuration");
            GlobalPlayerConfig.PlayConfig.mMaxBufferDuration = savedInstanceState.getInt("mMaxBufferDuration");
            GlobalPlayerConfig.PlayConfig.mMaxDelayTime = savedInstanceState.getInt("mMaxDelayTime");
            GlobalPlayerConfig.PlayConfig.mMaxProbeSize = savedInstanceState.getInt("mMaxProbeSize");
            GlobalPlayerConfig.PlayConfig.mReferrer = savedInstanceState.getString("mReferrer");
            GlobalPlayerConfig.PlayConfig.mHttpProxy = savedInstanceState.getString("mHttpProxy");
            GlobalPlayerConfig.PlayConfig.mNetworkTimeout = savedInstanceState.getInt("mNetworkTimeout");
            GlobalPlayerConfig.PlayConfig.mNetworkRetryCount = savedInstanceState.getInt("mNetworkRetryCount");
            GlobalPlayerConfig.PlayConfig.mEnableSei = savedInstanceState.getBoolean("mEnableSei");
            GlobalPlayerConfig.PlayConfig.mEnableClearWhenStop = savedInstanceState.getBoolean("mEnableClearWhenStop");
            GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen = savedInstanceState.getBoolean("mAutoSwitchOpen");
            GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule = savedInstanceState.getBoolean("mEnableAccurateSeekModule");
            GlobalPlayerConfig.PlayConfig.mEnablePlayBackground = savedInstanceState.getBoolean("mEnablePlayBackground");

            //CacheConfig
            GlobalPlayerConfig.PlayCacheConfig.mEnableCache = savedInstanceState.getBoolean("mEnableCache");
            GlobalPlayerConfig.PlayCacheConfig.mDir = savedInstanceState.getString("mDir");
            GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS = savedInstanceState.getInt("mMaxDurationS");
            GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB = savedInstanceState.getInt("mMaxSizeMB");

            GlobalPlayerConfig.mEnableHardDecodeType = savedInstanceState.getBoolean("mEnableHardDecodeType");

        }
    }

    /**
     * 初始化配置
     */
    private void initGlobalConfig() {
        GlobalPlayerConfig.mEnableHardDecodeType = true;
        GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen = false;
        GlobalPlayerConfig.PlayConfig.mEnablePlayBackground = false;
        GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule = false;
        GlobalPlayerConfig.mRotateMode = IPlayer.RotateMode.ROTATE_0;
        GlobalPlayerConfig.mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (commenUtils != null) {
            commenUtils.onDestroy();
            commenUtils = null;
        }
        if (Global.mDownloadMediaLists != null && Global.mDownloadMediaLists.size() > 0) {
            Global.mDownloadMediaLists.clear();
        }

    }

    private void copyAssets() {
        final String encryptPath = FileUtils.getDir(getContext()) + GlobalPlayerConfig.ENCRYPT_DIR_PATH;
        commenUtils = Common.getInstance(getContext()).copyAssetsToSD("encrypt", encryptPath);
        commenUtils.setFileOperateCallback(

                new Common.FileOperateCallback() {

                    @Override
                    public void onSuccess() {
                        PrivateService.initService(getContext(), encryptPath + "encryptedApp.dat");
                    }

                    @Override
                    public void onFailed(String error) {
                        Toast.makeText(getContext(), "encrypt copy error : " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 初始化下载相关信息
     */
    private void initDownloadInfo() {
        Global.mDownloadMediaLists = new ArrayList<>();
        DatabaseManager.getInstance().createDataBase(getActivity());
        mAliyunDownloadManager = AliyunDownloadManager.getInstance(getContext());
        mAliyunDownloadManager.setDownloadDir(FileUtils.getDir(getContext()) + GlobalPlayerConfig.DOWNLOAD_DIR_PATH);
    }

    private void initCacheDir() {
        //设置边播边缓存路径
        File externalFilesDir = getActivity().getExternalFilesDir(null);
        if (externalFilesDir != null) {
            if (!externalFilesDir.exists()) {
                externalFilesDir.mkdirs();

                GlobalPlayerConfig.PlayCacheConfig.mDir = externalFilesDir.getAbsolutePath();
            }
        }
    }

    private void initDataBase() {
        mAliyunDownloadManager.findDatasByDb(new LoadDbDatasListener() {
            @Override
            public void onLoadSuccess(List<AliyunDownloadMediaInfo> dataList) {
                Global.mDownloadMediaLists.addAll(dataList);
            }
        });
    }

    private void initView() {
    }

//        private void initListener() {
//        mBackImageView.setOnClickListener(this);
//        mStartPlayTextView.setOnClickListener(this);
//        mPlayTypeEditTextView.setOnClickListener(this);
//        mPlayConfigSettingImageView.setOnClickListener(this);
//
//        //播放方式
//        mPlayTypeUrlRadioButton.setOnClickListener(this);
//        mPlayTypeStsRadioButton.setOnClickListener(this);
//        mPlayTypeMpsRadioButton.setOnClickListener(this);
//        mPlayTypeAuthRadioButton.setOnClickListener(this);
//        mPlayTypeLiveStsRadioButton.setOnClickListener(this);
//        mPlayTypeDefaultRadioButton.setOnClickListener(this);
//
//        //镜像模式
//        mPlayTypeRotate0RadioButton.setOnClickListener(this);
//        mPlayTypeRotate90RadioButton.setOnClickListener(this);
//        mPlayTypeRotate180RadioButton.setOnClickListener(this);
//        mPlayTypeRotate270RadioButton.setOnClickListener(this);
//
//        //解码方式
//        mDecodeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//                GlobalPlayerConfig.mEnableHardDecodeType = checkedId != R.id.radio_btn_decode_soft;
//            }
//        });
//
//        //镜像模式
//        mMirrorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//                if (checkedId == R.id.radio_btn_mirror_none) {
//                    GlobalPlayerConfig.mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE;
//                } else if (checkedId == R.id.radio_btn_mirror_vertical) {
//                    GlobalPlayerConfig.mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_VERTICAL;
//                } else if (checkedId == R.id.radio_btn_mirror_horizontal) {
//                    GlobalPlayerConfig.mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_HORIZONTAL;
//                } else {
//                    GlobalPlayerConfig.mMirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE;
//                }
//            }
//        });
//
//        //auto自动开关
//        mAutoSwithRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//                if (checkedId == R.id.radio_btn_auto_open) {
//                    GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen = true;
//                } else {
//                    GlobalPlayerConfig.PlayConfig.mAutoSwitchOpen = false;
//                }
//            }
//        });
//
//        //seek模式
//        mSeekModuleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//                if (checkedId == R.id.radio_btn_seek_accurate) {
//                    GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule = true;
//                } else {
//                    GlobalPlayerConfig.PlayConfig.mEnableAccurateSeekModule = false;
//                }
//            }
//        });
//
//        //是否允许后台播放
//        mEnableBackgroundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
//                if (checkedId == R.id.radio_btn_background_open) {
//                    GlobalPlayerConfig.PlayConfig.mEnablePlayBackground = true;
//                } else {
//                    GlobalPlayerConfig.PlayConfig.mEnablePlayBackground = false;
//                }
//            }
//        });
//
//    }

//        public void onClick(View view) {
//    if (view == mStartPlayTextView) {
//            //开始播放
//            getCurrentPlayType();
//            checkedIsNeedNormalData();
//        } else if (view == mPlayTypeEditTextView) {
//            //编辑
//            getCurrentPlayType();
//            Intent intent = new Intent(this, AliyunPlayerTypeEditActivity.class);
//            startActivityForResult(intent, PLAY_TYPE_EDIT_ACTIVITY_REQUEST);
//        } else if (view == mPlayConfigSettingImageView) {
//            Intent intent = new Intent(this, AliyunPlayerConfigActivity.class);
//            startActivity(intent);
//        } else if (view == mPlayTypeUrlRadioButton) {
//            //url播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.URL;
//            selectedPlayType();
//        } else if (view == mPlayTypeStsRadioButton) {
//            //sts播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.STS;
//            selectedPlayType();
//        } else if (view == mPlayTypeMpsRadioButton) {
//            //mps播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.MPS;
//            selectedPlayType();
//        } else if (view == mPlayTypeAuthRadioButton) {
//            //auth播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.AUTH;
//            selectedPlayType();
//        } else if(view == mPlayTypeLiveStsRadioButton){
//            //LiveSts播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.LIVE_STS;
//            selectedPlayType();
//        }else if (view == mPlayTypeDefaultRadioButton) {
//            //默认播放方式
//            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.DEFAULT;
//            selectedPlayType();
//        } else if (view == mPlayTypeRotate0RadioButton) {
//            //旋转0
//            GlobalPlayerConfig.mRotateMode = IPlayer.RotateMode.ROTATE_0;
//            mPlayTypeRotate0RadioButton.setChecked(true);
//            mPlayTypeRotate90RadioButton.setChecked(false);
//            mPlayTypeRotate180RadioButton.setChecked(false);
//            mPlayTypeRotate270RadioButton.setChecked(false);
//        } else if (view == mPlayTypeRotate90RadioButton) {
//            //旋转90
//            GlobalPlayerConfig.mRotateMode = IPlayer.RotateMode.ROTATE_90;
//            mPlayTypeRotate90RadioButton.setChecked(true);
//            mPlayTypeRotate0RadioButton.setChecked(false);
//            mPlayTypeRotate180RadioButton.setChecked(false);
//            mPlayTypeRotate270RadioButton.setChecked(false);
//        } else if (view == mPlayTypeRotate180RadioButton) {
//            //旋转180
//            GlobalPlayerConfig.mRotateMode = IPlayer.RotateMode.ROTATE_180;
//            mPlayTypeRotate180RadioButton.setChecked(true);
//            mPlayTypeRotate0RadioButton.setChecked(false);
//            mPlayTypeRotate90RadioButton.setChecked(false);
//            mPlayTypeRotate270RadioButton.setChecked(false);
//        } else if (view == mPlayTypeRotate270RadioButton) {
//            //旋转270
//            GlobalPlayerConfig.mRotateMode = IPlayer.RotateMode.ROTATE_270;
//            mPlayTypeRotate270RadioButton.setChecked(true);
//            mPlayTypeRotate0RadioButton.setChecked(false);
//            mPlayTypeRotate90RadioButton.setChecked(false);
//            mPlayTypeRotate180RadioButton.setChecked(false);
//        }
//    }

    /**
     * 选择播放方式
     */
    private void selectedPlayType() {
        RadioButton selectedRadioButton;
        if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS) {
            selectedRadioButton = mPlayTypeStsRadioButton;
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH) {
            selectedRadioButton = mPlayTypeAuthRadioButton;
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS) {
            selectedRadioButton = mPlayTypeMpsRadioButton;
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL) {
            selectedRadioButton = mPlayTypeUrlRadioButton;
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.LIVE_STS) {
            selectedRadioButton = mPlayTypeLiveStsRadioButton;
        } else {
            selectedRadioButton = mPlayTypeDefaultRadioButton;
        }

        if (mPlayTypeRadioButtonList != null && selectedRadioButton != null) {
            for (RadioButton radioButton : mPlayTypeRadioButtonList) {
                radioButton.setChecked(radioButton == selectedRadioButton);
            }
        }
    }

    /**
     * 获取当前选中的播放方式
     */
    private void getCurrentPlayType() {
        if (mPlayTypeStsRadioButton.isChecked()) {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.STS;
        } else if (mPlayTypeAuthRadioButton.isChecked()) {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.AUTH;
        } else if (mPlayTypeMpsRadioButton.isChecked()) {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.MPS;
        } else if (mPlayTypeDefaultRadioButton.isChecked()) {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.DEFAULT;
        } else if (mPlayTypeLiveStsRadioButton.isChecked()) {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.LIVE_STS;
        } else {
            GlobalPlayerConfig.mCurrentPlayType = GlobalPlayerConfig.PLAYTYPE.URL;
        }
    }


    /**
     * 检查是否需要默认源
     */
    public void checkedIsNeedNormalData() {
        GetAuthInformation getAuthInformation = new GetAuthInformation();//转圈
        if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.URL && !GlobalPlayerConfig.URL_TYPE_CHECKED) {

            getAuthInformation.getVideoPlayUrlInfo(new GetAuthInformation.OnGetUrlInfoListener() {
                @Override
                public void onGetUrlError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetUrlSuccess(AliyunVideoList.VideoList dataBean) {
                    if (dataBean != null) {
                        List<AliyunVideoList.VideoList.VideoListItem> playInfoList = dataBean.getPlayInfoList();
                        if (playInfoList != null && playInfoList.size() > 0) {
                            AliyunVideoList.VideoList.VideoListItem videoListItem = playInfoList.get(0);
                            GlobalPlayerConfig.mUrlPath = videoListItem.getPlayURL();
                            startPlay();
                        }
                    }
                }
            });

        } else if ((GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.STS && !GlobalPlayerConfig.STS_TYPE_CHECKED)) {
            getAuthInformation.getVideoPlayStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mVid = dataBean.getVideoId();
                        GlobalPlayerConfig.mStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mStsAccessKeySecret = dataBean.getAccessKeySecret();
                        startPlay();
                    }
                }
            });

        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.AUTH && !GlobalPlayerConfig.AUTH_TYPE_CHECKED) {

            getAuthInformation.getVideoPlayAuthInfo(new GetAuthInformation.OnGetPlayAuthInfoListener() {

                @Override
                public void onGetPlayAuthError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetPlayAuthSuccess(AliyunPlayAuth.PlayAuthBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mVid = dataBean.getVideoMeta().getVideoId();
                        GlobalPlayerConfig.mPlayAuth = dataBean.getPlayAuth();
                        startPlay();
                    }
                }
            });

        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.MPS && !GlobalPlayerConfig.MPS_TYPE_CHECKED) {

            getAuthInformation.getVideoPlayMpsInfo(new GetAuthInformation.OnGetMpsInfoListener() {
                @Override
                public void onGetMpsError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetMpsSuccess(AliyunMps.MpsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mVid = dataBean.getMediaId();
                        GlobalPlayerConfig.mMpsRegion = dataBean.getRegionId();
                        GlobalPlayerConfig.mMpsAuthInfo = dataBean.getAuthInfo();
                        GlobalPlayerConfig.mMpsHlsUriToken = dataBean.getHlsUriToken();
                        GlobalPlayerConfig.mMpsAccessKeyId = dataBean.getAkInfo().getAccessKeyId();
                        GlobalPlayerConfig.mMpsSecurityToken = dataBean.getAkInfo().getSecurityToken();
                        GlobalPlayerConfig.mMpsAccessKeySecret = dataBean.getAkInfo().getAccessKeySecret();
                        startPlay();
                    }
                }
            });

        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.LIVE_STS && !GlobalPlayerConfig.LIVE_STS_TYPE_CHECKED) {
            getAuthInformation.getVideoPlayStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mStsAccessKeySecret = dataBean.getAccessKeySecret();
                        startPlay();
                    }
                }
            });
        } else if (GlobalPlayerConfig.mCurrentPlayType == GlobalPlayerConfig.PLAYTYPE.DEFAULT) {
            getAuthInformation.getVideoPlayStsInfo(new GetAuthInformation.OnGetStsInfoListener() {
                @Override
                public void onGetStsError(String msg) {
                    ToastUtils.show(getActivity(), msg);
                }

                @Override
                public void onGetStsSuccess(AliyunSts.StsBean dataBean) {
                    if (dataBean != null) {
                        GlobalPlayerConfig.mVid = "";
                        GlobalPlayerConfig.mStsAccessKeyId = dataBean.getAccessKeyId();
                        GlobalPlayerConfig.mStsSecurityToken = dataBean.getSecurityToken();
                        GlobalPlayerConfig.mStsAccessKeySecret = dataBean.getAccessKeySecret();
                        startPlay();
                    }
                }
            });
        } else {
            startPlay();
        }
    }


    /**
     * 开启播放界面
     */
    private void startPlay() {
//        Intent intent = new Intent(this, AliYunFragment.class);
//        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_TYPE_EDIT_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedPlayType();
        }
    }

}
