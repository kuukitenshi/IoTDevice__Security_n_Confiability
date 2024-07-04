package iotserver.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import iotserver.Device;
import iotserver.User;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         A singelton class responsible for managing the operations of the
 *         devices.
 */
public class DeviceManager {

    private static Logger LOGGER = ServerLogger.getLogger(DeviceManager.class.getSimpleName());
    private static DeviceManager instance;

    private final Map<String, Device> devices = new HashMap<>();

    /**
     * Private constructor of the class.
     */
    private DeviceManager() {}

    /**
     * Gets the instance of the DeviceManager
     * 
     * @return the instance of the DeviceManager
     */
    public static synchronized DeviceManager getInstance() {
        if (instance == null)
            instance = new DeviceManager();
        return instance;
    }

    /**
     * Gets the device if it exists.
     * 
     * @param user the user of the device
     * @param id   the id of the device
     * @return An optional containing the device if it exists, null otherwise.
     */
    public Device getDevice(String userIdDevId) {
        return this.devices.get(userIdDevId);
    }

    /**
     * Checks if the device already exists
     * 
     * @param userIdDevId the string pair <userId>:<deviceId>
     * @return true is the device exists, false otherwise
     */
    public boolean deviceExists(String userIdDevId) {
        return this.devices.containsKey(userIdDevId);
    }

    /**
     * Creates and return a new device made with the given params.
     * 
     * @param user the user of the device
     * @param id   the id of the device
     * @return the device created
     */
    public Device createDevice(User user, int id) {
        Device device = new Device(user, id);
        this.devices.put(user.getId() + ":" + id, device);
        LOGGER.info(() -> "Created device " + device);
        return device;
    }
}
