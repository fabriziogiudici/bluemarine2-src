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
package it.tidalwave.bluemarine2.ui.audio.renderer.impl;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.application.Platform;
import it.tidalwave.role.ui.UserAction8;
import it.tidalwave.role.ui.spi.UserActionLambda;
import it.tidalwave.messagebus.annotation.ListensTo;
import it.tidalwave.messagebus.annotation.SimpleMessageSubscriber;
import it.tidalwave.bluemarine2.model.MediaItem;
import it.tidalwave.bluemarine2.model.MediaItem.Metadata;
import it.tidalwave.bluemarine2.ui.commons.RenderMediaFileRequest;
import it.tidalwave.bluemarine2.ui.commons.OnDeactivate;
import it.tidalwave.bluemarine2.ui.audio.renderer.MediaPlayer;
import it.tidalwave.bluemarine2.ui.audio.renderer.AudioRendererPresentation;
import it.tidalwave.bluemarine2.ui.audio.renderer.MediaPlayer.Status;
import lombok.extern.slf4j.Slf4j;
import static it.tidalwave.role.Displayable.Displayable;
import static it.tidalwave.bluemarine2.model.MediaItem.Metadata.*;
import static it.tidalwave.bluemarine2.ui.audio.renderer.MediaPlayer.Status.*;

/***********************************************************************************************************************
 *
 * The Control of the {@link AudioRendererPresentation}.
 * 
 * @stereotype  Control
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@SimpleMessageSubscriber @Slf4j
public class DefaultAudioRendererPresentationControl 
  {
    @Inject
    private AudioRendererPresentation presentation;
    
    @Inject
    private MediaPlayer mediaPlayer;
    
    private final AudioRendererPresentation.Properties properties = new AudioRendererPresentation.Properties();
    
    private Duration duration = Duration.ZERO;
    
    private final UserAction8 rewindAction = new UserActionLambda(() -> mediaPlayer.rewind());
    
    private final UserAction8 stopAction = new UserActionLambda(() -> mediaPlayer.stop());
    
    private final UserAction8 pauseAction = new UserActionLambda(() ->  mediaPlayer.pause());
    
    private final UserAction8 playAction = new UserActionLambda(() -> mediaPlayer.play());
    
    private final UserAction8 fastForwardAction = new UserActionLambda(() -> mediaPlayer.fastForward());
    
    // FIXME: use expression binding
    // e.g.  properties.progressProperty().bind(mediaPlayer.playTimeProperty().asDuration().dividedBy/duration));
    // FIXME: weak, remove previous listeners
    private final ChangeListener<Duration> l = 
                (ObservableValue<? extends Duration> observable, 
                 Duration oldValue,
                 Duration newValue) -> 
        {
          // FIXME: the control shouldn't mess with JavaFX stuff
          Platform.runLater(() ->
            {
              properties.playTimeProperty().setValue(format(newValue));
              properties.progressProperty().setValue((double)newValue.toMillis() / duration.toMillis());
            });
        };
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @PostConstruct
    /* VisibleForTesting */ void initialize()
      {
        presentation.bind(properties, rewindAction, stopAction, pauseAction, playAction, fastForwardAction);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    /* VisibleForTesting */ void onOpenAudioRendererRequest (final @ListensTo @Nonnull RenderMediaFileRequest request) 
      throws MediaPlayer.Exception
      {
        log.info("onOpenAudioRendererRequest({})", request);
        
        final MediaItem mediaItem = request.getMediaItem();
        final Metadata metadata = mediaItem.getMetadata();
        log.info(">>>> mediaItem: {}", mediaItem);
        log.info(">>>> metadata:  {}", metadata);

        // FIXME: the control shouldn't mess with JavaFX stuff
        Platform.runLater(() ->
          {
            properties.titleProperty().setValue(metadata.get(TITLE).orElse(""));
            properties.artistProperty().setValue(metadata.get(ARTIST).orElse(""));
            properties.composerProperty().setValue(metadata.get(COMPOSER).orElse(""));
            duration = metadata.get(DURATION).orElse(Duration.ZERO);
            properties.durationProperty().setValue(format(duration)); 
            // FIXME: check - parent should be always present - correct?
            properties.folderNameProperty().setValue(mediaItem.getParent().as(Displayable).getDisplayName());
          });
        
        mediaPlayer.setMediaItem(mediaItem);
        bindMediaPlayer();

        presentation.showUp(this);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @OnDeactivate
    /* VisibleForTesting */ OnDeactivate.Result onDeactivate()
      throws MediaPlayer.Exception 
      {
        mediaPlayer.stop();
        unbindMediaPlayer();
        return OnDeactivate.Result.PROCEED;
      }
    
    /*******************************************************************************************************************
     *
     * Binds to the {@link MediaPlayer}.
     *
     ******************************************************************************************************************/
    /* VisibleForTesting */ void bindMediaPlayer()
      {
        log.debug("bindMediaPlayer()");
        final ObjectProperty<Status> status = mediaPlayer.statusProperty();
        stopAction.enabledProperty().bind(status.isEqualTo(PLAYING));
        pauseAction.enabledProperty().bind(status.isEqualTo(PLAYING));
        playAction.enabledProperty().bind(status.isNotEqualTo(PLAYING));
        mediaPlayer.playTimeProperty().addListener(l);
      }
    
    /*******************************************************************************************************************
     *
     * Unbinds from the {@link MediaPlayer}.
     *
     ******************************************************************************************************************/
    /* VisibleForTesting */ void unbindMediaPlayer()
      {
        log.debug("unbindMediaPlayer()");
        stopAction.enabledProperty().unbind();
        pauseAction.enabledProperty().unbind();
        playAction.enabledProperty().unbind();
        mediaPlayer.playTimeProperty().removeListener(l);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Nonnull
    private static String format (final @Nonnull Duration duration)
      {
        final long s = duration.get(ChronoUnit.SECONDS);
        final long hours = s / 3600;
        final long minutes = (s / 60) % 60;
        final long seconds = s % 60;
        
        return (hours == 0) ? String.format("%02d:%02d", minutes, seconds)
                            : String.format("%02d:%02d:%02d", hours, minutes, seconds);
      }
  }