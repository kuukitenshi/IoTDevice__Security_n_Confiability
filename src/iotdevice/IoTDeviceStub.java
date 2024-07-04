package iotdevice;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import common.OpCode;
import common.Utils;
import common.data.EncryptedData;
import common.messages.Message;
import common.messages.types.request.*;
import common.messages.types.response.*;
import iotdevice.utils.CommandLineArgs;
import iotdevice.utils.Pair;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         This class is responsible for the communication between
 *         client-server, abstracting the answer/response codes.
 */
public class IoTDeviceStub {

    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final CommandLineArgs commandLineArgs;
    private final KeyStore keyStore;
    private final KeyStore trustStore;

    /**
     * Constructor of the class
     * 
     * @param sck             the socket to communicate with the server
     * @param commandLineArgs the command line arguments
     * @param keyStore        the keystore
     * @param trustStore      the truststore
     * @throws IOException if an I/O error occurs when creating the input and output
     *                     streams
     */
    public IoTDeviceStub(Socket sck, CommandLineArgs commandLineArgs, KeyStore keyStore, KeyStore trustStore)
            throws IOException {
        this.out = new ObjectOutputStream(sck.getOutputStream());
        this.in = new ObjectInputStream(sck.getInputStream());
        this.commandLineArgs = commandLineArgs;
        this.keyStore = keyStore;
        this.trustStore = trustStore;
    }

    /**
     * Send the operation code {@code OP_CREATE} and the domain's name to the
     * server.
     * 
     * @param domainName the name of the domain
     * @return true if the response code was OK or false otherwise
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     *                                be found
     */
    public boolean createDomain(String domainName) throws ClassNotFoundException, IOException {
        CreateDomainMessageData data = new CreateDomainMessageData(domainName);
        Message response = messageSendReceive(new Message(OpCode.OP_CREATE, data));
        return response.getOpCode() == OpCode.OK;
    }

    /**
     * Send the operation code {@code OP_ADD}, the user id, and the domain's name to
     * the server.
     * 
     * @param user       the user id
     * @param domainName the name of the domain
     * @ensures {@code \result != null}
     * @return the response code from the server
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     *                                be found
     */
    public OpCode addUser(String userId, String domainName, byte[] domainKey)
            throws ClassNotFoundException, IOException {
        AddUserMessageData data = new AddUserMessageData(userId, domainName, domainKey);
        Message response = messageSendReceive(new Message(OpCode.OP_ADD, data));
        return response.getOpCode();
    }

    /**
     * Send the operation code {@code OP_RD} and the domain's name to the server.
     * 
     * @param domainName the name of the domain
     * @ensures {@code \result != null}
     * @return the response code from the server
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     */
    public OpCode registerDevice(String domainName) throws ClassNotFoundException, IOException {
        RDMessageData data = new RDMessageData(domainName);
        Message response = messageSendReceive(new Message(OpCode.OP_RD, data));
        return response.getOpCode();
    }

    /**
     * Send the operation code {@code OP_ET} and the temperature to the server.
     * 
     * @param temperature the float that represents the temperature
     * @return true if the response code was OK or false otherwise
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     */
    public boolean sendTemperature(float temperature) throws ClassNotFoundException, IOException {
        Map<String, Key> domainKeys = getDomainKeys();
        Map<String, EncryptedData> temperatures = new HashMap<>();
        for (String domain : domainKeys.keySet()) {
            Key key = domainKeys.get(domain);
            Cipher cipher;
            try {
                cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] encryptedTemperature = cipher.doFinal(Utils.floatToBytes(temperature));
                byte[] ivParams = cipher.getParameters().getEncoded();
                temperatures.put(domain, new EncryptedData(encryptedTemperature, ivParams));

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                    | BadPaddingException e) {
                System.err.println("Failed to encrypt temperature for domain " + domain);
            }
        }
        ETMessageData data = new ETMessageData(temperatures);
        Message response = messageSendReceive(new Message(OpCode.OP_ET, data));
        return response.getOpCode() == OpCode.OK;
    }

    /**
     * Send the operation code {@code OP_EI} and the image file name.
     * 
     * @param imageFile the image's file name
     * @return true if the response code was OK or false otherwise
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     */
    public boolean sendImage(File imageFile) throws ClassNotFoundException, IOException {
        Map<String, Key> domainKeys = getDomainKeys();
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        Map<String, EncryptedData> encryptedImages = new HashMap<>();
        for (String domain : domainKeys.keySet()) {
            Key key = domainKeys.get(domain);
            try {
                Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                byte[] iv = cipher.getParameters().getEncoded();
                byte[] encrypted = cipher.doFinal(imageBytes);
                encryptedImages.put(domain, new EncryptedData(encrypted, iv));

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                    | BadPaddingException e) {
                System.err.println("Failed to encrypt image for domain " + domain);
            }
        }
        EIMessageData data = new EIMessageData(encryptedImages);
        Message response = messageSendReceive(new Message(OpCode.OP_EI, data));
        return response.getOpCode() == OpCode.OK;
    }

    /**
     * Send the operation code {@code OP_RT} and the domain's name to the server.
     * 
     * @param domainName the name of the domain
     * @ensures {@code \result != null}
     * @return a pair with the response code from the server and the map with the
     *         device id and its temperature.
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     */
    public Pair<OpCode, Map<String, Float>> receiveTemperature(String domainName)
            throws ClassNotFoundException, IOException {
        RTMessageData reqData = new RTMessageData(domainName);
        Message response = messageSendReceive(new Message(OpCode.OP_RT, reqData));
        if (response.getOpCode() != OpCode.OK) {
            return new Pair<OpCode, Map<String, Float>>(response.getOpCode(), null);
        }
        RTResponseData resData = (RTResponseData) response.getData();
        Map<String, EncryptedData> encryptedTemps = resData.getDeviceTemperatures();
        Map<String, Float> temperatures = new HashMap<>();

        byte[] wrappedKey = resData.getWrappedDomainKey();
        for (String device : encryptedTemps.keySet()) {
            EncryptedData encryptedData = encryptedTemps.get(device);
            try {
                byte[] decryptedBytes = decryptEncryptedData(encryptedData, wrappedKey);
                float temperature = Utils.bytesToFloat(decryptedBytes);
                temperatures.put(device, temperature);
                
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                    | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException
                    | UnrecoverableKeyException | KeyStoreException e) {
                System.err.println("Failed to decrypt temperature of device " + device);
                return new Pair<OpCode, Map<String, Float>>(OpCode.ERROR, null);
            }
        }
        return new Pair<>(response.getOpCode(), temperatures);
    }

    /**
     * Send the operation code {@code OP_RI} and the pair user id and device id.
     * 
     * @param userIdDevId the pair <user_id>:<dev_id>
     * @ensures {@code \result != null}
     * @return a pair wtih the code from the server and the image received.a pair
     *         with the response code from the server and the image bytes.
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     */
    public Pair<OpCode, byte[]> receiveImage(String userIdDevId) throws ClassNotFoundException, IOException {
        RIMessageData data = new RIMessageData(userIdDevId);
        Message response = messageSendReceive(new Message(OpCode.OP_RI, data));
        if (response.getOpCode() != OpCode.OK)
            return new Pair<OpCode, byte[]>(response.getOpCode(), null);
        RIResponseData responseData = (RIResponseData) response.getData();
        EncryptedData encryptedData = responseData.getEncryptedData();
        byte[] wrappedKey = responseData.getWrappedDomainKey();
        byte[] imageBytes;
        try {
            imageBytes = decryptEncryptedData(encryptedData, wrappedKey);
        } catch (UnrecoverableKeyException | InvalidKeyException | NoSuchAlgorithmException | KeyStoreException
                | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | BadPaddingException | IOException e) {
            System.err.println("Failed to decrypt image of device " + userIdDevId);
            return new Pair<OpCode, byte[]>(OpCode.ERROR, null);
        }
        return new Pair<OpCode, byte[]>(response.getOpCode(), imageBytes);
    }

    public List<String> listDomains() throws IOException, ClassNotFoundException {
        Message message = new Message(OpCode.OP_MD);
        Message response = messageSendReceive(message);
        MDResponseMessageData data = (MDResponseMessageData) response.getData();
        return data.getDomainNames();
    }

    /**
     * Sends a message object and receives it.
     * 
     * @param message the Message to be sent
     * @return the received message
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     *                                be
     */
    private Message messageSendReceive(Message message) throws IOException, ClassNotFoundException {
        try {
            this.out.writeObject(message);
            this.out.flush();
            return (Message) this.in.readObject();
        } catch (Exception e) {
            System.err.println("Failed to read server message!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Gets the keys from all the domains
     * 
     * @return a map with the domain name and its key
     * @throws IOException            if an I/O error occurs when sending the
     *                                message
     * @throws ClassNotFoundException if the class of a serialized object could not
     *                                be
     */
    private Map<String, Key> getDomainKeys() throws ClassNotFoundException, IOException {
        Message domainKeysResponse = messageSendReceive(new Message(OpCode.OP_DOMAIN_KEYS));
        DomainKeysResponseData domainKeysData = (DomainKeysResponseData) domainKeysResponse.getData();

        Map<String, byte[]> wrappedDomainKeys = domainKeysData.getDomainKeys();
        Map<String, Key> domainKeys = new HashMap<>();

        wrappedDomainKeys.forEach((domain, warppedKey) -> {
            try {
                Key privateKey = this.keyStore.getKey("keyRSA", this.commandLineArgs.getKeyStorePassword().toCharArray());
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.UNWRAP_MODE, privateKey);
                Key key = cipher.unwrap(warppedKey, "PBEWithHmacSHA256AndAES_128", Cipher.SECRET_KEY);
                domainKeys.put(domain, key);
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | InvalidKeyException
                    | NoSuchPaddingException e) {
                System.err.println("Failed to unwrap domain key of domain " + domain + "!");
            }
        });
        return domainKeys;
    }

    /**
     * Authenticates a user by getting its nonce
     * 
     * @param userId the id of the user being authenticated
     * @throws IOException            if an I/O error occurs when sending message
     * @throws ClassNotFoundException if the class of a serialized object could not be
     */
    public void keyAuthentication(String userId) {
        try {
            KeyAuthenticationRequestData authData = new KeyAuthenticationRequestData(userId);
            Message responseAuth = messageSendReceive(new Message(OpCode.OP_KEY_AUTHENTICATION, authData));
            KeyAuthenticationResponseData responseData = (KeyAuthenticationResponseData) responseAuth.getData();
            long nonce = responseData.getNonce();

            PrivateKey privateKey;
            SignedObject signedNonce;
            try {
                privateKey = (PrivateKey) keyStore.getKey("keyRSA",
                        commandLineArgs.getKeyStorePassword().toCharArray());
                signedNonce = new SignedObject(nonce, privateKey, Signature.getInstance("MD5withRSA"));
            } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | InvalidKeyException
                    | SignatureException e) {
                System.err.println("Failed to sign nonce for user authentication!");
                System.exit(-1);
                return;
            }
            KeyAuthenticationSignedData signedData;
            if (responseData.isNewUser()) {
                System.out.println("New user!");
                Certificate certificate;
                try {
                    certificate = this.trustStore.getCertificate(userId);
                } catch (KeyStoreException e) {
                    System.err.println("Failed to get certificate!");
                    System.exit(-1);
                    return;
                }
                signedData = new KeyAuthenticationSignedData(signedNonce, certificate);
            } else {
                signedData = new KeyAuthenticationSignedData(signedNonce);
            }
            Message responseSigned = messageSendReceive(new Message(OpCode.OP_SIGNED_DATA, signedData));
            OpCode opCodeSigned = responseSigned.getOpCode();
            if (opCodeSigned == OpCode.OK) {
                System.out.println("User authenticated successfully!");
            } else {
                System.out.println("User authentication failed!");
                System.exit(-1);
            }
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Failed to send messages to server! (keyauth)");
            System.exit(-1);
        }
    }

    /**
     * Authenticates a user by means of 2 Factor Authentication (2FA)
     * 
     * @param userId the id of the user being authenticated
     */
    public void FA2Authentication(String userCode) {
        try {
            FA2AuthenticationRequestData authData = new FA2AuthenticationRequestData(userCode);
            Message responseAuth = messageSendReceive(new Message(OpCode.OP_2FA_AUTHENTICATION, authData));
            OpCode opCodeAuth = responseAuth.getOpCode();
            if (opCodeAuth == OpCode.OK) {
                System.out.println("2FA authentication successful!");
            } else {
                System.out.println("2FA authentication failed!");
                System.exit(-1);
            }
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Failed to send messages to server! (2fa)");
            System.exit(-1);
        }
    }

    /**
     * Does remote attestation of the user's device
     * 
     * @param deviceId  the id of the device being attested
     * @param fileBytes device bytes to concatenate with the received nonce
     */
    public void remoteAttestation(int deviceId, byte[] fileBytes) {
        try {
            RemoteAttestationRequestData attestationData = new RemoteAttestationRequestData(deviceId);
            Message responseAttestation = messageSendReceive(
                    new Message(OpCode.OP_REMOTE_ATTESTATION, attestationData));
            OpCode opCodeAttestation = responseAttestation.getOpCode();
            if (opCodeAttestation != OpCode.OK) {
                System.out.println("Remote attestation failed! The device is already active!");
                System.exit(-1);
            }
            RemoteAttestationResponseData responseData = (RemoteAttestationResponseData) responseAttestation.getData();
            long nonce = responseData.getNonce();
            byte[] nonceBytes = Utils.longToBytes(nonce);
            byte[] concatBytes = Utils.concatByteArrays(fileBytes, nonceBytes);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(concatBytes);
            RemoteAttestationRequestDataStep2 hashData = new RemoteAttestationRequestDataStep2(hash);
            Message responseHash = messageSendReceive(new Message(OpCode.OP_REMOTE_ATTESTATION_HASH, hashData));
            OpCode opCodeHash = responseHash.getOpCode();
            if (opCodeHash == OpCode.OK) {
                System.out.println("Remote attestation successful!");
            } else {
                System.out.println("Remote attestation failed!");
                System.exit(-1);
            }
        } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
            System.err.println("Failed to send messages to server! (att)");
            System.exit(-1);
        }
    }

    /**
     * Decrypts data with the parameters of PBE with Hmac and AES (128 bits)
     * 
     * @param encryptedData the data to be decrypted
     * @param wrappedKey    the key to use to decrypt the data
     * @throws NoSuchAlgorithmException           if the algorithm is not found
     * @throws UnrecoverableKeyException          if the key is not recoverable
     * @throws KeyStoreException                  if the keystore is not found
     * @throws IOException                        if an I/O error occurs
     * @throws NoSuchPaddingException             if the padding is not found
     * @throws InvalidKeyException                if the key is invalid
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are
     *                                            invalid
     * @throws IllegalBlockSizeException          if the block size is invalid
     * @throws BadPaddingException                if the padding is invalid
     */
    private byte[] decryptEncryptedData(EncryptedData encryptedData, byte[] wrappedKey) throws NoSuchAlgorithmException,
            UnrecoverableKeyException, KeyStoreException, IOException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
        p.init(encryptedData.getIV());
        Key privateKey = this.keyStore.getKey("keyRSA", this.commandLineArgs.getKeyStorePassword().toCharArray());

        Cipher unwrapCipher = Cipher.getInstance("RSA");
        unwrapCipher.init(Cipher.UNWRAP_MODE, privateKey);
        Key key = unwrapCipher.unwrap(wrappedKey, "PBEWithHmacSHA256AndAES_128", Cipher.SECRET_KEY);

        Cipher cipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        cipher.init(Cipher.DECRYPT_MODE, key, p);
        return cipher.doFinal(encryptedData.getData());
    }
}
