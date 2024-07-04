KEYSTORE_PASSWORD="sconfgrupo19"
OUTPUT_FOLDER=bin/stores
SERVER_KEYSTORE_FILE=$OUTPUT_FOLDER/server.keystore
DEVICES_TRUSTSTORE_FILE=$OUTPUT_FOLDER/devices.truststore

USER1_ID=""
USER2_ID=""
USER3_ID=""

read -p "Enter first user id: " USER1_ID
read -p "Enter second user id: " USER2_ID
read -p "Enter third user id: " USER3_ID

SSL_CER_FILE=$OUTPUT_FOLDER/ssl.cer
USER1_CER=$OUTPUT_FOLDER/$USER1_ID.cer
USER2_CER=$OUTPUT_FOLDER/$USER2_ID.cer
USER3_CER=$OUTPUT_FOLDER/$USER3_ID.cer

USER1_KEYSTORE=$OUTPUT_FOLDER/$USER1_ID.keystore
USER2_KEYSTORE=$OUTPUT_FOLDER/$USER2_ID.keystore
USER3_KEYSTORE=$OUTPUT_FOLDER/$USER3_ID.keystore

rm -rf $OUTPUT_FOLDER
mkdir -p $OUTPUT_FOLDER

(echo $KEYSTORE_PASSWORD; echo $KEYSTORE_PASSWORD; echo "Grupo 19"; echo "FCUL"; echo "ULisboa"; echo "Lisboa"; echo "Lisboa"; echo "PT"; echo "yes"; echo $KEYSTORE_PASSWORD) | keytool -genkeypair -alias keyRSA -keyalg RSA -keysize 2048 -keystore $SERVER_KEYSTORE_FILE
echo $KEYSTORE_PASSWORD | keytool -exportcert -alias keyRSA -keystore $SERVER_KEYSTORE_FILE -file $SSL_CER_FILE
(echo $KEYSTORE_PASSWORD; echo $KEYSTORE_PASSWORD; echo "yes") | keytool -importcert -alias sslcert -file $SSL_CER_FILE -keystore $DEVICES_TRUSTSTORE_FILE
rm $SSL_CER_FILE

(echo $KEYSTORE_PASSWORD; echo $KEYSTORE_PASSWORD; echo "Grupo 19"; echo "FCUL"; echo "ULisboa"; echo "Lisboa"; echo "Lisboa"; echo "PT"; echo "yes"; echo $KEYSTORE_PASSWORD) | keytool -genkeypair -alias keyRSA -keyalg RSA -keysize 2048 -keystore $OUTPUT_FOLDER/$USER1_ID.keystore
echo $KEYSTORE_PASSWORD | keytool -exportcert -alias keyRSA -keystore $USER1_KEYSTORE -file $USER1_CER
(echo $KEYSTORE_PASSWORD; echo "yes") | keytool -importcert -alias $USER1_ID -file $USER1_CER -keystore $DEVICES_TRUSTSTORE_FILE
rm $USER1_CER

(echo $KEYSTORE_PASSWORD; echo $KEYSTORE_PASSWORD; echo "Grupo 19"; echo "FCUL"; echo "ULisboa"; echo "Lisboa"; echo "Lisboa"; echo "PT"; echo "yes"; echo $KEYSTORE_PASSWORD) | keytool -genkeypair -alias keyRSA -keyalg RSA -keysize 2048 -keystore $OUTPUT_FOLDER/$USER2_ID.keystore
echo $KEYSTORE_PASSWORD | keytool -exportcert -alias keyRSA -keystore $USER2_KEYSTORE -file $USER2_CER
(echo $KEYSTORE_PASSWORD; echo "yes") | keytool -importcert -alias $USER2_ID -file $USER2_CER -keystore $DEVICES_TRUSTSTORE_FILE
rm $USER2_CER

(echo $KEYSTORE_PASSWORD; echo $KEYSTORE_PASSWORD; echo "Grupo 19"; echo "FCUL"; echo "ULisboa"; echo "Lisboa"; echo "Lisboa"; echo "PT"; echo "yes"; echo $KEYSTORE_PASSWORD) | keytool -genkeypair -alias keyRSA -keyalg RSA -keysize 2048 -keystore $OUTPUT_FOLDER/$USER3_ID.keystore
echo $KEYSTORE_PASSWORD | keytool -exportcert -alias keyRSA -keystore $USER3_KEYSTORE -file $USER3_CER
(echo $KEYSTORE_PASSWORD; echo "yes") | keytool -importcert -alias $USER3_ID -file $USER3_CER -keystore $DEVICES_TRUSTSTORE_FILE
rm $USER3_CER