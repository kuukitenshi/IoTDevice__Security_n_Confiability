package common.messages.types.request;

import java.util.Map;

import common.data.EncryptedData;
import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to send an image.
 */
public class EIMessageData implements MessageData {

    private final Map<String, EncryptedData> images;

    /**
     * Constructor of the class.
     * 
     * @param images Map with the images to send, where the key is the domain's name
     *               where the image will be stored and the value is the encrypted
     *               data of the image.
     */
    public EIMessageData(Map<String, EncryptedData> images) {
        this.images = images;
    }

    /**
     * Gets the images to send.
     * 
     * @return Map with the images to send, where the key is the domain's name
     *         where the image will be stored and the value is the encrypted data
     *         image.
     */
    public Map<String, EncryptedData> getImages() {
        return this.images;
    }
}
