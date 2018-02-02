package org.gbif.pipelines.demo;

import org.gbif.pipelines.core.config.DataProcessingPipelineOptions;
import org.gbif.pipelines.core.config.Interpretation;
import org.gbif.pipelines.demo.utils.PipelineUtils;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the {@link TaxonomyInterpretationPipeline}.
 */
public class TaxonomyInterpretationPipelineTest {

  private static TestUtils.MiniClusterConfig clusterConfig;
  private static Configuration configuration = new Configuration();

  @BeforeClass
  public static void setUp() throws Exception {
    clusterConfig = TestUtils.createMiniCluster(configuration);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    clusterConfig.hdfsCluster.shutdown();
  }

  @Test
  @Ignore
  // FIXME: ignored becasue when run with maven it does not find the properties file. Before fixing it we should
  // decide how we are gonna handle the properties files, externalized? reading from maven profiles? any other idea?
  public void taxonomyInterpretationPipelineTest() throws Exception {

    final String sourcePath = "data/extendedRecords*";
    final String taxonOutPath = clusterConfig.hdfsClusterBaseUri + "taxon";
    final String issuesOutPath = clusterConfig.hdfsClusterBaseUri + "taxonIssues";

    DataProcessingPipelineOptions options =
      PipelineUtils.createDefaultTaxonOptions(configuration, sourcePath, taxonOutPath, issuesOutPath);

    TaxonomyInterpretationPipeline.runPipelineProgrammatically(options);

    // test taxon results
    URI uriTargetPath =
      clusterConfig.hdfsClusterBaseUri.resolve(options.getTargetPaths().get(Interpretation.TAXONOMY).getFilePath()
                                               + "*");
    FileStatus[] fileStatuses = clusterConfig.fs.globStatus(new Path(uriTargetPath.toString()));

    Assert.assertNotNull(fileStatuses);
    Assert.assertTrue(fileStatuses.length > 0);

    // a bit redundant, just for demo purposes
    for (FileStatus fileStatus : fileStatuses) {
      Assert.assertTrue(fileStatus.isFile());
      Assert.assertTrue(clusterConfig.fs.exists(fileStatus.getPath()));
    }

    // test issues results
    uriTargetPath =
      clusterConfig.hdfsClusterBaseUri.resolve(options.getTargetPaths().get(Interpretation.ISSUES).getFilePath() + "*");
    fileStatuses = clusterConfig.fs.globStatus(new Path(uriTargetPath.toString()));

    Assert.assertNotNull(fileStatuses);
    Assert.assertTrue(fileStatuses.length > 0);

    // a bit redundant, just for demo purposes
    for (FileStatus fileStatus : fileStatuses) {
      Assert.assertTrue(fileStatus.isFile());
      Assert.assertTrue(clusterConfig.fs.exists(fileStatus.getPath()));
    }

  }

}