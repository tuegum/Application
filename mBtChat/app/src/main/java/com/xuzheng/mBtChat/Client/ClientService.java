package com.xuzheng.mBtChat.Client;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xuzheng.mBtChat.Chat.ChatService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ClientService implements ChatService {

    private static final String TAG = "ClientService";
    public static final String clientUuid = "aa87c0d0-afac-11de-8a39-0800200c9a66";
    private static ClientService instance;
    private static Handler mainHandler;
    private BluetoothDevice device;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    public ClientService() {
    }

    public static ClientService getInstance(Handler handler) {
        mainHandler = handler;
        if (instance == null) {
            Log.i(TAG, "instance is null");
            synchronized (ClientService.class) {
                if (instance == null) {
                    instance = new ClientService();
                }
            }
        }
        return instance;
    }

    public void connect(final BluetoothDevice connectDevice, final String uuid) {
        if (device != null && mSocket != null && device.getAddress().
                equals(device.getAddress()) && mSocket.isConnected()) {
            Log.i(TAG, "the same device to connect,return");
            return;
        }

        this.device = connectDevice;
        Log.i(TAG, "device:" + device.getName());
        Log.i(TAG, "uuid:" + uuid);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    mSocket.connect();

                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

                    createReadThread();

                    mainHandler.sendEmptyMessage(CONNECTED_SUCCESS);
                    Log.d(TAG, "run: CONNECTED_SUCCESS");

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "client connect failed : " + e.getMessage());
                    mainHandler.sendEmptyMessage(CONNECTED_FAIL);
                }
            }
        }).start();
    }

    public void createReadThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int len = 0;
                        while (len == 0) {
                            len = mInputStream.available();
                        }
                        byte[] bytes = new byte[len];

                        mInputStream.read(bytes);

                        String text = new String(bytes,0,len,"utf-8");
                        Log.i(TAG, "receive: " + text);
                        Message msg = mainHandler.obtainMessage();
                        msg.obj = text;
                        msg.what = READ_DATA_SUCCESS;
                        mainHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    mainHandler.sendEmptyMessage(READ_DATA_FAIL);
                    e.printStackTrace();
                    Log.e(TAG, "READ_DATA_FAIL" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void write(String text) {
        if (mSocket != null){
            if (!mSocket.isConnected()){
                mainHandler.sendEmptyMessage(BLUETOOTH_SOCKET_CLOSED);
                Log.i(TAG, "写入失败 cause => BLUETOOTH_SOCKET_CLOSED");
            }
        }
        Message msg = mainHandler.obtainMessage();
        String data = text;

        if (mOutputStream != null){
            try {
                mOutputStream.write(text.getBytes());
                mOutputStream.flush();
                msg.what = WRITE_DATA_SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                data = e.getMessage();
                msg.what = WRITE_DATA_FAIL;
                Log.e(TAG, "write failed  " + data );
//                mainHandler.sendEmptyMessage(WRITE_DATA_FAIL);
            }
        }
        msg.obj = data;
        mainHandler.sendMessage(msg);
    }

    public void cancel() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }

        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInputStream = null;
        }

    }
}
