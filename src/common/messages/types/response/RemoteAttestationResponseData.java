package common.messages.types.response;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters of the
 *         response of remote attestation request.
 */
public class RemoteAttestationResponseData implements MessageData {

    private final long nonce;

    /**
     * Constructor of the class
     * 
     * @param nonce the nonce
     */
    public RemoteAttestationResponseData(long nonce) {
        this.nonce = nonce;
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