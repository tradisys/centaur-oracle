package com.tradisys.oracle.actor

import akka.actor.{FSM, LoggingFSM, Props}
import com.tradisys.oracle.vo.OracleVO
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.duration._

object OracleMachine {

  def props(oracle: Seq[OracleVO], worker: Props): Props = Props.create(classOf[TraderMachine], oracle, worker)

  // public events:
  final case object Start
  final case object Stop
  final case class CheckForNextBlockEvent(oracle: OracleVO)
  final case class LogicErrorOccurred(ex: Exception)
  final case class NetworkErrorOccurred(th: Throwable)

  // machine states:
  private sealed trait State
  private case object Idle extends State
  private case object BlocksCheckingState extends State

  // data:
  private sealed trait Data
  private case object Empty extends Data

  private class TraderMachine(accounts: Seq[OracleVO], workerProps: Props) extends FSM[State, Data] with LoggingFSM[State, Data] {
    private val worker = context.actorOf(workerProps)
    val requestLogger: Logger = LoggerFactory.getLogger("requestLogger")
    private val waitingNextBlockTimerName = "waitingNextBlockCheck"

    startWith(Idle, Empty)

    self ! Start

    when(Idle) {
      case Event(Start, _) ⇒
        goto(BlocksCheckingState)

      case Event(NetworkErrorOccurred(_), _) ⇒
        stay

      case Event(event, _) ⇒
        log.info("Received event while idle: {}", event)
        stay
    }

    when(BlocksCheckingState) {

      case Event(CheckForNextBlockEvent, _) =>

        worker ! CheckForNextBlockEvent(accounts.head)
        stay
    }

    whenUnhandled {
      case Event(LogicErrorOccurred(ex), _) ⇒
        log.error(ex, "Logic error has been occurred")
        stay

      case Event(NetworkErrorOccurred(th), _) ⇒
        log.error(th, "Network error has been occurred")
        stay

      case Event(e, _) ⇒
        log.warning("Received unhandled request {} in state {}", e, stateName)
        stay
    }

    onTransition {
      case Idle -> (next @ BlocksCheckingState) ⇒
        log.info("State changed from {} to {}",stateName, next)
        setTimer(waitingNextBlockTimerName, CheckForNextBlockEvent, 5 seconds, repeat = true)
    }

    initialize()
  }

}
