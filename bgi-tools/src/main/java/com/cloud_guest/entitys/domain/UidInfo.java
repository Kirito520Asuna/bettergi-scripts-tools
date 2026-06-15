package com.cloud_guest.entitys.domain;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.cloud_guest.entitys.pojo.UidInfoConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * @Author yan
 * @Date 2026/3/30 17:24:44
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidInfo {
    @Schema(description = "uid")
    @NotBlank(message = "uid不能为空")
    private String uid;
    @NotBlank(message = "as不能为空")
    private String as;
    private String username;
    private String password;
    @SneakyThrows
    public UidInfoConfig toConfig(){
        UidInfoConfig uidInfoConfig = new UidInfoConfig(uid, as, username, password);
        return uidInfoConfig;
    }
}
