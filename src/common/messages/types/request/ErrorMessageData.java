package common.messages.types.request;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents an error message data.
 */
public class ErrorMessageData implements MessageData {

    private final String message;

    /**
     * Constructor of the class.
     * 
     * @param message error message
     */
    public ErrorMessageData(String message) {
        this.message = message;
    }

    /**
     * Gets the error message.
     * 
     * @return error message
     */
    public String getMessage() {
        return message;
    }
    
}
