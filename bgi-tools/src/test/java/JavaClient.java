import cn.hutool.json.JSONUtil;
import com.cloud_guest.result.Result;
import com.cloud_guest.utils.RSAUtil;
import com.cloud_guest.vo.KeyInfoVo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @Author yan
 * @Date 2026/3/28 20:55:31
 * @Description
 */
public class JavaClient {
    // OkHttp 客户端单例
    private static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws Exception {
        // 1. 生成客户端密钥 CR(私钥)、CP(公钥)
        KeyPair clientKeyPair = RSAUtil.generateKeyPair();
        String clientPrivateKeyCR = RSAUtil.privateKeyToString(clientKeyPair.getPrivate());
        String clientPublicKeyCP = RSAUtil.publicKeyToString(clientKeyPair.getPublic());

        System.out.println("===== 客户端生成密钥 =====");
        System.out.println("客户端公钥 CP：\n" + clientPublicKeyCP);

        // 2. 构建请求：CP 放入请求头 client-public-key
        Request request = new Request.Builder()
                .url("http://127.0.0.1:8081/bgi/key/exchangeKey")
                .addHeader("X-Encryption-Client-Key", clientPublicKeyCP) // 请求头传递公钥
                .post(RequestBody.create(new byte[0]))
                .build();

        // 3. 发送请求获取加密 SP
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("请求失败：" + response.code());
            }
            String re = response.body().string();
            Result bean = JSONUtil.toBean(re, Result.class);
            Object data = bean.getData();
            KeyInfoVo vo = JSONUtil.toBean(JSONUtil.toJsonStr(data), KeyInfoVo.class);
            String encryptedSP =  vo.getPublicKeyEncryption();

            System.out.println("\n===== 收到加密SP =====");
            System.out.println(encryptedSP);

            // 4. 私钥 CR 解密得到 SP
            PrivateKey crPrivateKey = RSAUtil.stringToPrivateKey(clientPrivateKeyCR);
            String serverPublicKeySP = RSAUtil.decryptByPrivateKey(encryptedSP, crPrivateKey);

            System.out.println("\n===== 解密成功 =====");
            System.out.println("服务端公钥 SP：\n" + serverPublicKeySP);
            PublicKey publicKey = RSAUtil.stringToPublicKey(clientPublicKeyCP);
            System.out.println("===== 待加密数据 =====");
            String number = "1230";
            System.out.println(number);
            System.out.println("===== 加密 =====");
            String encrypt = RSAUtil.encryptByPublicKey(number, publicKey);
            System.out.println(encrypt);
            PrivateKey privateKey = RSAUtil.stringToPrivateKey(clientPrivateKeyCR);
            System.out.println("===== 解密 =====");
            String decrypt = RSAUtil.decryptByPrivateKey(encrypt, privateKey);
            System.out.println(decrypt);
        }
    }
}