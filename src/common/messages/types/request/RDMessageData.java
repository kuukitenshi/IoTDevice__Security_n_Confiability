package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to register a device on a domain.
 */
public class RDMessageData implements MessageData {

    private final String domainName;

    /**
     * Constructor of the class
     * 
     * @param domainName the name of the domain
     */
    public RDMessageData(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Gets the name of the domain
     * 
     * @return the name of the domain
     */
    public String getDomainName() {
        return this.domainName;
    }
}
