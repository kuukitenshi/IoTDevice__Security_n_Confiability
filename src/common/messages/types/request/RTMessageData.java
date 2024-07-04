package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to receive a temperature from a domain.
 */
public class RTMessageData implements MessageData {

    private final String domainName;

    /**
     * Constructor of the class
     * 
     * @param domainName the domain name
     */
    public RTMessageData(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Gets the domain name
     * 
     * @return the domain name
     */
    public String getDomainName() {
        return this.domainName;
    }
}
