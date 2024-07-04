package iotserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import common.OpCode;
import common.Utils;
import common.data.EncryptedData;
import common.messages.DefaultMessages;
import common.messages.Message;
import common.messages.types.response.*;
import common.messages.types.request.*;
import iotserver.managers.DeviceManager;
import iotserver.managers.DomainManager;
import iotserver.managers.UserManager;
import iotserver.persistance.PersistanceUtils;
import iotserver.utils.CommandLineArgs;
import iotserver.utils.ServerLogger;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that contains the communication layer between the server and a
 *         device.
 */
public class IoTServerSkel {

    private static final Logger LOGGER = ServerLogger.getLogger(IoTServerSkel.class.getSimpleName());
    private static final DomainManager DOMAIN_MANAGER = DomainManager.getInstance();
    private static final UserManager USER_MANAGER = UserManager.getInstance();
    private static final DeviceManager DEVICE_MANAGER = DeviceManager.getInstance();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String EMAIL_API_URL = "https://lmpinto.eu.pythonanywhere.com/2FA?e=%s&c=%s&a=%s";
    private static final String CLIENT_DETAILS_FILE = "clientDetails.txt";

    private final SessionInfo sessionInfo = new SessionInfo();
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final CommandLineArgs commandLineArgs;
    private final Key secretKey;

    private boolean shouldClose = false;

    /**
     * Constructor of the class.
     * 
     * @param socket          The socket of the connected device.
     * @param commandLineArgs the command line arguments of the program.
     * @throws IOException If it fails to create the input and output streams.
     */
    public IoTServerSkel(Socket socket, CommandLineArgs commandLineArgs, Key secretKey) throws IOException {
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.commandLineArgs = commandLineArgs;
        this.secretKey = secretKey;
    }

    /**
     * Handles a message sent to the server, calling the specific method to handle
     * it.
     * 
     * @return True if the connection to the client should end, false otherwise.
     */
    public boolean handleMessage() {
        try {
            Message message = (Message) this.in.readObject();
            Message response = null;
            switch (message.getOpCode()) {
                case OP_CREATE:
                    response = handleCreate(message);
                    break;
                case OP_ADD:
                    response = handleAdd(message);
                    break;
                case OP_RD:
                    response = handleRD(message);
                    break;
                case OP_ET:
                    response = handleET(message);
                    break;
                case OP_EI:
                    response = handleEI(message);
                    break;
                case OP_RT:
                    response = handleRT(message);
                    break;
                case OP_RI:
                    response = handleRI(message);
                    break;
                case OP_MD:
                    response = handleMD(message);
                    break;
                case OP_DOMAIN_KEYS:
                    response = handleDomainKeys(message);
                    break;
                case OP_KEY_AUTHENTICATION:
                    response = handleKeyAuthentication(message);
                    break;
                case OP_SIGNED_DATA:
                    response = handleSignedData(message);
                    break;
                case OP_2FA_AUTHENTICATION:
                    response = handle2FAAuthentication(message);
                    break;
                case OP_REMOTE_ATTESTATION:
                    response = handleRemoteAttestation(message);
                    break;
                case OP_REMOTE_ATTESTATION_HASH:
                    response = handleRemoteAttestationHash(message);
                    break;
                default:
                    LOGGER.warning(() -> "Invalid OpCode received!");
                    break;
            }
            if (response != null) {
                this.out.writeObject(response);
                this.out.flush();
            }
        } catch (ClassNotFoundException | IOException e) {
            LOGGER.warning(() -> "Couldn't read client message!");
            closeConnection();
        }
        return shouldClose;
    }

    /**
     * Requests to close the connection of the client and turn off is device.
     */
    private void closeConnection() {
        this.shouldClose = true;
        Device device = this.sessionInfo.getDevice();
        if (device != null)
            device.turnOff();
    }

    /**
     * Handles the key authentication with the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     * @throws IOException If it fails to read the message.
     */
    private Message handleKeyAuthentication(Message message) throws IOException {
        if (this.sessionInfo.getState() != SessionState.KEY_AUTHENTICATION) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof KeyAuthenticationRequestData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        KeyAuthenticationRequestData data = (KeyAuthenticationRequestData) message.getData();
        String userId = data.getUserId();
        long nonce = RANDOM.nextLong();
        boolean newUser = !USER_MANAGER.userExists(userId);
        this.sessionInfo.setNonce(nonce);
        this.sessionInfo.setNewUser(newUser);
        this.sessionInfo.setState(SessionState.KEY_AUTHENTICATION_STEP2);
        if (!newUser)
            this.sessionInfo.setUser(USER_MANAGER.getUser(userId));
        else
            this.sessionInfo.setUser(new User(userId, null));
        return new Message(OpCode.OK, new KeyAuthenticationResponseData(newUser, nonce));
    }

    /**
     * Handles the signed data validation with the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleSignedData(Message message) {
        if (this.sessionInfo.getState() != SessionState.KEY_AUTHENTICATION_STEP2) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof KeyAuthenticationSignedData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        KeyAuthenticationSignedData data = (KeyAuthenticationSignedData) message.getData();
        Certificate certificate;
        File cerFile = new File("certs", this.sessionInfo.getUser().getId() + ".cer");
        if (this.sessionInfo.isNewUser()) {
            certificate = data.getCertificate();
            try {
                saveCertificateToFile(certificate, cerFile);
            } catch (CertificateEncodingException | IOException e) {
                LOGGER.warning(() -> "Failed to save user certificate file!");
                closeConnection();
                return null;
            }
        } else {
            try {
                certificate = loadCertificateFromFile(cerFile);
            } catch (CertificateException | IOException e) {
                LOGGER.warning(() -> "Failed to load user certificate file!");
                closeConnection();
                return null;
            }
        }
        SignedObject so = data.getSignedObject();
        try {
            if (so.verify(certificate.getPublicKey(), Signature.getInstance(so.getAlgorithm()))) {
                this.sessionInfo.setState(SessionState.TWO_FACTOR_AUTHENTICATION);
                if (this.sessionInfo.isNewUser()) {
                    User user = USER_MANAGER.createUser(this.sessionInfo.getUser().getId(), cerFile);
                    if (user != null)
                        this.sessionInfo.setUser(user);
                    else
                        return DefaultMessages.NOK_MESSAGE;
                }
                String c2fa = String.format("%05d", RANDOM.nextInt(100000));
                URL url = URI.create(String.format(EMAIL_API_URL, this.sessionInfo.getUser().getId(), c2fa,
                        commandLineArgs.getApiKey())).toURL();
                int status;
                do {
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setRequestMethod("GET");
                    status = http.getResponseCode();
                } while (status != 200);
                this.sessionInfo.setC2fa(c2fa);
                return DefaultMessages.OK_MESSAGE;
            }
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            LOGGER.warning(() -> "Failed to read user signature");
        }
        return DefaultMessages.NOK_MESSAGE;
    }

    /**
     * Handles the 2FA authentication with the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handle2FAAuthentication(Message message) {
        if (this.sessionInfo.getState() != SessionState.TWO_FACTOR_AUTHENTICATION) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof FA2AuthenticationRequestData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        FA2AuthenticationRequestData data = (FA2AuthenticationRequestData) message.getData();
        if (!data.getUserCode().equals(this.sessionInfo.getC2fa()))
            return DefaultMessages.NOK_MESSAGE;
        this.sessionInfo.setState(SessionState.ATTESTATION);
        return DefaultMessages.OK_MESSAGE;
    }

    /**
     * Handles the remote attestation with the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleRemoteAttestation(Message message) {
        if (this.sessionInfo.getState() != SessionState.ATTESTATION) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof RemoteAttestationRequestData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        RemoteAttestationRequestData data = (RemoteAttestationRequestData) message.getData();
        String userIdDevId = this.sessionInfo.getUser().getId() + ":" + data.getDevId();
        Device device;
        synchronized (DEVICE_MANAGER) {
            if (DEVICE_MANAGER.deviceExists(userIdDevId)) {
                device = DEVICE_MANAGER.getDevice(userIdDevId);
                LOGGER.info(() -> "Device " + device.toString() + " already exists checking if it is on!");
                if (device.isOn())
                    return DefaultMessages.NOK_MESSAGE;
            } else {
                device = DEVICE_MANAGER.createDevice(this.sessionInfo.getUser(), data.getDevId());
            }
            this.sessionInfo.setDevice(device);
            device.turnOn();
        }
        this.sessionInfo.setState(SessionState.ATTESTATION_STEP2);
        long nonce = RANDOM.nextLong();
        this.sessionInfo.setNonce(nonce);
        return new Message(OpCode.OK, new RemoteAttestationResponseData(nonce));
    }

    /**
     * Handles the hash validation with the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleRemoteAttestationHash(Message message) {
        if (this.sessionInfo.getState() != SessionState.ATTESTATION_STEP2) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof RemoteAttestationRequestDataStep2)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        RemoteAttestationRequestDataStep2 data = (RemoteAttestationRequestDataStep2) message.getData();
        String filePath = "";
        String hmacBase64;
        try (Scanner sc = new Scanner(new File(CLIENT_DETAILS_FILE))) {
            filePath = sc.nextLine();
            hmacBase64 = sc.nextLine();
        } catch (FileNotFoundException e) {
            LOGGER.warning(() -> "Client details file not found!");
            closeConnection();
            return null;
        }
        byte[] filePathBytes = filePath.getBytes();
        byte[] fileHmac = Base64.getDecoder().decode(hmacBase64);
        try {
            byte[] calculatedHmac = PersistanceUtils.hmac(filePathBytes, this.secretKey);
            if (!Arrays.equals(calculatedHmac, fileHmac)) {
                LOGGER.severe(() -> "Invalid " + CLIENT_DETAILS_FILE + " hmac!");
                closeConnection();
                return null;
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            LOGGER.warning(() -> "Couldn't calculate hmac of " + CLIENT_DETAILS_FILE + " file!");
            closeConnection();
            return null;
        }
        byte[] deviceHash = data.getDevHash();
        byte[] fileBytes = new byte[0];
        try {
            fileBytes = Files.readAllBytes(new File(filePath).toPath());
        } catch (IOException e) {
            LOGGER.warning(() -> "Couldn't read client details file!");
            closeConnection();
            return null;
        }
        byte[] nonceBytes = Utils.longToBytes(this.sessionInfo.getNonce());
        byte[] concatBytes;
        byte[] hash;
        try {
            concatBytes = Utils.concatByteArrays(fileBytes, nonceBytes);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(concatBytes);
            if (Arrays.equals(deviceHash, hash)) {
                this.sessionInfo.setState(SessionState.COMPLETED_AUTHENTICATED);
                return DefaultMessages.OK_MESSAGE;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.warning(() -> "Couldn't get hash!");
            closeConnection();
            return null;
        }
        return DefaultMessages.NOK_MESSAGE;
    }

    /**
     * Handles the create command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleCreate(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof CreateDomainMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        CreateDomainMessageData data = (CreateDomainMessageData) message.getData();
        String domainName = data.getDomainName();
        Domain result = DOMAIN_MANAGER.createDomain(domainName, this.sessionInfo.getUser());
        return result == null ? DefaultMessages.NOK_MESSAGE : DefaultMessages.OK_MESSAGE;
    }

    /**
     * Handles the add command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleAdd(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof AddUserMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        AddUserMessageData data = (AddUserMessageData) message.getData();
        if (!DOMAIN_MANAGER.domainsExists(data.getDomainName())) {
            return new Message(OpCode.NODM);
        }
        Domain domain = DOMAIN_MANAGER.getDomain(data.getDomainName());
        if (!domain.getOwner().equals(this.sessionInfo.getUser())) {
            return new Message(OpCode.NOPERM);
        }
        User user = USER_MANAGER.getUser(data.getUserId());
        if (user == null) {
            return new Message(OpCode.NOUSER);
        }
        boolean added = domain.addUser(user, data.getDomainKey());
        return added ? DefaultMessages.OK_MESSAGE : new Message(OpCode.ALREADY_ADDED);
    }

    /**
     * Handles the register device command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleRD(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof RDMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        RDMessageData data = (RDMessageData) message.getData();
        if (!DOMAIN_MANAGER.domainsExists(data.getDomainName())) {
            return new Message(OpCode.NODM);
        }
        Domain domain = DOMAIN_MANAGER.getDomain(data.getDomainName());
        if (!domain.containsUser(this.sessionInfo.getUser())) {
            return new Message(OpCode.NOPERM);
        }
        boolean result = domain.addDevice(this.sessionInfo.getDevice());
        return result ? DefaultMessages.OK_MESSAGE : new Message(OpCode.ALREADY_ADDED);
    }

    /**
     * Handles the send temperature command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleET(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof ETMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        ETMessageData data = (ETMessageData) message.getData();
        Map<String, EncryptedData> temperatures = data.getDomainsTemperatures();
        temperatures.forEach((domainName, temperature) -> {
            Domain domain = DOMAIN_MANAGER.getDomain(domainName);
            Device device = this.sessionInfo.getDevice();
            if (domain != null && domain.containsDevice(device)) {
                domain.updateDeviceTemp(device, temperature);
            }
        });
        return DefaultMessages.OK_MESSAGE;
    }

    /**
     * Handles the send image command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleEI(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof EIMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        EIMessageData data = (EIMessageData) message.getData();
        Map<String, EncryptedData> images = data.getImages();
        images.forEach((domainName, image) -> {
            Domain domain = DOMAIN_MANAGER.getDomain(domainName);
            Device device = this.sessionInfo.getDevice();
            if (domain != null && domain.containsDevice(device)) {
                domain.updateDeviceImage(device, image);
            }
        });
        return DefaultMessages.OK_MESSAGE;
    }

    /**
     * Handles the receive temperature command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleRT(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof RTMessageData)) {
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        }
        RTMessageData data = (RTMessageData) message.getData();
        Domain domain = DOMAIN_MANAGER.getDomain(data.getDomainName());
        if (domain == null) {
            return new Message(OpCode.NODM);
        }
        if (!domain.containsUser(this.sessionInfo.getUser())) {
            return new Message(OpCode.NOPERM);
        }
        Map<String, EncryptedData> deviceTemperatures = new HashMap<>();
        for (Device device : domain.getDevices()) {
            EncryptedData temperature = domain.getDeviceTemperature(device);
            if (temperature != null)
                deviceTemperatures.put(device.toString(), temperature);
        }
        if (deviceTemperatures.isEmpty())
            return new Message(OpCode.NODATA);
        return new Message(OpCode.OK,
                new RTResponseData(deviceTemperatures, domain.getUserDomainKey(this.sessionInfo.getUser())));
    }

    /**
     * Handles the receive image command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleRI(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        if (!(message.getData() instanceof RIMessageData))
            return DefaultMessages.DATA_TYPE_ERROR_MESSAGE;
        RIMessageData data = (RIMessageData) message.getData();
        Device device = DeviceManager.getInstance().getDevice(data.getUserIdDevId());
        if (device == null) {
            return new Message(OpCode.NOID);
        }
        boolean foundDomain = false;
        for (Domain domain : DOMAIN_MANAGER.getUsersDomains(this.sessionInfo.getUser())) {
            if (domain.containsDevice(device)) {
                foundDomain = true;
                EncryptedData imageData = domain.getDeviceImage(device);
                if (imageData != null) {
                    RIResponseData responseData = new RIResponseData(imageData,
                            domain.getUserDomainKey(this.sessionInfo.getUser()));
                    return new Message(OpCode.OK, responseData);
                }
            }
        }
        return foundDomain ? new Message(OpCode.NODATA) : new Message(OpCode.NOPERM);
    }

    /**
     * Handles the mydomains command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleMD(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        List<Domain> domains = DOMAIN_MANAGER.getDeviceDomains(this.sessionInfo.getDevice());
        List<String> domainNames = domains.stream().map(d -> d.getName()).collect(Collectors.toList());
        return new Message(OpCode.OK, new MDResponseMessageData(domainNames));
    }

    /**
     * Handles the domain keys command sent by the device.
     * 
     * @param message The received message from the device.
     * @return The message that will be sent to the client as response.
     */
    private Message handleDomainKeys(Message message) {
        if (this.sessionInfo.getState() != SessionState.COMPLETED_AUTHENTICATED) {
            return DefaultMessages.SESSION_INFO_ERROR_MESSAGE;
        }
        List<Domain> domains = DOMAIN_MANAGER.getDeviceDomains(this.sessionInfo.getDevice());
        Map<String, byte[]> domainsKeys = new HashMap<>();
        for (Domain domain : domains) {
            byte[] domainKey = domain.getUserDomainKey(this.sessionInfo.getUser());
            domainsKeys.put(domain.getName(), domainKey);
        }
        return new Message(OpCode.OK, new DomainKeysResponseData(domainsKeys));
    }

    /**
     * Loads a certificate from a file.
     * 
     * @param file The file that contains the certificate
     * @return The loaded certificate.
     * @throws CertificateException  If it fails to load the certificate.
     * @throws FileNotFoundException If it fails to load the certificate.
     * @throws IOException           If it fails to load the certificate.
     */
    private Certificate loadCertificateFromFile(File file)
            throws CertificateException, FileNotFoundException, IOException {
        synchronized (file) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (FileInputStream in = new FileInputStream(file)) {
                return cf.generateCertificate(in);
            }
        }
    }

    /**
     * Saves a certificate to a file.
     * 
     * @param certificate The certificate to be saved.
     * @param file        The file to save the certificate.
     * @throws FileNotFoundException        If it fails to load the certificate.
     * @throws IOException                  If it fails to load the certificate.
     * @throws CertificateEncodingException If it fails to load the certificate.
     */
    private void saveCertificateToFile(Certificate certificate, File file)
            throws FileNotFoundException, IOException, CertificateEncodingException {
        synchronized (file) {
            file.getParentFile().mkdirs();
            byte[] encoded = certificate.getEncoded();
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(encoded);
            }
        }
    }
}
