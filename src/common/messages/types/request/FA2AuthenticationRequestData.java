package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to made a 2FA Authentication.
 */
public class FA2AuthenticationRequestData implements MessageData {

    private final String userCode;

    /**
     * Constructor of the class.
     * 
     * @param userCode the user code to be authenticated.
     */
    public FA2AuthenticationRequestData(String userCode) {
        this.userCode = userCode;
    }

    /**
     * Gets the user code.
     * 
     * @return the user code.
     */
    public String getUserCode() {
        return this.userCode;
    }
}
