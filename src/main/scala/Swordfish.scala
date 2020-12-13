import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import org.apache.commons.codec.binary.Hex

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

object Swordfish extends App {
  /**
   * Total characters a-z, A-Z & 0-9 = 62
   *
   * 1_000_000 attempts take ~1.5 seconds
   * 62 ^ 10 / 1_000_000 =
   **/

  /** 10 characters
   * => 62^5 = 916132832 combinations
   * => ~22 minutes
   * => result: X42a0 */
  val system1Hash = "5aea476328379d3bff2204501bb57aa8b4268fac"

  /**
   * 10 characters
   * => 62^10 = 839_299_365_868_340_200 combinations
   * => 38_590.31 years
   * => result: unknown */
  val system2Hash = "d31d62ed0af022248e28fc0dc4a9580217987e55"

  /** 5-10 characters
   * => 62^5+62^6+62^7+62^8+62^9+62^10 = 853_058_371_851_163_300 combinations
   * => 39_222.94 years
   * => result: unknown */
  val system3Hash = "66ceeafde8453dda201978b2b497b9c85d4b6da5"

  val messageDigest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")
  implicit val system: ActorSystem = ActorSystem()

  // ___________________________________________________________________________________________________________________
  println(s"The correct password for system 2 is: " + Await.result(start(system2Hash, 5, 10), 24 hours))
  // ___________________________________________________________________________________________________________________


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

  def hashMatcher(hash: String, messageDigest: MessageDigest) =
    Flow[String].filter(password => {
      hash == sha1Hash(password, messageDigest)
    })

  def sha1Hash(of: String, messageDigest: MessageDigest): String = {
    messageDigest.update(of.getBytes)
    Hex.encodeHexString(messageDigest.digest())
  }

  def start(hash: String, from: Int, to: Int = -1) = {
      println(s"Starting to find passwords for $hash with ${
        if (to == -1) {
          from.toString
        } else {
          s"$from to $to"
        }
      } characters.")
    Source.repeat(
      if (to == -1) {
        println(s"This will take around ${combinationsToDurationString(Math.pow(62, from).toLong)}")
        from
      } else {
        val combinations = (from to to).map(i => Math.pow(62, i).toLong).sum
        println(s"$combinations different combinations will take around ${combinationsToDurationString(combinations.toLong)}")
        Random.nextInt(to - from + 1) - from
      }
    )
      .map(length => randomPassword(length = length))
      .via(hashMatcher(hash, messageDigest))
      .toMat(Sink.head)(Keep.right)
      .run()
  }

  def combinationsToDurationString(combinations: Long, millisPerMillionAttempts: Int = 1_450): String = {
    val millis = combinations * millisPerMillionAttempts.toDouble / 1_000_000
    val seconds = millis / 1_000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    if (days > 365) {
      s"${days / 365} years"
    } else {
      s"$days days"
    }
  }
}
