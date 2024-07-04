package common.messages.types.response;

import java.util.Map;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of a domian keys request.
 */
public class DomainKeysResponseData implements MessageData {

    private final Map<String, byte[]> domainKeys;

    /**
     * Constructor of the class
     * 
     * @param domainsKeys Map with the domains and the according cipher
     *                    key of the user
     */
    public DomainKeysResponseData(Map<String, byte[]> domainsKeys) {
        this.domainKeys = domainsKeys;
    }

    /**
     * Gets a map with the domains and the according cipher key of the user.
     * 
     * @return Map with the domains and the according cipher key of the user
     */
    public Map<String, byte[]> getDomainKeys() {
        return domainKeys;
    }

}
