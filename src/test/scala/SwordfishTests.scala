import Swordfish.sha1Hash
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers._
import org.scalatest.wordspec.AnyWordSpec

class SwordfishSpec extends AnyWordSpec {

  "Given the word 'hello'" should {
    "return the correct sha-1 hash" in {
      sha1Hash("hello", java.security.MessageDigest.getInstance("SHA-1")) should be("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d")
    }
  }
}
