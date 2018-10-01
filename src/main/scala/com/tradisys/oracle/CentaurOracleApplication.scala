package com.tradisys.oracle

import java.io.File
import java.util
import java.util.Base64

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.tradisys.oracle.api.DexDeployer
import com.tradisys.oracle.utility.{OracleConfigParser, OracleConstants}
import com.typesafe.config.ConfigFactory
import com.wavesplatform.wavesj.{Account, Base58, DataEntry, Node}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Bot application main entry point
  */
object CentaurOracleApplication{

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.parseFile(new File("app.conf"))
    val oracle = OracleConfigParser.createOracleFromConfig(config)

    implicit val system: ActorSystem = ActorSystem("simple-oracle")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher

    system.log.info("Starting")
    DexDeployer.deploy(oracle).onComplete {
      case Success(props) ⇒
        system.log.info("Deployment completed successfully.")
        system.actorOf(props)
      case Failure(ex) ⇒ system.log.error(ex, "Couldn't deploy.")
    }
  }
}
