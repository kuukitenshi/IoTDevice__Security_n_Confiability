package common.messages.types.response;

import common.data.EncryptedData;
import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of receive image request.
 */
public class RIResponseData implements MessageData {

    private final EncryptedData encryptedData;
    private final byte[] wrappedDomainKey;

    /**
     * Constructor of the class
     * 
     * @param encryptedData the encrypted data of the image
     * @param wrappedDomainKey the wrapped domain key
     */
    public RIResponseData(EncryptedData encryptedData, byte[] wrappedDomainKey) {
        this.encryptedData = encryptedData;
        this.wrappedDomainKey = wrappedDomainKey;
    }

    /**
     * Gets the encrypted data of the image
     * 
     * @return the encrypted data of the image
     */
    public EncryptedData getEncryptedData() {
        return encryptedData;
    }

    /**
     * Gets the wrapped domain key
     * 
     * @return the wrapped domain key
     */
    public byte[] getWrappedDomainKey() {
        return wrappedDomainKey;
    }
    
}
