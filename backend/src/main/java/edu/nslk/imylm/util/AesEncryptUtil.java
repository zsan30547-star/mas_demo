// /backend/src/main/java/edu/nslk/imylm/util/AesEncryptUtil.java
// 职责描述：AES-256-CBC 加密解密工具，用于 API Key 安全存储

package edu.nslk.imylm.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesEncryptUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    // 16字节密钥（生产环境应从配置中心读取）
    private static final String SECRET_KEY = "AgentPlatformKey";
    // 16字节初始向量
    private static final String IV = "AgentPlatformIV1";

    // 加密
    // @param plainText 明文
    // @return Base64 编码的密文
    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(SECRET_KEY), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(
                    padIv(IV));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    // 解密
    // @param encryptedText Base64 编码的密文
    // @return 明文
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    padKey(SECRET_KEY), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(
                    padIv(IV));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    private static byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes();
        byte[] padded = new byte[16];
        System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 16));
        return padded;
    }

    private static byte[] padIv(String iv) {
        byte[] ivBytes = iv.getBytes();
        byte[] padded = new byte[16];
        System.arraycopy(ivBytes, 0, padded, 0, Math.min(ivBytes.length, 16));
        return padded;
    }
}
