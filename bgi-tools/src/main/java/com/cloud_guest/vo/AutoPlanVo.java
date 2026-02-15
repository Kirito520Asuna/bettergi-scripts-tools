package com.cloud_guest.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/9 14:19:05
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutoPlanVo implements Serializable {
    private static final long serialVersionUID = 8997301368952007161L;
    @Schema(description = "执行顺序")
    @JsonProperty("order")
    private Integer order;
    @Schema(description = "执行日期")
    @JsonProperty("days")
    private List<Integer> days;
    @JsonProperty("dayName")
    private String dayName;
    //@Schema(description = "执行类型(展示用)")
    @JsonProperty("selectedType")
    private String selectedType;
    @Schema(description = "执行类型(秘境|地脉)")
    @JsonProperty("runType")
    private String runType;
    @Schema(description = "秘境参数")
    @JsonProperty("autoFight")
    private AutoFightDTO autoFight;
    @Schema(description = "地脉参数")
    @JsonProperty("autoLeyLineOutcrop")
    private AutoLeyLineOutcrop autoLeyLineOutcrop;

    @NoArgsConstructor
    @Data
    public static class AutoLeyLineOutcrop {
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

    @NoArgsConstructor
    @Data
    public static class AutoFightDTO {
        //@Schema(description = "国家地区")
        //@JsonProperty("country")
        //public String country;
        @Schema(description = "秘境名称")
        @JsonProperty("domainName")
        private String domainName;
        @Schema(description = "限时/周日 顺序1-3")
        @JsonProperty("sundaySelectedValue")
        private Integer sundaySelectedValue;
        @Schema(description = "队伍")
        @JsonProperty("partyName")
        private String partyName;
        @Schema(description = "秘境轮数")
        @JsonProperty("DomainRoundNum")
        private Integer domainRoundNum;
        @Schema(description = "树脂启用顺序")
        @JsonProperty("physical")
        private List<PhysicalDTO> physical;
    }

    @NoArgsConstructor
    @Data
    public static class PhysicalDTO {
        @JsonProperty("order")
        private Integer order;
        @JsonProperty("name")
        private String name;
        @JsonProperty("open")
        private boolean open;
    }
}
