package com.example.bluechat.Adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.bluechat.Bean.BlueToothBean;
import com.example.bluechat.R;

public class ItemBtListAdapter extends BaseAdapter {

    private List<BlueToothBean> objects;
    private Context context;
    private LayoutInflater layoutInflater;

    public ItemBtListAdapter(List<BlueToothBean> objects, Context context) {
        this.objects = objects;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public BlueToothBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_bt_list, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(BlueToothBean object, ViewHolder holder) {
        holder.tvBtName.setText(object.getName());
        holder.tvBtMac.setText(object.getMac());
        if (object.getScore()<0)
        {
            holder.tvFlag.setText("NEW");
        }
        else if (object.getScore()>=0 && object.getScore()<60)
        {
            holder.tvFlag.setText("Danger");
        }
        else if (object.getScore()>=60 && object.getScore()<70)
        {
            holder.tvFlag.setText("Trust");
        }
        else if (object.getScore()>=70 )
        {
            holder.tvFlag.setText("Trusted");
        }
    }

    protected class ViewHolder {
        private RelativeLayout layoutItem;
        private TextView tvBtName;
        private TextView tvBtMac;
        private TextView tvFlag;

        public ViewHolder(View view) {
            layoutItem = view.findViewById(R.id.layout_item);
            tvBtName = view.findViewById(R.id.tv_bt_name);
            tvBtMac = view.findViewById(R.id.tv_bt_mac);
            tvFlag = view.findViewById(R.id.tvFlag);
        }
    }
}
