package com.xuzheng.mBtChat.Base;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.xuzheng.mBtChat.Client.DeviceActivity;
import com.xuzheng.mBtChat.Server.AcceptService;
import com.xuzheng.myapplication.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_BLE_OPEN = 1;

    private ListView lvBle;
    private Button btnSearch;
    private TextView tvDevice;

    private List<BluetoothDevice> list;
    private Set<String> set = new HashSet<>();

    private BleAdapter bleAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    public ImmersionBar mImmersionBar;

    private BluetoothLeScanner scanner;
    private BluetoothDevice selectedDevice;
    public static final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.statusBarColor(R.color.colorPrimary).init();

        isSupportBle();
        bindView();
        initData();
        initView();
        registerBluetoothReceiver();
        requestPermissions();
        startDiscovery();
        beDiscovered();
        startAcceptService();
    }

    private void isSupportBle() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
        if (mBluetoothAdapter == null
                ||!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            showNotSupportBluetoothDialog();
            Log.e(TAG, "not support bluetooth");
        }else {
            Log.d(TAG, "support bluetooth");
        }
    }

    private void bindView() {
        lvBle = findViewById(R.id.lvBle);
        btnSearch = findViewById(R.id.btnSearch);
        tvDevice = findViewById(R.id.tvDevice);
    }

    private void initData() {
        list = new ArrayList<>();
        bleAdapter = new BleAdapter(this,list);
    }

    private void initView() {
        lvBle.setAdapter(bleAdapter);
        lvBle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                selectedDevice = list.get(position);
                Intent intent = new Intent(MainActivity.this, DeviceActivity.class);
                intent.putExtra("device",selectedDevice);
                startActivity(intent);

            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter != null){
                    list.clear();
                    mBluetoothAdapter.startDiscovery();
                }
            }
        });
    }


    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver,filter);
    }

    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btnSearch.setText("正在搜索···");
                list.add(device);
                Log.d(TAG, "discovery : " + device.getName());
                bleAdapter.notifyDataSetChanged();
            }else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Log.d(TAG, "搜索完毕！");
                btnSearch.setText("重新搜索");
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    };

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
            List<String> list = new ArrayList<>();
            for (String permission : permissions) {
                list.add(permission);
            }
            if (list.size() > 0) {
                ActivityCompat.requestPermissions(this, permissions, 100);
            }
        }
    }

    /**
     * Android 6.0 动态申请授权定位信息权限，否则扫描蓝牙列表为空
     */
/*
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "使用蓝牙需要授权定位信息", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_COARSE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户授权
            } else {
                finish();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

*/

    private void startDiscovery() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startDiscovery();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BLE_OPEN);
        }
    }

    private void beDiscovered() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
        }
    }

    private void startAcceptService() {
        Intent intent = new Intent(this, AcceptService.class);
        startService(intent);
    }

    private void showNotSupportBluetoothDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("当前设备不支持蓝牙！").create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }
}
