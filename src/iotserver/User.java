package iotserver;

import java.io.File;
import java.util.Objects;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represent a user.
 */
public class User {

    private final String id;
    private final File certificateFile;

    /**
     * Constructor of the class.
     * 
     * @param id              The unique user id of this user.
     * @param certificateFile The file of the user's certificate.
     */
    public User(String id, File certificateFile) {
        this.id = id;
        this.certificateFile = certificateFile;
    }

    /**
     * Returns the unique userId of this user.
     * 
     * @return The userId of this user.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the file of this user's certificate.
     * 
     * @return The file of this user's certificate.
     */
    public File getCertificateFile() {
        return certificateFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        User other = (User) obj;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
