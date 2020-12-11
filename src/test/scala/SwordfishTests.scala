import java.security.MessageDigest

import Swordfish.{randomPasswords, randomSymbols, sha1Hash}
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class SwordfishSpec extends AnyWordSpec {

  private val messageDigest: MessageDigest = java.security.MessageDigest.getInstance("SHA-1")

  "Given the word 'hello'" should {
    "return the correct sha-1 hash" in {
      sha1Hash("hello", messageDigest) should be("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")
    }
  }

  "RandomSymbols" should {
    "compute 5 when take 5 called" in {
      println(randomPasswords(5).head)
    }
  }
}
