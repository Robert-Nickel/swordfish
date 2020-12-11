import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.commons.codec.binary.Hex

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random

object Swordfish extends App {
  // 10 characters, a-z, A-Z, 0-9 => result: X42a0
  val system1HashedPassword = "5aea476328379d3bff2204501bb57aa8b4268fac"
  // 10 characters, a-z, A-Z, 0-9 => ~quadrillion combinations
  val system2HashedPassword = "d31d62ed0af022248e28fc0dc4a9580217987e55"
  // 5-10 characters, a-z, A-Z, 0-9 => ~ too many combinations
  val system3HashedPassword = "66ceeafde8453dda201978b2b497b9c85d4b6da5"

  private val messageDigest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")
  implicit val system: ActorSystem = ActorSystem()

  //println(s"The correct password for system 1 is: " + Await.result(swordfish1(), 10 minutes))
  println(s"The correct password for system 2 is: " + Await.result(swordfish2(), 8 hours))

  def randomSymbol: Char = {
    Random.nextInt(3) match {
      case 0 => (Random.nextInt(91 - 65) + 65).toChar // uppercase letter
      case 1 => (Random.nextInt(123 - 97) + 97).toChar // lowercase letter
      case 2 => (Random.nextInt(58 - 48) + 48).toChar // number
    }
  }

  def randomPassword(length: Int): String = {
    var randomPassword = ""
    for (_ <- 1 to length) {
      randomPassword += randomSymbol
    }
    randomPassword
  }

  def hashMatcher(hash: String) =
    Flow[String].filter(password => {
      hash == sha1Hash(password, messageDigest)
    })

  def sha1Hash(of: String, messageDigest: MessageDigest): String = {
    messageDigest.update(of.getBytes)
    Hex.encodeHexString(messageDigest.digest())
  }

  def countSink = Sink.fold[Int, String](0)((acc, _) => acc + 1)

  def swordfish1() = {
    Source(1 to 1_000_000_000)
      .map(_ => randomPassword(length = 5))
      .via(hashMatcher(system1HashedPassword))
      .toMat(Sink.head)(Keep.right)
      .run()
  }

  def swordfish2() = {
    Source(1 to 1_000_000_000)
      .map(_ => randomPassword(length = 10))
      .via(hashMatcher(system1HashedPassword))
      .toMat(Sink.head)(Keep.right)
      .run()
  }
}
