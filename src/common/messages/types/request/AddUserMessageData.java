package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to add a user to a domain.
 */
public class AddUserMessageData implements MessageData {

    private final String userId;
    private final String domainName;
    private final byte[] domainKey;

    /**
     * Constructor of the class.
     * 
     * @param username   the username
     * @param domainName the domain name
     * @param domainKey  the domain key
     */
    public AddUserMessageData(String userId, String domainName, byte[] domainKey) {
        this.userId = userId;
        this.domainName = domainName;
        this.domainKey = domainKey;
    }

    /**
     * Gets the userId of the user to be added.
     * 
     * @return the userId
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Gets the domain name where the user will be added.
     * 
     * @return the domain name
     */
    public String getDomainName() {
        return this.domainName;
    }

    /**
     * Gets the domain key of the domain where the user will be added.
     * 
     * @return the domain key
     */
    public byte[] getDomainKey() {
        return this.domainKey;
    }
}
