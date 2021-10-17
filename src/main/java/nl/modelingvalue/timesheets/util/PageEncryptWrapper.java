package nl.modelingvalue.timesheets.util;

import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import de.micromata.jira.rest.core.util.*;

import static java.nio.charset.StandardCharsets.*;

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

    public long write(String htmlPage, Path file) throws IOException {
        Path dir = file.getParent();
        Files.createDirectories(dir);
        String data = split(encrypt(htmlPage, password, IV));
        long   crc  = U.copyResourceCrc(file, WRAPPER_HTML_RSRC, s -> s.replace("@@@iv@@@", IV).replace("@@@data@@@", data));
        return WRAPPER_SUPPORT_RSRCS.stream().mapToLong(fn -> U.copyResourceCrc(dir.resolve(fn))).reduce(crc, (l1, l2) -> l1 ^ l2);
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
            throw new Wrapper("could not wrap page with crypt page", e);
        }
    }

    private SecretKey makeSecretKey(String secret) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(secret.getBytes());
        return new SecretKeySpec(md.digest(), "AES");
    }
}
