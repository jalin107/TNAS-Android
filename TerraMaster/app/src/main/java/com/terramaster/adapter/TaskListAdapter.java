package com.terramaster.adapter;

import android.content.Context;
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
import com.terramaster.dialog.TaskOptionPopup;
import com.terramaster.model.TaskDetail;
import com.terramaster.task.OnTaskOptionListener;
import com.terramaster.utils.IconUtils;
import com.terramaster.utils.StringUtils;

import java.util.List;

public class TaskListAdapter extends BaseAdapter {

    private Context mContext;
    private List<TaskDetail> items;
    private int selPos = -1;
    private TaskOptionPopup optionPopup;

    private View tvEmtyData;
    private OnTaskOptionListener onOptionClickListener;

    // private DisplayImageOptions options;
    // private ImageLoader imageLoader = ImageLoader.getInstance();

    public TaskListAdapter(Context context, List<TaskDetail> objects, View tvEmtyData, OnTaskOptionListener onOptionClickListener) {
        mContext = context;
        items = objects;
        this.tvEmtyData = tvEmtyData;
        this.onOptionClickListener = onOptionClickListener;
        displayEmptyMessage();
        // options = new
        // DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_launcher).showImageForEmptyUri(R.drawable.ic_launcher).showImageOnFail(R.drawable.ic_launcher).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
        // .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public TaskDetail getItem(int i) {
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

    public void notifyDataSetChanged(List<TaskDetail> taskList) {
        // TODO Auto-generated method stub
        this.items = taskList;
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
            convertView = vi.inflate(R.layout.item_task_list, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final View view = convertView;
        final TaskDetail taskDetail = items.get(position);
        String name = StringUtils.getNameFromPath(taskDetail.getFrom());
        holder.tvName.setText(name);

        displayThumb(name, holder.ivThumb);

        holder.tvSummary.setText(getTaskStatus(taskDetail.getTaskStatus(), taskDetail.getTaskType(), taskDetail.getProcess()));
        holder.ivTaskIndicator.setImageResource(getTaskIc(taskDetail.getTaskType()));
        holder.ivRightIc.setImageResource(selPos == position ? R.drawable.arrow_file_up : R.drawable.arrow_file_down);
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

    public int getTaskIc(int taskType) {
        if (taskType == DBKeys.TASK_DOWNLOADING) {
            return R.drawable.ic_arrow_down;
        } else if (taskType == DBKeys.TASK_UPLOADING) {
            return R.drawable.ic_arrow_up;
        }
        return 0;
    }

    public CharSequence getTaskStatus(int taskStatus, int taskType, long taskProcess) {
        // TODO Auto-generated method stub
        if (taskStatus == DBKeys.STATUS_WAITING) {
            return mContext.getString(R.string.waiting);
        } else if (taskStatus == DBKeys.STATUS_ERROR) {
            return mContext.getString(R.string.error);
        } else if (taskStatus == DBKeys.STATUS_PAUSED) {
            return mContext.getString(R.string.paused);
        } else if (taskStatus == DBKeys.STATUS_COMPLETE) {
            return mContext.getString(R.string.completed);
        } else if (taskStatus == DBKeys.STATUS_RUNNING) {
            if (taskType == DBKeys.TASK_DOWNLOADING) {
                return mContext.getString(R.string.downloading)+ taskProcess + "%";
            } else if (taskType == DBKeys.TASK_UPLOADING) {
                return mContext.getString(R.string.uploading);
            }
        }
        return "";
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
        optionPopup = new TaskOptionPopup(mContext, getItem(selPos).getTaskStatus(), new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                TaskDetail taskDetail = getItem(selPos);
                if (onOptionClickListener != null) {
                    onOptionClickListener.onSelect(taskDetail, v.getId());
                }
                dismissFileOption();
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

    private class ViewHolder {
        public TextView tvName, tvSummary;
        public ImageView ivThumb, ivRightIc, ivTaskIndicator;

        public ViewHolder(View convertView) {
            // TODO Auto-generated constructor stub
            tvName = (TextView) convertView.findViewById(R.id.tvName);
            tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);

            ivThumb = (ImageView) convertView.findViewById(R.id.ivThumb);
            ivRightIc = (ImageView) convertView.findViewById(R.id.ivRightIc);
            ivTaskIndicator = (ImageView) convertView.findViewById(R.id.ivTaskIndicator);
        }
    }
}