package com.vatsalyadav.apps.readsms.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.vatsalyadav.apps.readsms.R;
import com.vatsalyadav.apps.readsms.model.Message;
import com.vatsalyadav.apps.readsms.utils.Utils;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    private final LayoutInflater mInflater;
    private List<Message> messages; // Cached copy of words
    private Context context;

    public MessageListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message currentMessage = messages.get(position);
        holder.bindTo(currentMessage);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // messages has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (messages != null)
            return messages.size();
        else return 0;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageAddress;
        private final TextView messageDate;
        private final TextView messageAmount;

        private MessageViewHolder(View itemView) {
            super(itemView);
            messageAddress = itemView.findViewById(R.id.textView_address);
            messageDate = itemView.findViewById(R.id.textView_date);
            messageAmount = itemView.findViewById(R.id.textView_amount);
        }

        public void bindTo(Message message) {
            messageAddress.setText(message.getAddress());
            messageDate.setText(Utils.dateFormat(message.getDate()));
            messageAmount.setText("Amount: "+message.getBody());
        }
    }

}
