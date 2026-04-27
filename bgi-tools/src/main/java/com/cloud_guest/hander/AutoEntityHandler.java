package com.cloud_guest.hander;

import com.cloud_guest.mp.abs.handler.AbsEntityHandler;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.utils.AuthContextUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * @Author yan
 * @Date 2026/4/27 20:37:42
 * @Description
 */
@Component
public class AutoEntityHandler implements AbsEntityHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        String userId = getUserId();
        AbsEntityHandler.super.insertFill(metaObject);
        this.strictInsertFill(metaObject, BaseEntity.COL_CREATE_BY, () -> userId,String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String userId = getUserId();
        AbsEntityHandler.super.updateFill(metaObject);
        this.strictUpdateFill(metaObject, BaseEntity.COL_UPDATE_BY, () -> userId,String.class);
    }
    protected String getUserId(){
        String usernameNoThrow = AuthContextUtil.getUsernameNoThrow();
        return usernameNoThrow;
    }
}
