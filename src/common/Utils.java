package common;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class with utility methods.
 */
public class Utils {

    /**
     * Method that converts the given float to a byte array.
     * 
     * @return the byte array obtained from the float.
     */
    public static byte[] floatToBytes(float x) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(x);
        return buffer.array();
    }

    /**
     * Method that converts the given byte array to a float.
     * 
     * @return the float obtained from the byte array.
     */
    public static float bytesToFloat(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getFloat();
    }

    /**
     * Method that converts the given long to a byte array.
     * 
     * @return the byte array obtained from the long.
     */
    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    /**
     * Method that concats two byte arrays.
     * 
     * @return the byte array obtained from the concatenation of the two byte
     *         arrays.
     * @throws IOException
     */
    public static byte[] concatByteArrays(byte[] bytes1, byte[] bytes2) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(bytes1);
            out.write(bytes2);
            return out.toByteArray();
        }
    }

    /**
     * Method that loads a KeyStore from a file.
     * 
     * @param filePath the path to the file.
     * @param password the password to access the KeyStore.
     * 
     * @return the KeyStore loaded from the file.
     * @throws FileNotFoundException    If the file does not exist.
     * @throws IOException              If an I/O error occurs when reading the file.
     * @throws KeyStoreException        If the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException If the algorithm used to check the integrity
     *                                  of the keystore cannot be found.
     * @throws CertificateException     If any of the certificates in the keystore
     *                                  could not be loaded.
     */
    public static KeyStore loadKeyStoreFromFile(String filePath, char[] password) throws FileNotFoundException,
            IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fis, password);
            return keyStore;
        }
    }

    /**
     * Method that generates a SecretKey from a password.
     * 
     * @param password       the password to generate the SecretKey.
     * @param salt           the salt
     * @param iterationCount the number of iterations
     * 
     * @return the SecretKey generated from the password.
     * @throws InvalidKeySpecException  If the key specification is invalid.
     * @throws NoSuchAlgorithmException If the algorithm used to generate the key
     *                                  cannot be found.
     */
    public static SecretKey genSecretKeyFromPassword(String password, byte[] salt, int iterationCount)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, 128);
        SecretKey secretKey = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128").generateSecret(spec);
        return secretKey;
    }
}
