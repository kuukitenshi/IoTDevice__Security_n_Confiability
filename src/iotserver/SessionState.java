package iotserver;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Enumeration that represents the diffent states of a client authentication.
 */
public enum SessionState {
    KEY_AUTHENTICATION,
    KEY_AUTHENTICATION_STEP2,
    TWO_FACTOR_AUTHENTICATION,
    ATTESTATION,
    ATTESTATION_STEP2,
    COMPLETED_AUTHENTICATED
}
