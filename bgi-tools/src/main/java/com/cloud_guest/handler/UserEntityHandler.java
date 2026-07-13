package com.cloud_guest.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.mp.abs.handler.AbsEntityHandler;
import com.cloud_guest.mp.pojo.AutoEntity;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.mp.service.AuthUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/7/17 16:58:37
 * @Description
 */
@Slf4j
@Component
public class UserEntityHandler implements AbsEntityHandler {
    /**
     * 获取当前用户的ID
     * 该方法尝试从Spring上下文中获取AuthUserService Bean，并调用其getUserId方法获取用户ID
     * 如果获取失败则返回null，并记录警告日志
     *
     * @return 用户ID字符串，如果获取失败则返回null
     */
    public String getUserId() {
        // 初始化用户ID为null
        String userId = null;
        try {
            // 从Spring工具类中获取AuthUserService Bean实例
            AuthUserService bean = SpringUtil.getBean(AuthUserService.class);
            // 调用AuthUserService的getUserId方法获取用户ID
            userId = bean.getUserId();
        } catch (Exception e) {
            // 捕获所有异常，记录警告日志
            LoggerFactory.getLogger(getClass()).warn("{}", e.getMessage());
        }
        // 返回获取到的用户ID，如果获取失败则返回null
        return userId;
    }
    @Override
    public List<AutoEntity> getAutoEntityList() {
        List<AutoEntity> list = AbsEntityHandler.super.getAutoEntityList();
        String userId = getUserId();
        list.add(new AutoEntity(BaseEntity.COL_CREATE_BY, userId, String.class, true, false));
        list.add(new AutoEntity(BaseEntity.COL_UPDATE_BY, userId, String.class, true, true));
        return list;
    }
}
