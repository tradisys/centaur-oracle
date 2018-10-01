package com.tradisys.oracle.utility

import com.tradisys.oracle.vo.OracleVO
import com.typesafe.config.Config
import com.wavesplatform.wavesj.{Account, PrivateKeyAccount}
import scala.collection.JavaConverters._

/**
  * Parses passed configuration and creates Trader instance
  */
object OracleConfigParser {

  /**
    * Performs configuration parsing for oracle bot
    *
    * @param config config object to parse
    * @return oracle value object list with parsed data
    */
  def createOracleFromConfig(config: Config): Seq[OracleVO] = config.resolve().getObjectList("oracle").asScala.map { account ⇒

    val nodeURI = account.toConfig.getString("nodeURI")
    val address = account.toConfig.getString("address")
    val publicKey = account.toConfig.getString("publicKey")
    val privateKey = account.toConfig.getString("privateKey")
    val keyPass = account.toConfig.getString("keyPass")
    val storePass = account.toConfig.getString("storePass")
    val oracleCurrentHeight = account.toConfig.getLong("oracleCurrentHeight")

    OracleVO(
      oracleCurrentHeight,
      nodeURI,
      address,
      publicKey,
      privateKey,
      RSACrypto.getKeyPairFromKeyStore(storePass, keyPass, OracleConstants.KEYSTORE_ALIAS),
      PrivateKeyAccount.fromPrivateKey(privateKey, Account.MAINNET))
  }
}
