package com.xuzheng.mBtChat.Client;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xuzheng.mBtChat.Chat.ChatActivity;
import com.xuzheng.myapplication.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;

public class DeviceActivity extends AppCompatActivity {
    private static final String TAG = "DeviceActivity";

    BluetoothDevice mDevice;
    ClientService service;

    private TextView tvName, tvAddress, tvState, tvType, tvUUID, btn;
    private ListView lvUUID;
    private UuidAdapter uuidAdapter;
    private List<ParcelUuid> list = new ArrayList<>();
    private String selectedUUID = ClientService.clientUuid;
    //当前配对状态
    private int bondedState;
    //button的状态，如果已配对为false,未配对为true，先配置对后连接
    private boolean bond = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
        }

        mDevice = getIntent().getParcelableExtra("device");
        if (mDevice == null){
            Toast.makeText(this,"设备为空，重选吧！",Toast.LENGTH_LONG).show();
            finish();
        }

        bindView();

        initView();

        registerReceiver();


    }



    private void bindView() {
        tvName = findViewById(R.id.tvDeviceName);

        tvAddress = findViewById(R.id.tvDeviceAddress);

        tvUUID = findViewById(R.id.tvDeviceUUID);

        tvType = findViewById(R.id.tvDeviceType);

        tvState = findViewById(R.id.tvDeviceState);

        btn = findViewById(R.id.btn);

        lvUUID = findViewById(R.id.lvUUID);
    }

    private void initView() {
        uuidAdapter = new UuidAdapter(this, list);

        tvName.setText(mDevice.getName() + "");
        tvAddress.setText(mDevice.getAddress() + "");
        tvUUID.setText(selectedUUID);

        initDeviceBondedState();

        if (mDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE){
            tvType.setText("低功耗蓝牙");
        }else if (mDevice.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC){
            tvType.setText("经典蓝牙");
        }else if (mDevice.getType() == BluetoothDevice.DEVICE_TYPE_DUAL){
            tvType.setText("双模蓝牙");
        }else{
            tvType.setText("未知设备");
        }

        lvUUID.setAdapter(uuidAdapter);
        lvUUID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUUID = list.get(position).getUuid().toString();
                tvUUID.setText(selectedUUID);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //没配对就配对，配对了就绑定
                if (bond){
                    mDevice.createBond();
                    Log.d(TAG, "配对中！");
                    return;
                }else{
                    Log.d(TAG, "开始绑定！");
                    service = ClientService.getInstance(handler);
                    service.connect(mDevice,selectedUUID);
                    Log.d(TAG, "已绑定！");
                }

            }
        });

        for (ParcelUuid uuid : list) {
            if (uuid.getUuid().toString().equals(ClientService.clientUuid)){
                selectedUUID = ClientService.clientUuid;
                tvUUID.setText(selectedUUID);
                return;
            }
        }
    }

    private void initDeviceBondedState(){
        bondedState = mDevice.getBondState();
        if (bondedState == BluetoothDevice.BOND_BONDED){
            tvState.setText("已配对");
        }else if (bondedState == BluetoothDevice.BOND_BONDING){
            tvState.setText("配对中");
        }else{
            tvState.setText("未配对");
        }
        initBondedButton();
        if (mDevice.getUuids() != null && mDevice.getUuids().length > 0){
            /**
             * Arrays  static <T> List<T> asList(T... a) 返回一个受指定数组支持的固定大小的列表。
             */
            list.addAll(Arrays.asList(mDevice.getUuids()));
            uuidAdapter.notifyDataSetChanged();
        }

    }

    private void initBondedButton(){
        if (bondedState == BluetoothDevice.BOND_BONDED){
            btn.setText("连接");
            bond = false;
        }else{
            btn.setText("配对");
            bond = true;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ClientService.CONNECTED_SUCCESS:
                    Log.i(TAG, "connected success");
                    Intent intent = new Intent(DeviceActivity.this, ChatActivity.class);
                    intent.putExtra("device", mDevice);
                    intent.putExtra("uuid", selectedUUID);
                    startActivity(intent);
                    break;
                case ClientService.CONNECTED_FAIL:
                    Log.i(TAG, "connected fail");
                    break;
            }
        }
    };

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothReceicer,filter);
    }

    BroadcastReceiver bluetoothReceicer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,intent.getAction());
            if (intent.getAction().equals(ACTION_BOND_STATE_CHANGED)){
                initDeviceBondedState();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceicer);
    }
}


















