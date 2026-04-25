package com.cloud_guest.controller.key;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.abs.service.AbstractKeyService;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.domain.key.KeyInfo;
import com.cloud_guest.properties.auth.ApiProperties;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.IdUtils;
import com.cloud_guest.utils.RSAUtil;
import com.cloud_guest.utils.ServletUtil;
import com.cloud_guest.vo.KeyInfoVo;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.LinkedHashMap;

/**
 * @Author yan
 * @Date 2026/3/28 20:52:23
 * @Description
 */
@Slf4j
@RestController
@RequestMapping({"/key/", "/api/key/"})
@Tag(name = "密钥管理", description = "密钥管理")
public class KeyController {

    // 从请求头获取客户端公钥
    @SneakyThrows
    @SysLog(title = "密钥交换")
    @PostMapping("exchangeKey")
    public Result<KeyInfoVo> exchangeKey() {
        ApiProperties apiProperties = SpringUtil.getBean(ApiProperties.class);
        HttpServletRequest request = ServletUtil.getRequest();
        String clientPublicKeyStr = request.getHeader(apiProperties.getEncryptionAsName());
        // 服务端生成 SR、SP
        KeyPair serverKeyPair = RSAUtil.generateKeyPair();
        String serverPublicKeySP = RSAUtil.publicKeyToString(serverKeyPair.getPublic());
        String privateKeyBase64 = RSAUtil.privateKeyToString(serverKeyPair.getPrivate());

        log.debug("服务端公钥 SP：{}", serverPublicKeySP);

        // 用客户端公钥加密 SP
        PublicKey cp = RSAUtil.stringToPublicKey(clientPublicKeyStr);
        String encryptedSP = RSAUtil.encryptByPublicKey(serverPublicKeySP, cp);

        KeyInfo keyInfo = new KeyInfo()
                .setId(IdUtils.getNextIdStr())
                .setPublicKeyBase64(serverPublicKeySP)
                .setPrivateKeyBase64(privateKeyBase64)
                .setCreateTime(System.currentTimeMillis());
        // 保存密钥信息
        SpringUtil.getBean(AbstractKeyService.class).saveKeyInfo(keyInfo);

        KeyInfoVo build = KeyInfoVo.build(keyInfo, encryptedSP);
        return Result.ok(build);

    }
}
