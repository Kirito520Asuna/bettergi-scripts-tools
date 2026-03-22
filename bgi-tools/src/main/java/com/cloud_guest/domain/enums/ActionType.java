package com.cloud_guest.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author yan
 * @Date 2026/3/22 17:37:57
 * @Description
 */
@AllArgsConstructor
@Getter
public enum ActionType {
    send_private_msg("私聊"),send_group_msg("群聊");
     String desc;
}
