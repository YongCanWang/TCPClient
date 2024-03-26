package com.trans.libnet.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class PermissionsUtils {

    private static Activity activity;
    private static PermissionCallBack callBack;
    private static PermissionsUtils INSTANCE = new PermissionsUtils();
    // 要申请的权限
    private String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_AUDIO",
            "android.permission.READ_MEDIA_VIDEO",
//            Manifest.permission_group.PHONE,
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.ACCESS_WIFI_STATE,
//            Manifest.permission.READ_PHONE_STATE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.REQUEST_INSTALL_PACKAGES,
//            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
//            Manifest.permission.FOREGROUND_SERVICE,
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
    };

    private int RequestPermissionCode = 7890;
    private AlertDialog dialog;
    private int RequestGoSettingCode = 6789;

    private PermissionsUtils() {
    }

    public static PermissionsUtils getInstance(Activity activity, PermissionCallBack callBack) {
        PermissionsUtils.activity = activity;
        PermissionsUtils.callBack = callBack;
        return INSTANCE;
    }

    public void checkSelfPermission() {
        // 检查该权限是否已经获取
        int i = ContextCompat.checkSelfPermission(activity, permissions[0]);
        // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
        if (i != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予该权限，就去提示用户请求
            showDialogTipUserRequestPermission();
        } else {
            callBack.onPermissionDone();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestPermissionCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = activity.shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else
                        activity.finish();
                } else {
                    callBack.onPermissionDone();
                }
            }
        }
    }

    public void onActivityResult(int requestCode) {
        if (requestCode == RequestGoSettingCode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(activity, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTipUserGoToAppSettting();
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        }
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);

        activity.startActivityForResult(intent, RequestGoSettingCode);
    }


    // 提示用户去应用设置界面手动开启权限
    private void showDialogTipUserGoToAppSettting() {

        dialog = new AlertDialog.Builder(activity)
                .setTitle("权限不可用")
                .setMessage("请在-应用设置-权限中，允许应用使用相关权限来保存用户数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).setCancelable(false).show();
    }

    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {
        new AlertDialog.Builder(activity)
                .setTitle("权限不可用")
                .setMessage("应用需要获取存储、定位权限，为你存储个人信息； 否则，您将无法正常使用")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).setCancelable(false).show();
    }

    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(activity, permissions, RequestPermissionCode);
    }

    public interface PermissionCallBack {
        //获取权限后的回调方法
        void onPermissionDone();
    }


}
