package com.xuzheng.mBtChat.Client;

import android.content.Context;

import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xuzheng.myapplication.R;

import java.util.List;

public class UuidAdapter extends BaseAdapter {

    private Context context;
    private List<ParcelUuid> list;

    public UuidAdapter(Context context, List<ParcelUuid> list) {
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
        Holder holder = null;
        if (convertView == null){
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_ble,null,false);
            holder.tvBle = convertView.findViewById(R.id.tvBle);
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }
        holder.tvBle.setText(list.get(position).getUuid().toString());

        return convertView;
    }

    class Holder {
        TextView tvBle;
    }

}
