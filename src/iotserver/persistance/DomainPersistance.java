package iotserver.persistance;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Wrapper class that represents data stored by a domain
 *         in the Server
 */
public class DomainPersistance implements Serializable {

    private final String ownerUserId;
    private final Map<String, byte[]> users;
    private final List<DevicePersistance> devices;

    /**
     * Constructor of the class.
     * 
     * @param ownerUserID the id of the owner of the domain
     * @param users       a map with the user id and the domain key
     * @param devices     the list of the devices persisted in the domain
     */
    public DomainPersistance(String ownerUserID, Map<String, byte[]> users, List<DevicePersistance> devices) {
        this.ownerUserId = ownerUserID;
        this.users = users;
        this.devices = devices;
    }

    /**
     * Gets the if of the owner of the domain.
     * 
     * @return the id of the owner of the domain.
     */
    public String getOwnerUserId() {
        return ownerUserId;
    }

    /**
     * Gets the map with the users and the domain key.
     * 
     * @return the map with the users and the domain key.
     */
    public Map<String, byte[]> getUsers() {
        return users;
    }

    /**
     * Gets the list of devices with data persisted in the domain.
     * 
     * @return the list of devices.
     */
    public List<DevicePersistance> getDevices() {
        return devices;
    }
}
