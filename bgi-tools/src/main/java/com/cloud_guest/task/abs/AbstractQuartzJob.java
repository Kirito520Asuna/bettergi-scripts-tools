package com.cloud_guest.task.abs;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author yan
 * @Date 2025/4/9 18:37:42
 * @Description
 */
@Slf4j
public abstract class AbstractQuartzJob implements Job {
    /**
     * 线程本地变量
     */
    private static ThreadLocal<LocalDateTime> threadLocal = new ThreadLocal<>();


    @Override
    public void execute(JobExecutionContext context) {
        Map<String,Object> jobMap = new LinkedHashMap<>();
        BeanUtil.copyProperties(context.getMergedJobDataMap().get("TASK_PROPERTIES"), jobMap);
        try {
            before(context, jobMap);
            if (jobMap != null) {
                doExecute(context, jobMap);
            }
            after(context, jobMap, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：{}", e);
            after(context, jobMap, e);
        }
    }


    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param jobMap  系统计划任务
     */
    protected void before(JobExecutionContext context, Map<String, Object> jobMap) {
        threadLocal.set(LocalDateTime.now());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param jobMap  系统计划任务
     */
    protected void after(JobExecutionContext context, Map<String, Object> jobMap, Exception e) {
        LocalDateTime startTime = threadLocal.get();
        threadLocal.remove();
        jobMap.put("startTime", startTime);
        LocalDateTime stopTime = LocalDateTime.now();
        jobMap.put("stopTime", stopTime);
        long runTime = stopTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        jobMap.put("jobMessage", jobMap.get("jobName") + " 总共耗时：" + runTime + "毫秒");
        if (e != null) {
            jobMap.put("status", "1");
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            String errorMsg = sw.toString();
            errorMsg = StrUtil.sub(errorMsg, 0, 2000);
            jobMap.put("exceptionInfo", errorMsg);

        } else {
            jobMap.put("status", "0");
        }
        //SpringUtil.getBean(JobService.class).addJobMap(jobMap);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param jobMap  系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, Map<String, Object> jobMap) throws Exception;
}
