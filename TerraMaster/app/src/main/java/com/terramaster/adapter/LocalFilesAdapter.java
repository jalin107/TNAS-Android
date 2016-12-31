package com.terramaster.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.terramaster.LocalFilesActivity;
import com.terramaster.R;
import com.terramaster.model.FileItem;
import com.terramaster.utils.DateUtils;
import com.terramaster.utils.IconUtils;

import java.util.List;

public class LocalFilesAdapter extends BaseAdapter {

    private LocalFilesActivity mActivity;
    private List<FileItem> items;

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public LocalFilesAdapter(LocalFilesActivity activity, List<FileItem> objects) {
        mActivity = activity;
        items = objects;

        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher).showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public FileItem getItem(int i) {
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_local_file, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FileItem fileItem = items.get(position);
        holder.tvName.setText(fileItem.getName());
        if (fileItem.isDirectory()) {
            //Jaylin.. for folder uploading...
            holder.ivCheckBox.setVisibility(View.VISIBLE);
            holder.ivCheckBox.setSelected(mActivity.isSelected(fileItem));

            holder.ivThumb.setImageResource(R.drawable.folder_ic);
            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()));
        } else {
            holder.ivCheckBox.setVisibility(View.VISIBLE);
            holder.ivCheckBox.setSelected(mActivity.isSelected(fileItem));

            displayThumb(fileItem.getName(), fileItem.getPath(), holder.ivThumb);
            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()) + " " + fileItem.getFileSize());
        }

        holder.ivCheckBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Jaylin.. for folder uploading...
                //if (!fileItem.isDirectory()) {
                    mActivity.toggleSelection(fileItem);
                    notifyDataSetChanged();
                //}
            }
        });
        return convertView;
    }

    private void displayThumb(String name, String path, ImageView imageView) {
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