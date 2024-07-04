package common.messages;

import java.io.Serializable;

import common.OpCode;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message.
 *         Implements Serializable interface to allow the object to be
 *         serialized and deserialized.
 */
public class Message implements Serializable {

    private final OpCode opCode;
    private final MessageData data;

    /**
     * Constructor of the class
     * 
     * @param opCode the operation code of the message
     */
    public Message(OpCode opCode) {
        this(opCode, null);
    }

    /**
     * Constructor of the class
     * 
     * @param opCode the operation code of the message
     * @param data   the data of the message
     */
    public Message(OpCode opCode, MessageData data) {
        this.opCode = opCode;
        this.data = data;
    }

    /**
     * Gets the operation code of the message
     * 
     * @return the operation code of the message
     */
    public OpCode getOpCode() {
        return opCode;
    }

    /**
     * Gets the data of the message
     * 
     * @return the data of the message
     */
    public MessageData getData() {
        return data;
    }
}
