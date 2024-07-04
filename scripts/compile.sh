mkdir -p bin
mkdir -p bin/deviceCopy
cd src

javac common/*.java common/data/*.java common/messages/*.java common/messages/types/*/*.java
javac iotdevice/*.java iotdevice/utils/*.java
javac iotserver/*.java iotserver/managers/*.java iotserver/utils/*.java iotserver/persistance/*.java

echo "Main-Class: iotserver.IoTServer" > MANIFEST.MF
jar cfm ../bin/IoTServer.jar MANIFEST.MF common/*.class common/data/*.class common/messages/*.class common/messages/types/*/*.class  iotserver/*.class iotserver/managers/*.class iotserver/utils/*.class iotserver/persistance/*.class
echo "Main-Class: iotdevice.IoTDevice" > MANIFEST.MF
jar cfm ../bin/IoTDevice.jar MANIFEST.MF common/*.class common/data/*.class common/messages/*.class common/messages/types/*/*.class  iotdevice/*.class iotdevice/utils/*.class
cp ../bin/IoTDevice.jar ../bin/deviceCopy/IoTDevice.jar
rm MANIFEST.MF

# echo "deviceCopy/IoTDevice.jar" > ../bin/clientDetails.txt

rm common/*.class common/data/*.class common/messages/*.class common/messages/types/*/*.class 
rm iotdevice/*.class iotdevice/utils/*.class
rm iotserver/*.class iotserver/managers/*.class iotserver/utils/*.class iotserver/persistance/*.class

cd ../scripts

javac ClientDetailsGen.java
java ClientDetailsGen 123456
rm ClientDetailsGen.class