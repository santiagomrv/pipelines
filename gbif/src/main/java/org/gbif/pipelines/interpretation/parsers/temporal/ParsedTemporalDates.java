package org.gbif.pipelines.interpretation.parsers.temporal;

import java.time.Month;
import java.time.Year;
import java.time.temporal.Temporal;
import java.util.Optional;

/**
 * Base temporal class, consists of two parsed dates from and to.
 */
public class ParsedTemporalDates {

  private Year year = null;
  private Month month = null;
  private Integer day = null;
  private Temporal fromDate = null;
  private Temporal toDate = null;

  public ParsedTemporalDates() {
  }

  public ParsedTemporalDates(Temporal fromDate, Temporal toDate) {
    this.fromDate = fromDate;
    this.toDate = toDate;
  }

  public ParsedTemporalDates(Year year, Month month, Integer day, Temporal fromDate) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.fromDate = fromDate;
  }

  public Optional<Temporal> getFrom() {
    return Optional.ofNullable(fromDate);
  }

  public Optional<Temporal> getTo() {
    return Optional.ofNullable(toDate);
  }

  public Optional<Year> getYear() {
    return Optional.ofNullable(year);
  }

  public Optional<Month> getMonth() {
    return Optional.ofNullable(month);
  }

  public Optional<Integer> getDay() {
    return Optional.ofNullable(day);
  }

  public void setYear(Year year) {
    this.year = year;
  }

  public void setMonth(Month month) {
    this.month = month;
  }

  public void setDay(Integer day) {
    this.day = day;
  }

  public void setFromDate(Temporal fromDate) {
    this.fromDate = fromDate;
  }

  public void setToDate(Temporal toDate) {
    this.toDate = toDate;
  }
}