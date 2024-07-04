package iotserver;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import common.data.EncryptedData;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represent a domain.
 */
public class Domain {

    private static final Logger LOGGER = ServerLogger.getLogger(Domain.class.getSimpleName());

    private final Map<User, byte[]> userDomainKeys = new ConcurrentHashMap<>();
    private final Map<Device, EncryptedData> deviceTemperatures = new ConcurrentHashMap<>();
    private final Map<Device, EncryptedData> deviceImages = new ConcurrentHashMap<>();
    private final Set<Device> devices = ConcurrentHashMap.newKeySet();
    private final String name;
    private final User owner;

    /**
     * Constructor of the class.
     * 
     * @param name  The unique name of this domain.
     * @param owner The owner of this domain.
     */
    public Domain(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    /**
     * Returns the name of this domain.
     * 
     * @return The name of this domain.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the owner of this domain.
     * 
     * @return The owner of this domain.
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Adds a user to this domain.
     * 
     * @param user      The user to be added to this domain.
     * @param domainKey The encrypted domain key of this user.
     * @return True if the user was added successfully, false otherwise.
     */
    public boolean addUser(User user, byte[] domainKey) {
        byte[] key = this.userDomainKeys.putIfAbsent(user, domainKey);
        if (key != null)
            return false;
        String keyStringB64 = Base64.getEncoder().encodeToString(domainKey);
        LOGGER.info(() -> "User " + user.getId() + " added to domain " + this.name + " with key " + keyStringB64);
        return true;
    }

    /**
     * Adds a device to this domain.
     * 
     * @param device The device to be added.
     * @return True if the device was added successfully, false otherwise.
     */
    public boolean addDevice(Device device) {
        boolean added = this.devices.add(device);
        if (!added)
            return false;
        LOGGER.info(() -> "Device " + device.toString() + " added to domain " + this.name);
        return true;
    }

    /**
     * Updates the temperature of a device in this domain.
     * 
     * @param device      The device to update the temperature.
     * @param temperature The encrypted data of the temperature.
     */
    public void updateDeviceTemp(Device device, EncryptedData temperature) {
        this.deviceTemperatures.put(device, temperature);
        LOGGER.info(() -> "Update " + device.toString() + " temperature added to domain " + this.name + " bytes: " + Arrays.toString(temperature.getData()));
    }

    /**
     * Updates the image of a device in this domain.
     * 
     * @param device The device to update the image.
     * @param image  The encrypted data of the image.
     */
    public void updateDeviceImage(Device device, EncryptedData image) {
        this.deviceImages.put(device, image);
        LOGGER.info(() -> "Update " + device.toString() + " image added to domain " + this.name);
    }

    /**
     * Checks if this domain contains the specified user.
     * 
     * @param user The user to check.
     * @return True if the domain contains the user, false otherwise.
     */
    public boolean containsUser(User user) {
        return this.userDomainKeys.containsKey(user);
    }

    /**
     * Checks if this domain contains the specified device.
     * 
     * @param device The device to check.
     * @return True if the domain contains the device, false otherwise.
     */
    public boolean containsDevice(Device device) {
        return this.devices.contains(device);
    }

    /**
     * Returns the encrypted domain key of a user.
     * 
     * @param user The user to get the domain key from.
     * @return The domain key of the user or null otherwise.
     */
    public byte[] getUserDomainKey(User user) {
        return this.userDomainKeys.get(user);
    }

    /**
     * Returns the encrypted temperature data of a device.
     * 
     * @param device The device to get the data.
     * @return The encrypted temperature data or null otherwise.
     */
    public EncryptedData getDeviceTemperature(Device device) {
        return this.deviceTemperatures.get(device);
    }

    /**
     * Returns the encrypted image data of a device.
     * 
     * @param device The device to get the data.
     * @return The encrypted image data or null otherwise.
     */
    public EncryptedData getDeviceImage(Device device) {
        return this.deviceImages.get(device);
    }

    /**
     * Gets all devices registered in this domain.
     * 
     * @return Set containing all devices registered in this domain.
     */
    public Set<Device> getDevices() {
        return devices;
    }

    /**
     * Gets all users added to this domain.
     * 
     * @return Set containing all users in this domain.
     */
    public Set<User> getUsers() {
        return this.userDomainKeys.keySet();
    }
}
