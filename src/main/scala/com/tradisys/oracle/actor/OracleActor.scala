package com.tradisys.oracle.actor

import java.io.IOException
import java.util

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.tradisys.oracle.actor.OracleMachine.{CheckForNextBlockEvent, NetworkErrorOccurred}
import com.tradisys.oracle.api.WavesDexApi
import com.tradisys.oracle.utility.{OracleConstants, RSACrypto}
import com.tradisys.oracle.vo.OracleVO
import com.wavesplatform.wavesj.{DataEntry, Node}
import scala.concurrent.{ExecutionContext, Future}

object OracleActor {

  def props(accounts: Seq[OracleVO]): Props = Props.create(classOf[TradingActor], accounts)

  private class TradingActor(oracleSeq: Seq[OracleVO]) extends Actor with ActorLogging {

    private final implicit val system: ActorSystem = context.system
    private final implicit val executionContext: ExecutionContext = context.dispatcher
    private final implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
    private final val dexApi = WavesDexApi(oracleSeq, system, mat, executionContext)

    override def receive: Receive = {

      case CheckForNextBlockEvent(oracle) ⇒
        val node = new Node(oracle.nodeURI)
        dexApi.getCurrentHeight().flatMap(currentBlockHeight => {
          if (currentBlockHeight - oracle.currentHeight >= OracleConstants.BLOCK_PERIOD) {
            val lastBlockInfoFuture = dexApi.getBlockInfoByHeight(currentBlockHeight - 5)
            for{
              lastBlockInfo <- lastBlockInfoFuture
            }yield{
              oracle.currentHeight = currentBlockHeight
              val blockSignature = lastBlockInfo.signature
              val oracleData = RSACrypto.sign(blockSignature, oracle.keyPair.getPrivate)

              log.info("\nOracle calculation for block {} with signature {}", lastBlockInfo.height, blockSignature)
              log.info("Calculated random string is {}\n", oracleData)

              if (RSACrypto.verify(blockSignature, oracleData, oracle.keyPair.getPublic)) {
                val data = new util.LinkedList[DataEntry[_]]
                val oraclePushData = new DataEntry.StringEntry("randomNumber", oracleData)
                val oracleBlockHeight = new DataEntry.LongEntry("basedOnBlockHeight", lastBlockInfo.height)
                val oracleBasedOnSignature = new DataEntry.StringEntry("basedOnBlockSignature", blockSignature)
                data.add(oraclePushData)
                data.add(oracleBlockHeight)
                data.add(oracleBasedOnSignature)
                try{
                  node.data(oracle.oracleAccountOwner, data, OracleConstants.FEE * 5)
                }catch {
                  case ioe: IOException =>
                    context.parent ! NetworkErrorOccurred(ioe)
                  case th: Throwable =>
                    context.parent ! NetworkErrorOccurred(th)
                }
              }
            }
          }
          Future(currentBlockHeight)
        })
    }
  }

}
