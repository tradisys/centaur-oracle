package com.tradisys.oracle.utility

import java.io.{File, FileInputStream}
import java.security.{PrivateKey, PublicKey, Signature}
import javax.crypto.Cipher
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.util.Base64
import java.nio.charset.StandardCharsets.UTF_8

/**
  * Simple RSA crypto library
  */
object RSACrypto {

  /**
    * Generates new keypair
    *
    * @return keypair
    */
  def generateKeyPair: KeyPair = {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048, new SecureRandom)
    generator.generateKeyPair
  }

  /**
    * Fetch keypair from keystore specified
    *
    * @param password  store password
    * @param keyPassword  key password
    * @param alias  keystore alias
    * @return  keypair
    */
  def getKeyPairFromKeyStore(password: String, keyPassword: String, alias: String): KeyPair = {
    //Generated with:
    //keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks
    val stream = new FileInputStream(new File("keystore.jks"))
    val keyStore = KeyStore.getInstance("JKS")
    keyStore.load(stream, password.toCharArray) //Keystore password
    val keyPass = new KeyStore.PasswordProtection(keyPassword.toCharArray)
    val privateKeyEntry = keyStore.getEntry(alias, keyPass).asInstanceOf[KeyStore.PrivateKeyEntry]
    val cert = keyStore.getCertificate(alias)
    val publicKey = cert.getPublicKey
    val privateKey = privateKeyEntry.getPrivateKey
    new KeyPair(publicKey, privateKey)
  }

  /**
    * Performs encryption of a text
    *
    * @param plainText  text to encrypt
    * @param publicKey  using public key
    * @return  base64 encoded text
    */
  def encrypt(plainText: String, publicKey: PublicKey): String = {
    val encryptCipher = Cipher.getInstance("RSA")
    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8))
    Base64.getEncoder.encodeToString(cipherText)
  }

  /**
    * Performs decryption of a text
    *
    * @param cipherText  text to decrypt
    * @param privateKey  using private key
    * @return  decrypted text
    */
  def decrypt(cipherText: String, privateKey: PrivateKey): String = {
    val bytes = Base64.getDecoder.decode(cipherText)
    val decriptCipher = Cipher.getInstance("RSA")
    decriptCipher.init(Cipher.DECRYPT_MODE, privateKey)
    new String(decriptCipher.doFinal(bytes), UTF_8)
  }

  /**
    * Performs signing of a text by private key
    *
    * @param plainText  text to sign
    * @param privateKey  using private key
    * @return  base64 signature
    */
  def sign(plainText: String, privateKey: PrivateKey): String = {
    val privateSignature = Signature.getInstance("SHA256withRSA")
    privateSignature.initSign(privateKey)
    privateSignature.update(plainText.getBytes(UTF_8))
    val signature = privateSignature.sign
    Base64.getEncoder.encodeToString(signature)
  }

  /**
    * Performs signature verification
    *
    * @param plainText  original text
    * @param signature  signature
    * @param publicKey  public key to verify by
    * @return
    */
  def verify(plainText: String, signature: String, publicKey: PublicKey): Boolean = {
    val publicSignature = Signature.getInstance("SHA256withRSA")
    publicSignature.initVerify(publicKey)
    publicSignature.update(plainText.getBytes(UTF_8))
    val signatureBytes = Base64.getDecoder.decode(signature)
    publicSignature.verify(signatureBytes)
  }
}
