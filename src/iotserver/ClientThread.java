package iotserver;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.util.logging.Logger;

import iotserver.utils.CommandLineArgs;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a thread for a client connection.
 */
public class ClientThread extends Thread {

    private static final Logger LOGGER = ServerLogger.getLogger(ClientThread.class.getSimpleName());

    private final Socket socket;
    private final IoTServerSkel serverSkel;

    /**
     * Constructor of the class.
     * 
     * @param socket          the socket to communicate with the server
     * @param commandLineArgs the command line arguments
     * @throws IOException if an I/O error occurs when creating the input and output
     *                     streams.
     */
    public ClientThread(Socket socket, CommandLineArgs commandLineArgs, Key secretKey) throws IOException {
        this.socket = socket;
        this.serverSkel = new IoTServerSkel(socket, commandLineArgs, secretKey);
    }

    @Override
    public void run() {
        LOGGER.info(() -> "IoTDevice connected!");
        boolean shouldClouse = false;
        while (!shouldClouse) {
            shouldClouse = this.serverSkel.handleMessage();
        }
        try {
            this.socket.close();
        } catch (IOException e) {
            LOGGER.warning(() -> "Failed to close socket connection!");
        }
    }
}
