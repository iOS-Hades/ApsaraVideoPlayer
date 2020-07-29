package com.aliyun.vodplayerview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import com.aliyun.downloader.AliDownloaderFactory;
import com.aliyun.downloader.AliMediaDownloader;
import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.VidPlayerConfigGen;
import com.aliyun.player.alivcplayerexpand.constants.GlobalPlayerConfig;
import com.aliyun.player.alivcplayerexpand.playlist.AlivcVideoInfo;
import com.aliyun.player.alivcplayerexpand.widget.AliyunVodPlayerView;
import com.aliyun.player.aliyunplayerbase.bean.AliyunPlayAuth;
import com.aliyun.player.aliyunplayerbase.net.GetAuthInformation;
import com.aliyun.player.aliyunplayerbase.net.ServiceCommon;
import com.aliyun.player.aliyunplayerbase.util.AliyunScreenMode;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.source.Definition;
import com.aliyun.player.source.VidSts;
import com.aliyun.private_service.PrivateService;
import com.aliyun.svideo.common.okhttp.AlivcOkHttpClient;
import com.aliyun.svideo.common.utils.FileUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Request;

/**
 * Created by LiuHaiMing
 * on 2020/7/26.
 */
public class AliYunPlayerUtil {
    private Context context;

    public AliYunPlayerUtil(Context context) {
        this.context = context;
    }

    public AliPlayer initPlayer() {
        AliPlayer mAliPlayer = AliPlayerFactory.createAliPlayer(context.getApplicationContext());
        return mAliPlayer;
    }

    /**
     * 切换播放速度
     * AliPlayer是当前的播放器对象
     *
     * @param speedValue 播放速度
     */
    public void changeSpeed(AliPlayer player, Float speedValue) {
        if (player != null)
            player.setSpeed(speedValue);
    }

    /**
     * 设置全屏展示  本方法只对是否横屏做了改动
     *
     * @param finalScreenMode finalScreenMode可为AliyunScreenMode.Full 或AliyunScreenMode.Small full表示全屏，snall时不改动
     *                        为了方便，如果不传也默认横屏
     */
    public void setScreenFull(AliyunScreenMode finalScreenMode) {
        if (context instanceof Activity) {
            if (finalScreenMode == AliyunScreenMode.Full) {
                //不是固定竖屏播放。
//                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (finalScreenMode == null) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

        }
    }


    /**
     * 设置音量
     *
     * float 表示音量的大小 从0到-1
     */
    public void setVolum(AliPlayer mAliPlayer, float v) {
        if (mAliPlayer != null) {
            mAliPlayer.setVolume(v);
        }
    }


    /**
     * 设置屏幕亮度 demo中设置亮度的时候 是通过手势触发，然后展示一个百分比，再来
     * 改变亮度，此处只有改变亮度的方法 没有页面和手势的代码
     * brightness取值范围 0到100
     */
    //已测试
    public void setWindowBrightness(int brightness, Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 100.00f;
        window.setAttributes(lp);
    }

    /**
     * 清晰度选择
     * <p>
     * 该方法是用户点击了某一个清晰度才调用的，用户点击清晰度后
     * 会有一个回调  onQualityClick(TrackInfo qualityTrackInfo)
     * 然后再获取点击的清晰度
     * int trackIndex = trackInfo.getIndex();
     * mAliyunRenderView.selectTrack(trackIndex);
     * onTrackChangedListener是切换的回调 切换成功进入 onChangedSuccess()
     * 失败进入onChangedFail()
     *
     * @param trackIndex 索引 即选择的清晰度
     */
    public void selectTrack(AliPlayer mAliPlayer, int trackIndex, IPlayer.OnTrackChangedListener onTrackChangedListener) {
        if (mAliPlayer != null) {
            mAliPlayer.selectTrack(trackIndex);
        }
        if (onTrackChangedListener != null)
            mAliPlayer.setOnTrackChangedListener(onTrackChangedListener);
    }

    /**
     * 开始下载
     *
     * @param mContext
     * @param downloadDir 指定的下载目录
     *                    如果要进行下载监听可以进行如下设置
     *                    jniDownloader.setOnPreparedListener(new AliMediaDownloader.OnPreparedListener() {})
     */
    public void startDownLoad(Context mContext, String downloadDir) {
        final AliMediaDownloader jniDownloader = AliDownloaderFactory.create(mContext);
        jniDownloader.setSaveDir(downloadDir);
        jniDownloader.start();
    }

    /**
     * 文件加密
     *
     * @param context
     * @param encryptPath 加密文件的路径
     */
    public void encry(Context context, String encryptPath) {
        PrivateService.initService(context, encryptPath + "encryptedApp.dat");
    }

    /**
     * 是否循环播放
     */
    public void setLoop(AliPlayer mAliPlayer,boolean loop){
        if(mAliPlayer != null){
            mAliPlayer.setLoop(loop);
        }
    }
    /**
     * 删除下载的文件
     * vid 和 format 是下载后就要有的
     *
     * @param saveDir 文件路径
     * @param vid
     * @param format
     * @param index   如果返回 11或者12 删除失败
     */
    public int deleteFile(String saveDir, String vid, String format, int index) {
        int ret = AliDownloaderFactory.deleteFile(saveDir, vid, format, index);
        return ret;
    }


    /**
     * 根据vid获取PlayAuth信息
     * 可以根据需要决定是否需要回调
     * GetAuthInformation.OnGetPlayAuthInfoListener 可以直接new出来
     *
     * @param videoId vid
     * @param
     */
    public void getVideoPlayAuthInfoWithVideoId(String videoId, final GetAuthInformation.OnGetPlayAuthInfoListener listener) {
        HashMap<String, String> mHashMap = new HashMap<>();
        mHashMap.put("videoId", videoId);
        AlivcOkHttpClient.getInstance().get(ServiceCommon.GET_VIDEO_PLAY_AUTH, mHashMap, new AlivcOkHttpClient.HttpCallBack() {
            @Override
            public void onError(Request request, IOException e) {
                if (listener != null) {
                    listener.onGetPlayAuthError(e.getMessage());
                }
            }

            @Override
            public void onSuccess(Request request, String result) {
                Gson gson = new Gson();
                AliyunPlayAuth aliyunPlayAuth = gson.fromJson(result, AliyunPlayAuth.class);
                if (aliyunPlayAuth != null && aliyunPlayAuth.getCode() == ServiceCommon.RESPONSE_SUCCESS) {
                    AliyunPlayAuth.PlayAuthBean data = aliyunPlayAuth.getData();
                    if (listener != null) {
                        listener.onGetPlayAuthSuccess(data);
                    }
                }
            }
        });
    }


    /**
     * 设置试看时长 精确到秒，configGen可以通过
     * VidPlayerConfigGen vidPlayerConfigGen = new VidPlayerConfigGen();
     * 来获取，一般在设置PlayDomain（试看域名）的时候可以new一个这个对象，然后设置试看时长
     */
    public void setTrailerTime(int trailerTime, VidPlayerConfigGen configGen) {
        if (configGen != null) {
            GlobalPlayerConfig.IS_TRAILER = true;
            configGen.setPreviewTime(trailerTime);
        }
    }


    /**
     * 切换播放资源(也就是播放下一集),播放的时候需要当前的播放器对象、和当前的
     * 某一集的对象，这个对象是播放列表资源里面的具体的某一个 可以在一个list（播放资源列表）里面
     * 通过position去获取，如果传的videoListItem和当前播放的一直，则会重播
     */
    public void changePlayVidSource(AliyunVodPlayerView mAliyunVodPlayerView, @NonNull AlivcVideoInfo.DataBean.VideoListBean videoListItem) {
        if (mAliyunVodPlayerView != null) {
            CacheConfig cacheConfig = new CacheConfig();
            GlobalPlayerConfig.PlayCacheConfig.mDir = FileUtils.getDir(context) + GlobalPlayerConfig.CACHE_DIR_PATH;
            cacheConfig.mEnable = GlobalPlayerConfig.PlayCacheConfig.mEnableCache;
            cacheConfig.mDir = GlobalPlayerConfig.PlayCacheConfig.mDir;
            cacheConfig.mMaxDurationS = GlobalPlayerConfig.PlayCacheConfig.mMaxDurationS;
            cacheConfig.mMaxSizeMB = GlobalPlayerConfig.PlayCacheConfig.mMaxSizeMB;

            mAliyunVodPlayerView.setCacheConfig(cacheConfig);
            String mCurrentVideoId = videoListItem.getVideoId();
            VidSts vidSts = getVidSts(mCurrentVideoId);
            mAliyunVodPlayerView.setVidSts(vidSts);
        }
    }


    /**
     * 获取VidSts
     *
     * @param vid videoId
     */
    public VidSts getVidSts(String vid) {
        changePlayVidSource(null, null);
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

}
