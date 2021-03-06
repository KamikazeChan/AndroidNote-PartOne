package com.bingoogol.mobilesafe.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import com.bingoogol.mobilesafe.R;
import com.bingoogol.mobilesafe.domain.AppInfo;
import com.bingoogol.mobilesafe.engine.AppInfoProvider;
import com.bingoogol.mobilesafe.util.DensityUtil;
import com.bingoogol.mobilesafe.util.Logger;
import com.bingoogol.mobilesafe.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingoogol@sina.com on 14-3-30.
 */
public class AppManagerActivity extends Activity  implements View.OnClickListener {
    protected static final String TAG = "AppManagerActivity";
    private TextView tv_avail_rom;
    private TextView tv_avail_sd;
    private View loading;
    private ListView lv_appmanager;

    private TextView tv_status;

    private AppInfo appInfo;

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("---" + intent.getDataString());
        }

    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            loading.setVisibility(View.INVISIBLE);
            lv_appmanager.setAdapter(new AppAdapter());
        }
    };

    /**
     * 手机上用户程序的列表
     */
    private List<AppInfo> userAppInfos;
    /**
     * 手机上系统程序的列表
     */
    private List<AppInfo> systemAppInfos;


    /**
     * popup弹出窗体
     */
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        tv_avail_rom = (TextView) findViewById(R.id.tv_avail_rom);
        tv_avail_sd = (TextView) findViewById(R.id.tv_avail_sd);
        tv_status = (TextView) findViewById(R.id.tv_status);

        tv_avail_rom.setText("可用内存:" + getAvailROM());
        tv_avail_sd.setText("可用SD卡:" + getAvailSD());

        loading = findViewById(R.id.loading);
        lv_appmanager = (ListView) findViewById(R.id.lv_appmanager);

        lv_appmanager.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                dismissPopupWindow();
                if (userAppInfos != null && systemAppInfos != null) {
                    if (firstVisibleItem >= (userAppInfos.size() + 1)) {
                        tv_status.setText("系统程序:" + systemAppInfos.size() + "个");
                    } else {
                        tv_status.setText("用户程序:" + userAppInfos.size() + "个");
                    }
                }
            }
        });

        lv_appmanager.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                dismissPopupWindow();
                // 把点击的条目 赋值给类的成员变量
                appInfo = (AppInfo) lv_appmanager.getItemAtPosition(position);
                Logger.i(TAG, "被点击的条目包名:" + appInfo.getPackname());
                // popupwindow 类似于对话框 轻量级的activity 重量级的对话框
                View contentView = View.inflate(getApplicationContext(),
                        R.layout.popup_menu, null);
                LinearLayout ll_share = (LinearLayout) contentView
                        .findViewById(R.id.ll_share);
                LinearLayout ll_start = (LinearLayout) contentView
                        .findViewById(R.id.ll_start);
                LinearLayout ll_uninstall = (LinearLayout) contentView
                        .findViewById(R.id.ll_uninstall);

                ll_share.setOnClickListener(AppManagerActivity.this);
                ll_start.setOnClickListener(AppManagerActivity.this);
                ll_uninstall.setOnClickListener(AppManagerActivity.this);

                popupWindow = new PopupWindow(contentView,
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // 如果想让在点击别的地方的时候 关闭掉弹出窗体 一定要记得给popupwindow设置一个背景资源
                popupWindow.setBackgroundDrawable(new ColorDrawable(
                        Color.TRANSPARENT));

                int[] location = new int[2];
                view.getLocationInWindow(location);
                popupWindow.showAtLocation(parent, Gravity.TOP + Gravity.LEFT,
                        location[0] + DensityUtil.dip2px(getApplicationContext(), 60), location[1]);

                ScaleAnimation sa = new ScaleAnimation(0.5f, 1.2f, 0.5f, 1.2f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(500);

                contentView.startAnimation(sa);
            }
        });

        fillData();

    }

    /**
     * 填充数据
     */
    private void fillData() {
        loading.setVisibility(View.VISIBLE);
        new Thread() {
            public void run() {
                List<AppInfo> appInfos = AppInfoProvider.getAppInfos(getApplicationContext());
                userAppInfos = new ArrayList<AppInfo>();
                systemAppInfos = new ArrayList<AppInfo>();
                for (AppInfo info : appInfos) {
                    if (info.isUserApp()) {
                        userAppInfos.add(info);
                    } else {
                        systemAppInfos.add(info);
                    }
                }

                // 设置listview的数据.
                handler.sendEmptyMessage(0);
            }
        }.start();

    }

    /**
     * 获取手机内部存储空间
     *
     * @return
     */
    public String getAvailROM() {
        StatFs statfs = new StatFs(Environment.getDataDirectory()
                .getAbsolutePath());
        int blocks = statfs.getAvailableBlocks();
        int size = statfs.getBlockSize();
        long total = blocks * size;
        return Formatter.formatFileSize(this, total);
    }

    /**
     * 获取手机外部存储空间
     *
     * @return
     */
    public String getAvailSD() {
        StatFs statfs = new StatFs(Environment.getExternalStorageDirectory()
                .getAbsolutePath());
        int blocks = statfs.getAvailableBlocks();
        int size = statfs.getBlockSize();
        long total = blocks * size;
        return Formatter.formatFileSize(this, total);
    }

    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissPopupWindow();
        super.onDestroy();
    }

    static class ViewHolder {
        ImageView iv;
        TextView tv_name;
        TextView tv_location;
        TextView tv_version;
    }

    private class AppAdapter extends BaseAdapter {

        /**
         * 禁用掉 两个textview的点击事件.
         */
        @Override
        public boolean isEnabled(int position) {
            if (position == 0) {
                // 第0个位置的条目. 显示一个textview
                return false;
            } else if (position == (userAppInfos.size() + 1)) {
                // 第2个textview 显示
                return false;
            }
            return true;
        }

        /**
         * 返回listview里面有多少个条目,为了能多出来两个条目 一个显示用户程序的个数 一个显示系统程序的个数
         */
        @Override
        public int getCount() {
            return 1 + userAppInfos.size() + 1 + systemAppInfos.size();
        }

        /**
         * 返回某个位置绑定的数据
         */
        @Override
        public Object getItem(int position) {
            AppInfo appInfo = null;
            if (position == 0) {
                return null;
            } else if (position == (userAppInfos.size() + 1)) {
                return null;
            } else if (position <= userAppInfos.size()) {
                // 用户程序
                int newposition = position - 1;
                // 最上面有一个textview 把空间占据一个
                appInfo = userAppInfos.get(newposition);
            } else {
                // 剩下来只可能是系统程序
                // 分别减去两个textview
                int newposition = position - 1 - userAppInfos.size() - 1;
                appInfo = systemAppInfos.get(newposition);
            }
            return appInfo;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppInfo appInfo = null;
            if (position == 0) {// 第0个位置的条目. 显示一个textview
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.BLUE);
                tv.setText("用户程序:" + userAppInfos.size() + "个");
                return tv;
            } else if (position == (userAppInfos.size() + 1)) {// 第2个textview 显示
                // 有多少个系统程序
                TextView tv = new TextView(getApplicationContext());
                tv.setBackgroundColor(Color.GRAY);
                tv.setTextColor(Color.BLUE);
                tv.setText("系统程序:" + systemAppInfos.size() + "个");
                return tv;
            } else if (position <= userAppInfos.size()) {
                // 用户程序
                // 最上面有一个textview 把空间占据一个
                int newposition = position - 1;
                appInfo = userAppInfos.get(newposition);
            } else {
                // 剩下来只可能是系统程序
                // 分别减去两个textview和用户集合的个数
                int newposition = position - 1 - userAppInfos.size() - 1;
                appInfo = systemAppInfos.get(newposition);
            }
            View view = null;
            ViewHolder holder;
            if (convertView != null && convertView instanceof RelativeLayout) {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(getApplicationContext(), R.layout.app_item,
                        null);
                holder = new ViewHolder();
                holder.iv = (ImageView) view.findViewById(R.id.iv_app_icon);
                holder.tv_name = (TextView) view.findViewById(R.id.tv_app_name);
                holder.tv_version = (TextView) view
                        .findViewById(R.id.tv_app_version);
                holder.tv_location = (TextView) view
                        .findViewById(R.id.tv_app_location);
                view.setTag(holder);
            }

            holder.iv.setImageDrawable(appInfo.getAppIcon());
            holder.tv_name.setText(appInfo.getAppName());
            holder.tv_version.setText(appInfo.getVersion());
            if (appInfo.isInRom()) {
                holder.tv_location.setText("手机内存" + appInfo.isUserApp());
            } else {
                holder.tv_location.setText("外部存储" + appInfo.isUserApp());
            }
            return view;
        }

    }

    /**
     * 点击事件
     */
    @Override
    public void onClick(View v) {
        dismissPopupWindow();
        switch (v.getId()) {
            case R.id.ll_share:
                Logger.i(TAG, "分享:" + appInfo.getPackname());
                shareApplication();
                break;

            case R.id.ll_start:
                Logger.i(TAG, "开启:" + appInfo.getPackname());
                startApplication();

                break;
            case R.id.ll_uninstall:
                Logger.i(TAG, "卸载:" + appInfo.getPackname());
                uninstallApplication();
                break;
        }

    }

    /**
     * 卸载一个应用程序
     */
    private void uninstallApplication() {
        // <action android:name="android.intent.action.VIEW" />
        // <action android:name="android.intent.action.DELETE" />
        // <category android:name="android.intent.category.DEFAULT" />
        // <data android:scheme="package" />
        Intent intent = new Intent();
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + appInfo.getPackname()));
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillData();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 开启一个应用,实质上就是开启这个应用的第一个activity
     */
    private void startApplication() {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(appInfo.getPackname(),
                    PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activityInfos = info.activities;
            if (activityInfos != null && activityInfos.length > 0) {
                ActivityInfo activityInfo = activityInfos[0];// 第0个条目
                // 代表的就是当前应用程序入口的activity
                String className = activityInfo.name;
                Intent intent = new Intent();
                intent.setClassName(appInfo.getPackname(), className);
                startActivity(intent);
            } else {
                ToastUtil.makeText(this, "无法启动该应用");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.makeText(this, "没有找到当前应用");
        }

    }

    /**
     * 分享一个应用程序
     */
    private void shareApplication() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                "推荐您使用一款软件:名称叫" + appInfo.getAppName() + "下载地址:"
                        + "https://play.google.com/store/apps/details?id="
                        + appInfo.getPackname());
        startActivity(intent);
    }

}
