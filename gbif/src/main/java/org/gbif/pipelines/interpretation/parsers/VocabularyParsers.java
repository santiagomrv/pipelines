package org.gbif.pipelines.interpretation.parsers;

import org.gbif.api.vocabulary.BasisOfRecord;
import org.gbif.api.vocabulary.EstablishmentMeans;
import org.gbif.api.vocabulary.LifeStage;
import org.gbif.api.vocabulary.Sex;
import org.gbif.api.vocabulary.TypeStatus;
import org.gbif.common.parsers.BasisOfRecordParser;
import org.gbif.common.parsers.EstablishmentMeansParser;
import org.gbif.common.parsers.LifeStageParser;
import org.gbif.common.parsers.SexParser;
import org.gbif.common.parsers.TypeStatusParser;
import org.gbif.common.parsers.core.EnumParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.pipelines.io.avro.ExtendedRecord;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Utility class that parses Enum based terms.
 */
public class VocabularyParsers<T extends Enum<T>> {

  private static final TypeStatusParser TYPE_PARSER = TypeStatusParser.getInstance();
  private static final BasisOfRecordParser BOR_PARSER = BasisOfRecordParser.getInstance();
  private static final SexParser SEX_PARSER = SexParser.getInstance();
  private static final EstablishmentMeansParser EST_PARSER = EstablishmentMeansParser.getInstance();
  private static final LifeStageParser LST_PARSER = LifeStageParser.getInstance();

  //Parser to be used
  private final EnumParser<T> parser;

  //Term ot be parsed
  private final DwcTerm term;

  /**
   * Private constructor that keeps the basic info to run a parser.
   */
  private VocabularyParsers(EnumParser<T> parser, DwcTerm term) {
    this.parser = parser;
    this.term = term;
  }

  /**
   *
   * @return a basis of record parser.
   */
  public static VocabularyParsers<BasisOfRecord> basisOfRecordParser(){
    return new VocabularyParsers<>(BOR_PARSER, DwcTerm.basisOfRecord);
  }

  /**
   *
   * @return a sex parser.
   */
  public static VocabularyParsers<Sex> sexParser(){
    return new VocabularyParsers<>(SEX_PARSER, DwcTerm.sex);
  }

  /**
   *
   * @return a life stage parser.
   */
  public static VocabularyParsers<LifeStage> lifeStageParser(){
    return new VocabularyParsers<>(LST_PARSER, DwcTerm.lifeStage);
  }

  /**
   *
   * @return a establishmentMeans parser.
   */
  public static VocabularyParsers<EstablishmentMeans> establishmentMeansParser(){
    return new VocabularyParsers<>(EST_PARSER, DwcTerm.establishmentMeans);
  }

  /**
   *
   * @return a type status parser.
   */
  public static VocabularyParsers<TypeStatus> typeStatusParser(){
    return new VocabularyParsers<>(TYPE_PARSER, DwcTerm.typeStatus);
  }

  /**
   * Runs a parsing method on a extended record.
   * @param extendedRecord to be used as input
   * @param onParse consumer called during parsing
   */
  public void parse(ExtendedRecord extendedRecord, Consumer<ParseResult<T>> onParse) {
    Optional.ofNullable(extendedRecord.getCoreTerms().get(term.qualifiedName()).toString())
      .ifPresent( value -> onParse.accept(parser.parse(value)));
  }

}