package moe.xing.mvp_utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.WindowManager;

import me.yokeyword.fragmentation.SupportActivity;
import moe.xing.baseutils.utils.LogHelper;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Qi xingchen on 2016/7/14 0014.
 * <p>
 * activity 基类
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class BaseActivity extends SupportActivity {

    protected Activity mActivity;

    protected ProgressDialog mDialog;
    protected boolean active = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        active = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    public void showProgressDialog() {
        showProgressDialog("");
    }

    public void showProgressDialog(String title) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        mDialog = new ProgressDialog(mActivity);
        WindowManager.LayoutParams params = mDialog.getWindow()
                .getAttributes();
        params.dimAmount = 0f;
        mDialog.getWindow().setAttributes(params);

        if (TextUtils.isEmpty(title)) {
            title = "加载中...";
        }
        mDialog.setTitle(title);
        mDialog.show();
    }

    public void dismissProgressDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void showMessage(String message) {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        LogHelper.Snackbar(viewGroup, message);
    }

    public void showMessage(Throwable e) {
        showMessage(e.getLocalizedMessage());
    }

    public void showMessage(@StringRes int message) {
        showMessage(getString(message));
    }

    public void showMessage(@StringRes int message, String message2) {
        showMessage(getString(message) + message2);
    }

    /**
     * activity 生命周期与 Rx 协调
     * 判断 能否安全进行UI操作
     * 失败的操作调用 {@link Subscriber#onError(Throwable)} 并传递失败原因
     * 成功的操作调用 {@link Subscriber#onNext(Object)} 传递结果
     */
    @NonNull
    public <T> Observable.Operator<T, T> activityLifeTime() {
        return new Observable.Operator<T, T>() {
            @Override
            public Subscriber<? super T> call(final Subscriber<? super T> subscriber) {
                return new Subscriber<T>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (active) {
                            subscriber.onError(e);
                            LogHelper.e(e);
                        }
                    }

                    @Override
                    public void onNext(T t) {
                        if (active) {
                            subscriber.onNext(t);
                        }
                    }
                };
            }
        };
    }


}
