package iotserver.persistance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that contains utility methods for persistance.
 */
public class PersistanceUtils {

    /**
     * Converts an object to a byte array
     * 
     * @param object the object to be serialized
     * @return the byte array
     * @throws IOException if an I/O error occurs while writing stream header
     */
    public static byte[] objectToBytes(Serializable object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos)) {
            out.writeObject(object);
            out.flush();
            return baos.toByteArray();
        }
    }

    /**
     * Generates a HMAC from a byte array and a key
     * 
     * @param bytes the byte array
     * @param key   the key
     * @return the HMAC
     * @throws NoSuchAlgorithmException if the algorithm is not available
     * @throws InvalidKeyException      if the key is invalid
     */
    public static byte[] hmac(byte[] bytes, Key key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(key);
        return mac.doFinal(bytes);
    }
}
