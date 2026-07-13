package com.cloud_guest.mp.abs.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloud_guest.mp.pojo.AutoEntity;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.mp.service.AuthUserService;
import org.apache.ibatis.reflection.MetaObject;

import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yan
 * @Date 2024/5/22 0022 17:42
 * @Description
 */
public interface AbsEntityHandler extends MetaObjectHandler {
    /**
     * 获取自动实体列表
     * 该方法创建并返回一个包含自动实体(AutoEntity)的列表
     * 这些实体主要用于记录创建时间和更新时间
     *
     * @return 返回一个包含AutoEntity对象的列表，列表中至少包含创建时间和更新时间的实体
     */
    default List<AutoEntity> getAutoEntityList() {
        // 创建一个新的ArrayList用于存储AutoEntity对象
        List<AutoEntity> list = new ArrayList<>();
        // 获取当前系统时间
        LocalDateTime now = LocalDateTime.now();
        // 添加创建时间实体，字段名为COL_CREATE_TIME，值为当前时间，类型为LocalDateTime，为主键且不可更新
        list.add(new AutoEntity(BaseEntity.COL_CREATE_TIME, now, LocalDateTime.class, true, false));
        // 添加更新时间实体，字段名为COL_UPDATE_TIME，值为当前时间，类型为LocalDateTime，为主键且可更新
        list.add(new AutoEntity(BaseEntity.COL_UPDATE_TIME, now, LocalDateTime.class, true, true));
        // 返回包含创建时间和更新时间实体的列表
        return list;
    }

    /**
     * 自动填充方法，根据插入或更新操作自动填充实体字段值
     *
     * @param metaObject 元数据对象，用于封装对象信息
     * @param entity     自动填充实体，包含填充的相关配置信息
     * @param isInsert   是否为插入操作
     * @param isUpdate   是否为更新操作
     */
    default void autoFill(MetaObject metaObject, AutoEntity entity, boolean isInsert, boolean isUpdate) {
        // 如果是插入操作且实体配置为允许插入填充
        if (isInsert && entity.isInsert()) {
            // 使用驼峰命名作为键进行严格插入填充
            this.strictInsertFill(metaObject, entity.getCamelCaseKey(), () -> entity.getValue(), entity.getType());
            // 使用下划线命名作为键进行严格插入填充
            this.strictInsertFill(metaObject, entity.getUnderlineCaseKey(), () -> entity.getValue(), entity.getType());
        }
        // 如果是更新操作且实体配置为允许更新填充
        if (isUpdate && entity.isUpdate()) {
            // 使用驼峰命名作为键进行严格更新填充
            this.strictUpdateFill(metaObject, entity.getCamelCaseKey(), () -> entity.getValue(), entity.getType());
            // 使用下划线命名作为键进行严格更新填充
            this.strictUpdateFill(metaObject, entity.getUnderlineCaseKey(), () -> entity.getValue(), entity.getType());
        }
    }

    @Override
    default void insertFill(MetaObject metaObject) {
        getAutoEntityList().stream().filter(AutoEntity::isInsert).forEach(entity -> autoFill(metaObject, entity, true, false));
    }

    @Override
    default void updateFill(MetaObject metaObject) {
        getAutoEntityList().stream().filter(AutoEntity::isUpdate).forEach(entity -> autoFill(metaObject, entity, false, true));
    }

}
