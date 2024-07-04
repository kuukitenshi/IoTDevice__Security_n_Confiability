package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to make a remote attestation, more specifically step of the device hash.
 */
public class RemoteAttestationRequestDataStep2 implements MessageData {

    private final byte[] deviceHash;

    /**
     * Constructor of the class
     * 
     * @param deviceHash the hash of the device
     */
    public RemoteAttestationRequestDataStep2(byte[] deviceHash) {
        this.deviceHash = deviceHash;
    }

    /**
     * Gets the device hash
     * 
     * @return the device hash
     */
    public byte[] getDevHash() {
        return this.deviceHash;
    }
}