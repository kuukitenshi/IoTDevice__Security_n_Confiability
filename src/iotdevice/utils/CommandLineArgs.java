package iotdevice.utils;

import common.CommandLineArgsParseException;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that contains and parses the command line arguments.
 */
public class CommandLineArgs {

    private final int port;
    private final String serverAddress;
    private final String trustStore;
    private final String keyStore;
    private final String keyStorePassword;
    private final int deviceId;
    private final String userId;

    /**
     * Constructor of the class.
     * 
     * @param port             The port
     * @param serverAddress    The address of the server
     * @param trustStore       The path of the truststore file
     * @param keyStore         The path of the keystore file
     * @param keyStorePassword The password of the keystore and truststore
     * @param deviceId         The device id
     * @param userId           The user id
     */
    private CommandLineArgs(int port, String serverAddress, String trustStore, String keyStore, String keyStorePassword,
            int deviceId, String userId) {
        this.port = port;
        this.serverAddress = serverAddress;
        this.trustStore = trustStore;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    /**
     * Parses the raw arguments from the main.
     * 
     * @param args The arguments received from the main.
     * @return An instance of CommandLineArgs with parameters of the parsed
     *         arguments.
     * @throws CommandLineArgsParseException If there is an error in any of the
     *                                       arguments.
     */
    public static CommandLineArgs parse(String[] args) throws CommandLineArgsParseException {
        if (args.length != 6) {
            System.err.println(
                    "Usage: java -jar IoTDevice.jar <serverAddress> <truststore> <keystore> <password-keystore> <dev-id> <user-id>");
            throw new CommandLineArgsParseException("Invalid number of arguments!");
        }
        int port = 12345;
        String[] addressSplit = args[0].split(":");
        String serverAddress = addressSplit[0];
        if (addressSplit.length == 2) {
            try {
                port = Integer.parseInt(addressSplit[1]);
            } catch (NumberFormatException e) {
                throw new CommandLineArgsParseException("Invalid port!", e);
            }
        }
        String trustStore = args[1];
        String keyStore = args[2];
        String keyStorePassword = args[3];
        int deviceId;
        try {
            deviceId = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            throw new CommandLineArgsParseException("Invalid deviceId!", e);
        }
        String userId = args[5];
        return new CommandLineArgs(port, serverAddress, trustStore, keyStore, keyStorePassword, deviceId, userId);
    }

    /**
     * Returns the port that will be used to connect to the server.
     * 
     * @return The port of the server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns get address of the server.
     * 
     * @return address of the server.
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * Returns the path of the truststore file.
     * 
     * @return path of the truststore file.
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * Returns the path of the keystore file.
     * 
     * @return path of the keystore file.
     */
    public String getKeyStore() {
        return keyStore;
    }

    /**
     * Returns the password of the keystore and truststore.
     * 
     * @return the password of the keystore and truststore.
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Returns the device id of this device.
     * 
     * @return device id of this device.
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Returns the user id (email) of the user that owns this device.
     * 
     * @return the user id of the owner of this device.
     */
    public String getUserId() {
        return userId;
    }
}