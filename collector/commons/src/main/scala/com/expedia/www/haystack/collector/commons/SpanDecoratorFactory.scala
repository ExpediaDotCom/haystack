package com.expedia.www.haystack.collector.commons

import com.expedia.www.haystack.span.decorators.plugin.config.Plugin
import com.expedia.www.haystack.span.decorators.plugin.loader.SpanDecoratorPluginLoader
import com.expedia.www.haystack.span.decorators.{AdditionalTagsSpanDecorator, SpanDecorator}
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger

import scala.collection.JavaConverters._

object SpanDecoratorFactory {
  def get(pluginConfig: Plugin, additionalTagsConfig: Map[String, String], LOGGER: Logger): List[SpanDecorator] = {
    var tempList = List[SpanDecorator]()
    if (pluginConfig != null) {
      val externalSpanDecorators: List[SpanDecorator] = SpanDecoratorPluginLoader.getInstance(LOGGER, pluginConfig).getSpanDecorators().asScala.toList
      if (externalSpanDecorators != null) {
        tempList = tempList ++: externalSpanDecorators
      }
    }

    val additionalTagsSpanDecorator = new AdditionalTagsSpanDecorator()
    additionalTagsSpanDecorator.init(ConfigFactory.parseMap(additionalTagsConfig.asJava))
    tempList.::(additionalTagsSpanDecorator)
  }
}
