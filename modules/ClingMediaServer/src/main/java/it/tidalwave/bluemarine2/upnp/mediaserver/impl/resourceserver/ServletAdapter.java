/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://bitbucket.org/tidalwave/bluemarine2-src.git
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
package it.tidalwave.bluemarine2.upnp.mediaserver.impl.resourceserver;

import java.io.IOException;
import java.util.Enumeration;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class ServletAdapter extends HttpServlet
  {
    private final AbstractHandler handler = new AbstractHandler()
      {
        @Override
        public void handle (final @Nonnull String target,
                            final @Nonnull Request baseRequest,
                            final @Nonnull HttpServletRequest request,
                            final @Nonnull HttpServletResponse response)
          throws IOException, ServletException
          {
            log.trace("handle({}, {}, {}, {}", target, baseRequest, request, response);
            log.debug(">>>> request URI: {}", request.getRequestURI());

            for (final Enumeration<String> names = request.getHeaderNames(); names.hasMoreElements(); )
              {
                final String headerName = names.nextElement();
                log.debug(">>>> request header: {} = {}", headerName, request.getHeader(headerName));
              }

            service(request, response);

            log.debug(">>>> response {}", response.getStatus());
            baseRequest.setHandled(true);

            for (final String headerName : response.getHeaderNames())
              {
                log.debug(">>>> response header: {} = {}", headerName, response.getHeaders(headerName));
              }
          }
      };

    @Nonnull
    public AbstractHandler asHandler()
      {
        return handler;
      }
  }
