package com.terramaster.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.terramaster.R;
import com.terramaster.SelectDeviceFilesActivity;
import com.terramaster.model.FTPFileItem;
import com.terramaster.utils.DateUtils;
import com.terramaster.utils.IconUtils;

import java.util.List;

public class SelectDeviceFilesAdapter extends BaseAdapter {
    private SelectDeviceFilesActivity mActivity;
    private List<FTPFileItem> items;

    private View tvEmtyData;

    // private DisplayImageOptions options;
    // private ImageLoader imageLoader = ImageLoader.getInstance();

    public SelectDeviceFilesAdapter(SelectDeviceFilesActivity activity, List<FTPFileItem> objects, View tvEmtyData) {
        mActivity = activity;
        items = objects;
        this.tvEmtyData = tvEmtyData;

        displayEmptyMessage();
        // options = new
        // DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher).showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
        // .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public FTPFileItem getItem(int i) {
        return items.get(i);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void notifyDataSetChanged(List<FTPFileItem> ftpFileItems) {
        // TODO Auto-generated method stub
        items = ftpFileItems;
        super.notifyDataSetChanged();
        displayEmptyMessage();
    }

    public void displayEmptyMessage() {
        tvEmtyData.setVisibility(getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_select_device_file, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FTPFileItem fileItem = items.get(position);
        holder.tvName.setText(fileItem.getName());
        if (fileItem.isDirectory()) {
            holder.ivCheckBox.setVisibility(View.INVISIBLE);
            holder.ivThumb.setImageResource(R.drawable.folder_ic);

            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()));
        } else {
            holder.ivCheckBox.setVisibility(View.VISIBLE);
            holder.ivCheckBox.setSelected(mActivity.isSelected(fileItem));

            displayThumb(fileItem.getName(), holder.ivThumb);

            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()) + " " + fileItem.getFileSize());
        }

        return convertView;
    }

    private void displayThumb(String name, ImageView imageView) {
        imageView.setImageResource(IconUtils.findIcon(name));
    }

    private class ViewHolder {
        public TextView tvName, tvSummary;
        public ImageView ivThumb, ivCheckBox;

        public ViewHolder(View convertView) {
            // TODO Auto-generated constructor stub
            tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);

            ivThumb = (ImageView) convertView.findViewById(R.id.ivThumb);
            ivCheckBox = (ImageView) convertView.findViewById(R.id.ivCheckBox);
        }
    }

}