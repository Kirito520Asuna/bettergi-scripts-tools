package com.cloud_guest.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Author yan
 * @Date 2026/9/2 11:09:03
 * @Description
 */
public interface IServicePlus<T> extends IService<T> {
    default LambdaQueryWrapper<T> lambdaQueryWrapper() {
        return Wrappers.lambdaQuery(this.getEntityClass());
    }

    default LambdaUpdateWrapper<T> lambdaUpdateWrapper() {
        return Wrappers.lambdaUpdate(this.getEntityClass());
    }
}
