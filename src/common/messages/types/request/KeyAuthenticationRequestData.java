package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to make a Key Authentication.
 */
public class KeyAuthenticationRequestData implements MessageData {

    private final String userId;

    /**
     * Constructor of the class
     * 
     * @param userId the user id
     */
    public KeyAuthenticationRequestData(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the user id
     * 
     * @return the user id
     */
    public String getUserId() {
        return userId;
    }
}
