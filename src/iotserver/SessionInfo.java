package iotserver;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents the current session information of a connected
 *         client.
 */
public class SessionInfo {

    private User user = null;
    private Device device = null;
    private long nonce = 0;
    private boolean newUser = false;
    private SessionState state = SessionState.KEY_AUTHENTICATION;
    private String c2fa;

    /**
     * Returns the user of the connected client.
     * 
     * @return The user of the connected client.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user of the connected client.
     * 
     * @param user The user for this connected client.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the device of the connected client.
     * 
     * @return The device of the connected client.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Sets the device of the connected client.
     * 
     * @param device The device for this connected client.
     */
    public void setDevice(Device device) {
        this.device = device;
    }

    /**
     * Returns the last sent nonce to the connected client.
     * 
     * @return The last sent nonce to the connected client.
     */
    public long getNonce() {
        return nonce;
    }

    /**
     * Sets the last sent nonce to the connected client.
     * 
     * @param nonce The last sent nonce to the connected client.
     */
    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    /**
     * Returns if the connected client is a new user.
     * 
     * @return True if the connected client has a new user, false otherwise.
     */
    public boolean isNewUser() {
        return newUser;
    }

    /**
     * Sets the value that determines if the connected client is a new user.
     * 
     * @param newUser Value that determines if the connected client is a new user.
     */
    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    /**
     * Returns the current state of the client's authentication.
     * 
     * @return The current state of the client's authentication.
     */
    public SessionState getState() {
        return state;
    }

    /**
     * Sets the current state of the client's anthentication.
     * 
     * @param state The next state of the client's authentication
     */
    public void setState(SessionState state) {
        this.state = state;
    }

    /**
     * Returns the last sent C2FA code to the client.
     * 
     * @return The last sent C2FA code to the client.
     */
    public String getC2fa() {
        return c2fa;
    }

    /**
     * Sets the last sent C2FA code to the client.
     * 
     * @param c2fa The last sent C2FA code to client.
     */
    public void setC2fa(String c2fa) {
        this.c2fa = c2fa;
    }
}
