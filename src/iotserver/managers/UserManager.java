package iotserver.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import iotserver.User;
import iotserver.utils.ServerLogger;


/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         A singelton class responsible for managing the operations of the
 *         users.
 */
public class UserManager {

    private static final Logger LOGGER = ServerLogger.getLogger(UserManager.class.getSimpleName());
    private static final File IV_USERS_FILES = new File("users.iv");
    private static final File USERS_FILE = new File("users.txt");
    private static UserManager instance;

    private final Map<String, User> users = new ConcurrentHashMap<>();

    /**
     * Private constructor of the class.
     */
    private UserManager() {}

    /**
     * Gets the instance of the UserManager
     * 
     * @return the instance of the UserManager
     */
    public static synchronized UserManager getInstance() {
        if (instance == null)
            instance = new UserManager();
        return instance;
    }

    /**
     * Gets the user with the given id.
     * 
     * @param userId the id of the user
     * @return the user with the given id
     */
    public User getUser(String userId) {
        return this.users.get(userId);
    }

    /**
     * Creates and return a new user if it doesn't exist.
     * 
     * @param userId the id of the user
     * @param certificateFile the user's certificate, where it's public key is stored
     * @return the user created if it does not yet exist, null otherwise.
     */
    public User createUser(String userId, File certificateFile) {
        User user = new User(userId, certificateFile);
        User result = this.users.putIfAbsent(userId, user);
        if (result != null) {
            return null;
        }
        LOGGER.info(() -> "Created user " + userId);
        return user;
    }

    /**
     * Checks if a specific user exists.
     * 
     * @param userId the id of the user
     * @return true if the user exists, null otherwise.
     */
    public boolean userExists(String userId) {
        return this.users.containsKey(userId);
    }

    /**
     * Save the users to a file.
     * 
     * @param key the key to encrypt the users file
     */
    public void saveUsers(Key key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            LOGGER.severe(() -> "Failed to initialize users encrypt cipher!");
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(IV_USERS_FILES))) {
            out.writeObject(cipher.getParameters().getEncoded());
        } catch (IOException e) {
            LOGGER.severe(() -> "Failed to write users iv parameters!");
            return;
        }
        try (PrintWriter writer = new PrintWriter(new CipherOutputStream(new FileOutputStream(USERS_FILE), cipher))) {
            this.users.values().forEach(u -> {
                writer.println(u.getId() + " " + u.getCertificateFile().getPath());
            });
        } catch (IOException e) {
            LOGGER.severe(() -> "Failed to write users files!");
        }
    }

    /**
     * Load the users from a file.
     * 
     * @param key the key to decrypt the users file
     * @return true if the users were loaded successfully, false otherwise.
     */
    public boolean loadUsers(Key key) {
        if (!USERS_FILE.exists())
            return true;
        if (!IV_USERS_FILES.exists()) {
            LOGGER.severe(() -> "Users IV file not found!");
            return false;
        }
        byte[] ivParams;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(IV_USERS_FILES))) {
            ivParams = (byte[]) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.severe(() -> "Failed to load users iv parameters!");
            return false;
        }
        try {
            AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
            p.init(ivParams);
            Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            cipher.init(Cipher.DECRYPT_MODE, key, p);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new CipherInputStream(new FileInputStream(USERS_FILE), cipher)))) {
                reader.lines().forEach(line -> {
                    String[] fields = line.split(" ");
                    String userId = fields[0];
                    String cerFilePath = fields[1];
                    createUser(userId, new File(cerFilePath));
                });
            }
            return true;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException
                | InvalidAlgorithmParameterException e) {
            LOGGER.severe(() -> "Failed to red users file!");
            return false;
        }
    }
}
