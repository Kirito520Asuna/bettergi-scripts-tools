package com.cloud_guest.entitys.common.auto_plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/6/21 4:34:09
 * @Description
 */
@Data @Schema(description = "Boss参数")
@NoArgsConstructor
@AllArgsConstructor
public class AutoBoss {
    /** 需要讨伐的 Boss 名称。*/
    @Schema(description = "需要讨伐的 Boss 名称。")
    private String bossName;
    /** UI 中选择的战斗策略名称；当没有自定义策略路径时会同步更新 。*/
    @Schema(description = "UI 中选择的战斗策略名称；当没有自定义策略路径时会同步更新。")
    private String strategyName;
    /** 实际用于解析自动战斗脚本的路径。JS 可直接设置该路径来覆盖 UI 选择。*/
    @Schema(description = "实际用于解析自动战斗脚本的路径。JS 可直接设置该路径来覆盖 UI 选择。")
    private String combatStrategyPath;
    /** 讨伐前需要切换到的队伍名称；为空时保持当前队伍。*/
    @Schema(description = "讨伐前需要切换到的队伍名称；为空时保持当前队伍。")
    private String teamName;
    /** 是否启用“指定讨伐次数”模式；关闭时刷取至原粹树脂耗尽。*/
    @Schema(description = "是否启用“指定讨伐次数”模式；关闭时刷取至原粹树脂耗尽。")
    private boolean specifyRunCount = true;
    /** 指定模式下成功领取奖励的目标次数。*/
    @Schema(description = "指定模式下成功领取奖励的目标次数。")
    private int runCount = 1;
    /** 指定讨伐次数模式下，原粹树脂不足时是否允许使用须臾树脂补充。*/
    @Schema(description = "指定讨伐次数模式下，原粹树脂不足时是否允许使用须臾树脂补充。")
    private boolean useTransientResin;
    /** 指定讨伐次数模式下，原粹树脂不足时是否允许使用脆弱树脂补充。*/
    @Schema(description = "指定讨伐次数模式下，原粹树脂不足时是否允许使用脆弱树脂补充。")
    private boolean useFragileResin;
    /** 检测到角色死亡后，回神像恢复并重试当前首领讨伐的最大次数。*/
    @Schema(description = "检测到角色死亡后，回神像恢复并重试当前首领讨伐的最大次数。")
    private int reviveRetryCount = 3;
    /** 每轮领奖后是否先返回七天神像，再重新前往 Boss。*/
    @Schema(description = "每轮领奖后是否先返回七天神像，再重新前往 Boss。")
    private boolean returnToStatueAfterEachRound;
    /** 是否启用奖励名称识别。默认关闭。*/
    @Schema(description = "是否启用奖励名称识别。默认关闭。")
    private boolean rewardRecognitionEnabled;
}
