package com.xuzheng.mBtChat.Server;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xuzheng.mBtChat.Chat.ChatActivity;
import com.xuzheng.mBtChat.Client.ClientService;

public class AcceptService extends Service {
    private static final String TAG = "AcceptService";
    private ServerService serverService;
    private BluetoothAdapter adapter;
    private  Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();
        initServerService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HandlerLeak")
    private void initServerService() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ClientService.CONNECTED_SUCCESS:
                        Log.i(TAG, "accept connected");
                        BluetoothDevice device = (BluetoothDevice) msg.obj;
                        Intent intent = new Intent(AcceptService.this, ChatActivity.class);
                        intent.putExtra("device", device);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case ClientService.CONNECTED_FAIL:
                        Log.i(TAG, "connected fail");
                        break;
                }
            }
        };
        serverService = ServerService.getInstance(handler);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();


        serverService.startAccept(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serverService.cancle();
    }
}
