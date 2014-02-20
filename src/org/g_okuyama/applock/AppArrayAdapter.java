package org.g_okuyama.applock;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AppArrayAdapter extends ArrayAdapter<AppData> {
    private LayoutInflater mInflater;
    Context mContext;

    public AppArrayAdapter(Context context, int textViewResourceId, List<AppData> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        //if(convertView == null){
        convertView = mInflater.inflate(R.layout.app_item, null);
        //}

        final AppData data = (AppData)getItem(position);

        ImageView icon = (ImageView)convertView.findViewById(R.id.app_item_icon);
        icon.setImageDrawable(data.getDrawable());
        
        TextView name = (TextView)convertView.findViewById(R.id.app_item_name);
        name.setText(data.getAppName());
        
        CheckBox box = (CheckBox)convertView.findViewById(R.id.app_item_check);
        box.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton btn, boolean flag) {
				data.setLockFlag(flag);
			}
        });

        return convertView;
    }
}
