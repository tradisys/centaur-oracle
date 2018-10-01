package com.tradisys.oracle.vo

import java.security.{KeyPair, PrivateKey, PublicKey}

import com.wavesplatform.wavesj.PrivateKeyAccount

case class OracleVO(
                     var currentHeight: Long,
                     var nodeURI: String,
                     var address: String,
                     var publicKey: String,
                     var privateKey: String,
                     var keyPair: KeyPair,
                     var oracleAccountOwner : PrivateKeyAccount,
)
