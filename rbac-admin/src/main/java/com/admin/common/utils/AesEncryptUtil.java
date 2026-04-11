package com.admin.common.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 加密解密工具类
 * 用于敏感数据（如配置值、API Key 等）的加密存储
 */
public class AesEncryptUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // 默认密钥（生产环境应从配置文件或环境变量读取）
    private static final String DEFAULT_KEY = "WorkBuddy2024!!";

    /**
     * 加密字符串
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_KEY);
    }

    /**
     * 加密字符串（指定密钥）
     */
    public static String encrypt(String content, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(padKey(key).getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密字符串
     */
    public static String decrypt(String encrypted) {
        return decrypt(encrypted, DEFAULT_KEY);
    }

    /**
     * 解密字符串（指定密钥）
     */
    public static String decrypt(String encrypted, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(padKey(key).getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 密钥填充（AES 需要 16/24/32 字节）
     */
    private static String padKey(String key) {
        if (key == null) {
            key = DEFAULT_KEY;
        }
        if (key.length() < 16) {
            // 不足 16 字节，填充到 16
            return String.format("%-16s", key).substring(0, 16);
        } else if (key.length() < 24) {
            return key.substring(0, 16);
        } else if (key.length() < 32) {
            return key.substring(0, 24);
        }
        return key.substring(0, 32);
    }

    /**
     * 判断是否已加密（简单判断：Base64 格式且包含加密特征）
     */
    public static boolean isEncrypted(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            // 加密后的字符串解密后应该包含原始内容
            // 这里简单判断：加密内容通常以特定前缀开头，或者长度足够长
            return value.startsWith("ENC(") || (value.length() > 20 && Base64.isArrayByteBase64(value.getBytes()));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 带前缀的加密（方便识别）
     */
    public static String encryptWithPrefix(String content) {
        return "ENC(" + encrypt(content) + ")";
    }

    /**
     * 带前缀的解密（自动识别并去除前缀）
     */
    public static String decryptWithPrefix(String value) {
        if (value != null && value.startsWith("ENC(") && value.endsWith(")")) {
            return decrypt(value.substring(4, value.length() - 1));
        }
        return value;
    }
}
