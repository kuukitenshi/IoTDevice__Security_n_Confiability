package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to receive an image from a domain.
 */
public class RIMessageData implements MessageData {

    private final String userIdDevId;

    /**
     * Constructor of the class
     * 
     * @param userIdDevId string with the user id and device id, separated by a colon
     */
    public RIMessageData(String userIdDevId) {
        this.userIdDevId = userIdDevId;
    }

    /**
     * Gets the user id and device id
     * 
     * @return string with the user id and device id, separated by a colon
     */
    public String getUserIdDevId() {
        return userIdDevId;
    }
}
