package com.cloud_guest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author yan
 * @Date 2026/2/18 0:53:08
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoLeyLineOutcrop {
    // 刷取次数
    @Schema(description = "刷取次数")
    public int count;
    @Schema(description = "国家地区")
    public String country;
    @Schema(description = "地脉花类型 ")
    //地脉花类型
    public String leyLineOutcropType;
    //@Schema(description = "是否开启树脂耗尽模式")
    //@JsonProperty("isResinExhaustionMode")
    //// 是否开启树脂耗尽模式
    //public boolean isResinExhaustionMode;
    //@Schema(description = "[耗尽模式]是否开启取小值模式")
    //// 开启取小值模式
    //public boolean openModeCountMin;
    @Schema(description = "是否使用冒险之证寻找地脉花")
    //是否使用冒险之证寻找地脉花
    public boolean useAdventurerHandbook;
    @Schema(description = "好感队名称")
    //好感队名称
    public String friendshipTeam;
    @Schema(description = "战斗的队伍名称")
    //战斗的队伍名称
    public String team;
    @Schema(description = "战斗超时时间")
    //战斗超时时间
    public int timeout = 120;
    @Schema(description = "是否前往合成台合成浓缩树脂")
    @JsonProperty("isGoToSynthesizer")
    //是否前往合成台合成浓缩树脂
    public boolean isGoToSynthesizer;
    @Schema(description = "是否使用脆弱树脂")
    //是否使用脆弱树脂
    public boolean useFragileResin;
    @Schema(description = "是否使用须臾树脂")
    //是否使用须臾树脂
    public boolean useTransientResin;
    @Schema(description = "通过BGI通知系统发送详细通知")
    @JsonProperty("isNotification")
    //通过BGI通知系统发送详细通知
    public boolean isNotification;
}
