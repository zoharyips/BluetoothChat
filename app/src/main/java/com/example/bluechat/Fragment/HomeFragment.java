package com.example.bluechat.Fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluechat.Activity.ChatActivity;
import com.example.bluechat.Activity.GroupActivity;
import com.example.bluechat.Activity.MainActivity;
import com.example.bluechat.Adapter.ItemBtListAdapter;
import com.example.bluechat.Bean.BlueToothBean;
import com.example.bluechat.CallBack.BlueToothInterface;
import com.example.bluechat.R;
import com.example.bluechat.Receiver.BluetoothStateBroadcastReceive;
import com.example.bluechat.Service.BluetoothChatService;
import com.example.bluechat.Util.BluetoothUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_DIALOG;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_SUCCESS;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_TOAST;


public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    public static final int MAX_GROUP_NUM = 3;
    private BluetoothUtil bluetoothUtil;
    private BluetoothStateBroadcastReceive broadcastReceive;
    private SwipeRefreshLayout layoutSwipeRefresh;
    private ListView lvBtList;
    private List<BlueToothBean> list;
    private List<BlueToothBean> groupAddlist;
    private ItemBtListAdapter adapter;
    private LinearLayout layoutHide;
    private ProgressDialog progressDialog;
    private BluetoothChatService mBluetoothChatService;
    private TextView tv1, tv2;
    private RelativeLayout groupBox;
    private Button btnAddGroup;
    private ListView groupList;

    private BlueToothInterface blueToothInterface = new BlueToothInterface() {
        @Override
        public void getBlueToothDevices(BluetoothDevice device) {
            BlueToothBean blueToothBean = new BlueToothBean(device.getName(), device.getAddress());
            if (device.getName() != null && device.getAddress() != null) {
                int k = 0;
                for (BlueToothBean i : list)
                    if (i.getMac().equals(blueToothBean.getMac()))
                        k++;
                if (k == 0) {
                    List<BlueToothBean> tmp = BlueToothBean.find(BlueToothBean.class, "mac = ?", blueToothBean.getMac());
                    if (tmp.isEmpty()) {
                        blueToothBean.save();
                    } else {
                        blueToothBean = tmp.get(0);
                    }
                    list.add(blueToothBean);
                    adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void getConnectedBlueToothDevices(BluetoothDevice device) {
            Snackbar.make(getView(), "连接" + device.getName() + "成功", Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void getDisConnectedBlueToothDevices(BluetoothDevice device) {
            Log.i("zjh-DisConnected", "断开连接");
            update();
        }

        @Override
        public void searchFinish() {
            layoutSwipeRefresh.setRefreshing(false);
        }

        @Override
        public void open() {
            mBluetoothChatService = BluetoothChatService.getInstance(handler);
            mBluetoothChatService.start();
            update();
        }

        @Override
        public void disable() {
            mBluetoothChatService.stop();
            update();
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //正在连接
                case BLUE_TOOTH_DIALOG:
//                    showProgressDialog((String) msg.obj);
                    break;
                //连接失败
                case BLUE_TOOTH_TOAST:
                    dismissProgressDialog();
                    Snackbar.make(getView(), (String) msg.obj, Snackbar.LENGTH_LONG).show();

                    break;
                //连接成功
                case BLUE_TOOTH_SUCCESS:
                    BluetoothDevice remoteDevice = (BluetoothDevice) msg.obj;
                    dismissProgressDialog();
                    myDevice = null;
                    final Intent intent = new Intent(getContext(), ChatActivity.class);
                    intent.putExtra(ChatActivity.DEVICE_NAME_INTENT, remoteDevice.getName());
                    intent.putExtra(ChatActivity.DEVICE_MAC_INTENT, remoteDevice.getAddress());
                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setMessage("连接设备" + msg.obj + "成功");
                    dialog.setCancelable(false);
                    dialog.show();
                    Timer timer = new Timer();
                    TimerTask tast = new TimerTask() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            startActivityForResult(intent, 0);
                        }
                    };
                    timer.schedule(tast, 1500);
                    break;
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bluetoothUtil = new BluetoothUtil(getContext());
        layoutSwipeRefresh = view.findViewById(R.id.layout_swipe_refresh);
        layoutSwipeRefresh.setOnRefreshListener(this);
        lvBtList = view.findViewById(R.id.lv_bt_list);
        tv1 = view.findViewById(R.id.tv1);
        tv2 = view.findViewById(R.id.tv2);
        groupBox = view.findViewById(R.id.groupBox);
        btnAddGroup = view.findViewById(R.id.btnAddGroup);
        groupList = view.findViewById(R.id.groupList);
        list = new ArrayList<>();
        groupAddlist = new ArrayList<>();
        adapter = new ItemBtListAdapter(list, getContext());
        lvBtList.setAdapter(adapter);
        lvBtList.setOnItemClickListener(this);
        layoutHide = view.findViewById(R.id.layout_hide);
        update();
        registerBluetoothReceiver();

        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv1.setTextColor(0xFFFFFFFF);
                tv1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tv2.setTextColor(0xFF000000);
                tv2.setBackgroundColor(0xFFFFFFFF);
                layoutSwipeRefresh.setVisibility(View.VISIBLE);
                groupBox.setVisibility(View.GONE);
            }
        });

        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv2.setTextColor(0xFFFFFFFF);
                tv2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tv1.setTextColor(0xFF000000);
                tv1.setBackgroundColor(0xFFFFFFFF);
                layoutSwipeRefresh.setVisibility(View.GONE);
                groupBox.setVisibility(View.VISIBLE);
            }
        });

        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!list.isEmpty()) {
                    groupAddlist.clear();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("选择成员(最多选择" + (MAX_GROUP_NUM) + "人)");
                    String[] macs = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        macs[i] = list.get(i).getName() + "\n" + list.get(i).getMac();
                    }
                    dialog.setMultiChoiceItems(macs, new boolean[macs.length], new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            Log.e("roc", list.get(which).getName() + "**" + isChecked);
                            if (isChecked && groupAddlist.size() < MAX_GROUP_NUM) {
                                groupAddlist.add(list.get(which));
                            } else if (!isChecked) {
                                groupAddlist.remove(list.get(which));
                            }
                        }
                    }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (groupAddlist.size() > 0 && groupAddlist.size() <= MAX_GROUP_NUM) {
                                String res = "";
                                for (BlueToothBean b : groupAddlist) {
                                    res += "m" + b.getMac();
                                }
                                res = res.substring(1);
                                bluetoothUtil.close();
                                Intent intent = new Intent(getActivity(), GroupActivity.class);
                                intent.putExtra("list", res);
                                intent.putExtra("flag", true);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), "群聊成员数有误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("取消", null).show();
                }

            }
        });
    }

    private void registerBluetoothReceiver() {
        Log.i("zjh", "蓝牙广播监听启动");
        if (broadcastReceive == null) {
            broadcastReceive = new BluetoothStateBroadcastReceive(blueToothInterface);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        getContext().registerReceiver(broadcastReceive, intentFilter);
    }

    private void unregisterBluetoothReceiver() {
        Log.i("zjh", "蓝牙广播监听关闭");
        if (broadcastReceive != null) {
            getContext().unregisterReceiver(broadcastReceive);
            broadcastReceive = null;
        }
    }

    @Override
    public void onRefresh() {
        update();
    }

    private void update() {
        layoutSwipeRefresh.setRefreshing(true);
        list.clear();
        if (bluetoothUtil.isBluetoothEnable()) {
            layoutHide.setVisibility(View.INVISIBLE);
            List<BlueToothBean> alltmp = bluetoothUtil.getDevicesList();
//            list.addAll(bluetoothUtil.getDevicesList());
            for (BlueToothBean blueToothBean : alltmp) {
                List<BlueToothBean> tmp = BlueToothBean.find(BlueToothBean.class, "mac = ?", blueToothBean.getMac());
                if (tmp.isEmpty()) {
                    blueToothBean.save();
                }
            }

            list.addAll(BlueToothBean.listAll(BlueToothBean.class));

        } else {
            layoutHide.setVisibility(View.VISIBLE);
            layoutSwipeRefresh.setRefreshing(false);
        }
        adapter.notifyDataSetChanged();
        if (bluetoothUtil.isBluetoothEnable()) {

            mBluetoothChatService = BluetoothChatService.getInstance(handler);
            mBluetoothChatService.start();
        }
        layoutSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mBluetoothChatService.start();
        ShowDialog(bluetoothUtil.getBluetoothDevice(list.get(position).getMac()));
    }

    private BluetoothDevice myDevice;

    /**
     * 蓝牙连接
     *
     * @param device
     */
    private void ShowDialog(final BluetoothDevice device) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this.getActivity());
        ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //连接
                myDevice = device;
                mBluetoothChatService.connectDevice(device);
            }
        });
        ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.setMessage("你确定要与" + device.getName() + "建立连接吗？");
        ad.setTitle("提示");
        ad.setCancelable(false);
        ad.show();
    }

    /**
     * 进度对话框
     *
     * @param msg
     */
    public void showProgressDialog(String msg) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(msg + "\n连接设备需打开本应用");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothChatService.stop();
                update();
            }
        });
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        update();
    }

    @Override
    public void onDestroy() {
        Log.i("zjh-onDestroy", "Home关闭");
        super.onDestroy();
        unregisterBluetoothReceiver();
        bluetoothUtil.close();
        if (mBluetoothChatService != null)
            mBluetoothChatService.stop();
    }
}
