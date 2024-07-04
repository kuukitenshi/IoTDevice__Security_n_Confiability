package iotserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import common.CommandLineArgsParseException;
import common.Utils;
import iotserver.managers.DomainManager;
import iotserver.managers.UserManager;
import iotserver.utils.CommandLineArgs;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Main class of the server.
 */
public class IoTServer {

    private static final Logger LOGGER = ServerLogger.getLogger(IoTServer.class.getSimpleName());
    private static final File KEY_PARAMS_FILE = new File("server.keyparams");

    public static void main(String[] args) {
        try {
            CommandLineArgs commandLineArgs = CommandLineArgs.parse(args);
            ServerSocket serverSocket = createServerSocket(commandLineArgs);
            Key cipherKey = generetePBEKey(commandLineArgs);
            if (UserManager.getInstance().loadUsers(cipherKey) && DomainManager.getInstance().loadDomains(cipherKey)) {
                shutdownHook(cipherKey);
                mainLoop(serverSocket, commandLineArgs, cipherKey);
            }
        } catch (CommandLineArgsParseException e) {
            System.err.println("Failed to parse command line args: " + e.getMessage());
        }
    }

    /**
     * Creates a new SSLServersocket.
     * 
     * @param commandLineArgs The command line arguments of the program.
     * @return The created server socket.
     */
    private static ServerSocket createServerSocket(CommandLineArgs commandLineArgs) {
        System.setProperty("javax.net.ssl.keyStore", commandLineArgs.getKeyStore());
        System.setProperty("javax.net.ssl.keyStorePassword", commandLineArgs.getKeyStorePassword());
        ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        try {
            return ssf.createServerSocket(commandLineArgs.getPort());
        } catch (IOException e) {
            LOGGER.severe(() -> "Failed to create server socket!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Starts the main loop of the server, accepting incoming connections.
     * 
     * @param serverSocket    The server socket.
     * @param commandLineArgs The command line arguments of the program.
     */
    private static void mainLoop(ServerSocket serverSocket, CommandLineArgs commandLineArgs, Key secretKey) {
        LOGGER.info(() -> "Server started!");
        while (true) {
            try {
                Socket sock = serverSocket.accept();
                ClientThread thread = new ClientThread(sock, commandLineArgs, secretKey);
                thread.start();
            } catch (IOException e) {
                LOGGER.warning(() -> "Couldn't establish client connection!");
            }
        }
    }

    /**
     * Adds a shutdown hook to save data on program shutdown.
     * 
     * @param cipherKey The secret key generated when the server starts.
     */
    private static void shutdownHook(Key cipherKey) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DomainManager.getInstance().saveDomains(cipherKey);
            UserManager.getInstance().saveUsers(cipherKey);
        }));
    }

    /**
     * Generates a new PBE key using the password specified in the command line
     * arguments.
     * 
     * @param commandLineArgs The command line arguments of the program.
     * @return The generated PBE key.
     */
    private static Key generetePBEKey(CommandLineArgs commandLineArgs) {
        byte[] salt = null;
        int iterCount = 0;
        if (KEY_PARAMS_FILE.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(KEY_PARAMS_FILE))) {
                salt = (byte[]) in.readObject();
                iterCount = in.readInt();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.severe(() -> "Failed to load key params file!");
                System.exit(-1);
            }
        } else {
            SecureRandom random = new SecureRandom();
            salt = new byte[100];
            random.nextBytes(salt);
            iterCount = random.nextInt(4096 - 1000 + 1) + 1000;
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(KEY_PARAMS_FILE))) {
                out.writeObject(salt);
                out.writeInt(iterCount);
            } catch (IOException e) {
                LOGGER.severe(() -> "Failed to save key params file!");
                System.exit(-1);
            }
        }
        try {
            return Utils.genSecretKeyFromPassword(commandLineArgs.getCipherPassword(), salt, iterCount);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGGER.severe(() -> "Failed to generate secret key!");
            System.exit(-1);
        }
        return null;
    }
}
