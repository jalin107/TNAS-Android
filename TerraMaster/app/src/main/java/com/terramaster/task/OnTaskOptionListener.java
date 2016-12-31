package com.terramaster.task;

import com.terramaster.model.TaskDetail;

public interface OnTaskOptionListener {
    public void onSelect(TaskDetail taskDetail, int itemId);
}
