package org.gbif.pipelines.interpretation.parsers.temporal.accumulator;

import org.gbif.api.vocabulary.OccurrenceIssue;
import org.gbif.pipelines.interpretation.parsers.temporal.utils.ParsedUnitUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * The main function convert ChronoAccumulator to Temporal in approrative way
 */
public class ChronoAccumulatorConverter {

  private static final Year OLD_YEAR = Year.of(1600);
  private static final Map<ChronoField, Function<String, Optional<Integer>>> FUNCTION_MAP = new EnumMap<>(ChronoField.class);

  static {
    FUNCTION_MAP.put(YEAR, ParsedUnitUtils::parseYear);
    FUNCTION_MAP.put(MONTH_OF_YEAR, ParsedUnitUtils::parseMonth);
    FUNCTION_MAP.put(DAY_OF_MONTH, ParsedUnitUtils::parseDay);
    FUNCTION_MAP.put(HOUR_OF_DAY, ParsedUnitUtils::parseHour);
    FUNCTION_MAP.put(MINUTE_OF_HOUR, ParsedUnitUtils::parseMinute);
    FUNCTION_MAP.put(SECOND_OF_MINUTE, ParsedUnitUtils::parseSecond);
  }

  private ChronoAccumulatorConverter() {
    // Can't have an instance
  }

  /**
   * Converts ChronoAccumulator to Temporal
   *
   * @return some Temporal value: Year, YearMonth, LocalDate, LocalDateTime
   */
  public static Optional<Temporal> toTemporal(ChronoAccumulator accumulator, List<OccurrenceIssue> issueList) {

    //Check Year
    Optional<Integer> intYear = convert(accumulator, YEAR, issueList);
    if (!intYear.isPresent()) {
      return Optional.empty();
    }
    Year year = Year.of(intYear.get());
    if (year.isBefore(OLD_YEAR)) {
      issueList.add(OccurrenceIssue.RECORDED_DATE_UNLIKELY);
    }

    //Check Month
    Optional<Integer> intMonth = convert(accumulator, MONTH_OF_YEAR, issueList);
    if (!intMonth.isPresent()) {
      return Optional.of(year);
    }


    YearMonth yearMonth = year.atMonth(intMonth.get());
    //Check Day
    Optional<Integer> intDay = convert(accumulator, DAY_OF_MONTH, issueList);
    if (!intDay.isPresent()) {
      return Optional.of(yearMonth);
    }

    if (!yearMonth.isValidDay(intDay.get())) {
      issueList.add(OccurrenceIssue.RECORDED_DATE_INVALID);
      return Optional.of(yearMonth);
    }
    LocalDate localDate = yearMonth.atDay(intDay.get());

    //Check Hour
    Optional<Integer> intHour = convert(accumulator, HOUR_OF_DAY, issueList);
    if (!intHour.isPresent()) {
      return Optional.of(localDate);
    }
    LocalDateTime localDateTime = localDate.atTime(intHour.get(), 0);

    //Check Minute
    Optional<Integer> intMinute = convert(accumulator, MINUTE_OF_HOUR, issueList);
    if (!intMinute.isPresent()) {
      return Optional.of(localDateTime);
    }
    localDateTime = localDateTime.withMinute(intMinute.get());

    //Check Second
    Optional<Integer> intSecond = convert(accumulator, SECOND_OF_MINUTE, issueList);
    if (!intSecond.isPresent()) {
      return Optional.of(localDateTime);
    }
    return Optional.of(localDateTime.withSecond(intSecond.get()));
  }

  /**
   * Looks for the year in a ChronoAccumulator
   *
   * @param accumulator - where to look for a value
   *
   * @return Year value if present
   */
  public static Optional<Year> getYear(ChronoAccumulator accumulator, List<OccurrenceIssue> issueList) {
    return convert(accumulator, YEAR, issueList).map(Year::of);
  }

  /**
   * Looks for the month in a ChronoAccumulator
   *
   * @param accumulator - where to look for a value
   *
   * @return Month value if present
   */
  public static Optional<Month> getMonth(ChronoAccumulator accumulator, List<OccurrenceIssue> issueList) {
    return convert(accumulator, MONTH_OF_YEAR, issueList).map(Month::of);
  }

  /**
   * Looks for the day in a ChronoAccumulator
   *
   * @param accumulator - where to look for a value
   *
   * @return Integer day value if present
   */
  public static Optional<Integer> getDay(ChronoAccumulator accumulator, List<OccurrenceIssue> issueList) {
    return convert(accumulator, DAY_OF_MONTH, issueList);
  }

  /**
   * Converts raw value to integer and put into the map
   *
   * @param accumulator raw value for parsing
   * @param chronoField one of the ChronoFields: YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, HOUR_OF_DAY, MINUTE_OF_HOUR, SECOND_OF_MINUTE
   */
  private static Optional<Integer> convert(ChronoAccumulator accumulator, ChronoField chronoField, List<OccurrenceIssue> issueList) {
    String rawValue = accumulator.getChronoFileValue(chronoField);
    if (!isEmpty(rawValue)) {
      Optional<Integer> value = FUNCTION_MAP.get(chronoField).apply(rawValue);
      if (!value.isPresent()) {
        issueList.add(OccurrenceIssue.RECORDED_DATE_INVALID);
      }
      return value;
    }
    return Optional.empty();
  }

}
