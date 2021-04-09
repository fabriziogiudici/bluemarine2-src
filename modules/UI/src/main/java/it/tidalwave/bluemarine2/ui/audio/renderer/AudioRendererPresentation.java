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
package it.tidalwave.bluemarine2.ui.audio.renderer;

import javax.annotation.Nonnull;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import it.tidalwave.role.ui.UserAction;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/***********************************************************************************************************************
 *
 * The Presentation of the renderer of audio media files.
 * 
 * @stereotype  Presentation
 * 
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
public interface AudioRendererPresentation 
  {
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @Getter @Accessors(fluent = true) @ToString
    public static class Properties
      {
        private final Property<String> titleProperty = new SimpleStringProperty("");
        private final Property<String> folderNameProperty = new SimpleStringProperty("");
        private final Property<String> artistProperty = new SimpleStringProperty("");
        private final Property<String> composerProperty = new SimpleStringProperty("");
        private final Property<String> durationProperty = new SimpleStringProperty("");
        private final Property<String> playTimeProperty = new SimpleStringProperty("");
        private final Property<String> nextTrackProperty = new SimpleStringProperty("");
        private final DoubleProperty progressProperty = new SimpleDoubleProperty(0);
      }
    
    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    public void bind (@Nonnull Properties properties,
                      @Nonnull UserAction prevAction,
                      @Nonnull UserAction rewindAction,
                      @Nonnull UserAction stopAction,
                      @Nonnull UserAction pauseAction,
                      @Nonnull UserAction playAction,
                      @Nonnull UserAction fastForwardAction,
                      @Nonnull UserAction nextAction);
    
    /*******************************************************************************************************************
     *
     * Shows this presentation on the screen.
     *
     ******************************************************************************************************************/
    public void showUp (@Nonnull Object control);
    
    /*******************************************************************************************************************
     *
     * Put focus on the play button.
     *
     ******************************************************************************************************************/
    public void focusOnPlayButton();

    /*******************************************************************************************************************
     *
     * Put focus on the stop button.
     *
     ******************************************************************************************************************/
    public void focusOnStopButton();
  }
