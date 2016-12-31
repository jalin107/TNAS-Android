package com.terramaster;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.terramaster.widget.MyTextView;

public class LockIntervalActivity extends BaseActivity {

    private ListView listView;
    private LockIntervalAdapter adapter;
    private String[] intervals;
    private int selInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_interval);

        initObjects();
    }

    @Override
    protected void initObjects() {
        // TODO Auto-generated method stub
        super.initObjects();
        enableBackButton();
        setTitle(R.string.lock_interval);

        selInterval = prefUtils.getSelectedLockInterval();
        intervals = getResources().getStringArray(R.array.lock_intervals);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                showToast("Sel " + position);
                selInterval = position;
                prefUtils.setLockInterval(selInterval);
                adapter.notifyDataSetChanged();
            }
        });
        adapter = new LockIntervalAdapter();
        listView.setAdapter(adapter);

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.tvBackButton:
                onBackPressed();
                break;
            default:
                super.onClick(v);
                break;
        }

    }

    private class LockIntervalAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return intervals.length;
        }

        @Override
        public String getItem(int position) {
            // TODO Auto-generated method stub
            return intervals[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(LockIntervalActivity.this).inflate(R.layout.item_lock_interval, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvName.setText(getItem(position));
            holder.ivCheckBox.setSelected(selInterval == position);

            return convertView;
        }

        private class ViewHolder {
            MyTextView tvName;
            ImageView ivCheckBox;

            public ViewHolder(View v) {
                // TODO Auto-generated constructor stub
                tvName = (MyTextView) v.findViewById(R.id.tvName);
                ivCheckBox = (ImageView) v.findViewById(R.id.ivCheckBox);
            }
        }

    }
}
