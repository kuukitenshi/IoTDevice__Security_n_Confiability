package common.messages.types.response;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of a key authentication request.
 */
public class KeyAuthenticationResponseData implements MessageData {

    private final boolean newUser;
    private final long nonce;

    /**
     * Constructor of the class
     * 
     * @param newUser if the user is new
     * @param nonce   the nonce
     */
    public KeyAuthenticationResponseData(boolean newUser, long nonce) {
        this.newUser = newUser;
        this.nonce = nonce;
    }

    /**
     * Method that returns if the user is new
     * 
     * @return true if the user is new, false otherwise
     */
    public boolean isNewUser() {
        return newUser;
    }

    /**
     * Method that returns the nonce
     * 
     * @return the nonce
     */
    public long getNonce() {
        return nonce;
    }
}