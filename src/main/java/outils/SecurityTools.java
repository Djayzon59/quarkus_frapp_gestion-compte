package outils;

import entities.UtilisateurEntity;
import io.smallrye.jwt.build.Jwt;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

public class SecurityTools {

    private static String algorithm = "AES";
    private static String secretKeyString = "TheVerySecretK3Y";
    private static SecretKeySpec secretKey;

    static {
        try {
            secretKey = generateSecretKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static SecretKeySpec generateSecretKey() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(secretKeyString.getBytes(StandardCharsets.UTF_8));
        byte[] keyBytes = new byte[128 / 8];
        System.arraycopy(digest, 0, keyBytes, 0, keyBytes.length);
        return new SecretKeySpec(keyBytes, algorithm);
    }

    public static String encrypt(String data) {
        byte[] dataToCrypt = data.getBytes();
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte [] encryptedData = cipher.doFinal(dataToCrypt);
            byte[] encryptedDataForUrl = Base64.getUrlEncoder().encode(encryptedData);
            return new String(encryptedDataForUrl);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 BadPaddingException | IllegalBlockSizeException e) {
            return null;
        }
    }

    public static String decrypt(String data) {
        try {
            byte[] cryptedData = Base64.getUrlDecoder().decode(data);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(cryptedData));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            return null;
        }
    }
    public static String getToken(UtilisateurEntity utilisateur) {
        return Jwt.issuer("http://sackebandt.fr")
                .expiresIn(Duration.ofMinutes(20))
                .upn(utilisateur.getMail_utilisateur())
                .groups(utilisateur.getRoleEntity().getLibelleRole())
                .sign();
    }

    public static String getfirstToken(UtilisateurEntity utilisateur) {
        Boolean isValidate = false;
        return Jwt.issuer("http://sackebandt.fr")
                .expiresIn(Duration.ofMinutes(20))
                .upn(utilisateur.getMail_utilisateur())
                .groups(utilisateur.getRoleEntity().getLibelleRole())
                .claim("isValidate", isValidate)
                .sign();
    }

    public static String getTokenOtp(UtilisateurEntity utilisateur, String otp) {
        return Jwt.issuer("http://sackebandt.fr")
                .expiresIn(Duration.ofMinutes(20))
                .upn(utilisateur.getMail_utilisateur())
                .groups(utilisateur.getRoleEntity().getLibelleRole())
                .claim("otp", otp )
                .sign();
    }

}
