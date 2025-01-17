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
package it.tidalwave.bluemarine2.rest;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import lombok.extern.slf4j.Slf4j;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Slf4j
public class ResponseEntityIo
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static void store (@Nonnull final Path path,
                              @Nonnull final ResponseEntity<?> response,
                              @Nonnull final List<String> ignoredHeaders)
      {
          store(path, response, ignoredHeaders, Function.identity());
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public static void store (@Nonnull final Path path,
                              @Nonnull final ResponseEntity<?> response,
                              @Nonnull final List<String> ignoredHeaders,
                              @Nonnull final Function<String, String> postProcessor)
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
                        .sorted(comparing(Map.Entry::getKey))
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
    /* package */ static Optional<ResponseEntity<String>> load (@Nonnull final Path path)
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
