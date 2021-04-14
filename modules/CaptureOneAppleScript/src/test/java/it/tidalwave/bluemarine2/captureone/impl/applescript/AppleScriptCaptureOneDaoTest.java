/*
 * *********************************************************************************************************************
 *
 * blueMarine II: Semantic Media Centre
 * http://tidalwave.it/projects/bluemarine2
 *
 * Copyright (C) 2015 - 2021 by Tidalwave s.a.s. (http://tidalwave.it)
 *
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 * git clone https://bitbucket.org/tidalwave/bluemarine2-src
 * git clone https://github.com/tidalwave-it/bluemarine2-src
 *
 * *********************************************************************************************************************
 */
package it.tidalwave.bluemarine2.captureone.impl.applescript;

import javax.annotation.Nonnull;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.xml.bind.JAXBException;
import javax.script.ScriptException;
import it.tidalwave.bluemarine2.captureone.impl.C1Collection;
import it.tidalwave.bluemarine2.captureone.impl.C1Document;
import it.tidalwave.bluemarine2.captureone.impl.C1Image;
import org.testng.annotations.Test;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.util.test.FileComparisonUtils.assertSameContents;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public class AppleScriptCaptureOneDaoTest
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    static class TestSet
      {
        final Path TEST_RESULTS = Paths.get("target/test-results");
        final Path EXPECTED_RESULTS = Paths.get("target/test-classes/expected-results");

        final Path actualDocumentXmlFile;
        final Path expectedDocumentXmlFile;
        final Path actualDocumentDumpFile;
        final Path expectedDocumentDumpFile;

        public TestSet (@Nonnull final String name)
          {
            actualDocumentXmlFile = TEST_RESULTS.resolve(name + ".xml");
            expectedDocumentXmlFile = EXPECTED_RESULTS.resolve(name + ".xml");
            actualDocumentDumpFile = TEST_RESULTS.resolve(name + ".txt");
            expectedDocumentDumpFile = EXPECTED_RESULTS.resolve(name + ".txt");
          }
      }

    private final TestSet testSet = new TestSet("document");

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test(enabled = false)
    public void must_properly_retrieve_document()
      throws IOException, ScriptException
      {
        // given
        final AppleScriptCaptureOneDao underTest = new AppleScriptCaptureOneDao();
        // when
        final String document = underTest.getDocumentAsXml();
        // then
        final Path actualFile = testSet.actualDocumentXmlFile;
        final Path expectedFile = testSet.expectedDocumentXmlFile;
        Files.createDirectories(actualFile.getParent());
        Files.write(actualFile, List.of(document));
        assertSameContents(expectedFile.toFile(), actualFile.toFile());
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Test
    public void must_properly_parse_document()
      throws IOException, JAXBException
      {
        // given
        final AppleScriptCaptureOneDao underTest = new AppleScriptCaptureOneDao();
        final String in = String.join("\n", Files.readAllLines(testSet.expectedDocumentXmlFile, UTF_8));
        // when
        final C1Document document = underTest.parse(in);
        // then
        final Path actualFile = testSet.actualDocumentDumpFile;
        final Path expectedFile = testSet.expectedDocumentDumpFile;
        dump(actualFile, document);
        assertSameContents(expectedFile.toFile(), actualFile.toFile());
      }

    // TODO: test getDocument (dependsOnMethods = {"must_properly_retrieve_document", "must_properly_parse_document"})

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void dump (@Nonnull final Path path, @Nonnull final C1Document document)
      throws IOException
      {
        Files.createDirectories(path.getParent());

        try (final PrintWriter pw = new PrintWriter(path.toFile()))
          {
            pw.println(document.getName());
            document.getCollections().forEach(c -> dump(pw, "", c));
          }
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void dump (@Nonnull final PrintWriter pw,
                             @Nonnull final String indent,
                             @Nonnull final C1Collection collection)
      {
        pw.println(indent + " * " + collection.getName());
        collection.getCollections().forEach(c -> dump(pw, indent + "  ", c));
        collection.getImages().forEach(i -> dump(pw, indent + "  ", i));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void dump (@Nonnull final PrintWriter pw, @Nonnull final String indent, @Nonnull final C1Image image)
      {
        pw.println(indent + " * " + image.getName() + " (" + image.getPath() + ")");
      }
  }
