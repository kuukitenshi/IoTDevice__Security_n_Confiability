package common;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents an exception releated to the parsing of the
 *         command line arguments.
 */
public final class CommandLineArgsParseException extends Exception {

    /**
     * Constructor of the class
     */
    public CommandLineArgsParseException() {
    }

    /**
     * Constructor of the class
     * 
     * @param message the message to be displayed
     */
    public CommandLineArgsParseException(String message) {
        super(message);
    }

    /**
     * Constructor of the class
     * 
     * @param message the message to be displayed
     * @param cause   the cause of the exception
     */
    public CommandLineArgsParseException(String message, Throwable cause) {
        super(message, cause);
    }
}