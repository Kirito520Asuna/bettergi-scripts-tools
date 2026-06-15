package com.cloud_guest.entitys.pojo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cloud_guest.entitys.domain.UidInfo;
import com.cloud_guest.mp.pojo.BaseEntity;
import com.cloud_guest.runner.EncryptPasswordRunner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.env.Environment;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * @Author yan
 * @Date 2026/4/28 18:39:25
 * @Description
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
@TableName(UidInfoConfig.TABLE_NAME)
public class UidInfoConfig extends BaseEntity {
    @TableId(COL_UID)
    private String uid;
    @TableField(COL_AS)
    private String asName;
    @TableField(COL_USERNAME)
    private String username;
    @TableField(COL_PASSWORD)
    private String password;
    @TableField(COL_SALT)
    private String salt = StrUtil.EMPTY;
    public static final String TABLE_NAME = "uid_info_config";
    public static final String COL_UID = "uid";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_SALT = "salt";
    public static final String COL_AS = "col_as";

    @SneakyThrows
    public UidInfo toUidInfo() {
        String decryptedPassword = StrUtil.isBlankIfStr(password) ? password : decryptPassword(password, salt);
        return new UidInfo(uid, asName, username, decryptedPassword);
    }

    @SneakyThrows
    public UidInfoConfig(String uid, String asName, String username, String password) {
        this.uid = uid;
        this.asName = asName;
        this.username = username;
        this.salt = UUID.randomUUID().toString();
        String encryptPassword = StrUtil.isBlankIfStr(password) ? password : encryptPassword(password, salt);
        this.password = encryptPassword;
    }

    /**
     * 加密密码
     *
     * @param plainPassword 明文密码
     * @return 加密后的密码（Base64编码）
     */
    public static String encryptPassword(String plainPassword, String salt) throws Exception {
        if (StrUtil.isEmpty(plainPassword)) {
            throw new Exception("密码不能为空");
        } else if (StrUtil.isEmpty(salt)) {
            salt = StrUtil.EMPTY;
        }
        salt+= EncryptPasswordRunner.SALT;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(salt.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKeySpec secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 解密密码
     *
     * @param encryptedPassword 加密后的密码（Base64编码）
     * @return 解密后的明文密码
     */
    public static String decryptPassword(String encryptedPassword, String salt) throws Exception {
        if (StrUtil.isEmpty(encryptedPassword)) {
            throw new Exception("密码不能为空");
        } else if (StrUtil.isEmpty(salt)) {
            salt = StrUtil.EMPTY;
        }
        salt+= EncryptPasswordRunner.SALT;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(salt.toCharArray(), salt.getBytes(StandardCharsets.UTF_8), 65536, 256);
        SecretKeySpec secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

}
