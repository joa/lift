package net.liftweb.util
import org.specs._
import java.io.ByteArrayInputStream

object SecurityHelpersSpec extends Specification with SecurityHelpers with IoHelpers with StringHelpers {
  "Security Helpers" should {
    "provide a randomLong method returning a random Long modulo a number" in {
      randomLong(7L) must be_<(7L)
    }
    "provide a randomInt method returning a random Int modulo a number" in {
      randomInt(7) must be_<(7)
    }
    "provide a shouldShow function always returning true only a given percentage of time, expressed as a Int between 0 and 100" in {
      shouldShow(100) must beTrue
      shouldShow(0) must beFalse
    }
    "provide a shouldShow function always returning true only a given percentage of time, expressed as a Double between 0 and 1.0" in {
      shouldShow(1.0) must beTrue
      shouldShow(0.0) must beFalse
    }
    "provide makeBlowfishKey, blowfishEncrypt, blowfishDecrypt functions to encrypt/decrypt Strings with Blowfish keys" in {
      val key = makeBlowfishKey
      val encrypted = blowfishEncrypt("hello world", key)
      encrypted must_!= "hello world"
      blowfishDecrypt(encrypted, key) must_== "hello world"
    }
    "provide a md5 function to create a md5 digest from a string" in {
      md5("hello") must_!= md5("hell0")
    }
    "provide a hash function to create a SHA digest from a string" in {
      hash("hello") must_!= hash("hell0")
    }
    "provide a hash256 function to create a SHA-256 digest from a string" in {
      hash256("hello") must_!= hash256("hell0")
    }
  }
}
import org.specs.runner._
class SecurityHelpersSpecTest extends JUnit4(SecurityHelpersSpec)
