package common.messages.types.response;

import java.util.List;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of a my domains request.
 */
public class MDResponseMessageData implements MessageData {

    private final List<String> domainNames;

    /**
     * Constructor of the class
     * 
     * @param domainNames list of domain names
     */
    public MDResponseMessageData(List<String> domainNames) {
        this.domainNames = domainNames;
    }

    /**
     * Method that returns the list of domain names
     * 
     * @return list of domain names
     */
    public List<String> getDomainNames() {
        return this.domainNames;
    }
}
