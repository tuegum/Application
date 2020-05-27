package com.xuzheng.mBtChat.Server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.xuzheng.mBtChat.Chat.ChatService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ServerService implements ChatService {
    private static final String TAG = "ServerService";
    public static final String serverUuid = "aa87c0d0-afac-11de-8a39-0800200c9a66";
    private static ServerService instance;
    private static Handler mainHandler;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BluetoothSocket mSocket;
    private BluetoothDevice device;

    volatile static boolean stop = false;

    public ServerService() {
    }

    public static ServerService getInstance(Handler handler) {
        mainHandler = handler;
        if (instance == null) {
            Log.i(TAG, "instance is null");
            synchronized (ServerService.class) {
                if (instance == null) {
                    instance = new ServerService();
                }
            }
        }
        return instance;
    }

    public void startAccept(final BluetoothAdapter adapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord
                            ("chat-server", UUID.fromString(serverUuid));

                    mSocket = serverSocket.accept();
                    mSocket.getRemoteDevice();

                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();

                    createReadThread();

                    Message msg = new Message();
                    msg.what = CONNECTED_SUCCESS;
                    msg.obj = mSocket.getRemoteDevice();
                    mainHandler.sendMessage(msg);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "client connect error : " + e.getMessage());
                    mainHandler.sendEmptyMessage(CONNECTED_FAIL);
                }
            }
        }).start();
    }

    private void createReadThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true && !stop) {
                        int len = 0;
                        while (len == 0 && !stop) {
                            len = mInputStream.available();
                        }
                        byte[] bytes = new byte[len];

                        mInputStream.read(bytes);
                        String text = new String(bytes, 0, len, "utf-8");
                        Log.i(TAG, "receive:" + text);
                        Message msg = mainHandler.obtainMessage();
                        msg.obj = text;
                        msg.what = READ_DATA_SUCCESS;
                        mainHandler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mainHandler.sendEmptyMessage(READ_DATA_FAIL);
                    Log.e(TAG, "send data failed" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void write(String text) {
        if (mSocket != null) {
            if (!mSocket.isConnected()) {
                mainHandler.sendEmptyMessage(BLUETOOTH_SOCKET_CLOSED);
                Log.e(TAG, "connect failed : BLUETOOTH_SOCKET_CLOSED");
            }
        }
        Message msg = mainHandler.obtainMessage();
        String data = text;

        if (mOutputStream != null) {
            try {
                mOutputStream.write(text.getBytes());
                mOutputStream.flush();
                msg.what = WRITE_DATA_SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                data = e.getMessage();
                msg.what =WRITE_DATA_FAIL;
            }
        }
        msg.obj = data;
        mainHandler.sendMessage(msg);
    }

    public void cancle() {
        if (mSocket != null){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }
        if (mOutputStream != null){
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOutputStream = null;
        }
        if (mInputStream != null){
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInputStream = null;
        }
    }
}
