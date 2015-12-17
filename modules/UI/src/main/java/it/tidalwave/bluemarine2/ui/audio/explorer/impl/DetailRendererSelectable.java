/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2015 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.ui.audio.explorer.impl;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.role.ui.Selectable;
import lombok.RequiredArgsConstructor;

/***********************************************************************************************************************
 *
 * Support class for roles that are capable to render details upon selection, in the context of
 * {@link DefaultAudioExplorerPresentationControl}.
 * 
 * @stereotype  Role
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Configurable @RequiredArgsConstructor
public abstract class DetailRendererSelectable<ENTITY> implements Selectable
  {
    @Nonnull
    protected final ENTITY owner;
    
    @Inject
    private AudioExplorerPresentationControlSpi control;

    @Override
    public void select() 
      {
        control.clearDetails();
        renderDetails();
      }
    
    protected void renderDetails (final @Nonnull String details) 
      {
        control.renderDetails(details);
      }
    
    protected void renderCoverArt (final @Nonnull Optional<URL> optionalImageUri) 
      {
        control.requestCoverArt(optionalImageUri);
      }
    
    protected abstract void renderDetails();
  }
