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
package it.tidalwave.bluemarine2.commons.test;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import it.tidalwave.util.As;
import it.tidalwave.role.SimpleComposite;
import it.tidalwave.bluemarine2.util.Dumpable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.util.test.FileComparisonUtilsWithPathNormalizer.*;
import static it.tidalwave.role.SimpleComposite._SimpleComposite_;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE) @Slf4j
public class TestUtilities
  {
    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void dumpAndAssertResults (@Nonnull final String fileName, @Nonnull final Collection<?> data)
      throws IOException
      {
        final Path actualResult = Paths.get("target", "test-results", fileName);
        final Path expectedResult = Paths.get("target", "test-classes", "expected-results", fileName);
        Files.createDirectories(actualResult.getParent());
        final Stream<String> stream = data.stream().map(Object::toString);
        Files.write(actualResult, (Iterable<String>)stream::iterator, StandardCharsets.UTF_8);
        assertSameContents(expectedResult, actualResult);
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    @Nonnull
    public static List<String> dump (@Nonnull final As entity)
      {
        final List<String> result = new ArrayList<>();
        result.add((entity instanceof Dumpable) ? ((Dumpable)entity).toDumpString() : entity.toString());
        final Optional<SimpleComposite> composite = entity.maybeAs(_SimpleComposite_);
        composite.ifPresent(c -> c.findChildren().results().forEach(child -> result.addAll(dump((As)child))));

        return result;
      }

    /*******************************************************************************************************************
     *
     ******************************************************************************************************************/
    public static void loadRepository (@Nonnull final Repository repository, @Nonnull final Path path)
      throws Exception
      {
        log.info("loadRepository(..., {})", path);

        try (final RepositoryConnection connection = repository.getConnection())
          {
            connection.add(path.toFile(), null, RDFFormat.N3);
            connection.commit();
          }
      }
  }
