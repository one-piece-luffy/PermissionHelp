package com.baofu.permissionhelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * Created by arvinljw on 2018/9/17 16:30
 * Function：
 * Desc：6.0权限动态申请工具类
 */
public class PermissionUtil {
    private static final String TAG = PermissionUtil.class.getSimpleName();

    private  Builder defaultBuilder;
    private  Builder mCustomBuilder;
    private PermissionFragment permissionFragment;
    private RequestPermissionListener requestPermissionListener;
    private RequestInstallAppListener requestInstallAppListener;


    private static PermissionUtil instance;

    public static PermissionUtil getInstance() {
        if (instance == null) {
            instance = new PermissionUtil();
        }
        return instance;
    }

    private  void initBuild(Context context) {
        if(context==null)
            return;
        defaultBuilder = new Builder()
                .setTitleText(context.getString(R.string.permission_tips))//弹框标题
                .setEnsureBtnText(context.getString(R.string.permission_ok))//权限说明弹框授权按钮文字
                .setCancelBtnText(context.getString(R.string.permission_cancel))//权限说明弹框取消授权按钮文字
                .setSettingEnsureText(context.getString(R.string.permission_setting))//打开设置说明弹框打开按钮文字
                .setSettingCancelText(context.getString(R.string.permission_cancel))//打开设置说明弹框关闭按钮文字
                .setSettingMsg(context.getString(R.string.please_open_permission))//打开设置说明弹框内容文字
                .setInstallAppMsg(context.getString(R.string.allow_install_app))//打开允许安装此来源的应用设置
                .setShowRequest(true)//是否显示申请权限弹框
                .setShowSetting(true)//是否显示设置弹框
                .setShowInstall(true)//是否显示允许安装此来源弹框
                .setRequestCancelable(true)//申请权限说明弹款是否cancelable
                .setSettingCancelable(true)//打开设置界面弹款是否cancelable
                .setInstallCancelable(true)//打开允许安装此来源引用弹款是否cancelable
                .setTitleColor(Color.BLACK)//弹框标题文本颜色
                .setMsgColor(Color.GRAY)//弹框内容文本颜色
                .setEnsureBtnColor(Color.BLACK)//弹框确定文本颜色
                .setCancelBtnColor(Color.BLACK);//弹框取消文本颜色
    }

    public void setConfig(Builder builder) {
        this.mCustomBuilder = builder;

    }

    private void initFragment(FragmentActivity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionFragment = initFragment(activity.getSupportFragmentManager());
            }
            return;
        }

        Log.e(TAG, "PermissionUtil must set activity or fragment");
    }

    private void initFragment(Fragment fragment) {
        if (fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionFragment = initFragment(fragment.getChildFragmentManager());
            }
            return;
        }

        Log.e(TAG, "PermissionUtil must set activity or fragment");
    }

    public void destroy() {
        //以防以后需要销毁的数据这里处理
        removeListener();
        mCustomBuilder=null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private PermissionFragment initFragment(FragmentManager fragmentManager) {
        PermissionFragment fragment = (PermissionFragment) fragmentManager.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new PermissionFragment();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
        }
        fragment.setPermissionUtil(this);
        return fragment;
    }


    public void request(FragmentActivity activity, String msg, String[] permissions, RequestPermissionListener listener) {
        initFragment(activity);
        if(defaultBuilder==null){
            initBuild(activity);
        }
        startRequest(msg, permissions, listener);
    }

    public void request(Fragment fragment, String msg, String[] permissions, RequestPermissionListener listener) {
        initFragment(fragment);
        if(defaultBuilder==null){
            initBuild(fragment.getContext());
        }
        startRequest(msg, permissions, listener);
    }

    private void startRequest(String msg, String[] permissions, RequestPermissionListener listener) {
        if (permissionFragment == null) {
//            Log.e(TAG, "PermissionUtil must set activity or fragment");
            listener.callback(true, false);
            return;
        }
        if (permissions == null || permissions.length == 0) {
            Log.d(TAG, "permissions requires at least one input permission");
            return;
        }
        this.requestPermissionListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionFragment.request(msg, permissions);
        }
    }

    public static String[] asArray(String... permissions) {
        return permissions;
    }

    public Builder getBuilder() {
        if(mCustomBuilder!=null){
            return mCustomBuilder;
        }
        return defaultBuilder;
    }

    void requestBack(boolean granted, boolean isAlwaysDenied) {
        if (requestPermissionListener != null) {
            requestPermissionListener.callback(granted, isAlwaysDenied);
        }
    }

    public void requestInstallApp(RequestInstallAppListener listener) {
        if (permissionFragment == null) {
            listener.canInstallApp(false);
            Log.e(TAG, "PermissionUtil must set activity or fragment");
            return;
        }
        this.requestInstallAppListener = listener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionFragment.requestInstallApp();
        } else {
            listener.canInstallApp(true);
        }
    }

    void callCanInstallApp(boolean canInstall) {
        if (requestInstallAppListener != null) {
            requestInstallAppListener.canInstallApp(canInstall);
        }
    }

    public void removeListener() {
        requestPermissionListener = null;
        requestInstallAppListener = null;
        permissionFragment = null;
    }

    public static Uri getUri(@NonNull Context context, @NonNull File file, @NonNull String authority) {
        return getUri(context, null, file, authority);
    }

    public static Uri getUri(@NonNull Context context, Intent intent, @NonNull File file, @NonNull String authority) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (intent != null) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static class Builder {
        private FragmentActivity activity;
        private Fragment fragment;

        /*没有设置代表不需要标题*/
        private String titleText;
        /*默认是：确定*/
        private String ensureBtnText;
        /*默认是：取消*/
        private String cancelBtnText;
        /*申请权限说明弹款是否cancelable*/
        private boolean isRequestCancelable = true;
        /*打开设置界面弹款是否cancelable*/
        private boolean isSettingCancelable = true;
        /*打开允许安装此来源引用弹款是否cancelable*/
        private boolean isInstallCancelable = true;
        /*是否显示申请权限弹框，默认显示*/
        private boolean isShowRequest = true;
        /*是否显示设置弹框，默认显示*/
        private boolean isShowSetting = true;
        /*是否显示允许安装此来源弹框，默认显示*/
        private boolean isShowInstall = true;
        /*如果用户手动选择了不在提示申请权限的弹框，则让用户去打开设置界面，就是指这个文字提示，
         * 默认是：当前应用缺少必要权限。\n请点击"设置"-"权限"-打开所需权限。*/
        private String settingMsg;
        /*默认是：设置*/
        private String settingEnsureText;
        /*默认是：取消*/
        private String settingCancelText;
        /*默认是：允许安装来自此来源的应用*/
        private String installAppMsg;

        /*颜色没有设置就是默认使用系统AlertDialog的对应颜色*/
        @ColorInt
        private int titleColor;
        @ColorInt
        private int msgColor;
        @ColorInt
        private int ensureBtnColor;
        @ColorInt
        private int cancelBtnColor;

        public Builder() {
        }

        public Builder with(FragmentActivity activity) {
            this.activity = activity;
            return this;
        }

        public Builder with(Fragment fragment) {
            this.fragment = fragment;
            return this;
        }

        public Builder setTitleText(String titleText) {
            this.titleText = titleText;
            return this;
        }

        public Builder setEnsureBtnText(String ensureBtnText) {
            this.ensureBtnText = ensureBtnText;
            return this;
        }

        public Builder setCancelBtnText(String cancelBtnText) {
            this.cancelBtnText = cancelBtnText;
            return this;
        }

        public Builder setShowRequest(boolean showRequest) {
            isShowRequest = showRequest;
            return this;
        }

        public Builder setRequestCancelable(boolean requestCancelable) {
            isRequestCancelable = requestCancelable;
            return this;
        }

        public Builder setSettingCancelable(boolean settingCancelable) {
            isSettingCancelable = settingCancelable;
            return this;
        }

        public Builder setInstallCancelable(boolean installCancelable) {
            isInstallCancelable = installCancelable;
            return this;
        }

        public Builder setShowSetting(boolean showSetting) {
            isShowSetting = showSetting;
            return this;
        }

        public Builder setShowInstall(boolean showInstall) {
            isShowInstall = showInstall;
            return this;
        }

        public Builder setSettingMsg(String settingMsg) {
            this.settingMsg = settingMsg;
            return this;
        }

        public Builder setSettingEnsureText(String settingEnsureText) {
            this.settingEnsureText = settingEnsureText;
            return this;
        }

        public Builder setSettingCancelText(String settingCancelText) {
            this.settingCancelText = settingCancelText;
            return this;
        }

        public Builder setInstallAppMsg(String installAppMsg) {
            this.installAppMsg = installAppMsg;
            return this;
        }

        public Builder setTitleColor(@ColorInt int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder setMsgColor(@ColorInt int msgColor) {
            this.msgColor = msgColor;
            return this;
        }

        public Builder setEnsureBtnColor(@ColorInt int ensureBtnColor) {
            this.ensureBtnColor = ensureBtnColor;
            return this;
        }

        public Builder setCancelBtnColor(@ColorInt int cancelBtnColor) {
            this.cancelBtnColor = cancelBtnColor;
            return this;
        }

        public String getTitleText() {
            return titleText;
        }

        public String getEnsureBtnText() {
            return ensureBtnText;
        }

        public String getCancelBtnText() {
            return cancelBtnText;
        }

        public boolean isRequestCancelable() {
            return isRequestCancelable;
        }

        public boolean isSettingCancelable() {
            return isSettingCancelable;
        }

        public boolean isInstallCancelable() {
            return isInstallCancelable;
        }

        public boolean isShowRequest() {
            return isShowRequest;
        }

        public boolean isShowSetting() {
            return isShowSetting;
        }

        public boolean isShowInstall() {
            return isShowInstall;
        }

        public String getSettingMsg() {
            return settingMsg;
        }

        public String getSettingEnsureText() {
            return settingEnsureText;
        }

        public String getSettingCancelText() {
            return settingCancelText;
        }

        public String getInstallAppMsg() {
            return installAppMsg;
        }

        public int getTitleColor() {
            return titleColor;
        }

        public int getMsgColor() {
            return msgColor;
        }

        public int getEnsureBtnColor() {
            return ensureBtnColor;
        }

        public int getCancelBtnColor() {
            return cancelBtnColor;
        }

        public PermissionUtil build() {
            Context context = null;
            if (activity != null) {
                context = activity;
            }
            if (fragment != null) {
                context = fragment.getActivity();
            }
            if (context == null) {
                return new PermissionUtil();
            }

            return new PermissionUtil();
        }


    }


    public interface RequestPermissionListener {
        /**
         * @param granted        权限是否通过，如果有多个权限的话表示是否全部通过
         * @param isAlwaysDenied false表示会重复提示，true表示拒绝且不再提示
         */
        void callback(boolean granted, boolean isAlwaysDenied);
    }

    public interface RequestInstallAppListener {
        void canInstallApp(boolean canInstall);
    }
}
