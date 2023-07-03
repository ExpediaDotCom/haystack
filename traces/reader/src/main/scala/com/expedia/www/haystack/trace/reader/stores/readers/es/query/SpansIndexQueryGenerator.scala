/*
 *  Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */

package com.expedia.www.haystack.trace.reader.stores.readers.es.query

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import com.expedia.open.tracing.api.Operand.OperandCase
import com.expedia.open.tracing.api.{ExpressionTree, Field}
import com.expedia.www.haystack.trace.commons.clients.es.document.TraceIndexDoc
import com.expedia.www.haystack.trace.commons.config.entities.{IndexFieldType, WhitelistIndexFieldConfiguration}
import io.searchbox.strings.StringUtils
import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.index.query.QueryBuilders.{boolQuery, nestedQuery, termQuery}
import org.elasticsearch.index.query._
import org.elasticsearch.search.aggregations.AggregationBuilder
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.aggregations.support.ValueType

import scala.collection.JavaConverters._

abstract class SpansIndexQueryGenerator(nestedDocName: String,
                                        whitelistIndexFieldConfiguration: WhitelistIndexFieldConfiguration) {
  private final val TIME_ZONE = TimeZone.getTimeZone("UTC")

  // create search query by using filters list
  @deprecated
  protected def createFilterFieldBasedQuery(filterFields: java.util.List[Field]): BoolQueryBuilder = {
    val traceContextWhitelistFields = whitelistIndexFieldConfiguration.globalTraceContextIndexFieldNames
    val (traceContextFields, serviceContextFields) = filterFields
      .asScala
      .partition(f => traceContextWhitelistFields.contains(f.getName.toLowerCase))

    val query = boolQuery()

    createNestedQuery(serviceContextFields).map(query.filter)

    traceContextFields foreach {
      field => {
        createNestedQuery(Seq(field)) match {
          case Some(nestedQuery) => query.filter(nestedQuery)
          case _ => /* may be log ? */
        }
      }
    }
    query
  }

  // create search query by using filters expression tree
  protected def createExpressionTreeBasedQuery(expression: ExpressionTree): BoolQueryBuilder = {
    val query = boolQuery()
    val contextFiltersList = listOfContextFilters(expression)

    // create a nested boolean query per context
    contextFiltersList foreach {
      filters => {
        createNestedQuery(filters) match {
          case Some(nestedQuery) => query.filter(nestedQuery)
          case _ => /* may be log ?*/
        }
      }
    }
    query
  }

  // create list of fields, one for each trace level query and one for each span level groups
  // assuming that first level is trace level filters
  // and second level are span level filter groups
  private def listOfContextFilters(expression: ExpressionTree): List[List[Field]] = {
    val (spanLevel, traceLevel) = expression.getOperandsList.asScala.partition(operand => operand.getOperandCase == OperandCase.EXPRESSION)

    val traceLevelFilters = traceLevel.map(field => List(field.getField))
    val spanLevelFilters = spanLevel.map(tree => toListOfSpanLevelFilters(tree.getExpression))

    (spanLevelFilters ++ traceLevelFilters).toList
  }

  private def toListOfSpanLevelFilters(expression: ExpressionTree): List[Field] = {
    expression.getOperandsList.asScala.map(field => field.getField).toList
  }

  private def createNestedQuery(fields: Seq[Field]): Option[NestedQueryBuilder] = {
    if (fields.isEmpty) {
      None
    } else {
      val nestedBoolQueryBuilder = createNestedBoolQuery(fields)
      Some(nestedQuery(nestedDocName, nestedBoolQueryBuilder, ScoreMode.None))
    }
  }

  private def buildNestedTermQuery(field: Field): TermQueryBuilder = {
    termQuery(withBaseDoc(field.getName.toLowerCase), field.getValue)
  }

  private def buildNestedRangeQuery(field: Field): RangeQueryBuilder = {
    def rangeValue(): Any = {
      if(field.getName == TraceIndexDoc.DURATION_KEY_NAME || field.getName == TraceIndexDoc.START_TIME_KEY_NAME) {
        field.getValue.toLong
      } else {
        val fieldType = whitelistIndexFieldConfiguration.whitelistIndexFields
          .find(wf => wf.name.equalsIgnoreCase(field.getName))
          .map(wf => wf.`type`)
          .getOrElse(IndexFieldType.string)

        fieldType match {
          case IndexFieldType.int | IndexFieldType.long => field.getValue.toLong
          case IndexFieldType.double => field.getValue.toDouble
          case IndexFieldType.bool => field.getValue.toBoolean
          case _ => field.getValue
        }
      }
    }

    val rangeQuery = QueryBuilders.rangeQuery(withBaseDoc(field.getName.toLowerCase))
    val value = rangeValue()
    field.getOperator match {
      case Field.Operator.GREATER_THAN => rangeQuery.gt(value)
      case Field.Operator.LESS_THAN => rangeQuery.lt(value)
      case _ => throw new RuntimeException("Fail to understand the operator -" + field.getOperator)
    }
    rangeQuery
  }

  protected def createNestedBoolQuery(fields: Seq[Field]): BoolQueryBuilder = {
    val boolQueryBuilder = boolQuery()

    val validFields = fields.filterNot(f => StringUtils.isBlank(f.getValue))
    validFields foreach {
      field => {
        field match {
          case _ if field.getOperator == null || field.getOperator == Field.Operator.EQUAL =>
            boolQueryBuilder.filter(buildNestedTermQuery(field))
          case _ if field.getOperator == Field.Operator.NOT_EQUAL =>
            boolQueryBuilder.mustNot(buildNestedTermQuery(field))
          case _ if field.getOperator == Field.Operator.GREATER_THAN || field.getOperator == Field.Operator.LESS_THAN =>
            boolQueryBuilder.filter(buildNestedRangeQuery(field))
          case _ => throw new RuntimeException("Fail to understand the operator type of the field!")
        }
      }
    }


    boolQueryBuilder
  }

  protected def createNestedAggregationQuery(fieldName: String): AggregationBuilder =
    new NestedAggregationBuilder(nestedDocName, nestedDocName)
      .subAggregation(
        new TermsAggregationBuilder(fieldName, ValueType.STRING)
          .field(withBaseDoc(fieldName))
          .size(1000))

  protected def createNestedAggregationQueryWithNestedFilters(fieldName: String, filterFields: java.util.List[Field]): AggregationBuilder = {
    val boolQueryBuilder = createNestedBoolQuery(filterFields.asScala)

    new NestedAggregationBuilder(nestedDocName, nestedDocName)
      .subAggregation(
        new FilterAggregationBuilder(s"$fieldName", boolQueryBuilder)
          .subAggregation(new TermsAggregationBuilder(s"$fieldName", ValueType.STRING)
            .field(withBaseDoc(fieldName))
            .size(1000))
      )
  }

  def getESIndexes(startTimeInMicros: Long,
                   endTimeInMicros: Long,
                   indexNamePrefix: String,
                   indexHourBucket: Int,
                   indexHourTtl: Int): Seq[String] = {

    if (!isValidTimeRange(startTimeInMicros, endTimeInMicros, indexHourTtl)) {
      Seq(s"$indexNamePrefix")
    } else {
      val INDEX_BUCKET_TIME_IN_MICROS: Long = indexHourBucket.toLong * 60 * 60 * 1000 * 1000
      val flooredStarttime = startTimeInMicros - (startTimeInMicros % INDEX_BUCKET_TIME_IN_MICROS)
      val flooredEndtime = endTimeInMicros - (endTimeInMicros % INDEX_BUCKET_TIME_IN_MICROS)

      for (datetimeInMicros <- flooredStarttime to flooredEndtime by INDEX_BUCKET_TIME_IN_MICROS)
        yield {
          val date = new Date(datetimeInMicros / 1000)
          val dateBucket = createSimpleDateFormat("yyyy-MM-dd").format(date)
          val hourBucket = createSimpleDateFormat("HH").format(date).toInt / indexHourBucket

          s"$indexNamePrefix-$dateBucket-$hourBucket"
        }
    }
  }

  private def createSimpleDateFormat(pattern: String): SimpleDateFormat = {
    val sdf = new SimpleDateFormat(pattern)
    sdf.setTimeZone(TIME_ZONE)
    sdf
  }

  private def isValidTimeRange(startTimeInMicros: Long,
                               endTimeInMicros: Long,
                               indexHourTtl: Int): Boolean = {
    (endTimeInMicros - startTimeInMicros) < (indexHourTtl.toLong * 60 * 60 * 1000 * 1000)
  }
  
  protected def withBaseDoc(field: String) = s"$nestedDocName.$field"
}
