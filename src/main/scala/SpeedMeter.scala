import Swordfish.{hashMatcher, randomPassword}
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object SpeedMeter {

  def measureSpeed: Int = {
    implicit val system: ActorSystem = ActorSystem()
    val now = System.currentTimeMillis()

    Await.result(Source(1 to 1_000_000)
      .map(_ => randomPassword(length = 10))
      .via(hashMatcher("d31d62ed0af022248e28fc0dc4a9580217987e55", java.security.MessageDigest.getInstance("SHA-1")))
      .toMat(Sink.foreach(println))(Keep.right)
      .run(), 10 seconds)

    (System.currentTimeMillis() - now).toInt
  }
}
