package com.cloud_guest.mp.abs.handler;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cloud_guest.mp.pojo.BaseEntity;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @Author yan
 * @Date 2024/5/22 0022 17:42
 * @Description
 */
public interface AbsEntityHandler extends MetaObjectHandler {
    @Override
    default void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, BaseEntity.COL_CREATE_TIME, () -> LocalDateTime.now(), LocalDateTime.class);
    }

    @Override
    default void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject,  BaseEntity.COL_UPDATE_TIME, () -> LocalDateTime.now(), LocalDateTime.class);
    }

}
