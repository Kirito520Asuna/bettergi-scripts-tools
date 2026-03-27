package com.cloud_guest.controller.key;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.aop.log.SysLog;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.IdUtils;
import com.cloud_guest.utils.RSAUtil;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/key/")
@Tag(name = "密钥管理", description = "密钥管理")
public class KeyController {

    // 从请求头获取客户端公钥
    @SneakyThrows
    @SysLog(title = "密钥交换")
    @PostMapping("exchangeKey")
    public Result<String> exchangeKey(@RequestHeader("client-public-key") String clientPublicKeyStr) {
        // 服务端生成 SR、SP
        KeyPair serverKeyPair = RSAUtil.generateKeyPair();
        String serverPublicKeySP = RSAUtil.publicKeyToString(serverKeyPair.getPublic());

        log.debug("服务端公钥 SP：{}", serverPublicKeySP);

        // 用客户端公钥加密 SP
        PublicKey cp = RSAUtil.stringToPublicKey(clientPublicKeyStr);
        String encryptedSP = RSAUtil.encryptByPublicKey(serverPublicKeySP, cp);
        /*LinkedHashMap<String, Object> keyMap = Maps.newLinkedHashMap();
        String nextIdStr = IdUtils.getNextIdStr();
        keyMap.put(nextIdStr, StrUtil.format("""
                {
                    "sp": "{}",
                    "startTime": {},
                    "timeout": {}
                }
                """, encryptedSP,System.currentTimeMillis(),5*60*1000));*/
        return Result.ok(encryptedSP);

    }
}
