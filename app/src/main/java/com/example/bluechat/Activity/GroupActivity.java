package com.example.bluechat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluechat.Adapter.RecyclerChatAdapter;
import com.example.bluechat.Bean.BlueToothBean;
import com.example.bluechat.Bean.ChatInfo;
import com.example.bluechat.Bean.ChatRecord;
import com.example.bluechat.R;
import com.example.bluechat.Service.BluetoothChatService;
import com.example.bluechat.Util.BluetoothUtil;
import com.example.bluechat.Util.FileUtils;
import com.example.bluechat.Util.XPermissionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_READ;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_READ_FILE;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_READ_FILE_NOW;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_SUCCESS;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_TOAST;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_WRAITE;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_WRAITE_FILE;
import static com.example.bluechat.Activity.MainActivity.BLUE_TOOTH_WRAITE_FILE_NOW;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String GROUP_FLAG = "GROUP**";
    public static final String CLOSE_FLAG = "设备连接失败/传输关闭";
    private static final int UPDATE_DATA = 0x666;
    private String deviceName;
    private String deviceMac;
    private BluetoothChatService bluetoothChatService;
    private ProgressDialog dialog;
    private BluetoothUtil bluetoothUtil;
//    private DBManager dbManager;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BLUE_TOOTH_TOAST:
                    String msgToast = (String) msg.obj;
                    if (CLOSE_FLAG.equals((String) msg.obj) )
                    {
                        if (nowindex<allBeans.size()-1)
                        {
                            bluetoothChatService.start();
                            nowindex++;
                            NextConnect();

                        }
                        else
                        {
                            groupMsg = "";
                        }
                    }
                    break;
                case BLUE_TOOTH_READ:
                    String readMessage = (String) msg.obj;
                    String[] temp = readMessage.split(",,,");
                    for (BlueToothBean b:allBeans)
                    {
                        if (b.getMac().equals(temp[0]))
                        {
                            deviceName = b.getName();
                            break;
                        }
                    }
                    Log.i("zjh聊天", deviceName + ":" + readMessage);
                    if (!temp[1].startsWith(GROUP_FLAG) )
                    {
                        list.add(new ChatInfo(ChatInfo.TAG_LEFT, deviceName, temp[1]));
                        recyclerChatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(list.size());
                    }
                    bluetoothChatService.stop();
//                    dbManager.add(new ChatRecord(deviceMac, ChatInfo.TAG_LEFT, deviceName, readMessage));
//                    ChatRecord chatRecord = new ChatRecord(deviceMac, ChatInfo.TAG_LEFT, deviceName, readMessage);
//                    chatRecord.save();
                    break;
                case BLUE_TOOTH_WRAITE:
                    String writeMessage = (String) msg.obj;
                    bluetoothChatService.setMyDecice(null);
                    Log.i("zjh聊天", "我" + ":" + writeMessage);
                    if (writeMessage.startsWith(GROUP_FLAG) )
                    {
                        if (creatFlag )
                        {
//                            serviceMap.get(allBeans.get(nowindex).getMac()).stop();

//                            CloseBlue();
                        }
                    }
                    else if (writeMessage.equals(groupMsg))
                    {

//                        CloseBlue();
                    }

//                    dbManager.add(new ChatRecord(deviceMac, ChatInfo.TAG_RIGHT, "我", writeMessage));
//                    ChatRecord chatRecord2 = new ChatRecord(deviceMac, ChatInfo.TAG_RIGHT, "我", writeMessage);
//                    chatRecord2.save();
                    break;
                case BLUE_TOOTH_READ_FILE_NOW:
                    Log.i("zjh蓝牙文件传输", msg.obj + "");
                    Snackbar.make(et_write, (String) msg.obj, Snackbar.LENGTH_LONG).show();
                    break;
                case BLUE_TOOTH_WRAITE_FILE_NOW:
                    Log.i("zjh蓝牙文件传输", msg.obj + "");
                    if (msg.obj.toString().equals("文件发送失败")) {
                        dialog.dismiss();
                        Snackbar.make(et_write, (String) msg.obj, Snackbar.LENGTH_LONG).show();
                    } else {
                        dialog.setMessage(msg.obj + "");
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                    break;
                case BLUE_TOOTH_READ_FILE:
                    Log.i("zjh蓝牙文件传输", "文件接收完成(" + msg.obj + ")");
                    list.add(new ChatInfo(ChatInfo.TAG_FILE_LEFT, deviceName, msg.obj + ""));
                    recyclerChatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(list.size());
//                    dbManager.add(new ChatRecord(deviceMac, ChatInfo.TAG_FILE_LEFT, deviceName, msg.obj + ""));
                    ChatRecord chatRecord3 = new ChatRecord(deviceMac, ChatInfo.TAG_FILE_LEFT, deviceName, msg.obj + "");
                    chatRecord3.save();
                    break;
                case BLUE_TOOTH_WRAITE_FILE:
                    Log.i("zjh蓝牙文件传输", "文件发送完成");
                    dialog.dismiss();
                    list.add(new ChatInfo(ChatInfo.TAG_FILE_RIGHT, "我", msg.obj + ""));
                    recyclerChatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(list.size());
//                    dbManager.add(new ChatRecord(deviceMac, ChatInfo.TAG_FILE_RIGHT, "我", msg.obj + ""));
                    ChatRecord chatRecord4 = new ChatRecord(deviceMac, ChatInfo.TAG_FILE_RIGHT, "我", msg.obj + "");
                    chatRecord4.save();
                    break;
                case UPDATE_DATA:
                    recyclerChatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(list.size());
                    break;
                //连接成功
                case BLUE_TOOTH_SUCCESS:
//                    final ProgressDialog dialog = new ProgressDialog(GroupActivity.this);
//                    dialog.setMessage("连接设备" + msg.obj + "成功");
//                    dialog.setCancelable(false);
//                    dialog.show();
                    if (!TextUtils.isEmpty(groupMsg))
                    {
                        Timer timer2 = new Timer();
                        TimerTask tast2 = new TimerTask() {
                            @Override
                            public void run() {
                                dialog.dismiss();
//                            serviceMap.get(allBeans.get(nowindex).getMac()).sendData(GROUP_FLAG+groupFlag);
                                bluetoothChatService.sendData(groupMsg);
                            }
                        };
                        timer2.schedule(tast2, 400);
                    }
                    else
                    {
                        String[] res = groupFlag.split("m");
                        String tmp = "";
                        for (int i=0;i<res.length;i++)
                        {
                            if (res[i].equals(allBeans.get(nowindex).getMac()))
                            {
                                tmp+="m"+"creator";
                            }
                            else
                            {
                                tmp+="m"+res[i];
                            }
                        }
                        tmp=tmp.substring(1);
                        Timer timer2 = new Timer();
                        final String finalTmp = tmp;
                        TimerTask tast2 = new TimerTask() {
                            @Override
                            public void run() {
                                dialog.dismiss();
//                            serviceMap.get(allBeans.get(nowindex).getMac()).sendData(GROUP_FLAG+groupFlag);
                                bluetoothChatService.sendData(GROUP_FLAG+ finalTmp);
                            }
                        };
                        timer2.schedule(tast2, 1700);
                    }


                    break;
            }
        }
    };

    private boolean is_bt_add = true;
    private RecyclerView recyclerView;
    private List<ChatInfo> list;
    private RecyclerChatAdapter recyclerChatAdapter;
    private ImageButton bt_send;
    private ImageButton bt_add;
    private RelativeLayout layout_add;
    private EditText et_write;
    private TextView tvGroup;
    private ImageView ivFile;

    private List<BlueToothBean> allBeans = new ArrayList<>();
    private Map<String, BluetoothChatService> serviceMap = new HashMap<>();
    private int nowindex;
    private String groupFlag;
    private boolean creatFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("群聊");
        initView();

        bluetoothUtil = new BluetoothUtil(this);
        Intent intent = getIntent();
        try {
            groupFlag = intent.getStringExtra("list");
            String[] res = groupFlag.split("m");
            for (String s : res) {
                BlueToothBean b = BlueToothBean.find(BlueToothBean.class, "mac = ?", s).get(0);
                tvGroup.append(b.getName()+",");
                allBeans.add(b);
//                BluetoothChatService bs = new BluetoothChatService(handler);
//                serviceMap.put(b.getMac(), bs);
            }
            bluetoothChatService = BluetoothChatService.getInstance2(handler);
            creatFlag = intent.getBooleanExtra("flag",false);
            if (creatFlag)
            {
                CreateGroup();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        bluetoothChatService = BluetoothChatService.getInstance(handler);
//        try {
//            blueToothBean = BlueToothBean.find(BlueToothBean.class, "mac = ?", deviceMac).get(0);
//            blueToothBean.setName(deviceName);
//            blueToothBean.AddCount();
//            blueToothBean.CalScore();
//            blueToothBean.save();
//        } catch (Exception e) {
//            e.printStackTrace();
//            finish();
//            return;
//        }

//        dbManager = new DBManager(this);

//        List<ChatRecord> chatRecordList = dbManager.query(deviceMac);
//        List<ChatRecord> chatRecordList = ChatRecord.find(ChatRecord.class, "mac = ?", deviceMac);
//        for (ChatRecord i : chatRecordList)
//            list.add(new ChatInfo(i.getTag(), i.getName(), i.getContent()));
//        recyclerChatAdapter.notifyDataSetChanged();
//        recyclerView.smoothScrollToPosition(list.size());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            exit();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothChatService.stop();
//        dbManager.closeDB();
    }

    private void CreateGroup() {
        nowindex = 0;
//        serviceMap.get(allBeans.get(nowindex).getMac()).connectDevice(bluetoothUtil.getBluetoothDevice(allBeans.get(nowindex).getMac()));
        bluetoothChatService.connectDevice(bluetoothUtil.getBluetoothDevice(allBeans.get(nowindex).getMac()));
    }

    private void CloseBlue()
    {
        bluetoothChatService.stop();

    }
    private void NextConnect( ) {

        Timer timer2 = new Timer();
        TimerTask tast2 = new TimerTask() {
            @Override
            public void run() {
//                serviceMap.get(allBeans.get(nowindex).getMac()).connectDevice(bluetoothUtil.getBluetoothDevice(allBeans.get(nowindex).getMac()));
                bluetoothChatService.connectDevice(bluetoothUtil.getBluetoothDevice(allBeans.get(nowindex).getMac()));
            }
        };
        timer2.schedule(tast2, 1500);

    }

    private void initView() {
        dialog = new ProgressDialog(this);
        list = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setFocusable(true);
        recyclerView.setFocusableInTouchMode(true);
        recyclerView.requestFocus();
        recyclerChatAdapter = new RecyclerChatAdapter(this);
        recyclerChatAdapter.setList(list);
        recyclerView.setAdapter(recyclerChatAdapter);
        et_write = findViewById(R.id.et_write);
        et_write.setOnClickListener(this);
        et_write.setFocusable(true);
        et_write.setFocusableInTouchMode(true);
        et_write.requestFocus();
        bt_send = findViewById(R.id.bt_send);
        bt_send.setOnClickListener(this);
        bt_send.setClickable(false);
        tvGroup = findViewById(R.id.tvTop);
        et_write.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bt_send.setBackgroundResource(R.drawable.nosend);
                bt_send.setClickable(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String write = et_write.getText().toString().trim();
                if (TextUtils.isEmpty(write))
                    return;
                bt_send.setBackgroundResource(R.drawable.send);
                bt_send.setClickable(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send:
                send();
                break;
        }
    }

    private void hintKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private void send() {
        String write = et_write.getText().toString().trim();
        if (TextUtils.isEmpty(write)) {
            Snackbar.make(et_write, "发送内容不能为空", Snackbar.LENGTH_LONG).show();
            return;
        }
        SendGroup(write);
        et_write.setText("");
    }

    private String groupMsg;
    private void SendGroup(String msg)
    {
        groupMsg = msg;
        list.add(new ChatInfo(ChatInfo.TAG_RIGHT, "我", msg));
        recyclerChatAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(list.size());
        CreateGroup();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            exit();
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        AlertDialog.Builder ad = new AlertDialog.Builder(GroupActivity.this);
        ad.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bluetoothChatService = BluetoothChatService.getInstance2(null);
                finish();
//                dbManager.closeDB();
            }
        });
        ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ad.setMessage("你确定要断开连接吗？");
        ad.setTitle("提示");
        ad.setCancelable(false);
        ad.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        XPermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

