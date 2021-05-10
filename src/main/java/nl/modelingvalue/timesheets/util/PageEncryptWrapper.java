package nl.modelingvalue.timesheets.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PageEncryptWrapper {
    private static final int          LINE_LENGTH           = 128;
    private static final String       IV                    = "5D9r9ZVzEYYgha93/aUK2w==";
    public static final  String       WRAPPER_HTML_RSRC     = "wrapper.html";
    private static final List<String> WRAPPER_SUPPORT_RSRCS = List.of(
            "wrapper.css",
            "wrapper.js",
            "wrapper.jpg"
    );
    //
    private final        String       password;


    public PageEncryptWrapper(String password) {
        this.password = password;
    }

    public void write(String htmlPage, Path file) throws IOException {
        Path dir = file.getParent();
        Files.createDirectories(dir);
        String data = split(encrypt(htmlPage, password, IV));
        U.copyResource(file, WRAPPER_HTML_RSRC, s -> s.replace("@@@iv@@@", IV).replace("@@@data@@@", data));
        WRAPPER_SUPPORT_RSRCS.forEach(fn -> U.copyResource(dir.resolve(fn)));
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
