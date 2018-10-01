package com.tradisys.oracle.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{Uri, _}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.tradisys.oracle.vo.{BlockInfoVo, OracleVO}
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.{Logger, LoggerFactory}

case class WavesDexApi(oracle: Seq[OracleVO], as: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext){

  private implicit val materializer: ActorMaterializer = mat
  private implicit val actorSystem: ActorSystem = as
  private implicit val executionContest: ExecutionContext = ec
  val errorLogger: Logger = LoggerFactory.getLogger("requestLogger")

  /**
    * Get last block information
    *
    * @return BlockInfoVo  future VO with block data
    */
  def getBlockInfoByHeight(height: Long): Future[BlockInfoVo] = {
    val uri = Uri.from(scheme="https", host=s"${oracle.head.nodeURI}", path = s"/blocks/at/$height")
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = uri
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty) {
          case (acc, b) ⇒ acc ++ b
        }.map { bs ⇒
          val root = Json.parse(bs.toArray)
          val blockHeight = (root \ "height").asOpt[Long].get
          val blockSignature = (root \ "signature").asOpt[String].get
          BlockInfoVo(blockHeight,blockSignature)
        }
      case HttpResponse(_, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { body =>
          errorLogger.info("Error occurred during /blocks/height call")
          throw new IllegalArgumentException(body.utf8String)
        }
    }
  }

  /**
    * Get last block height
    *
    * @return future with current blockchain height
    */
  def getCurrentHeight(): Future[Long] = {
    val uri = Uri.from(scheme="https", host=s"${oracle.head.nodeURI}", path = "/blocks/height")
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = uri
      )
    ).flatMap {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty) {
          case (acc, b) ⇒ acc ++ b
        }.map { bs ⇒
          val root = Json.parse(bs.toArray)
          (root \ "height").asOpt[Long].get
        }
      case HttpResponse(_, _, entity, _) =>
        entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { body =>
          errorLogger.info("Error occurred during /blocks/height call")
          throw new IllegalArgumentException(body.utf8String)
        }
    }
  }
}
