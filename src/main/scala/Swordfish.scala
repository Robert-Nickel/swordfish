import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.apache.commons.codec.binary.Hex

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Random

object Swordfish extends App {

  val system1HashedPassword = "5aea476328379d3bff2204501bb57aa8b4268fac"
  private val messageDigest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")
  implicit val system = ActorSystem()

  def randomSymbols: LazyList[Char] = {
    val randomSymbol = Random.nextInt(3) match {
      case 0 => (Random.nextInt(91 - 65) + 65).toChar // uppercase letter
      case 1 => (Random.nextInt(123 - 97) + 97).toChar // lowercase letter
      case 2 => (Random.nextInt(58 - 48) + 48).toChar // number
    }
    randomSymbol #:: randomSymbols
  }

  def randomPasswords(length: Int): LazyList[String] = {
    randomSymbols.take(length).toString() #:: randomPasswords(length)
  }

  def checker =
    Flow[String].filter(password => system1HashedPassword == sha1Hash(password, messageDigest))

  def sha1Hash(of: String, messageDigest: MessageDigest): String = {
    messageDigest.update(of.getBytes)
    Hex.encodeHexString(messageDigest.digest())
  }

  val findPassword = Source(randomPasswords(length = 5))
    .via(checker)
    .take(100_000_000)
    .runWith(Sink.foreach(result => println("The password is: " + result)))

  Await.result(findPassword, 60 seconds)
}
