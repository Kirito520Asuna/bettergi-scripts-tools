package com.cloud_guest.service;


import com.cloud_guest.domain.SysJob;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.List;

public interface SysJobService {
    /**
     * 获取Scheduler
     * @return
     */
    default Scheduler getScheduler() {
        return cn.hutool.extra.spring.SpringUtil.getBean(Scheduler.class);
    }

    boolean removeById(Long jobId);

    /**
     * 获取quartz调度器的计划任务
     *
     * @param job 调度信息
     * @return 调度任务集合
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * 暂停任务
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean pauseJob(SysJob job) ;

    /**
     * 恢复任务
     *
     * @param job
     * @return
     * @
     */

    boolean resumeJob(SysJob job) ;

    /**
     * 删除任务后，所对应的trigger也将被删除
     *
     * @param job 调度信息
     * @return 结果
     */
    boolean deleteJob(SysJob job) ;

    /**
     * 批量删除调度信息
     * @param ids
     * @
     */
    void deleteJobByIds(List<Long> ids) ;

    /**
     * 调度任务 状态修改
     * @param job
     * @return
     * @
     */
    boolean changeStatus(SysJob job) ;

    /**
     * 立即执行任务
     *
     * @param job 调度信息
     */
    boolean run(SysJob job) ;

    /**
     * 新增任务
     *
     * @param job 调度信息 调度信息
     */
    boolean insertJob(SysJob job) ;

    /**
     * 更新任务的时间表达式
     *
     * @param job 调度信息
     */
    boolean updateJob(SysJob job) ;

    SysJob getById(Long id);

    boolean save(SysJob job);

    boolean updateById(SysJob job);

    /**
     * 校验cron表达式是否有效
     *
     * @param cronExpression 表达式
     * @return 结果
     */
    boolean checkCronExpressionIsValid(String cronExpression);
}
