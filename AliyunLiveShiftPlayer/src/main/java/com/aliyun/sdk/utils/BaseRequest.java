package com.aliyun.sdk.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 非主线程请求基本框架。
 */
public abstract class BaseRequest {

    public static final int WHAT_SUCCESS = 1;
    public static final int WHAT_FAIL    = 0;
    public static final String DATA_KEY_EXTRA = "data_extra";
    private static ExecutorService sThreadPool = Executors.newCachedThreadPool();
    public WeakReference<Context> mContextWeak;

    protected boolean wantStop = false;

    private MsgDispatcher handler = null;

    private OnRequestListener outerListener = null;
    private OnRequestListener innerListener = new OnRequestListener() {

        @Override
        public void onSuccess(Object requestInfo, String extra) {
            if (outerListener != null) {
                outerListener.onSuccess(requestInfo, extra);
            }
        }

        @Override
        public void onFail(int code, String msg, String extra) {
            if (outerListener != null) {
                outerListener.onFail(code, msg, extra);
            }
        }
    };

    public BaseRequest(Context context, OnRequestListener l) {
        mContextWeak = new WeakReference<Context>(context);
        outerListener = l;
    }

    public abstract void runInBackground();

    public abstract void stopInner();

    /**
     * 同步请求
     */
    public void getSync() {
        runInBackground();
    }

    /**
     * 异步请求
     */
    public void getAsync() {
        handler = new MsgDispatcher(this);
        sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                runInBackground();
            }
        });
    }

    private void dealMsg(Message msg) {
        Bundle data      = msg.getData();
        String requestId = (data != null) ? data.getString(DATA_KEY_EXTRA, "") : "";
        if (msg.what == WHAT_SUCCESS) {
            innerListener.onSuccess(msg.obj, requestId);
        } else if (msg.what == WHAT_FAIL) {
            innerListener.onFail(msg.arg1, (String) msg.obj, requestId);
        }
    }

    public void stop() {
        wantStop = true;
        stopInner();
    }

    /**
     * 处理成功之后调用，发送成功对象到主线程
     */
    public void sendSuccessResult(Object requestInfo, String extra) {
        if (wantStop) {
            return;
        }

        if (handler == null) {
            innerListener.onSuccess(requestInfo, extra);
        } else {
            Message msg = handler.obtainMessage(WHAT_SUCCESS);
            msg.obj = requestInfo;
            Bundle data = new Bundle();
            data.putString(DATA_KEY_EXTRA, extra);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }

    /**
     * 处理事变之后调用，发送失败对象到主线程
     */
    public void sendFailResult(int code, String msgStr, String extra) {
        if (wantStop) {
            return;
        }

        if (handler == null) {
            innerListener.onFail(code, msgStr, extra);
        } else {
            Message msg = handler.obtainMessage(WHAT_FAIL);
            msg.what = WHAT_FAIL;
            msg.arg1 = code;
            msg.obj = msgStr;
            Bundle data = new Bundle();
            data.putString(DATA_KEY_EXTRA, extra);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    }


    /**
     * 请求结果监听器
     *
     * @param <Result> 返回的类型
     */
    public interface OnRequestListener<Result> {
        public void onSuccess(Result requestInfo, String extra);

        public void onFail(int code, String msg, String extra);
    }

    private static class MsgDispatcher extends Handler {
        private BaseRequest mBaseRequest;

        public MsgDispatcher(BaseRequest baseRequest) {
            super();
            mBaseRequest = baseRequest;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mBaseRequest != null) {
                mBaseRequest.dealMsg(msg);
            }
        }
    }
}
