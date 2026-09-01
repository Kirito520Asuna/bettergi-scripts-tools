package com.cloud_guest.task.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/8/26 17:53:47
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Task {
    //任务执行方法
    public abstract void execute();
    //任务名称
    private String name;
    //任务key
    private String key;
    //任务参数
    private String json;
}
