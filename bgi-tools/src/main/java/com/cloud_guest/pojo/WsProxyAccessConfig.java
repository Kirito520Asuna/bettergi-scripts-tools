package com.cloud_guest.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.domain.WsProxyAccess;
import com.cloud_guest.domain.enums.ActionType;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.cloud_guest.pojo.AutoPlanConfig.COL_ID;

/**
 * @Author yan
 * @Date 2026/4/28 13:36:48
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(WsProxyAccessConfig.TABLE_NAME)
public class WsProxyAccessConfig extends BaseEntity {
    @TableId(value = COL_UID)
    private String uid;
    @Schema(description = "操作类型 私聊/群聊")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = COL_ACTION)
    private ActionType action;
    @Schema(description = "ws地址")
    @TableField(COL_WS_URL)
    private String url;
    @Schema(description = "ws代理地址")
    @TableField(COL_PROXY_URL)
    private String proxyUrl;
    @Schema(description = "授权token")
    @TableField(COL_TOKEN)
    private String token;
    @Schema(description = "at列表")
    @TableField(COL_AT_LIST)
    private String atList;
    @Schema(description = "用户id")
    @TableField(COL_USER_ID)
    private String userId;
    @Schema(description = "群id")
    @TableField(COL_GROUP_ID)
    private String groupId;

    public static final String TABLE_NAME = "ws_proxy_access_config";
    public static final String COL_UID = "uid";
    public static final String COL_ACTION = "action";
    public static final String COL_WS_URL = "ws_url";
    public static final String COL_PROXY_URL = "proxy_url";
    public static final String COL_TOKEN = "ws_token";
    public static final String COL_AT_LIST = "at_list";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_GROUP_ID = "group_id";
    public WsProxyAccess toWsProxyAccess(){
        return new WsProxyAccess(action,url,proxyUrl,token,atList,userId,groupId,uid);
    }
}
