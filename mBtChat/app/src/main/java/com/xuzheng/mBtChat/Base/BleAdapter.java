package com.xuzheng.mBtChat.Base;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xuzheng.myapplication.R;

import java.util.List;

public class BleAdapter extends BaseAdapter {

    private Context context;
    private List<BluetoothDevice> list;
    private BluetoothDevice device;


    public BleAdapter(Context context, List<BluetoothDevice> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_ble, null, false);
            holder.tvBle = convertView.findViewById(R.id.tvBle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        device = list.get(position);
        if (device == null) {
            holder.tvBle.setText(device.getAddress());
        } else {
            holder.tvBle.setText(device.getName() + "  :  " + device.getAddress());
        }

        return convertView;
    }

    class ViewHolder {
        TextView tvBle;
    }
}
