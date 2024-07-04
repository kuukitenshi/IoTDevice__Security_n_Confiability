package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to create a user.
 */
public class CreateDomainMessageData implements MessageData {

    private final String domainName;

    /**
     * Constructor of the class.
     * 
     * @param domainName the domain name
     */
    public CreateDomainMessageData(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Gets the name of the domain to be created.
     * 
     * @return the domain name
     */
    public String getDomainName() {
        return this.domainName;
    }
}
