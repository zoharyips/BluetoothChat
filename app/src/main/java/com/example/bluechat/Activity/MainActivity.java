package com.example.bluechat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.example.bluechat.Adapter.ViewPagerAdapter;
import com.example.bluechat.Fragment.HomeFragment;
import com.example.bluechat.Fragment.SettingFragment;
import com.example.bluechat.R;
import com.example.bluechat.Util.XPermissionUtil;


public class MainActivity extends AppCompatActivity {

    public static final int BLUE_TOOTH_DIALOG = 0x111;
    public static final int BLUE_TOOTH_TOAST = 0x123;
    public static final int BLUE_TOOTH_WRAITE = 0X222;
    public static final int BLUE_TOOTH_READ = 0X333;
    public static final int BLUE_TOOTH_WRAITE_FILE_NOW = 0X511;
    public static final int BLUE_TOOTH_READ_FILE_NOW = 0X996;
    public static final int BLUE_TOOTH_WRAITE_FILE = 0X555;
    public static final int BLUE_TOOTH_READ_FILE = 0X888;
    public static final int BLUE_TOOTH_SUCCESS = 0x444;

    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        initView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {

        viewPager = findViewById(R.id.viewpager);

        setupViewPager(viewPager);
        XPermissionUtil.requestPermissions(this, 1,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                new XPermissionUtil.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        //权限获取成功
                    }

                    @Override
                    public void onPermissionDenied() {
                        //权限获取失败
                        Snackbar.make(bottomNavigationView, "请手动到设置界面给予相关权限", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
//        adapter.addFragment(new SettingFragment());
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        XPermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
