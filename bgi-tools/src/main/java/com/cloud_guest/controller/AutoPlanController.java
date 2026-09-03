package com.cloud_guest.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.aop.security.Token;
import com.cloud_guest.entitys.ClassConvert;
import com.cloud_guest.entitys.Valid;
import com.cloud_guest.entitys.common.auto_plan.*;
import com.cloud_guest.entitys.common.enums.AutoPlanType;
import com.cloud_guest.entitys.domain.UidInfo;
import com.cloud_guest.entitys.dto.AutoPlanDTO;
import com.cloud_guest.entitys.dto.AutoPlanJsonDto;
import com.cloud_guest.entitys.pojo.AutoPlanConfig;
import com.cloud_guest.entitys.pojo.AutoPlanUidGlobalConfig;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
import com.cloud_guest.entitys.records.UidGlobalInfo;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.result.Result;
import com.cloud_guest.service.AutoPlanService;
import com.cloud_guest.service.AutoPlanUidGlobalService;
import com.cloud_guest.service.UidService;
import com.cloud_guest.utils.EnumUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.view.BasicJsonView;
import com.cloud_guest.entitys.vo.AutoPlanVo;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cloud_guest.result.Result.ok;

/**
 * @Author yan
 * @Date 2026/2/8 15:49:23
 * @Description
 */
@Slf4j
@Tag(name = "自动体力计划服务")
@RestController
@RequestMapping(value = {"/auto/plan/", "/api/auto/plan/", "/jwt/auto/plan/"})
public class AutoPlanController {

    static {
        // 注册转换
        ClassConvert.register(
                UidGlobalInfo.class, AutoPlanUidGlobalConfig.class,
                info -> {
                    if (ObjectUtils.isEmpty(info)) {
                        throw new GlobalException("全局UID自动计划配置不能为空");
                    }
                    String uid = info.uid();
                    if (StrUtil.isBlankIfStr(uid)) {
                        throw new GlobalException("UID不能为空");
                    }
                    Boolean cultivate = Boolean.TRUE.equals(info.cultivate());
                    return new AutoPlanUidGlobalConfig(uid, cultivate);
                },
                info -> new UidGlobalInfo(info.getUid(), info.getCultivate())
        );
        // 注册转换
        ClassConvert.register(AutoPlan.class,AutoPlanConfig.class,
        info->{
            String id = info.getId();
            Boolean cultivate = info.getCultivate();
            
            AutoPlanConfig planConfig = new AutoPlanConfig();
            planConfig.setId(id!=null?Long.parseLong(id):null);
            planConfig.setCultivate(cultivate);

            String autoDomain = JSONUtil.toJsonStr(info.getAutoDomain());
            String autoLeyLineOutcrop = JSONUtil.toJsonStr(info.getAutoLeyLineOutcrop());
            String autoStygianOnslaught = JSONUtil.toJsonStr(info.getAutoStygianOnslaught());
            String autoBoss = JSONUtil.toJsonStr(info.getAutoBoss());

            //todo: 后续版本需要移除的字段-start
            planConfig.setAutoFight(autoDomain);
            planConfig.setAutoLeyLineOutcrop(autoLeyLineOutcrop);
            planConfig.setAutoStygianOnslaught(autoStygianOnslaught);
            planConfig.setAutoBoss(autoBoss);
            //todo: 后续版本需要移除的字段-end

            String json =  CollUtil.newArrayList(autoDomain, autoLeyLineOutcrop, autoStygianOnslaught, autoBoss)
                    .stream().filter(StrUtil::isNotBlank).findFirst().orElse(null);  // 安全兜底，避免 NoSuchElementException
            planConfig.setJson(json);

            List<Integer> list = new ArrayList<>();
            List<Integer> days = info.getDays();
            if (CollUtil.isNotEmpty(days)) {
                list.addAll(days);
            }
            planConfig.setDays(list.stream().map(String::valueOf).collect(Collectors.joining(",")));

            String dayName = info.getDayName();
            Boolean enable = info.getEnable();
            Boolean record = info.getRecord();
            Integer order = info.getOrder();
            String runType = info.getRunType();
            String selectedType = info.getSelectedType();

            planConfig.setDayName(dayName);
            planConfig.setEnable(enable);
            planConfig.setRecord(record);
            planConfig.setOrderSort(order);
            planConfig.setRunType(runType);
            planConfig.setSelectedType(selectedType);
            return planConfig;
        });
        ClassConvert.register(AutoPlanConfig.class,AutoPlanVo.class,
                info->{
                    Long id = info.getId();
                    String runType = info.getRunType();
                    Boolean cultivate = info.getCultivate();
                    String selectedType = info.getSelectedType();
                    Boolean enable = info.getEnable();
                    Boolean record = info.getRecord();
                    Integer orderSort = info.getOrderSort();
                    String days = info.getDays();
                    String dayName = info.getDayName();
                    String json = info.getJson();
                    String autoFight = info.getAutoFight();
                    String autoLeyLineOutcrop = info.getAutoLeyLineOutcrop();
                    String autoStygianOnslaught = info.getAutoStygianOnslaught();
                    String autoBoss = info.getAutoBoss();

                    AutoPlanVo autoPlanVo = new AutoPlanVo();
                    autoPlanVo
                            .setId(id != null ? String.valueOf(id) : null)
                            .setRunType(runType)
                            .setCultivate(cultivate)
                            .setSelectedType(selectedType)
                            .setEnable(enable)
                            .setRecord(record)
                            .setDays(StrUtil.isBlank(days) ? new ArrayList<>() : Arrays.stream(days.split(",")).map(Integer::valueOf).toList())
                            .setDayName(dayName)
                            .setOrder(orderSort)
                    ;

                    AutoPlanType planType = EnumUtils.getEnumByPrivateFieldName(AutoPlanType.class, runType, "key");

                    switch (planType) {
                        case DOMAIN:
                            autoPlanVo.setAutoDomain(info.parsePlanConfig(json, autoFight, AutoDomain.class));
                            break;
                        case LEY_LINE_OUTCROP:
                            autoPlanVo.setAutoLeyLineOutcrop(info.parsePlanConfig(json, autoLeyLineOutcrop, AutoLeyLineOutcrop.class));
                            break;
                        case BOSS:
                            autoPlanVo.setAutoBoss(info.parsePlanConfig(json, autoBoss, AutoBoss.class));
                            break;
                        case STYGIAN_ONSLAUGHT:
                            autoPlanVo.setAutoStygianOnslaught(info.parsePlanConfig(json, autoStygianOnslaught, AutoStygianOnslaught.class));
                            break;
                        default:
                            break;
                    }

                    return autoPlanVo;
                },
                info->{
                    String id = info.getId();
                    Boolean cultivate = info.getCultivate();

                    AutoPlanConfig planConfig = new AutoPlanConfig();
                    planConfig.setId(id!=null?Long.parseLong(id):null);
                    planConfig.setCultivate(cultivate);

                    String autoDomain = JSONUtil.toJsonStr(info.getAutoDomain());
                    String autoLeyLineOutcrop = JSONUtil.toJsonStr(info.getAutoLeyLineOutcrop());
                    String autoStygianOnslaught = JSONUtil.toJsonStr(info.getAutoStygianOnslaught());
                    String autoBoss = JSONUtil.toJsonStr(info.getAutoBoss());

                    //todo: 后续版本需要移除的字段-start
                    planConfig.setAutoFight(autoDomain);
                    planConfig.setAutoLeyLineOutcrop(autoLeyLineOutcrop);
                    planConfig.setAutoStygianOnslaught(autoStygianOnslaught);
                    planConfig.setAutoBoss(autoBoss);
                    //todo: 后续版本需要移除的字段-end

                    String json =  CollUtil.newArrayList(autoDomain, autoLeyLineOutcrop, autoStygianOnslaught, autoBoss)
                            .stream().filter(StrUtil::isNotBlank).findFirst().orElse(null);  // 安全兜底，避免 NoSuchElementException
                    planConfig.setJson(json);

                    List<Integer> list = new ArrayList<>();
                    List<Integer> days = info.getDays();
                    if (CollUtil.isNotEmpty(days)) {
                        list.addAll(days);
                    }
                    planConfig.setDays(list.stream().map(String::valueOf).collect(Collectors.joining(",")));

                    String dayName = info.getDayName();
                    Boolean enable = info.getEnable();
                    Boolean record = info.getRecord();
                    Integer order = info.getOrder();
                    String runType = info.getRunType();
                    String selectedType = info.getSelectedType();

                    planConfig.setDayName(dayName);
                    planConfig.setEnable(enable);
                    planConfig.setRecord(record);
                    planConfig.setOrderSort(order);
                    planConfig.setRunType(runType);
                    planConfig.setSelectedType(selectedType);
                    return planConfig;
                }
                );
        // 验证
        Valid.register(AutoPlanDTO.class, info -> {
            List<AutoPlanType> planTypes = EnumUtils.getAllEnums(AutoPlanType.class);
            for (AutoPlan autoPlan : info.getAutoPlanList()) {
                AutoPlanType planType = EnumUtils.getEnumByPrivateFieldName(AutoPlanType.class, autoPlan.getRunType(), "key");
                if (planType == null) {
                    throw new GlobalException("runType参数错误");
                }

                switch (planType) {
                    case DOMAIN:
                        //秘境效益
                        AutoDomain autoDomain = autoPlan.getAutoDomain();
                        String domainName = autoDomain.getDomainName();
                        if (StrUtil.isBlank(domainName)) {
                            throw new GlobalException("秘境名称不能为空");
                        }
                        break;
                    case LEY_LINE_OUTCROP:
                        List<String> leyLineOutcropTypes = Arrays.asList("启示之花", "藏金之花");
                        //地脉效益
                        AutoLeyLineOutcrop autoLeyLineOutcrop = autoPlan.getAutoLeyLineOutcrop();
                        String country = autoLeyLineOutcrop.getCountry();
                        String leyLineOutcropType = autoLeyLineOutcrop.getLeyLineOutcropType();
                        if (!leyLineOutcropTypes.contains(leyLineOutcropType)) {
                            throw new GlobalException("地脉类型错误,支持类型:" + leyLineOutcropTypes.stream().collect(Collectors.joining(",")));
                        }
                        if (StrUtil.isBlank(country)) {
                            throw new GlobalException("国家地区不能为空");
                        }
                        break;
                    case BOSS:
                        break;
                    case STYGIAN_ONSLAUGHT:
                        break;
                    default:
                        String runTypesStr = planTypes.stream().map(AutoPlanType::getKey).collect(Collectors.joining(","));
                        throw new GlobalException("runType参数错误,支持类型:" + runTypesStr);
                }

            }
        });
    }

    @Resource
    private AutoPlanUidGlobalService uidGlobalService;
    @Resource
    private UidService uidService;

    @Resource
    private AutoPlanService autoPlanService;

    @SysLog(result = false)
    @Operation(summary = "查询全部国家JSON")
    @GetMapping("country/json/all")
    public Result<List<String>> infoCountryAll() {
        List<String> list = autoPlanService.findCountryAll();
        return ok(list);
    }

    @PostMapping("country/json/all")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储全部国家JSON")
    public Result<String> saveCountryAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                         @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                         @RequestBody AutoPlanJsonDto dto) {
        autoPlanService.saveCountryAll(dto.getJson());

        String source = dto.getSource();
        if (ObjectUtils.equals(source, "WEB_API")) {
            autoPlanService.saveCountryAll(dto.getJson());
        } else if (ObjectUtils.equals(source, "JS_API")) {
            autoPlanService.saveCountryAllByAdd(dto.getJson());
        }

        return ok();
    }

    @PostMapping("domain/json/all")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储基础全部JSON")
    public Result<String> saveDomainAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                        @RequestBody AutoPlanJsonDto dto) {
        String source = dto.getSource();
        if (ObjectUtils.equals(source, "WEB_API")) {
            autoPlanService.saveDomainAll(dto.getJson());
        } else if (ObjectUtils.equals(source, "JS_API")) {
            autoPlanService.saveDomainAllByAdd(dto.getJson());
        }
        return ok();
    }

    @PostMapping("boss/json/all")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储BOSS基础全部JSON")
    public Result<String> saveBossAll(@JsonView(value = BasicJsonView.AutoPlanDomainALLView.class)
                                      @Validated(value = BasicJsonView.AutoPlanDomainALLView.class)
                                      @RequestBody AutoPlanJsonDto dto) {
        String source = dto.getSource();
        if (ObjectUtils.equals(source, "WEB_API")) {
            autoPlanService.saveBossAll(dto.getJson());
        } else if (ObjectUtils.equals(source, "JS_API")) {
            autoPlanService.saveBossAllByAdd(dto.getJson());
        }
        return ok();
    }

    @SysLog(result = false)
    @Operation(summary = "查询基础全部JSON")
    @GetMapping("domain/json/all")
    public Result<List<Map<String, Object>>> infoDomainAll() {
        List<Map<String, Object>> list = autoPlanService.findDomainAll();
        return ok(list);
    }

    @SysLog(result = false)
    @Operation(summary = "查询Boss基础全部JSON")
    @GetMapping("boss/json/all")
    public Result<List<Map<String, Object>>> infoBossAll() {
        List<Map<String, Object>> list = autoPlanService.findBossAll();
        return ok(list);
    }

    @GetMapping("uid/global/info")
    @SysLog
    @Operation(summary = "查询UID全局体力配置")
    public Result<UidGlobalInfo> uidGlobalInfo(@RequestParam String uid) {

        UidGlobalInfo data = Optional.ofNullable(uidGlobalService.getById(uid))
                .map(info -> ClassConvert.convert(AutoPlanUidGlobalConfig.class, UidGlobalInfo.class, info))
                .orElse(null);
        return ok(data);
    }

    @PostMapping("uid/global/info")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储UID全局体力配置")
    public Result uidGlobalInfo(@RequestBody UidGlobalInfo info) {
        AutoPlanUidGlobalConfig globalConfig = ClassConvert.convert(UidGlobalInfo.class, AutoPlanUidGlobalConfig.class, info);
        uidGlobalService.saveOrUpdate(globalConfig);
        return ok();
    }


    @PostMapping("info")
    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]存储UID体力计划")
    public Result<String> saveInfo(@Validated @RequestBody AutoPlanDTO dto) {
        Valid.validate(AutoPlanDTO.class, dto);
        List<AutoPlanConfig> configList = dto.toConfigList();
        //autoPlanService.save(dto.getUid(), JSONUtil.toJsonStr(dto.getAutoPlanList()));
        autoPlanService.saveOrUpdateBatchList(configList, dto.getRemoveCultivate());
        //autoPlanService.saveBatch(configList);
        return ok(dto.getUid());
    }

    @SysLog
    @Operation(summary = "查询UID映射JSON")
    @GetMapping("json")
    public Result<List<AutoPlanVo>> info(@RequestParam String uid, @RequestParam(required = false, defaultValue = "JS_API") String source,
                                         @RequestParam(required = false) Boolean enable,
                                         @RequestParam(required = false, defaultValue = "true") Boolean order) {

        Stream<AutoPlanVo> stream = autoPlanService.find(uid, enable).stream().map(info->ClassConvert.convert(AutoPlanConfig.class,AutoPlanVo.class,info));
        if ("JS_API".equals(source)) {
            AutoPlanUidGlobalConfig uidGlobalConfig = uidGlobalService.getById(uid);
            if (ObjectUtils.isNotEmpty(uidGlobalConfig) && Boolean.FALSE.equals(uidGlobalConfig.getCultivate())) {
                stream = stream.filter(o -> !Boolean.TRUE.equals(o.getCultivate()));
            }
        }

        List<AutoPlanVo> list = stream.sorted((
                a, b
        ) -> {
            // 将可能为 null 的 cultivate 安全转化为 boolean，null 视为 false
            boolean cultivateA = Boolean.TRUE.equals(a.getCultivate());
            boolean cultivateB = Boolean.TRUE.equals(b.getCultivate());
            // 比较 cultivate，false < true
            int cultivateCmp = Boolean.compare(cultivateA, cultivateB);
            if (cultivateCmp != 0) {
                return order ? cultivateCmp : -cultivateCmp;
            }
            // cultivate 相同，比较 order 字段
            int orderCmp = Integer.compare(a.getOrder(), b.getOrder());
            return order ? orderCmp : -orderCmp;
        }).toList();

        return ok(list);
    }

    @SysLog
    @Operation(summary = "查询全部UID")
    @GetMapping("uid/all")
    public Result<List<String>> uidALL() {
        List<String> uidList = autoPlanService.findUidAll();
        //uidList = uidList.stream().map(uid -> uid.substring(uid.lastIndexOf(":") + 1)).collect(Collectors.toList());
        return ok(uidList);
    }

    @SysLog
    @Operation(summary = "查询全部UID")
    @GetMapping("uid/all/mapping")
    public Result<List<UidInfo>> uidMappingALL() {
        List<UidInfo> uidAll = uidService.findUidAll().stream().map(info -> ClassConvert.convert(UidInfoConfig.class, UidInfo.class, info)).toList();
        //去重
        List<UidInfo> list = new HashSet<UidInfo>(uidAll).stream().toList();
        return ok(list);
    }

    @SysLog
    @Token
    @Operation(summary = "[需要登录/授权token]批量删除UID映射JSON")
    @DeleteMapping("json")
    public Result<Boolean> infoDel(@Validated @NotBlank @RequestParam String uidStr) {
        List<String> ids = Arrays.stream(uidStr.split(",")).collect(Collectors.toList());
        return ok(autoPlanService.removeByUidList(ids));
    }
}
