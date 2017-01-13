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

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Optional;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import static java.util.Collections.*;
import static org.springframework.http.HttpHeaders.*;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: $
 *
 **********************************************************************************************************************/
@Slf4j
public class CachingRestClientSupport
  {
    public enum CacheMode
      {
        /** Always use the network. */
        DONT_USE_CACHE
          {
            @Override @Nonnull
            public ResponseEntity<String> request (final @Nonnull CachingRestClientSupport api,
                                                   final @Nonnull String url)
              throws IOException, InterruptedException
              {
                return api.requestFromNetwork(url);
              }
          },
        /** Never use the network. */
        ONLY_USE_CACHE
          {
            @Override @Nonnull
            public ResponseEntity<String> request (final @Nonnull CachingRestClientSupport api,
                                                   final @Nonnull String url)
              throws IOException
              {
                return api.requestFromCache(url).get();
              }
          },
        /** First try the cache, then the network. */
        USE_CACHE
          {
            @Override @Nonnull
            public ResponseEntity<String> request (final @Nonnull CachingRestClientSupport api,
                                                   final @Nonnull String url)
              throws IOException, InterruptedException
              {
                return api.requestFromCacheAndThenNetwork(url);
              }
          };

        @Nonnull
        public abstract ResponseEntity<String> request (@Nonnull CachingRestClientSupport api,
                                                        @Nonnull String url)
          throws IOException, InterruptedException;
      }

    private final RestTemplate restTemplate = new RestTemplate(); // FIXME: inject?

    @Getter @Setter
    private CacheMode cacheMode = CacheMode.USE_CACHE;

    @Getter @Setter
    private Path cachePath;

    @Getter @Setter
    private String accept = "application/xml";

    @Getter @Setter
    private String userAgent = "blueMarine II (fabrizio.giudici@tidalwave.it)";

    @Getter @Setter
    private long throttleLimit;

    private long latestNetworkAccessTimestamp = 0;

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    private final ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor()
      {
        @Override
        public ClientHttpResponse intercept (final HttpRequest request,
                                             final byte[] body,
                                             final ClientHttpRequestExecution execution)
          throws IOException
          {
            final HttpHeaders headers = request.getHeaders();
            headers.add(USER_AGENT, userAgent);
            headers.add(ACCEPT, accept);
            return execution.execute(request, body);
          }
      };

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    public CachingRestClientSupport()
      {
        restTemplate.setInterceptors(singletonList(interceptor));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    void initialize()
      {
      }

    /*******************************************************************************************************************
     *
     * Performs a  web request.
     *
     * @return                  the response
     *
     ******************************************************************************************************************/
    @Nonnull
    protected ResponseEntity<String> request (final @Nonnull String url)
      throws IOException, InterruptedException
      {
        log.debug("request({})", url);
        return cacheMode.request(this, url);
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private Optional<ResponseEntity<String>> requestFromCache (final @Nonnull String url)
      throws IOException
      {
        log.debug("requestFromCache({})", url);
        return ResponseEntityIo.retrieve(cachePath.resolve(fixedPath(url)));
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResponseEntity<String> requestFromNetwork (final @Nonnull String url)
      throws IOException, InterruptedException
      {
        log.debug("requestFromNetwork({})", url);

        final long now = System.currentTimeMillis();
        final long delta = now - latestNetworkAccessTimestamp;
        final long toWait = Math.max(throttleLimit - delta, 0);

        if (toWait > 0)
          {
            log.info(">>>> throttle limit: waiting for {} msec...", toWait);
            Thread.sleep(toWait);
          }

        latestNetworkAccessTimestamp = now;
        final ResponseEntity<String> response = restTemplate.getForEntity(URI.create(url), String.class);
//        final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//        log.trace(">>>> response: {}", response);
        return response;
      }

    /*******************************************************************************************************************
     *
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private ResponseEntity<String> requestFromCacheAndThenNetwork (final @Nonnull String url)
      throws IOException, InterruptedException
      {
        log.debug("requestFromCacheAndThenNetwork({})", url);

        return requestFromCache(url).orElseGet(() ->
          {
            try
              {
                final ResponseEntity<String> response = requestFromNetwork(url);
                ResponseEntityIo.store(cachePath.resolve(fixedPath(url)), response, emptyList());
                return response;
              }
            catch (IOException | InterruptedException e)
              {
                throw new RestException(e); // FIXME
              }
          });
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    /* package */ static String fixedPath (final @Nonnull String url)
      {
        return url.replace("://", "/");
      }
  }