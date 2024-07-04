package iotserver.persistance;

import java.io.Serializable;

import common.data.EncryptedData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Wrapper class that represents data stored by a device
 *         in the Server
 */
public class DevicePersistance implements Serializable {

    private final String userIdDevId;
    private final EncryptedData temperature;
    private final EncryptedData image;

    /**
     * Constructor of the class.
     * 
     * @param userIdDevId the user id and device id separated by a colon
     * @param temperature the encrypted data of the temperature
     * @param image       the encrypted data of the image
     */
    public DevicePersistance(String userIdDevId, EncryptedData temperature, EncryptedData image) {
        this.userIdDevId = userIdDevId;
        this.temperature = temperature;
        this.image = image;
    }

    /**
     * Gets the user id and device id separated by a colon.
     * 
     * @return the user id and device id separated by a colon.
     */
    public String getUserIdDevId() {
        return userIdDevId;
    }

    /**
     * Gets the ecnrypted data of the temperature sent by instance's device.
     * 
     * @return the encrypted data of the temperature.
     */
    public EncryptedData getTemperature() {
        return temperature;
    }

    /**
     * Gets the encrypted data of the image sent by instance's device.
     * 
     * @return the ecnrypted data of the image.
     */
    public EncryptedData getImage() {
        return image;
    }
}
