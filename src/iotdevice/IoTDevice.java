package iotdevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import common.CommandLineArgsParseException;
import common.OpCode;
import common.Utils;
import iotdevice.utils.CommandLineArgs;
import iotdevice.utils.Pair;

/**
 * @author 58180 Rodrigo Correia
 * @author 58188 Laura Cunha
 * @author 58199 Daniela Camarinha
 * 
 *         Class that represents the Client service.
 */
public class IoTDevice {

    private static final Scanner SC = new Scanner(System.in);

    private static IoTDeviceStub ioTDevStub;
    private static String userId;
    private static int deviceId;
    private static KeyStore trustStore;

    public static void main(String[] args) {
        try {
            CommandLineArgs commandLineArgs = CommandLineArgs.parse(args);
            userId = commandLineArgs.getUserId();
            deviceId = commandLineArgs.getDeviceId();

            trustStore = loadTrustStore(commandLineArgs);
            KeyStore keyStore = loadKeyStore(commandLineArgs);

            Socket clientSocket = createClientSocket(commandLineArgs);
            ioTDevStub = new IoTDeviceStub(clientSocket, commandLineArgs, keyStore, trustStore);

            ioTDevStub.keyAuthentication(userId);
            ioTDevStub.FA2Authentication(getCfaCode());
            ioTDevStub.remoteAttestation(deviceId, getBytesJarFile());

            while (true) {
                String command = menuOptions();
                handlerOptions(command);
            }
        } catch (CommandLineArgsParseException e) {
            System.err.println("Failed to parse command line args: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Failed to initialize socket!");
        }
    }

    /**
     * Loads the keystore specified in the command line arguments.
     * 
     * @param commandLineArgs The command line arguments of the program.
     * @return The load Keystore.
     */
    private static KeyStore loadKeyStore(CommandLineArgs commandLineArgs) {
        try {
            return Utils.loadKeyStoreFromFile(commandLineArgs.getKeyStore(),
                    commandLineArgs.getKeyStorePassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.err.println("Failed to load keystore!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Loads the truststore specified in the command line arguments.
     * 
     * @param commandLineArgs The command line arguments of the program.
     * @return The load truststore.
     */
    private static KeyStore loadTrustStore(CommandLineArgs commandLineArgs) {
        try {
            return Utils.loadKeyStoreFromFile(commandLineArgs.getTrustStore(),
                    commandLineArgs.getKeyStorePassword().toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.err.println("Failed to load truststore!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Creates a SSLSocket to connect to the server.
     * 
     * @param commandLineArgs The command line arguments of the program.
     * @return The created Socket.
     */
    private static Socket createClientSocket(CommandLineArgs commandLineArgs) {
        System.setProperty("javax.net.ssl.trustStore", commandLineArgs.getTrustStore());
        System.setProperty("javax.net.ssl.trustStorePassword", commandLineArgs.getKeyStorePassword());
        SocketFactory sf = SSLSocketFactory.getDefault();
        try {
            return sf.createSocket(commandLineArgs.getServerAddress(), commandLineArgs.getPort());
        } catch (IOException e) {
            System.err.println("Failed to create SSL client socket!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Displays the options menu and reads the user input.
     * 
     * @return The user input.
     */
    private static String menuOptions() {
        System.out.println("\n>----------- COMMANDS -----------<");
        System.out.println("> CREATE <dm> \t\t\t\t- create a new domain");
        System.out.println(
                "> ADD <user1> <dm> <password-domain> \t- add an user to a domain using the password of the domain");
        System.out.println("> RD <dm> \t\t\t\t- register your device in a domain");
        System.out.println("> ET <float> \t\t\t\t- send device temperature info");
        System.out.println("> EI <filename.jpg> \t\t\t- send device image");
        System.out.println("> RT <dm> \t\t\t\t- receive all temperatures from a domain");
        System.out.println("> RI <user-id>:<dev_id> \t\t- receive an imagem from a specific device");
        System.out.println("> MYDOMAINS  \t\t\t\t- prints the list of domains the device belongs to\n");
        System.out.print(String.format("$(%s:%s)> Insert command: ", userId, deviceId));
        return SC.nextLine();
    }

    /**
     * Handles command that the user inputs.
     * 
     * @param command The command that the user inputs.
     */
    private static void handlerOptions(String command) {
        String[] cmd = command.split(" ");
        if (cmd.length == 0) {
            System.out.println("Invalid command. Try again...");
            return;
        }
        String option;
        try {
            option = cmd[0].toUpperCase();
            switch (option) {
                case "CREATE":
                    handlerCreate(cmd);
                    break;
                case "ADD":
                    handlerAdd(cmd);
                    break;
                case "RD":
                    handlerRD(cmd);
                    break;
                case "ET":
                    handlerET(cmd);
                    break;
                case "EI":
                    handlerEI(cmd);
                    break;
                case "RT":
                    handlerRT(cmd);
                    break;
                case "RI":
                    handlerRI(cmd);
                    break;
                case "MYDOMAINS":
                    handlerMyDomains();
                    break;
                default:
                    System.out.println("Invalid option. Try again...");
                    break;
            }
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Invalid option. Try again...");
        }
    }

    /**
     * Check if a command has the required number of parameters.
     * 
     * @param cmd       The command and parameters.
     * @param numParams Num of parameters required.
     * @return True if the number of parameters is correct, false otherwise.
     */
    private static boolean isValidCommand(String[] cmd, int numParams) {
        if (cmd.length != numParams) {
            System.out.println("Invalid command. Try again...");
            return false;
        }
        return true;
    }

    /**
     * Handles the create command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerCreate(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String domainName = cmd[1];
            if (domainName.isEmpty()) {
                System.err.println("Error: The domain name shouldn't be empty!");
                return;
            }
            boolean wasCreated = ioTDevStub.createDomain(domainName);
            if (wasCreated)
                System.out.println("Domain named " + domainName + " was created!");
            else
                System.out.println("Domain named " + domainName + " already exists!");
        }
    }

    /**
     * Handles the add command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerAdd(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 4)) {
            String username = cmd[1];
            String domainName = cmd[2];
            String domainPassword = cmd[3];
            if (username.isEmpty() || domainName.isEmpty() || domainPassword.isEmpty()) {
                System.err.println("Error: The username, domain name and domain password shouldn't be empty!");
                return;
            }
            Key domainKeyPBE = genPBEKey(domainPassword, domainName);
            byte[] domainPasswordBytes = cipherDomainKeyWithPublicKey(username, domainKeyPBE);
            if (domainPasswordBytes == null) {
                System.err.println("Users does not exists!");
                return;
            }
            OpCode code = ioTDevStub.addUser(username, domainName, domainPasswordBytes);
            if (code == OpCode.OK)
                System.out.println("User named " + username + " was added to domain " + domainName + "!");
            else if (code == OpCode.NOPERM)
                System.out.println("The given user isn't the owner of the domain!");
            else if (code == OpCode.NODM)
                System.out.println("The given domain doesn't exists!");
            else if (code == OpCode.NOUSER)
                System.out.println("The given user doesn't exists!");
            else if (code == OpCode.ALREADY_ADDED)
                System.out.println("User " + username + " already belongs to domain " + domainName + "!");
        }
    }

    /**
     * Handles the register device command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerRD(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String domainName = cmd[1];
            if (domainName.isEmpty()) {
                System.err.println("Error: The domain name shouldn't be empty!");
                return;
            }
            OpCode code = ioTDevStub.registerDevice(domainName);
            if (code == OpCode.OK)
                System.out.println("The current device was registed on the domain " + domainName + "!");
            else if (code == OpCode.NOPERM)
                System.out.println("The given user doesn't belong to the domain!");
            else if (code == OpCode.NODM)
                System.out.println("The given domain doesn't exists!");
            else if (code == OpCode.ALREADY_ADDED)
                System.out.println("The device is already registered in this domain.");
        }
    }

    /**
     * Handles the send temperature command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerET(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String tempString = cmd[1];
            try {
                float temp = Float.parseFloat(tempString);
                boolean wasSent = ioTDevStub.sendTemperature(temp);
                if (wasSent)
                    System.out.println("The temperature " + temp + " was sent to server!");
                else
                    System.out.println("The temperature can't be sent to server!");
            } catch (NumberFormatException e) {
                System.err.println("Temperature should be a number!");
                return;
            }
        }
    }

    /**
     * Handles the send image command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerEI(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String filename = cmd[1];
            if (filename.isEmpty()) {
                System.err.println("Image name mustn't be empty!");
                return;
            }
            File fileImg = new File(filename);
            if (!fileImg.exists()) {
                System.out.println("Image file not found.");
                return;
            }
            boolean wasSent = ioTDevStub.sendImage(fileImg);
            if (wasSent)
                System.out.println("Image sent to server!");
            else
                System.out.println("The server didn't accept the image!");
        }
    }

    /**
     * Handles the receive temperature command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerRT(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String domainName = cmd[1];
            if (domainName.isEmpty()) {
                System.err.println("Error: The domain name shouldn't be empty!");
                return;
            }
            Pair<OpCode, Map<String, Float>> result = ioTDevStub.receiveTemperature(domainName);
            OpCode code = result.getFirst();
            if (code == OpCode.NODATA)
                System.out.println("Has not data published!");
            else if (code == OpCode.NODM)
                System.out.println("The given domain doesn't exists!");
            else if (code == OpCode.NOPERM)
                System.out.println("The user doens't have read permisions!");
            else if (code == OpCode.OK) {
                File outputFile = new File("rt-" + domainName + "-" + System.nanoTime() + ".txt");
                try (PrintWriter writer = new PrintWriter(outputFile)) {
                    for (String device : result.getSecond().keySet()) {
                        float temp = result.getSecond().get(device);
                        writer.println(device + " " + temp);
                    }
                }
                System.out.println("Temperature data was written to " + outputFile.getName());
            }
        }
    }

    /**
     * Handles the receive image command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerRI(String[] cmd) throws ClassNotFoundException, IOException {
        if (isValidCommand(cmd, 2)) {
            String userIdDevId = cmd[1];
            String[] splittedParam = userIdDevId.split(":");
            if (splittedParam.length != 2 || splittedParam[0].isEmpty() || splittedParam[1].isEmpty()) {
                System.err.println("User id and device id musn't be empty!");
                return;
            }
            Pair<OpCode, byte[]> result = ioTDevStub.receiveImage(userIdDevId);
            OpCode code = result.getFirst();
            if (code == OpCode.NODATA)
                System.out.println("The given device id has not published data!");
            else if (code == OpCode.NOID)
                System.out.println("The given device id doesn't exists!");
            else if (code == OpCode.NOPERM)
                System.out.println("The user doens't have read permisions!");
            else if (code == OpCode.OK) {
                File outputFile = new File("ri-" + userIdDevId + "-" + System.nanoTime() + ".jpg");
                try (FileOutputStream out = new FileOutputStream(outputFile)) {
                    out.write(result.getSecond());
                }
                System.out.println("Image data was written to " + outputFile.getName());
            }
        }
    }

    /**
     * Handles the my domains command.
     * 
     * @param cmd The command used by the user.
     * @throws ClassNotFoundException If the command fails to send to the server.
     * @throws IOException            If the command fails to send to the server.
     */
    private static void handlerMyDomains() throws ClassNotFoundException, IOException {
        List<String> domains = ioTDevStub.listDomains();
        if (domains.size() == 0) {
            System.out.println("No domains available!");
            return;
        }
        System.out.println("Registered domains:");
        for (String domainName : domains) {
            System.out.println("- " + domainName);
        }
    }

    /**
     * Asks the user to input the 2FA code sent to his email.
     * 
     * @return The user input.
     */
    private static String getCfaCode() {
        System.out.print("Insert 2FA code sent to your email (" + userId + "): ");
        System.out.flush();
        return SC.nextLine();
    }

    /**
     * Gets the bytes of the currently executing jar file.
     * 
     * @return The bytes of the jar file.
     * @throws IOException If it fails to read the file bytes.
     */
    private static byte[] getBytesJarFile() throws IOException {
        String path = IoTDevice.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File file = new File(path);
        return Files.readAllBytes(file.toPath());
    }

    /**
     * Generates a PBE key and wraps it with the specified user's certificate.
     * 
     * @param userId       The user that will have his certificate used to encrypt
     *                     the key.
     * @param domainKeyPBE The domain key with password-based encryption.
     * @return The wrapped key with the public key of the user.
     */
    private static byte[] cipherDomainKeyWithPublicKey(String userId, Key domainKeyPBE) {
        try {
            Certificate certificate = trustStore.getCertificate(userId);
            if (certificate == null)
                return null;
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.WRAP_MODE, certificate.getPublicKey());
            return cipher.wrap(domainKeyPBE);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | NoSuchPaddingException
                | KeyStoreException e) {
            System.err.println("Failed to cipher domain key with PBE encryption!");
            System.exit(-1);
            return null;
        }
    }

    /**
     * Generates a PBE key using a password for the specified domain.
     * 
     * @param password   The password to generate the key.
     * @param domainName The domain this key belongs to.
     * @return The generated key.
     */
    private static Key genPBEKey(String password, String domainName) {
        byte[] salt = null;
        int iterCount = 0;
        File file = new File(domainName + ".keyparams");
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                salt = (byte[]) in.readObject();
                iterCount = in.readInt();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Failed to load key params file!");
                System.exit(-1);
            }
        } else {
            SecureRandom random = new SecureRandom();
            salt = new byte[100];
            random.nextBytes(salt);
            iterCount = random.nextInt(4096 - 1000 + 1) + 1000;
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(salt);
                out.writeInt(iterCount);
            } catch (IOException e) {
                System.err.println("Failed to save key params file");
                System.exit(-1);
            }
        }
        try {
            return Utils.genSecretKeyFromPassword(password, salt, iterCount);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            System.err.println("Failed to generate secret key!");
            System.exit(-1);
        }
        return null;
    }
}
