package com.cloud_guest.domain;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author yan
 * @Date 2026/3/12 19:17:30
 * @Description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AutoPlan {
    @NotNull
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
    @NotBlank
    @JsonProperty("runType")
    private String runType;
    @Schema(description = "是否启用")
    @JsonProperty("enable")
    private Boolean enable = Boolean.TRUE;
    @Schema(description = "秘境参数")
    @JsonProperty("autoFight")
    private AutoFight autoFight;
    @Schema(description = "地脉参数")
    @JsonProperty("autoLeyLineOutcrop")
    private AutoLeyLineOutcrop autoLeyLineOutcrop;
    @Schema(description = "幽境参数")
    @JsonProperty("autoStygianOnslaught")
    private AutoStygianOnslaught autoStygianOnslaught;

    //public static void main(String[] args) {
    //    String s = "[{\"order\":999,\"days\":[],\"runType\":\"地脉\",\"autoLeyLineOutcrop\":{\"count\":1,\"country\":\"挪德卡莱\",\"leyLineOutcropType\":\"藏金之花\",\"useAdventurerHandbook\":true,\"friendshipTeam\":\"\",\"team\":\"速通\",\"timeout\":120,\"isGoToSynthesizer\":false,\"useFragileResin\":false,\"useTransientResin\":false,\"isNotification\":false}},{\"order\":998,\"days\":[],\"runType\":\"地脉\",\"autoLeyLineOutcrop\":{\"count\":1,\"country\":\"挪德卡莱\",\"leyLineOutcropType\":\"启示之花\",\"useAdventurerHandbook\":true,\"friendshipTeam\":\"\",\"team\":\"速通\",\"timeout\":120,\"isGoToSynthesizer\":false,\"useFragileResin\":false,\"useTransientResin\":false,\"isNotification\":false}},{\"order\":997,\"days\":[],\"runType\":\"幽境\",\"autoStygianOnslaught\":{\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":true,\"count\":1},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":1},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":1},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":1}],\"specifyResinUse\":false,\"bossNum\":2,\"fightTeamName\":\"速通\"}},{\"order\":99,\"days\":[0,2,5],\"dayName\":\"已选中:周日, 周二, 周五\",\"runType\":\"秘境\",\"selectedType\":\"武器\",\"autoFight\":{\"sundaySelectedName\":\"长夜燧火的烈辉\",\"domainName\":\"失落的月庭\",\"sundaySelectedValue\":2,\"partyName\":\"速通\",\"domainRoundNum\":2,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}},{\"order\":99,\"days\":[0,3,6],\"dayName\":\"已选中:周日, 周三, 周六\",\"runType\":\"秘境\",\"selectedType\":\"武器\",\"autoFight\":{\"sundaySelectedName\":\"凛风奔狼的怀乡\",\"domainName\":\"塞西莉亚苗圃\",\"sundaySelectedValue\":3,\"partyName\":\"速通\",\"domainRoundNum\":2,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}},{\"order\":98,\"days\":[0,2,5],\"dayName\":\"已选中:周日, 周二, 周五\",\"runType\":\"秘境\",\"selectedType\":\"武器\",\"autoFight\":{\"sundaySelectedName\":\"凛风奔狼的怀乡\",\"domainName\":\"塞西莉亚苗圃\",\"sundaySelectedValue\":2,\"partyName\":\"速通\",\"domainRoundNum\":2,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}},{\"order\":1,\"days\":[],\"runType\":\"秘境\",\"selectedType\":\"圣遗物\",\"autoFight\":{\"sundaySelectedName\":null,\"domainName\":\"月童的库藏\",\"sundaySelectedValue\":null,\"partyName\":\"速通\",\"domainRoundNum\":1,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}},{\"order\":1,\"days\":[],\"runType\":\"秘境\",\"selectedType\":\"圣遗物\",\"autoFight\":{\"sundaySelectedName\":null,\"domainName\":\"霜凝的机枢\",\"sundaySelectedValue\":null,\"partyName\":\"速通\",\"domainRoundNum\":1,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}},{\"order\":1,\"days\":[],\"runType\":\"秘境\",\"selectedType\":\"圣遗物\",\"autoFight\":{\"sundaySelectedName\":null,\"domainName\":\"虹灵的净土\",\"sundaySelectedValue\":null,\"partyName\":\"速通\",\"domainRoundNum\":1,\"physical\":[{\"order\":0,\"name\":\"浓缩树脂\",\"open\":false,\"count\":0},{\"order\":1,\"name\":\"原粹树脂\",\"open\":true,\"count\":0},{\"order\":2,\"name\":\"须臾树脂\",\"open\":false,\"count\":0},{\"order\":3,\"name\":\"脆弱树脂\",\"open\":false,\"count\":0}]}}]";
    //    List<AutoPlan> list = JSONUtil.toList(s, AutoPlan.class);
    //    System.err.println( list);
    //}
}
