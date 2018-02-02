package org.gbif.pipelines.core.tools;

import org.gbif.api.v2.NameUsageMatch2;
import org.gbif.api.vocabulary.Rank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.codehaus.jackson.node.NullNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the class {@link AvroSchemaGenerator}.
 */
public class AvroSchemaGeneratorTest {

  @Test
  @Ignore
  public void generateDefaultTaxonomicSchemaTest() {
    String path = AvroSchemaGenerator.DEFAULT_TAXON_SCHEMA_PATH;

    Schema schemaGenerated = AvroSchemaGenerator.generateDefaultTaxonomicSchema();
    System.out.println(schemaGenerated.toString(true));

    try {
      AvroSchemaGenerator.writeSchemaToFile(path, schemaGenerated);
    } catch (IOException e) {
      Assert.fail("Could not generate default taxonomic schema in path " + path + ": " + e.getMessage());
    }

  }

  @Test
  public void generateSchemaTest() {
    String name = "TaxonRecord";
    String doc = "Taxonomic Record";
    String namespace = "org.gbif.pipelines.io.avro";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(NameUsageMatch2.class, name, doc, namespace);
    System.out.println(schemaGenerated.toString(true));
  }

  private static class ClassNotParametrizedList {

    List list = new ArrayList<>();
  }

  @Test
  public void testNotParametrizedListSchema() {
    String name = "Test";
    String namespace = "ns.test";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(ClassNotParametrizedList.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));

    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                         Schema.createArray(Schema.create(Schema.Type.STRING)))),
                        schemaGenerated.getField("list").schema());
  }

  private static class ClassParametrizedList {

    List<Integer> list = new ArrayList<>();
  }

  @Test
  public void testParametrizedListSchema() {
    String name = "Test";
    String namespace = "ns.test";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(ClassParametrizedList.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));

    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                         Schema.createArray(Schema.create(Schema.Type.INT)))),
                        schemaGenerated.getField("list").schema());
  }

  private static class ClassFloatFields {

    Float floatField;
    float floatPrimitiveField;
  }

  @Test
  public void testFloatFieldsSchema() {
    String name = "Test";
    String namespace = "ns.test";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(ClassFloatFields.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));

    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                         Schema.create(Schema.Type.FLOAT))),
                        schemaGenerated.getField("floatField").schema());
    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                         Schema.create(Schema.Type.FLOAT))),
                        schemaGenerated.getField("floatPrimitiveField").schema());
  }

  private static class ClassWithEnum {

    Rank rank;
  }

  @Test
  public void testEnumSchema() {
    String name = "Test";
    String namespace = "ns.test";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(ClassWithEnum.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));

    List<String> rankValues = Arrays.stream(Rank.values()).map(value -> value.toString()).collect(Collectors.toList());

    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                         Schema.createEnum("Rank", null, namespace, rankValues))),
                        schemaGenerated.getField("rank").schema());
  }

  private static class ClassWithCustomField {

    ClassFloatFields floatFields;
  }

  @Test
  public void testCustomTypesSchema() {
    String name = "Test";
    String namespace = "ns.test";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(ClassWithCustomField.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));

    Schema record = Schema.createRecord(ClassFloatFields.class.getSimpleName(), null, namespace, false);
    List<Schema.Field> fields = new ArrayList<>();
    fields.add(new Schema.Field("floatField",
                                Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                                 Schema.create(Schema.Type.FLOAT))),
                                null,
                                NullNode.getInstance()));
    fields.add(new Schema.Field("floatPrimitiveField",
                                Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL),
                                                                 Schema.create(Schema.Type.FLOAT))),
                                null,
                                NullNode.getInstance()));
    record.setFields(fields);

    Assert.assertEquals(Schema.createUnion(Arrays.asList(Schema.create(Schema.Type.NULL), record)),
                        schemaGenerated.getField("floatFields").schema());
  }

  @Test( expected = IllegalArgumentException.class)
  public void testNullValues() {
    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(null, null, null, null);
  }

  @Test()
  public void testNullDocValue() {
    String name = "TaxonRecord";
    String namespace = "org.gbif.pipelines.io.avro";

    Schema schemaGenerated = AvroSchemaGenerator.generateSchema(NameUsageMatch2.class, name, null, namespace);
    System.out.println(schemaGenerated.toString(true));
  }

}