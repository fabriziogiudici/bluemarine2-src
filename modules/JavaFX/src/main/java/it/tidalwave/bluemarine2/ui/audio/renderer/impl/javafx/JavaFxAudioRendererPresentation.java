/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - lightweight MediaCenter
 * http://bluemarine.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluemarine2-src
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
package it.tidalwave.bluemarine2.ui.audio.renderer.impl.javafx;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import it.tidalwave.role.ui.UserAction;
import it.tidalwave.ui.javafx.JavaFXSafeProxyCreator.NodeAndDelegate;
import it.tidalwave.bluemarine2.model.MediaItem;
import it.tidalwave.bluemarine2.ui.commons.flowcontroller.FlowController;
import it.tidalwave.bluemarine2.ui.audio.renderer.AudioRendererPresentation;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.ui.javafx.JavaFXSafeProxyCreator.createNodeAndDelegate;

/***********************************************************************************************************************
 *
 * The JavaFX implementation of {@link AudioRendererPresentation}.
 * 
 * @stereotype  Presentation
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class JavaFxAudioRendererPresentation implements AudioRendererPresentation
  {
    private static final String FXML_URL = "/it/tidalwave/bluemarine2/ui/impl/javafx/AudioRenderer.fxml";
    
    @Inject
    private FlowController flowController;
    
    private final NodeAndDelegate nad = createNodeAndDelegate(getClass(), FXML_URL);
    
    private final AudioRendererPresentation delegate = nad.getDelegate();
            
    // FIXME: use @Delegate
    
    @Override
    public void bind (final @Nonnull UserAction rewindAction,
                      final @Nonnull UserAction stopAction,
                      final @Nonnull UserAction playAction,
                      final @Nonnull UserAction fastForwardAction,
                      final @Nonnull Properties properties)
      {
        delegate.bind(rewindAction, stopAction, playAction, fastForwardAction, properties);
      }
    
    @Override
    public void showUp()  
      {
        delegate.showUp();
        flowController.showPresentation(nad.getNode());
      }

    @Override
    public void setMediaItem (final @Nonnull MediaItem mediaItem) 
      {
        delegate.setMediaItem(mediaItem);
      }
  }
