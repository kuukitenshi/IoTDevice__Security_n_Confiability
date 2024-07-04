package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to make a remote attestation.
 */
public class RemoteAttestationRequestData implements MessageData {

    private final int deviceId;

    /**
     * Constructor of the class
     * 
     * @param deviceId id of the device
     */
    public RemoteAttestationRequestData(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets id of the device
     * 
     * @return id of the device
     */
    public int getDevId() {
        return this.deviceId;
    }
}