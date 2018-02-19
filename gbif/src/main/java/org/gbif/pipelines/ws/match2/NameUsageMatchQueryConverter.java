package org.gbif.pipelines.ws.match2;

import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.api.vocabulary.Rank;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.common.parsers.utils.ClassificationUtils;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.terms.GbifTerm;
import org.gbif.dwc.terms.Term;
import org.gbif.pipelines.io.avro.ExtendedRecord;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import static org.gbif.pipelines.interpretation.parsers.VocabularyParsers.rankParser;
import static org.gbif.pipelines.interpretation.parsers.VocabularyParsers.verbatimTaxonRankParser;

public class NameUsageMatchQueryConverter {

  private static class AtomizedFields {

    private final String genus;
    private final String specificEpithet;
    private final String infraspecificEpithet;

    private static Builder newBuilder() {
      return new Builder();
    }

    private AtomizedFields(String genus, String specificEpithet, String infraspecificEpithet) {
      this.genus = genus;
      this.specificEpithet = specificEpithet;
      this.infraspecificEpithet = infraspecificEpithet;
    }

    private AtomizedFields(Builder builder) {
      genus = builder.genus;
      specificEpithet = builder.specificEpithet;
      infraspecificEpithet = builder.infraspecificEpithet;
    }

    public String getGenus() {
      return genus;
    }

    public String getSpecificEpithet() {
      return specificEpithet;
    }

    public String getInfraspecificEpithet() {
      return infraspecificEpithet;
    }

    public static final class Builder {

      private String genus;
      private String specificEpithet;
      private String infraspecificEpithet;

      private Builder() {}

      public Builder withGenus(String genus) {
        this.genus = Strings.emptyToNull(genus);
        return this;
      }

      public Builder withSpecificEpithet(String specificEpithet) {
        this.specificEpithet = Strings.emptyToNull(specificEpithet);
        return this;
      }

      public Builder withInfraspecificEpithet(String infraspecificEpithet) {
        this.infraspecificEpithet = Strings.emptyToNull(infraspecificEpithet);
        return this;
      }

      public AtomizedFields build() {
        return new AtomizedFields(this);
      }
    }
  }


  public static Map<String, String> convert(ExtendedRecord extendedRecord) {
    ImmutableMap.Builder<String,String> builder = ImmutableMap.builder();

    AtomizedFields.Builder atomizedFieldsBuilder = AtomizedFields.newBuilder();

    getTaxonValue(extendedRecord, DwcTerm.kingdom)
      .ifPresent(cleanValue -> builder.put("kingdom", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.phylum)
      .ifPresent(cleanValue -> builder.put("phylum", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.phylum)
      .ifPresent(cleanValue -> builder.put("phylum", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.class_)
      .ifPresent(cleanValue -> builder.put("class", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.order)
      .ifPresent(cleanValue -> builder.put("order", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.family)
      .ifPresent(cleanValue -> builder.put("family", cleanValue));

    getTaxonValue(extendedRecord, DwcTerm.genus)
      .ifPresent(cleanValue -> {
        builder.put("genus", cleanValue);
        atomizedFieldsBuilder.withGenus(cleanValue);
      });

    Optional<String> genericName = getTaxonValue(extendedRecord, GbifTerm.genericName);
    genericName.ifPresent(cleanValue -> builder.put("genericName", cleanValue));

    Optional<String> scientificNameAuthorship = getAuthorValue(extendedRecord, DwcTerm.scientificNameAuthorship);
    scientificNameAuthorship.ifPresent(cleanValue -> builder.put("scientificNameAuthorship", cleanValue));

    getAuthorValue(extendedRecord, DwcTerm.specificEpithet)
      .ifPresent(cleanValue -> {
        builder.put("specificEpithet", cleanValue);
        atomizedFieldsBuilder.withSpecificEpithet(cleanValue);
    });

    getAuthorValue(extendedRecord, DwcTerm.infraspecificEpithet)
      .ifPresent(cleanValue -> {
        builder.put("infraspecificEpithet", cleanValue);
        atomizedFieldsBuilder.withInfraspecificEpithet(cleanValue);
      });

    AtomizedFields atomizedFields = atomizedFieldsBuilder.build();

    interpretRank(extendedRecord, atomizedFields)
    .ifPresent(rank -> builder.put("rank", rank.name()));

    interpretScientificName(extendedRecord, atomizedFields, scientificNameAuthorship.orElse(null), genericName.orElse(null))
    .ifPresent(scientificName -> builder.put("scientificName", scientificName));

    builder.put("strict", Boolean.FALSE.toString());
    builder.put("verbose", Boolean.FALSE.toString());
    return builder.build();
  }

  /**
   * Gets a clean version of taxa parameter.
   *
   */
  private static Optional<String> getTaxonValue(ExtendedRecord extendedRecord, Term term) {
    return Optional.ofNullable(extendedRecord.getCoreTerms().get(term.qualifiedName()))
      .map(value -> ClassificationUtils.clean(value.toString()));
  }

  /**
   * Gets a clean version of field with authorship.
   *
   */
  private static Optional<String> getAuthorValue(ExtendedRecord extendedRecord, Term term) {
    return Optional.ofNullable(extendedRecord.getCoreTerms().get(term.qualifiedName()))
      .map(value -> ClassificationUtils.cleanAuthor(value.toString()));
  }

  private static Optional<Rank> interpretRank(ExtendedRecord extendedRecord, AtomizedFields atomizedFields) {
    Optional<Rank> interpretedRank = rankParser().map(extendedRecord, Function.identity())
      .map(rankParseResult -> rankParseResult.isSuccessful() ? rankParseResult.getPayload() :
        verbatimTaxonRankParser().map(extendedRecord, ParseResult::getPayload).get());

     if(!interpretedRank.isPresent()) {
       return fromAtomizedFields(atomizedFields);
     }

     return interpretedRank;
  }

  private static Optional<Rank> fromAtomizedFields(AtomizedFields atomizedFields) {
    if(!Objects.nonNull(atomizedFields.getGenus())) {
      if(Objects.nonNull(atomizedFields.getSpecificEpithet())) {
        return Objects.nonNull(atomizedFields.getInfraspecificEpithet())? Optional.of(Rank.INFRASPECIFIC_NAME) : Optional.of(Rank.SPECIES);
      }
      return Optional.of(Rank.GENUS);
    }
    return Optional.empty();
  }

  /**
   * Assembles the most complete scientific name based on full and individual name parts.
   */
  private static Optional<String> interpretScientificName(ExtendedRecord extendedRecord, AtomizedFields atomizedFields,
                                                          String authorship, String genericName) {
    Optional<String> interpretedScientificName = Optional.of(extendedRecord.getCoreTerms().get(DwcTerm.scientificName.qualifiedName()))
      .map(scientificNameValue -> {
        String scientificName = ClassificationUtils.clean(scientificNameValue.toString());
        return Strings.isNullOrEmpty(authorship)
               && !scientificName.toLowerCase().contains(authorship.toLowerCase()) ? scientificName : scientificName + " " + authorship;
      });
    if (!interpretedScientificName.isPresent()) {
      // handle case when the scientific name is null and only given as atomized fields: genus & speciesEpitheton
      ParsedName pn = new ParsedName();
      if (Strings.isNullOrEmpty(genericName)) {
        pn.setGenusOrAbove(atomizedFields.getGenus());
      } else {
        pn.setGenusOrAbove(genericName);
      }
      pn.setSpecificEpithet(atomizedFields.getSpecificEpithet());
      pn.setInfraSpecificEpithet(atomizedFields.getInfraspecificEpithet());
      pn.setAuthorship(authorship);
      return Optional.ofNullable(pn.canonicalNameComplete());
    }
    return interpretedScientificName;

  }
}
