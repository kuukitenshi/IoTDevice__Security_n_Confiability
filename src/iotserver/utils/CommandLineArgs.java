package iotserver.utils;

import common.CommandLineArgsParseException;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents an exception releated to the parsing of the
 *         command line arguments.
 */
public class CommandLineArgs {

    private final int port;
    private final String cipherPassword;
    private final String keyStore;
    private final String keyStorePassword;
    private final String apiKey;

    /**
     * Constructor of the class
     * 
     * @param port             the server port
     * @param cipherPassword   the password to gen the simetric key to cipher the
     *                         files of the application
     * @param keyStore         the keystore
     * @param keyStorePassword the keystore password
     * @param apiKey           the API key
     */
    private CommandLineArgs(int port, String cipherPassword, String keyStore, String keyStorePassword, String apiKey) {
        this.port = port;
        this.cipherPassword = cipherPassword;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.apiKey = apiKey;
    }

    /**
     * Method that parses the command line arguments
     * 
     * @param args the command line arguments
     * @return the command line arguments
     * @throws CommandLineArgsParseException if the number of arguments is invalid
     */
    public static CommandLineArgs parse(String[] args) throws CommandLineArgsParseException {
        if (args.length != 4 && args.length != 5) {
            System.err.println(
                    "Usage: java -jar IoTServer.jar [port] <cipher-password> <keystore> <keystore-password> <2FA-APIkey>");
            throw new CommandLineArgsParseException("Invalid number of arguments!");
        }
        int port = 12345;
        if (args.length == 5) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                throw new CommandLineArgsParseException("Invalid port!", e);
            }
        }
        String cipherPassword = args.length == 4 ? args[0] : args[1];
        String keyStore = args.length == 4 ? args[1] : args[2];
        String keyStorePassword = args.length == 4 ? args[2] : args[3];
        String apiKey = args.length == 4 ? args[3] : args[4];
        return new CommandLineArgs(port, cipherPassword, keyStore, keyStorePassword, apiKey);
    }

    /**
     * Method that returns the server port
     * 
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Method that returns the password to gen the simetric key to cipher the files
     * of the application
     * 
     * @return the password to cipher the files of the application
     */
    public String getCipherPassword() {
        return cipherPassword;
    }

    /**
     * Method that returns the keystore
     * 
     * @return the keystore
     */
    public String getKeyStore() {
        return keyStore;
    }

    /**
     * Method that returns the keystore password
     * 
     * @return the keystore password
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Method that returns the API key
     * 
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }
}
