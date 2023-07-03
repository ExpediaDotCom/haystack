package com.expedia.www.haystack.commons.kstreams.serde

import com.expedia.www.haystack.commons.unit.UnitTestSpec

class SpanSerdeSpec extends UnitTestSpec {


  "span serializer" should {
    "should serialize a span" in {
      Given("a span serializer")
      val serializer = (new SpanSerde).serializer
      And("a valid span is provided")
      val span = generateTestSpan("foo", "bar", 100, client = true, server = false)
      When("span serializer is used to serialize the span")
      val bytes = serializer.serialize("proto-spans", span)
      Then("it should serialize the object")
      bytes.nonEmpty should be(true)
    }
  }
  "span deserializer" should {
    "should deserialize a span" in {
      Given("a span deserializer")
      val serializer = (new SpanSerde).serializer
      val deserializer = (new SpanSerde).deserializer
      And("a valid span is provided")
      val span = generateTestSpan("foo", "bar", 100, client = true, server = false)
      When("span deserializer is used on valid array of bytes")
      val bytes = serializer.serialize("proto-spans", span)
      val span2 = deserializer.deserialize("proto-spans", bytes)
      Then("it should deserialize correctly")
      span should be(span2)
    }
  }



}
