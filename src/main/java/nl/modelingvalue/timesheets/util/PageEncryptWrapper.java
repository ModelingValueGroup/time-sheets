package nl.modelingvalue.timesheets.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PageEncryptWrapper {
    private static final String IV           = "5D9r9ZVzEYYgha93/aUK2w==";
    private static final String WRAPPER_PAGE = U.readResource("wrapper.html");
    private static final int    LINE_LENGTH  = 128;
    //
    private final        String password;


    public PageEncryptWrapper(String password) {
        this.password = password;
    }

    public String wrap(String htmlPage) {
        return WRAPPER_PAGE
                .replace("@@@iv@@@", IV)
                .replace("@@@data@@@", split(encrypt(htmlPage, password, IV)));
    }

    private String split(String oneLiner) {
        StringBuilder buf = new StringBuilder();
        int           i;
        for (i = 0; LINE_LENGTH < oneLiner.length() - i; i += LINE_LENGTH) {
            buf.append(oneLiner, i, i + LINE_LENGTH).append("\" +\n            \"");
        }
        buf.append(oneLiner, i, oneLiner.length());
        return buf.toString();
    }

    private String encrypt(String data, String secret, String iv) {
        try {
            AlgorithmParameterSpec spec   = new IvParameterSpec(Base64.getDecoder().decode(iv));
            Cipher                 cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey              key    = makeSecretKey(secret);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(UTF_8)));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new Error("could not wrap page with crypt page", e);
        }
    }

    private SecretKey makeSecretKey(String secret) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(secret.getBytes());
        return new SecretKeySpec(md.digest(), "AES");
    }
}
