package iotserver.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import common.data.EncryptedData;
import iotserver.Device;
import iotserver.Domain;
import iotserver.User;
import iotserver.persistance.DevicePersistance;
import iotserver.persistance.DomainPersistance;
import iotserver.persistance.PersistanceUtils;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         A singelton class responsible for managing the operations of the
 *         domains.
 */
public class DomainManager {

    private static DomainManager instance;
    private static final File DOMAINS_DIRECTORY = new File("domains");
    private static final Logger LOGGER = ServerLogger.getLogger(DomainManager.class.getSimpleName());
    private static final UserManager USER_MANAGER = UserManager.getInstance();
    private static final DomainManager DOMAIN_MANAGER = DomainManager.getInstance();
    private static final DeviceManager DEVICE_MANAGER = DeviceManager.getInstance();

    private final Map<String, Domain> domains = new ConcurrentHashMap<>();

    /**
     * Private constructor of the class.
     */
    private DomainManager() {
    }

    /**
     * Gets the instance of the DomainManager
     * 
     * @return the instance of the DomainManager
     */
    public static synchronized DomainManager getInstance() {
        if (instance == null)
            instance = new DomainManager();
        return instance;
    }

    /**
     * Creates and return a new domain if it doesn't exist.
     * 
     * @param domainName the name of the domain
     * @param owner      the user that is owner of the domain
     * @return the domain created if it does not yet exist, null otherwise.
     */
    public Domain createDomain(String domainName, User owner) {
        Domain domain = new Domain(domainName, owner);
        Domain result = this.domains.putIfAbsent(domainName, domain);
        if (result == null)
            return domain;
        return null;
    }

    /**
     * Checks if the domain exists.
     * 
     * @param domainName the domain's name
     * @return true if a domain with the specified name exists, false otherwise
     */
    public boolean domainsExists(String domainName) {
        return this.domains.containsKey(domainName);
    }

    /**
     * Gets the domain with the given name.
     * 
     * @param domainName the domain's name
     * @return the Domain if it exists, null otherwise.
     */
    public Domain getDomain(String domainName) {
        return this.domains.get(domainName);
    }

    /**
     * Gets the domains a specific device belongs to.
     * 
     * @param device the device specified
     * @return A list of the domains in which the device belongs, null if it
     *         does not belong to any domains.
     */
    public List<Domain> getDeviceDomains(Device device) {
        List<Domain> domains = new ArrayList<>();
        for (Domain domain : this.domains.values()) {
            if (domain.containsDevice(device))
                domains.add(domain);
        }
        return domains;
    }

    /**
     * Gets the domains a specific user is in.
     * 
     * @param user the user specified
     * @return A list of the domains in which the user is in, null if it is
     *         not in any domain.
     */
    public List<Domain> getUsersDomains(User user) {
        List<Domain> domains = new ArrayList<>();
        for (Domain domain : this.domains.values()) {
            if (domain.containsUser(user))
                domains.add(domain);
        }
        return domains;
    }

    /**
     * Save the domains to a file.
     * 
     * @param key the key to encrypt the domains file
     */
    public void saveDomains(Key key) {
        if (!DOMAINS_DIRECTORY.exists())
            DOMAINS_DIRECTORY.mkdirs();
        for (Domain domain : this.domains.values()) {
            try {
                Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                File domainFile = new File(DOMAINS_DIRECTORY, domain.getName());
                String ownerUserId = domain.getOwner().getId();
                Map<String, byte[]> users = new HashMap<>();
                for (User user : domain.getUsers()) {
                    byte[] domainKey = domain.getUserDomainKey(user);
                    users.put(user.getId(), domainKey);
                }
                List<DevicePersistance> devices = domain.getDevices().stream().map(d -> {
                    EncryptedData temperature = domain.getDeviceTemperature(d);
                    EncryptedData image = domain.getDeviceImage(d);
                    return new DevicePersistance(d.toString(), temperature, image);
                }).collect(Collectors.toList());
                DomainPersistance dp = new DomainPersistance(ownerUserId, users, devices);
                byte[] dpBytes = PersistanceUtils.objectToBytes(dp);
                byte[] hmac = PersistanceUtils.hmac(dpBytes, key);
                try (ObjectOutputStream out = new ObjectOutputStream(
                        new CipherOutputStream(new FileOutputStream(domainFile), cipher))) {
                    out.writeObject(dp);
                    out.writeObject(hmac);
                }
                File ivFile = new File(DOMAINS_DIRECTORY, domain.getName() + ".iv");
                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ivFile))) {
                    out.writeObject(cipher.getParameters().getEncoded());
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException e) {
                LOGGER.severe(() -> "Failed to save domain " + domain.getName());
                return;
            }
        }
    }

    /**
     * Load the domains from a file.
     * 
     * @param key the key to decrypt the domains file
     * @return true if the domains were successfully loaded, false otherwise
     */
    public boolean loadDomains(Key key) {
        if (!DOMAINS_DIRECTORY.exists())
            return true;
        for (File file : DOMAINS_DIRECTORY.listFiles()) {
            if (file.getName().endsWith(".iv"))
                continue;
            File ivFile = new File(DOMAINS_DIRECTORY, file.getName() + ".iv");
            byte[] ivParams;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ivFile))) {
                ivParams = (byte[]) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.severe(() -> "Failed to load domain " + file.getName() + " iv file!");
                e.printStackTrace();
                return false;
            }
            try {
                AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
                p.init(ivParams);
                Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                cipher.init(Cipher.DECRYPT_MODE, key, p);
                try (ObjectInputStream in = new ObjectInputStream(
                        new CipherInputStream(new FileInputStream(file), cipher))) {
                    DomainPersistance dp = (DomainPersistance) in.readObject();
                    byte[] hmac = (byte[]) in.readObject();
                    byte[] dpBytes = PersistanceUtils.objectToBytes(dp);
                    byte[] hmacCalculated = PersistanceUtils.hmac(dpBytes, key);
                    if (!Arrays.equals(hmac, hmacCalculated)) {
                        LOGGER.severe(() -> "Failed to verify integrity of domain file " + file.getName() + " skipping loading of it.");
                        return false;
                    } else {
                        LOGGER.info(() -> "Integrity of domain " + file.getName() + " has been verified with hmac: " + Base64.getEncoder().encodeToString(hmac));
                    }
                    String ownerUserId = dp.getOwnerUserId();
                    User owner = USER_MANAGER.getUser(ownerUserId);
                    if (owner == null)
                        owner = USER_MANAGER.createUser(ownerUserId, new File("certs", ownerUserId + ".cer"));
                    Domain domain = DOMAIN_MANAGER.createDomain(file.getName(), owner);
                    for (String userId : dp.getUsers().keySet()) {
                        byte[] domainKey = dp.getUsers().get(userId);
                        User user = USER_MANAGER.getUser(userId);
                        if (user == null)
                            user = USER_MANAGER.createUser(userId, new File("certs", userId + ".cer"));
                        domain.addUser(user, domainKey);
                    }
                    for (DevicePersistance devicePersistance : dp.getDevices()) {
                        String devIdString = devicePersistance.getUserIdDevId();
                        Device device = DEVICE_MANAGER.getDevice(devIdString);
                        if (device == null) {
                            String[] split = devIdString.split(":");
                            String userId = split[0];
                            int devId = Integer.parseInt(split[1]);
                            User user = USER_MANAGER.getUser(userId);
                            if (user == null)
                                user = USER_MANAGER.createUser(userId, new File("certs", userId + ".cer"));
                            device = DEVICE_MANAGER.createDevice(user, devId);
                        }
                        domain.addDevice(device);
                        EncryptedData temperature = devicePersistance.getTemperature();
                        EncryptedData image = devicePersistance.getImage();
                        if (temperature != null)
                            domain.updateDeviceTemp(device, temperature);
                        if (image != null)
                            domain.updateDeviceImage(device, image);
                    }
                }
            } catch (Exception e) {
                LOGGER.severe(() -> "Failed to load domain " + file.getName());
                return false;
            }
        }
        return true;
    }
}
