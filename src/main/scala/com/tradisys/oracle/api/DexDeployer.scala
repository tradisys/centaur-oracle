package com.tradisys.oracle.api

import akka.actor.{ActorSystem, Props}
import akka.stream.Materializer
import com.tradisys.oracle.actor.{OracleActor, OracleMachine}
import com.tradisys.oracle.vo.OracleVO

import scala.concurrent.{ExecutionContext, Future}

object DexDeployer {

  def deploy(oracle: Seq[OracleVO])(implicit sys: ActorSystem, exc: ExecutionContext, mat: Materializer): Future[Props] = {

    val securityWorker = OracleActor.props(oracle)
    Future(OracleMachine.props(oracle, securityWorker))

  }
}
