package com.terramaster.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.terramaster.R;
import com.terramaster.SelectPhotoAlbumActivity;
import com.terramaster.model.AlbumDetail;

import java.util.List;

public class SelectPhotoAlbumAdapter extends BaseAdapter {
    private SelectPhotoAlbumActivity mActivity;
    private List<AlbumDetail> items;

    private View tvEmtyData;

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public SelectPhotoAlbumAdapter(SelectPhotoAlbumActivity activity, List<AlbumDetail> objects, View tvEmtyData) {
        mActivity = activity;
        items = objects;
        this.tvEmtyData = tvEmtyData;

        displayEmptyMessage();
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.trans).showImageForEmptyUri(R.drawable.trans).showImageOnFail(R.drawable.trans).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public AlbumDetail getItem(int i) {
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

    public void notifyDataSetChanged(List<AlbumDetail> albumList) {
        // TODO Auto-generated method stub
        items = albumList;
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
            convertView = vi.inflate(R.layout.item_select_photo_album, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AlbumDetail albumDetail = items.get(position);
        holder.tvName.setText(albumDetail.getAlbumName());
        holder.tvSummary.setText(mActivity.getString(R.string.total_number_of_photos) + albumDetail.noOfPhotos);

        holder.ivCheckBox.setSelected(mActivity.isSelected(albumDetail));

        holder.llBottomThumb.setVisibility((albumDetail.images.size() > 2) ? View.VISIBLE : View.GONE);
        for (int i = 0; i < holder.ivThumb.length; i++) {
            if (i < albumDetail.images.size()) {
                holder.ivThumb[i].setVisibility(View.VISIBLE);
                imageLoader.displayImage("file://" + albumDetail.images.get(i), holder.ivThumb[i], options);
            } else {
                holder.ivThumb[i].setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private class ViewHolder {
        public TextView tvName, tvSummary;
        public ImageView ivCheckBox;
        public ImageView ivThumb[];
        public View llBottomThumb;

        public ViewHolder(View convertView) {
            // TODO Auto-generated constructor stub
            tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);
            llBottomThumb = convertView.findViewById(R.id.llBottomThumb);
            ivThumb = new ImageView[4];
            ivThumb[0] = (ImageView) convertView.findViewById(R.id.ivThumb1);
            ivThumb[1] = (ImageView) convertView.findViewById(R.id.ivThumb2);
            ivThumb[2] = (ImageView) convertView.findViewById(R.id.ivThumb3);
            ivThumb[3] = (ImageView) convertView.findViewById(R.id.ivThumb4);
            ivCheckBox = (ImageView) convertView.findViewById(R.id.ivCheckBox);
        }
    }

}