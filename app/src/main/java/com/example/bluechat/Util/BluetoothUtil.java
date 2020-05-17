package com.example.bluechat.Util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.example.bluechat.Bean.BlueToothBean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothUtil {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothUtil(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //获取本地蓝牙实例
    }

    /**
     * 判断蓝牙是否开启
     */
    public boolean isBluetoothEnable() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * 开启蓝牙,可被发现300秒
     */
    public void openBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(intent);
    }

    /**
     * 关闭蓝牙
     */
    public void disableBluetooth() {
        bluetoothAdapter.disable();
    }

    /**
     * 查询已配对设备
     */
    public List<BlueToothBean> getDevicesList() {
        List<BlueToothBean> list = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices)
                list.add(new BlueToothBean(device.getName(), device.getAddress()));
        }
        return list;
    }

    /**
     * 扫描附件设备需要定位权限
     */
    public void startDiscovery() {
        bluetoothAdapter.startDiscovery();
    }

    public BluetoothDevice getBluetoothDevice(String mac) {
        return bluetoothAdapter.getRemoteDevice(mac);
    }

    public void close(){
        bluetoothAdapter.cancelDiscovery();
    }

    public String getBluetoothAddress() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Field field = bluetoothAdapter.getClass().getDeclaredField("mService");
            // 参数值为true，禁用访问控制检查
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }
            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            Object address = method.invoke(bluetoothManagerService);
            if (address != null && address instanceof String) {
                return (String) address;
            } else {
                return null;
            }
            //抛一个总异常省的一堆代码...
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
