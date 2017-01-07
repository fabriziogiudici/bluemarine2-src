/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2017 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
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
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.bluemarine2.metadata.cddb.impl;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import it.tidalwave.bluemarine2.model.MediaItem.Metadata.ITunesComment;
import it.tidalwave.bluemarine2.metadata.cddb.CddbAlbum;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import it.tidalwave.bluemarine2.commons.test.TestSetLocator;
import it.tidalwave.bluemarine2.commons.test.TestSetTriple;
import it.tidalwave.bluemarine2.metadata.cddb.CddbResponse;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static it.tidalwave.bluemarine2.commons.test.TestSetTriple.*;
import static it.tidalwave.bluemarine2.rest.CachingRestClientSupport.CacheMode.*;
import static it.tidalwave.util.test.FileComparisonUtils8.assertSameContents;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: $
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultCddbMetadataTest
  {
    private static final Path METADATA = Paths.get("target/metadata");

    private static final Path CACHE = Paths.get("target/cache");

    private static final Path CDDB_CACHE = CACHE.resolve("cddb");

    private static final Path TEST_RESULTS = Paths.get("target/test-results");

    private static final Path EXPECTED_RESULTS = Paths.get("target/expected-results");

    private DefaultCddbMetadataProvider underTest;

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @BeforeMethod
    public void setup()
      {
        underTest = new DefaultCddbMetadataProvider();
//        underTest.initialize();
      }

    /*******************************************************************************************************************
     *
     * Test sets iTunes-fg-20160504-1 and iTunes-fg-20161210-1 validated on 2017-01-07 18:00.
     *
     ******************************************************************************************************************/
    @Test(dataProvider = "trackResourcesProvider")
    public void must_correctly_download_MusicBrainz_resources (final @Nonnull TestSetTriple triple)
      throws Exception
      {
        // given
        final Path relativePath = triple.getRelativePath();
        final String testSetName = triple.getTestSetName();
        final Path actualResult = TEST_RESULTS.resolve(testSetName).resolve(relativePath);
        final Path expectedResult = EXPECTED_RESULTS.resolve("cddb").resolve(testSetName).resolve(relativePath);

        underTest.setCacheMode(USE_CACHE);
        underTest.setCachePath(CDDB_CACHE.resolve(testSetName));
//        underTest.initialize(); // FIXME

        final Optional<ITunesComment> iTunesComment = readiTunesCommentFrom(triple.getFilePath());
        CddbResponse<CddbAlbum> response = null;
        final String string;

        if (!iTunesComment.isPresent())
          {
            string = "NO ITUNES COMMENT";
          }
        else
          {
            // when
            response = underTest.findCddbAlbum(iTunesComment.get());
            // then
            string = response.map(CddbAlbum::toDumpString).orElse("NOT FOUND");
          }

        log.info(">>>> writing to {}", actualResult);
        Files.createDirectories(actualResult.getParent());
        Files.write(actualResult, singletonList(string), UTF_8);
        assertSameContents(expectedResult, actualResult);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<ITunesComment> readiTunesCommentFrom (final @Nonnull Path path)
      throws IOException
      {
        return Files.lines(path, UTF_8).filter(s -> s.contains("[iTunes.comment]"))
                                       .findFirst()
                                       .map(s -> ITunesComment.fromToString(s.replaceAll("^.* = ", "")));
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @DataProvider
    private static Object[][] trackResourcesProvider()
      {
        return streamOfTestSetTriples(TestSetLocator.allTestSets(), name -> METADATA.resolve(name))
                                                    .collect(toTestNGDataProvider());
      }
  }