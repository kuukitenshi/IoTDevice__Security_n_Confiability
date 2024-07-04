package common.messages.types.response;

import java.util.Map;

import common.data.EncryptedData;
import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of receive temperature request.
 */
public class RTResponseData implements MessageData {

    private final Map<String, EncryptedData> deviceTemperatures;
    private final byte[] wrappedDomainKey;

    /**
     * Constructor of the class
     * 
     * @param deviceTemperatures a map with the device id and the encrypted data of
     *                           the temperature
     * @param wrappedDomainKey   the wrapped domain key
     */
    public RTResponseData(Map<String, EncryptedData> deviceTemperatures, byte[] wrappedDomainKey) {
        this.deviceTemperatures = deviceTemperatures;
        this.wrappedDomainKey = wrappedDomainKey;
    }

    /**
     * Gets the map with the device id and the encrypted data of the temperature
     * 
     * @return the map with the device id and the encrypted data of the temperature
     */
    public Map<String, EncryptedData> getDeviceTemperatures() {
        return deviceTemperatures;
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
