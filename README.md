# centaur-oracle

This is an example of Oracle system for Waves Blockchain.

Preliminary steps before running Centaur Oracle:
1. Create keystore file using the following command 

   **keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks**
2. Public key from the keystore has to be pushed to Waves Account using Data Transaction
3. Then take /resources/smartscript.txt, this is oracle smart account script. And then add it to your account.

How to run Oracle:
1. Open file app.conf
2. Add keyPass, storePass from keystore
3. Add waves account address, publicKey, privateKey
4. oracleCurrentHeight option can be set to current blockchain height, it is useful when restarting the application
4. Place recently generated keystore file at the same location with app.conf
5. Currently Oracle has been setup write random digit each 30 blocks. This digit can be changed using variable OracleConstants#BLOCK_PERIOD
6. sbt compile
7. sbt assembly
8. java -jar CentaurOracle-assembly-1.0.jar &
