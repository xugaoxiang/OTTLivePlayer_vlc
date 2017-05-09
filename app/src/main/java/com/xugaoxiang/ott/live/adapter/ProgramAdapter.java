package com.xugaoxiang.ott.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xugaoxiang.ott.live.R;
import com.xugaoxiang.ott.live.activity.VideoPlayerActivity;
import com.xugaoxiang.ott.live.bean.LiveBean;

/**
 * Created by user on 2016/10/10.
 */
public class ProgramAdapter extends BaseAdapter{
    private Context context;

    public ProgramAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return VideoPlayerActivity.liveBean.getData().size();
    }

    @Override
    public LiveBean.DataBean getItem(int position) {
        return VideoPlayerActivity.liveBean.getData().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = View.inflate(context , R.layout.lv_program_item , null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(TextUtils.isEmpty(getItem(position).getNum())
                ?getItem(position).getName()
                :getItem(position).getNum()+"    "+getItem(position).getName());
        return convertView;
    }

    static class ViewHolder{

        private final TextView textView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.tv_program_name);
        }
    }
}
