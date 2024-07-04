package common;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         An enemeration of the possible opcodes.
 */
public enum OpCode {

    OK,
    NOK,
    ERROR,

    NOPERM,
    NODM,
    NOUSER,
    NODATA,
    NOID,
    ALREADY_ADDED,

    OP_CREATE,
    OP_ADD,
    OP_RD,
    OP_ET,
    OP_EI,
    OP_RT,
    OP_RI,
    OP_MD,
    
    OP_DOMAIN_KEYS,
    OP_KEY_AUTHENTICATION,
    OP_SIGNED_DATA,
    OP_2FA_AUTHENTICATION,
    OP_REMOTE_ATTESTATION,
    OP_REMOTE_ATTESTATION_HASH
}
