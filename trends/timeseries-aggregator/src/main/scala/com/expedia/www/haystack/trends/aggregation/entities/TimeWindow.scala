package com.expedia.www.haystack.trends.aggregation.entities

import com.expedia.www.haystack.commons.entities.Interval.Interval


/**
  * This class encapsulates a time window which contains a start time and an end-time
  */
case class TimeWindow(startTime: Long, endTime: Long) extends Ordered[TimeWindow] {

  override def compare(that: TimeWindow): Int = {
    this.startTime.compare(that.startTime)
  }

  override def hashCode(): Int = {
    this.startTime.hashCode()
  }

  override def equals(that: scala.Any): Boolean = {
    this.startTime == that.asInstanceOf[TimeWindow].startTime && this.endTime == that.asInstanceOf[TimeWindow].endTime
  }
}

object TimeWindow {

  /**
    * This function creates the time window based on the given time in seconds and the interval of the window
    * Eg : given a timestamp 145 seconds and an interval of 1 minute, the window would be 120 seconds - 180 seconds
    * @param timestamp given time in seconds
    * @param interval interval for which we would need to create the window
    * @return time window
    */

  def apply(timestamp: Long, interval: Interval): TimeWindow = {
    val intervalTimeInSeconds = interval.timeInSeconds
    val windowStart = (timestamp / intervalTimeInSeconds) * intervalTimeInSeconds
    val windowEnd = windowStart + intervalTimeInSeconds
    TimeWindow(windowStart, windowEnd)
  }
}
