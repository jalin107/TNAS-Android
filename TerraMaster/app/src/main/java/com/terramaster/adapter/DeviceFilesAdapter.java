package com.terramaster.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.terramaster.R;
import com.terramaster.dbhelper.DBKeys;
import com.terramaster.dbhelper.DatabaseHelper;
import com.terramaster.dialog.DeviceFileOptionPopup;
import com.terramaster.dialog.RenameDialog;
import com.terramaster.dialog.ShareDialog;
import com.terramaster.dialog.SuccessToast;
import com.terramaster.discover.DeviceData;
import com.terramaster.discover.DeviceService;
import com.terramaster.model.FTPFileItem;
import com.terramaster.model.TaskDetail;
import com.terramaster.service.BackgroundTaskService;
import com.terramaster.task.DeleteFTPFileTask;
import com.terramaster.task.OnSuccessListener;
import com.terramaster.task.RemoveFTPDirectoryTask;
import com.terramaster.utils.AlertUtils;
import com.terramaster.utils.BundleParamsKey;
import com.terramaster.utils.DateUtils;
import com.terramaster.utils.IconUtils;
import com.terramaster.utils.SDCardUtils;
import com.terramaster.utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceFilesAdapter extends BaseAdapter {

    private Context mContext;
    private List<FTPFileItem> items;
    private int selPos = -1;
    private DeviceFileOptionPopup optionPopup;

    private View tvEmtyData;

    // private DisplayImageOptions options;
    // private ImageLoader imageLoader = ImageLoader.getInstance();

    public DeviceFilesAdapter(Context context, List<FTPFileItem> objects, View tvEmtyData) {
        mContext = context;
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

    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
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
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.item_device_file, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final View view = convertView;
        final FTPFileItem fileItem = items.get(position);
        holder.tvName.setText(fileItem.getName());
        if (fileItem.isWritable()) {
            holder.ivRightIc.setVisibility(View.VISIBLE);
            holder.ivRightIc.setImageResource(selPos == position ? R.drawable.arrow_file_up : R.drawable.arrow_file_down);
        } else {
            holder.ivRightIc.setVisibility(View.INVISIBLE);
        }

        if (fileItem.isDirectory()) {
            holder.ivThumb.setImageResource(R.drawable.folder_ic);
            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()));
        } else {
            // holder.ivCheckBox.setSelected(hashMapSelected.containsKey(fileItem.getPath()));
            displayThumb(fileItem.getName(), holder.ivThumb);
            holder.tvSummary.setText(DateUtils.DATE_TIME_FORMAT.format(fileItem.getDate()) + " " + fileItem.getFileSize());
        }

        holder.ivRightIc.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (selPos == position) {
                    selPos = -1;
                    dismissFileOption();
                } else {
                    selPos = position;
                    notifyDataSetChanged();
                    showFileOption(view);
                }

            }
        });

        return convertView;
    }

    private void displayThumb(String name, ImageView imageView) {
        imageView.setImageResource(IconUtils.findIcon(name));
    }

    private void dismissFileOption() {
        if (optionPopup != null) {
            optionPopup.dismiss();
            optionPopup = null;
        }
    }

    private void showFileOption(View anchorView) {
        dismissFileOption();
        optionPopup = new DeviceFileOptionPopup(mContext, getItem(selPos).isDirectory(), new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch (v.getId()) {
//                    case R.id.tvShare:
//                        initFileForShare(getItem(selPos));
//                        break;
                    case R.id.tvDownload:
                        startDownloadFile(getItem(selPos));
                        dismissFileOption();
                        break;
                    case R.id.tvRename:
                    case R.id.tvRenameDirectory:
                        new RenameDialog(mContext, getItem(selPos), new OnSuccessListener() {

                            @Override
                            public void onSuccess() {
                                // TODO Auto-generated method stub
                                dismissFileOption();
                                notifyDataSetChanged();
                            }
                        }).show();
                        break;
                    case R.id.tvDelete:
                        AlertUtils.showConfirmAlert(mContext, mContext.getString(R.string.confirm), mContext.getString(R.string.confirm_delete_file), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                deleteFile(getItem(selPos));
                            }
                        });
                        break;
                    case R.id.tvDeleteDirectory:
                        AlertUtils.showConfirmAlert(mContext, mContext.getString(R.string.confirm), mContext.getString(R.string.confirm_delete_directory), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                removeDirectory(getItem(selPos));
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        }, new OnDismissListener() {

            @Override
            public void onDismiss() {
                // TODO Auto-generated method stub
                dismissFileOption();
                selPos = -1;
                notifyDataSetChanged();
            }
        });
        optionPopup.show(anchorView);

    }

    protected void initFileForShare(final FTPFileItem item) {
        // TODO Auto-generated method stub
        ArrayList<String> list = new ArrayList<>();
        list.add(item.getPath());
        DeviceData deviceData=SharedPrefUtils.getInstance(mContext).getDeviceData();
        DeviceService deviceServiceSYC=deviceData==null?null:deviceData.getDeviceService("sys");
        ShareDialog.startShareLinks(mContext,""+(deviceServiceSYC==null?"":deviceServiceSYC.port), list);
    }

    private void deleteFile(final FTPFileItem item) {
        new DeleteFTPFileTask(mContext, item, new OnSuccessListener() {
            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                dismissFileOption();
                items.remove(item);
                notifyDataSetChanged();
            }
        }).start();
    }

    private void removeDirectory(final FTPFileItem item) {
        new RemoveFTPDirectoryTask(mContext, item, new OnSuccessListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                dismissFileOption();
                items.remove(item);
                notifyDataSetChanged();
            }
        }).start();
    }

    private void startDownloadFile(final FTPFileItem item) {
        DeviceData deviceData = SharedPrefUtils.getInstance(mContext).getDeviceData();

        TaskDetail taskDetail = new TaskDetail();
        taskDetail.setTaskFileSize(item.getSize());
        taskDetail.setFrom(item.getPath());
        Date date = new Date();
        taskDetail.setLastUpdated(date);
        taskDetail.setCreatedAt(date);
        taskDetail.setTaskType(DBKeys.TASK_DOWNLOADING);
        taskDetail.setTaskStatus(DBKeys.STATUS_WAITING);
        taskDetail.setMacAddress(deviceData.getMac());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(mContext);
        long id = dbHelper.insertTask(taskDetail);
        taskDetail.setRowId(id);

        Intent intent = new Intent(Intent.ACTION_SYNC, null, mContext, BackgroundTaskService.class);
        intent.putExtra(BundleParamsKey.OBJECT_KEY, taskDetail);
        intent.putExtra(BundleParamsKey.SERVICE_ACTION_KEY, BundleParamsKey.START_ACTION);
        mContext.startService(intent);

        new SuccessToast(mContext,
                mContext.getString(R.string.success_download),
                mContext.getString(R.string.download_tip)+"/"+ SDCardUtils.getDownloadingPath().getName()).show();
    }

    private class ViewHolder {
        public TextView tvName, tvSummary;
        public ImageView ivThumb, ivRightIc;

        public ViewHolder(View convertView) {
            // TODO Auto-generated constructor stub
            tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);

            ivThumb = (ImageView) convertView.findViewById(R.id.ivThumb);
            ivRightIc = (ImageView) convertView.findViewById(R.id.ivRightIc);
        }
    }
}