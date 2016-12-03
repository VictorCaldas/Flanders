package com.flanders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageListAdapter extends ArrayAdapter<ChatMessage> {

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    public MessageListAdapter(Context context, List<ChatMessage> objects) {
        super(context, R.layout.list_item_message, R.id.message_text, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ChatMessage message = getItem(position);

        TextView nameText = (TextView) view.findViewById(R.id.name_text);
        nameText.setText(message.getName());

        TextView latText = (TextView) view.findViewById(R.id.lat_text);
        latText.setText(String.valueOf(message.getLat()));

        TextView lonText = (TextView) view.findViewById(R.id.lon_text);
        lonText.setText(String.valueOf(message.getLon()));

        TextView velText = (TextView) view.findViewById(R.id.vel_text);
        velText.setText(String.valueOf(message.getVel()));

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        messageText.setText(message.getText());

        TextView timestampText = (TextView) view.findViewById(R.id.timestamp_text);
        timestampText.setText(mSimpleDateFormat.format(new Date(message.getTimestamp())));

        return view;
    }
}