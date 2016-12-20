/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.gracenote.api.impl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static it.tidalwave.util.test.FileComparisonUtils8.*;
import static it.tidalwave.bluemarine2.commons.test.TestSetLocator.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: $
 *
 **********************************************************************************************************************/
@Slf4j
public class DefaultGracenoteApiTest
  {
    private DefaultGracenoteApi underTest;

    @BeforeMethod
    public void setup()
      {
        underTest = new DefaultGracenoteApi();
        underTest.initialize();
        underTest.setUserId(System.getProperty("gracenote.user", ""));
        underTest.setClientId(System.getProperty("gracenote.client", ""));
      }

    @Test
    public void testQueryAlbumToc()
      throws Exception
      {
        // given
        // when
        final ResponseEntity<String> response = underTest.queryAlbumToc("150 21860 38705 47155 68112 81095 99740 114517 131995 145947 163532 176950 188370 218577 241080 272992 287877 307292");
        final Path actualResult = dump("queryAlbumTocResponse.txt", response);
        // then
        assertSameContents(PATH_EXPECTED_TEST_RESULTS.resolve("queryAlbumTocResponse.txt"), actualResult);
      }

    @Test
    public void testQueryAlbumFetch()
      throws Exception
      {
        // given
        // when
        final ResponseEntity<String> response = underTest.queryAlbumFetch("161343049-DE60B292E7510AB532A959E2F8140814");
        final Path actualResult = dump("queryAlbumFetchResponse.txt", response);
        // then
        assertSameContents(PATH_EXPECTED_TEST_RESULTS.resolve("queryAlbumFetchResponse.txt"), actualResult);
      }

    @Test(enabled = false)
    public void downloadGracenoteResource()
      throws IOException
      {
        try (final Stream<Path> dirStream = Files.walk(Paths.get("target/metadata/iTunes-fg-20160504-1"), FOLLOW_LINKS))
          {
            dirStream.forEach(path -> process(path));
          }
      }

    private void process (final @Nonnull Path path)
      {
//        if (path.endsWith("-dump.txt"))
        if (Files.isRegularFile(path))          {
            try
              {
                log.info("{}", path);
                final Optional<String> iTunesComment = Files.lines(path, UTF_8).filter(s -> s.contains("[iTunes.comment]"))
                                                            .findFirst()
                                                            .map(s -> s.replaceAll("^.*cddb1=", "").replaceAll(", .*$", ""));
                log.info(">>>> {}", iTunesComment);

                if (iTunesComment.isPresent())
                  {
                    final String offsets = itunesCommentToAlbumToc(iTunesComment.get());
//                    underTest.queryAlbumToc(offsets);
                  }
              }
            catch (IOException e)
              {
                log.error("", e);
              }
          }
      }

    @Nonnull
    private static String itunesCommentToAlbumToc (final @Nonnull String comment)
      {
        return Stream.of(comment.split("\\+")).skip(2).collect(Collectors.joining(" "));
      }

    private final static List<String> IGNORED_HEADERS =
            Arrays.asList("Date", "Server", "X-Powered-By", "Connection", "Keep-Alive", "Vary");

    @Nonnull
    private static Path dump (final @Nonnull String resourceName, final @Nonnull ResponseEntity<String> response)
      throws IOException
      {
        final Path actualResult = PATH_TEST_RESULTS.resolve(resourceName);
        Files.createDirectories(actualResult.getParent());
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.printf("HTTP/1.1 %d %s%n", response.getStatusCode().value(), response.getStatusCode().name());
        response.getHeaders().entrySet().stream()
                                        .filter(e -> !IGNORED_HEADERS.contains(e.getKey()))
                                        .sorted(comparing(e -> e.getKey()))
                                        .forEach(e -> pw.printf("%s: %s%n", e.getKey(), e.getValue().get(0)));
        pw.println();
        pw.print(response.getBody());
        pw.close();
        Files.write(actualResult, Arrays.asList(sw.toString()), UTF_8);
        return actualResult;
      }
  }
