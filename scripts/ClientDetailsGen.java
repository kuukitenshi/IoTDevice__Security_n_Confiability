import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ClientDetailsGen {

    private static final File KEY_PARAMS_FILE = new File("../bin/server.keyparams");
    private static final File CLIENT_DETAILS_FILE = new File("../bin/clientDetails.txt");
    private static final String PATH_JAR = "deviceCopy/IoTDevice.jar";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java ClientDetailsGen <cipher-password>");
            return;
        }
        String password = args[0];
        Key cipherKey = generatePBEKey(password);
        byte[] hmac = hmac(PATH_JAR.getBytes(), cipherKey);
        try (PrintWriter writer = new PrintWriter(CLIENT_DETAILS_FILE)) {
            writer.println(PATH_JAR);
            String hmacBase64 = Base64.getEncoder().encodeToString(hmac);
            writer.println(hmacBase64);
        }
    }

    private static Key generatePBEKey(String password) throws Exception {
        byte[] salt;
        int iterCount;
        if (KEY_PARAMS_FILE.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(KEY_PARAMS_FILE))) {
                salt = (byte[]) in.readObject();
                iterCount = in.readInt();
            }
        } else {
            SecureRandom random = new SecureRandom();
            salt = new byte[100];
            random.nextBytes(salt);
            iterCount = random.nextInt(4096 - 1000 + 1) + 1000;
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(KEY_PARAMS_FILE))) {
                out.writeObject(salt);
                out.writeInt(iterCount);
            }
        }
        return genSecretKeyFromPassword(password, salt, iterCount);
    }

    private static byte[] hmac(byte[] bytes, Key key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        return mac.doFinal(bytes);
    }

    private static SecretKey genSecretKeyFromPassword(String password, byte[] salt, int iterationCount)
            throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, 128);
        SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128").generateSecret(spec);
        return secretKey;
    }
}