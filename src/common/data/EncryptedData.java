package common.data;

import java.io.Serializable;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that contains the data and the IV required for encrypt data.
 */
public class EncryptedData implements Serializable {

    private final byte[] data;
    private final byte[] iv;

    /**
     * Constructor of the class.
     * 
     * @param data the given data
     * @param iv   the iv (initialization vector)
     */
    public EncryptedData(byte[] data, byte[] iv) {
        this.data = data;
        this.iv = iv;
    }

    /**
     * Gets the data for encryption.
     * 
     * @return the data for encryption.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the iv for encryption.
     * 
     * @return the iv for encryption.
     */
    public byte[] getIV() {
        return iv;
    }
}
