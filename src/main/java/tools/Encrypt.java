package tools;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * @author: zpf
 **/

//TODO 变成单例子
public class Encrypt {

    private static final String presetKey = "0CoJUm6Qyw8W8jud";
    private static final String iv = "0102030405060708";
    private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDgtQn2JZ34ZC28NWYpAUd98iZ37BUrX/aKzmFbt7clFSs6sXqHauqKWqdtLkF2KexO40H1YTX8z2lSgBBOAxLsvaklV8k4cBFK9snQXE9/DDaFt6Rr7iVZMldczhC0JNgTz+SHXT6CBHuX3e9SdB1Ua44oncaTWz7OBGLbCiK45wIDAQAB";
    private static final String base62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static void main(String[] args) throws Exception {

        Map<String, String> paramAndEncSecKey = getParamAndEncSecKey("{s:\"red\",type:1,limit:30,offset:0}");
        System.out.println(paramAndEncSecKey);

    }

    public static Map<String,String> getParamAndEncSecKey(String json) {

        Map<String, String> result = new HashMap<>();
        try {
            String securityKey = getRandom(16);
            String aes1 = aesEncrypt(presetKey, iv, json);
            String params = aesEncrypt(securityKey, iv, aes1);

            PublicKey pKey = getPublicKey(publicKey);
            byte[] bytes = rsaEncrypt(new StringBuffer(securityKey).reverse().toString().getBytes(), pKey);
            String encSecKey = DatatypeConverter.printHexBinary(bytes);

            result.put(MConstants.PARAMS, params);
            result.put(MConstants.ENCSECKEY, encSecKey);
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("获取加密参数失败");
            return null;
        }
        return result;

    }

    public static String getMd5(String pwd){

        String md = Hashing.md5().newHasher().putString(pwd, Charsets.UTF_8).hash().toString();
        return md;

    }


    public static String getRandom(int length){

        Random random=new Random();
        StringBuffer str = new StringBuffer();
        for(int i=0;i<length;i++){
            int number = random.nextInt(base62.length());
            str.append(base62.charAt(number));
        }
        return str.toString();

    }

    public static String aesEncrypt(String key, String initVector, String value) throws Exception {

        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

        byte[] encrypted = cipher.doFinal(value.getBytes());

        return Base64.getEncoder().encodeToString(encrypted);

    }

    public static PublicKey getPublicKey(String publicKey) throws Exception {

        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);

    }

    public static byte[] rsaEncrypt(byte[] content, PublicKey publicKey) throws Exception {

        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);

    }


}
