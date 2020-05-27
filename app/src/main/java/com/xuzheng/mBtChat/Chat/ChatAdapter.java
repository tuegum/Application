package com.xuzheng.mBtChat.Chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuzheng.myapplication.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter {
    private static final String TAG = "ChatAdapter";
    private static final int SELF_CHAT = 0;
    private static final int FRIEND_CHAT = 1;

    private Context context;
    private List<Chat> list;

    public ChatAdapter(Context context, List<Chat> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SELF_CHAT){
            return new RightHolder(LayoutInflater.from(context).inflate(R.layout.adapter_chat_right,null,false));
        }else {
            return new LeftHolder(LayoutInflater.from(context).inflate(R.layout.adapter_chat_left,null,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RightHolder){
            ((RightHolder)holder).tvText.setText(list.get(position).getText());
        }else {
            ((LeftHolder)holder).tvText.setText(list.get(position).getText());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).isSelf()){
            return SELF_CHAT;
        }else {
            return FRIEND_CHAT;
        }
    }

    class RightHolder extends RecyclerView.ViewHolder{
        TextView tvText;

        public RightHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvRightChatText);
        }
    }

    class LeftHolder extends RecyclerView.ViewHolder{
        TextView tvText;

        public LeftHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvLeftChatText);
        }
    }
}
