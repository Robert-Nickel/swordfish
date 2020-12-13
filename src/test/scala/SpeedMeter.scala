import Swordfish.{hashMatcher, randomPassword}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object SpeedMeter extends App {
  Swordfish
  implicit val system: ActorSystem = ActorSystem()
  val now = System.currentTimeMillis()

  Source(1 to 1_000_000)
    .map(_ => randomPassword(length = 10))
    .via(hashMatcher("d31d62ed0af022248e28fc0dc4a9580217987e55", java.security.MessageDigest.getInstance("SHA-1")))
    .toMat(Sink.foreach(println))(Keep.right)
    .run()
    .onComplete {
      case Success(_) => println(s"1_000_000 attempts took ${System.currentTimeMillis() - now} milliseconds.")
      case Failure(error) => println(s"Measurement failed: ${error.getStackTrace}")
    }
}
