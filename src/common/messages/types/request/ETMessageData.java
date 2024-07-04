package common.messages.types.request;

import java.util.Map;

import common.data.EncryptedData;
import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to send a temperature.
 */
public class ETMessageData implements MessageData {

    private final Map<String, EncryptedData> domainsTemperatures;

    /**
     * Constructor of the class.
     * 
     * @param images Map with the temperatures to send, where the key is the
     *               domain's name where the temperature will be stored and
     *               the value is the encrypted data of the temperature.
     */
    public ETMessageData(Map<String, EncryptedData> domainsTemepratures) {
        this.domainsTemperatures = domainsTemepratures;
    }

    /**
     * Gets the temperatures to send.
     * 
     * @return Map with the temperatures to send, where the key is the domain's name
     *         where the temperature will be stored and the value is the encrypted
     *         data of the temperature.
     */
    public Map<String, EncryptedData> getDomainsTemperatures() {
        return this.domainsTemperatures;
    }
}
