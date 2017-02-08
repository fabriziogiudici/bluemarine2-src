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
package it.tidalwave.bluemarine2.rest;

import java.io.ByteArrayOutputStream;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: $
 *
 **********************************************************************************************************************/
@Slf4j
public class ResponseEntityIo
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static void store (final @Nonnull Path path,
                              final @Nonnull ResponseEntity<?> response,
                              final @Nonnull List<String> ignoredHeaders)
      {
          store(path, response, ignoredHeaders, Function.identity());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    public static void store (final @Nonnull Path path,
                              final @Nonnull ResponseEntity<?> response,
                              final @Nonnull List<String> ignoredHeaders,
                              final @Nonnull Function<String, String> postProcessor)
      {
        try
          {
            log.trace("store({}, ..., ...)", path);

            Files.createDirectories(path.getParent());
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (final PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, UTF_8)))
              {
                pw.printf("HTTP/1.1 %d %s%n", response.getStatusCode().value(), response.getStatusCode().name());
                response.getHeaders().entrySet().stream()
                        .filter(e -> !ignoredHeaders.contains(e.getKey()))
                        .sorted(comparing(e -> e.getKey()))
                        .forEach(e -> pw.printf("%s: %s%n", e.getKey(), e.getValue().get(0)));
                pw.println();
                pw.flush();
                final Object body = response.getBody();
                log.info(">>>> TYPE {}", body.getClass());

                if (body instanceof String)
                  {
                    pw.print(postProcessor.apply((String)body));
                  }
                else
                  {
                    baos.write((byte[])body);
                  }
              }

            Files.write(path, baos.toByteArray());
          }
        catch (IOException e)
          {
            log.error("Coundln't store a cache item {}", path);
          }
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    /* package */ static Optional<ResponseEntity<String>> load (final @Nonnull Path path)
      throws IOException
      {
        log.trace("load({})", path);

        if (!Files.exists(path))
          {
            return Optional.empty();
          }

        final List<String> lines = Files.readAllLines(path, UTF_8);
        final HttpStatus status = HttpStatus.valueOf(Integer.parseInt(lines.get(0).split(" ")[1]));
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        int i = 1;

        for (; (i < lines.size()) && !lines.get(i).equals(""); i++)
          {
            final String[] split = lines.get(i).split(":");
            headers.add(split[0], split[1].trim());
          }

        final String body = lines.stream().skip(i + 1).collect(joining("\n"));
        final ResponseEntity<String> response = new ResponseEntity<>(body, headers, status);
//        log.trace(">>>> returning {}", response);

        return Optional.of(response);
      }
  }
