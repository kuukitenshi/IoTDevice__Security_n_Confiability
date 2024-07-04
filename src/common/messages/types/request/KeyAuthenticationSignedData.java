package common.messages.types.request;

import java.security.SignedObject;
import java.security.cert.Certificate;

import common.messages.MessageData;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents a message that contains the parameters required
 *         to make a Key Authentication, more specifically the step to signed the nonce.
 */
public class KeyAuthenticationSignedData implements MessageData {

    private final SignedObject signedObject;
    private final Certificate certificate;

    /**
     * Constructor of the class
     * 
     * @param signedObject signed object that contains the signed nonce
     */
    public KeyAuthenticationSignedData(SignedObject signedObject) {
        this(signedObject, null);
    }

    /**
     * Constructor of the class
     * 
     * @param signedObject signed object that contains the signed nonce
     * @param certificate certificate with the public key of the client
     */
    public KeyAuthenticationSignedData(SignedObject signedObject, Certificate certificate) {
        this.signedObject = signedObject;
        this.certificate = certificate;
    }

    /**
     * Gets the signed object
     * 
     * @return the signed object
     */
    public SignedObject getSignedObject() {
        return signedObject;
    }
    
    /**
     * Gets the certificate of the client
     * 
     * @return the certificate
     */
    public Certificate getCertificate() {
        return certificate;
    }
}
