package com.expedia.www.haystack.commons.entities.encoders

import com.expedia.www.haystack.commons.unit.UnitTestSpec

class EncoderFactorySpec extends UnitTestSpec {
  "EncoderFactory" should {

    "return a NoopEncoder by default for null" in {
      When("encoder is null")
      val encoder = EncoderFactory.newInstance(null)

      Then("should be a NoopEncoder")
      encoder shouldBe an[NoopEncoder]
    }

    "return a NoopEncoder by default for empty string" in {
      When("encoder is empty string")
      val encoder = EncoderFactory.newInstance("")

      Then("should be a NoopEncoder")
      encoder shouldBe an[NoopEncoder]
    }

    "return a Base64Encoder when value = base64" in {
      When("encoder is empty string")
      val encoder = EncoderFactory.newInstance(EncoderFactory.BASE_64)

      Then("should be a Base64Encoder")
      encoder shouldBe an[Base64Encoder]
    }

    "return a Base64Encoder when value = baSe64" in {
      When("encoder is empty string")
      val encoder = EncoderFactory.newInstance("baSe64")

      Then("should be a Base64Encoder")
      encoder shouldBe an[Base64Encoder]
    }

    "return a PeriodReplacementEncoder when value = periodreplacement" in {
      When("encoder is empty string")
      val encoder = EncoderFactory.newInstance("periodreplacement")

      Then("should be a PeriodReplacementEncoder")
      encoder shouldBe an[PeriodReplacementEncoder]
    }

    "return a PeriodReplacementEncoder when value = periodReplacement" in {
      When("encoder is empty string")
      val encoder = EncoderFactory.newInstance(EncoderFactory.PERIOD_REPLACEMENT)

      Then("should be a PeriodReplacementEncoder")
      encoder shouldBe an[PeriodReplacementEncoder]
    }
  }
}
