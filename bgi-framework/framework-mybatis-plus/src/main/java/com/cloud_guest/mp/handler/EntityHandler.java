package com.cloud_guest.mp.handler;

import com.cloud_guest.mp.abs.handler.AbsEntityHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;


/**
 * @Author yan
 * @Date 2024/5/22 0022 17:40
 * @Description
 */
@Component
@ConditionalOnMissingBean(AbsEntityHandler.class)
public class EntityHandler implements AbsEntityHandler {
}
