# IoTDevice - Security and Confiability

This project was a collaborative effort involving two more students for our ```Security and Confiability``` subject.

In this project we have a client ```IoTDevice``` and a server ```IoTServer``` with the functionality of sending temperature and images, which can be carried out by several clients at the same time, due to concurrency management.

```Communication``` between the client and the server was ```secure```, encrypting end-to-end messages, carrying out remote attestation and integrity testing, with the client also having to perform a 2FA to authenticate.


---
## Files and Implementation

The project contains 3 subprojects:
- ```common```: contains the codes for communication between client and server and other common functions in both.
- ```iotserver```: code related to the server.
- ```iotdevice```: code related to the client (device).

It should also be noted that access to structures shared by various clients is carried out in an atomic manner.

---
## Compilation instructions

To ```compile``` all the project files just run the following command.

```bash
./compile.sh
```

To generate ```keystores``` run the following command.

```bash
./gen-keystores.sh
```

The script, in addition to compiling the files, also generates the ```clientDetails.txt``` file with the path to the copy of the client executable (```.jar```) that the server has.

---
To run the server and client jars, run the following commands respectively:

### Server:
Where ```[port]``` is optional, being linked by default to port ```12345```.
```bash
$ java -jar IoTServer.jar [port] <password-cipher> <keystore> <password-keystore> <2FA-APIKey>
```

###Client:
Where ```<serverAddress>``` is of type ```<IP/hostname>[:Port]```, the port being optional, the default port being ```12345```.
```bash
$ java -jar IoTDevice.jar <serverAddress> <truststore> <keystore> <password-keystore> <dev-id> <user-id>
```

Alternatively, you can run scripts like ```run-device.sh```, one per client, and ```run-server.sh``` (using the project root as the working directory).

---
## Grades
You may need to add execute permissions to your scripts. To do this, run the following command:
```bash
chmod +x ./scripts/*.sh
```

---
## Functionalities

The project implements all the requested features, thus presenting no limitations.