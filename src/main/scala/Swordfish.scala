import java.security.MessageDigest

import SpeedMeter.measureSpeed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.typesafe.config.ConfigFactory
import org.apache.commons.codec.binary.Hex

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.io.StdIn
import scala.math.BigDecimal.RoundingMode.HALF_UP
import scala.util.Random

object Swordfish extends App {
  val messageDigest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")
  implicit val system: ActorSystem = ActorSystem()
  println("Swordfish reverts hashes for passwords that contain: a-z, A-Z & 0-9")
  val hash = StdIn.readLine("Enter the hash you want to revert: ")
  val length = StdIn.readLine("Enter the length of the password (e.g. 5 or 5-9): ")
  println("Doing a quick speed test")
  val speed = measureSpeed
  println(s"1_000_000 attempts took $speed milliseconds.")
  val eventualString: Future[String] = if (length.contains("-")) {
    val fromTo = length.split("-")
    start(hash, fromTo(0).toInt, fromTo(1).toInt, speed = speed)
  } else {
    start(hash, length.toInt, speed = speed)
  }
  println(s"The reverted hash is: " + Await.result(eventualString, 100 days))

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

  def start(hash: String, from: Int, to: Int = -1, speed: Int) = {
    println(s"Starting to find passwords for $hash with ${
      if (to == -1) {
        from.toString
      } else {
        s"$from to $to"
      }
    } characters.")
    Source.repeat(
      if (to == -1) {
        println(s"This will take around ${combinationsToDurationString(Math.pow(62, from).toLong, speed)}")
        from
      } else {
        val combinations = (from to to).map(i => Math.pow(62, i).toLong).sum
        println(s"$combinations different combinations will take around " +
          s"${combinationsToDurationString(combinations.toLong, speed)}")
        Random.nextInt(to - from + 1) - from
      }
    )
      .map(length => randomPassword(length = length))
      .via(hashMatcher(hash, messageDigest))
      .toMat(Sink.head)(Keep.right)
      .run()
  }

  def combinationsToDurationString(combinations: Long, speed: Int): String = {
    val millis = combinations * speed.toDouble / 1_000_000
    val seconds = millis / 1_000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    days match {
      case days if days > 36 => s"${BigDecimal(days / 365).setScale(2, HALF_UP)} years"
      case days if days < 0.04167 => s"${BigDecimal(days * 24 * 60).setScale(2, HALF_UP)} minutes"
      case days if days < 1 => s"${BigDecimal(days * 24).setScale(2, HALF_UP)} hours"
      case _ => s"${BigDecimal(days).setScale(2, HALF_UP)} days"
    }
  }
}
