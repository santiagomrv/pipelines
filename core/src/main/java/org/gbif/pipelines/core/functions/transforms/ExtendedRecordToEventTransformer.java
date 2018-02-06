package org.gbif.pipelines.core.functions.transforms;

import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwca.avro.Event;
import org.gbif.pipelines.core.functions.interpretation.DayInterpreter;
import org.gbif.pipelines.core.functions.interpretation.InterpretationException;
import org.gbif.pipelines.core.functions.interpretation.MonthInterpreter;
import org.gbif.pipelines.core.functions.interpretation.YearInterpreter;
import org.gbif.pipelines.core.functions.interpretation.error.Issue;
import org.gbif.pipelines.core.functions.interpretation.error.IssueLineageRecord;
import org.gbif.pipelines.core.functions.interpretation.error.Lineage;
import org.gbif.pipelines.io.avro.ExtendedRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This function converts an extended record to an interpreted KeyValue of occurrenceId and Event.
 * This function returns multiple outputs,
 * a. Interpreted version of raw temporal data as KV<String,Event>
 * b. Issues and lineages applied on raw data to get the interpreted result, as KV<String,IssueLineageRecord>
 */
public class ExtendedRecordToEventTransformer extends DoFn<ExtendedRecord, KV<String, Event>> {

  /**
   * tags for locating different types of outputs send by this function
   */
  public static final TupleTag<KV<String, Event>> EVENT_DATA_TAG = new TupleTag<>();
  public static final TupleTag<KV<String, IssueLineageRecord>> EVENT_ISSUE_TAG = new TupleTag<>();
  private static final Logger LOG = LoggerFactory.getLogger(ExtendedRecordToEventTransformer.class);

  @ProcessElement
  public void processElement(ProcessContext ctx) {
    ExtendedRecord record = ctx.element();
    Event evt = new Event();

    Map<CharSequence, List<Issue>> fieldIssueMap = new HashMap<>();
    Map<CharSequence, List<Lineage>> fieldLineageMap = new HashMap<>();
    //mapping raw record with interpreted ones
    evt.setOccurrenceID(record.getId());

    evt.setBasisOfRecord(record.getCoreTerms().get(DwcTerm.basisOfRecord.qualifiedName()));
    evt.setEventID(record.getCoreTerms().get(DwcTerm.eventID.qualifiedName()));
    evt.setParentEventID(record.getCoreTerms().get(DwcTerm.parentEventID.qualifiedName()));
    evt.setFieldNumber(record.getCoreTerms().get(DwcTerm.fieldNumber.qualifiedName()));
    evt.setEventDate(record.getCoreTerms().get(DwcTerm.eventDate.qualifiedName()));
    evt.setStartDayOfYear(record.getCoreTerms().get(DwcTerm.startDayOfYear.qualifiedName()));
    evt.setEndDayOfYear(record.getCoreTerms().get(DwcTerm.endDayOfYear.qualifiedName()));

    /*
      Day month year interpretation
     */
    CharSequence raw_year = record.getCoreTerms().get(DwcTerm.year.qualifiedName());
    CharSequence raw_month = record.getCoreTerms().get(DwcTerm.month.qualifiedName());
    CharSequence raw_day = record.getCoreTerms().get(DwcTerm.day.qualifiedName());

    if (raw_day != null) {
      try {
        evt.setDay(new DayInterpreter().interpret(raw_day.toString()));
      } catch (InterpretationException e) {
        fieldIssueMap.put(DwcTerm.day.name(), e.getIssues());
        fieldLineageMap.put(DwcTerm.day.name(), e.getLineages());
        if (e.getInterpretedValue().isPresent()) evt.setDay((Integer) e.getInterpretedValue().get());
      }
    }

    if (raw_month != null) {
      try {
        evt.setMonth(new MonthInterpreter().interpret(raw_month.toString()));
      } catch (InterpretationException e) {
        fieldIssueMap.put(DwcTerm.month.name(), e.getIssues());
        fieldLineageMap.put(DwcTerm.month.name(), e.getLineages());
        if (e.getInterpretedValue().isPresent()) evt.setMonth((Integer) e.getInterpretedValue().get());
      }
    }

    if (raw_year != null) {
      try {
        evt.setYear(new YearInterpreter().interpret(raw_year.toString()));
      } catch (InterpretationException e) {
        fieldIssueMap.put(DwcTerm.year.name(), e.getIssues());
        fieldLineageMap.put(DwcTerm.year.name(), e.getLineages());
        if (e.getInterpretedValue().isPresent()) evt.setYear((Integer) e.getInterpretedValue().get());
      }
    }

    evt.setVerbatimEventDate(record.getCoreTerms().get(DwcTerm.verbatimEventDate.qualifiedName()));
    evt.setHabitat(record.getCoreTerms().get(DwcTerm.habitat.qualifiedName()));
    evt.setSamplingProtocol(record.getCoreTerms().get(DwcTerm.samplingProtocol.qualifiedName()));
    evt.setSamplingEffort(record.getCoreTerms().get(DwcTerm.samplingEffort.qualifiedName()));
    evt.setSampleSizeValue(record.getCoreTerms().get(DwcTerm.sampleSizeValue.qualifiedName()));
    evt.setSampleSizeUnit(record.getCoreTerms().get(DwcTerm.sampleSizeUnit.qualifiedName()));
    evt.setFieldNotes(record.getCoreTerms().get(DwcTerm.fieldNotes.qualifiedName()));
    evt.setEventRemarks(record.getCoreTerms().get(DwcTerm.eventRemarks.qualifiedName()));
    evt.setInstitutionID(record.getCoreTerms().get(DwcTerm.institutionID.qualifiedName()));
    evt.setCollectionID(record.getCoreTerms().get(DwcTerm.collectionID.qualifiedName()));
    evt.setDatasetID(record.getCoreTerms().get(DwcTerm.datasetID.qualifiedName()));
    evt.setInstitutionCode(record.getCoreTerms().get(DwcTerm.institutionCode.qualifiedName()));
    evt.setCollectionCode(record.getCoreTerms().get(DwcTerm.collectionCode.qualifiedName()));
    evt.setDatasetName(record.getCoreTerms().get(DwcTerm.datasetName.qualifiedName()));
    evt.setOwnerInstitutionCode(record.getCoreTerms().get(DwcTerm.ownerInstitutionCode.qualifiedName()));
    evt.setDynamicProperties(record.getCoreTerms().get(DwcTerm.dynamicProperties.qualifiedName()));
    evt.setInformationWithheld(record.getCoreTerms().get(DwcTerm.informationWithheld.qualifiedName()));
    evt.setDataGeneralizations(record.getCoreTerms().get(DwcTerm.dataGeneralizations.qualifiedName()));
    //all issues and lineages are dumped on this object
    final IssueLineageRecord finalRecord = IssueLineageRecord.newBuilder()
      .setOccurenceId(record.getId())
      .setFieldIssuesMap(fieldIssueMap)
      .setFieldLineageMap(fieldLineageMap)
      .build();

    ctx.output(EVENT_DATA_TAG, KV.of(evt.getOccurrenceID().toString(), evt));
    ctx.output(EVENT_ISSUE_TAG, KV.of(evt.getOccurrenceID().toString(), finalRecord));
  }

}