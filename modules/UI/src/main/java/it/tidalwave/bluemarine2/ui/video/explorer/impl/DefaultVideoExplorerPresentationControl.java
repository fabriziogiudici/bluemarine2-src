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
package it.tidalwave.bluemarine2.ui.video.explorer.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import it.tidalwave.messagebus.annotation.ListensTo;
import it.tidalwave.messagebus.annotation.SimpleMessageSubscriber;
import it.tidalwave.bluemarine2.ui.commons.OpenVideoExplorerRequest;
import it.tidalwave.bluemarine2.ui.video.explorer.VideoExplorerPresentation;
import it.tidalwave.util.annotation.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * The Control of the {@link VideoExplorerPresentation}.
 * 
 * @stereotype  Control
 * 
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@SimpleMessageSubscriber @Slf4j
public class DefaultVideoExplorerPresentationControl 
  {
    @Inject
    private VideoExplorerPresentation presentation;

    @VisibleForTesting void onOpenVideoExplorerRequest (@ListensTo @Nonnull final OpenVideoExplorerRequest request)
      {
        log.info("onOpenVideoExplorerRequest({})", request);
        presentation.showUp();
      }
  }
