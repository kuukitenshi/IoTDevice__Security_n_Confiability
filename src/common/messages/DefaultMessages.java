package common.messages;

import common.OpCode;
import common.messages.types.request.ErrorMessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that contains default messages already created for OK, NOK,
 *         DATA_TYPE_ERROR and SESSION_INFO_ERROR.
 */
public class DefaultMessages {

    public static Message OK_MESSAGE = new Message(OpCode.OK);

    public static Message NOK_MESSAGE = new Message(OpCode.NOK);

    public static Message DATA_TYPE_ERROR_MESSAGE = new Message(OpCode.ERROR,
            new ErrorMessageData("Invalid data type!"));

    public static Message SESSION_INFO_ERROR_MESSAGE = new Message(OpCode.ERROR,
            new ErrorMessageData("Invalid stage on session!"));

}
