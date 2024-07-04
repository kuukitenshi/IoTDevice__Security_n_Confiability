package iotserver;

import java.util.Objects;
import java.util.logging.Logger;

import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a Device.
 */
public class Device {

    private static final Logger LOGGER = ServerLogger.getLogger(Device.class.getSimpleName());
    private final User user;
    private final int id;
    private boolean isOn = false;

    /**
     * Constructor of the class.
     * 
     * @param user the user of the device
     * @param id   the id of the device
     */
    public Device(User user, int id) {
        this.user = user;
        this.id = id;
    }

    /**
     * Gets the user of the device.
     * 
     * @return the user of this device
     */
    public User getUser() {
        return user;
    }

    /**
     * Gets the id of the device.
     * 
     * @return the id of this device
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if the device is active.
     * 
     * @return true if the device is on, false otherwise
     */
    public boolean isOn() {
        return isOn;
    }

    /**
     * Makes the device active.
     */
    public void turnOn() {
        LOGGER.info(() -> toString() + " turned on!");
        this.isOn = true;
    }

    /**
     * Makes the device inactive.
     */
    public void turnOff() {
        LOGGER.info(() -> toString() + " turned off!");
        this.isOn = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        Device other = (Device) obj;
        return this.user.equals(other.user) && this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.user, this.id);
    }

    @Override
    public String toString() {
        return user.getId() + ":" + id;
    }
}
