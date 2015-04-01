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
package it.tidalwave.bluemarine2.ui.audio.explorer;

import javax.annotation.Nonnull;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import it.tidalwave.role.ui.PresentationModel;
import it.tidalwave.role.ui.UserAction;
import lombok.Getter;
import lombok.experimental.Accessors;

/***********************************************************************************************************************
 *
 * The Presentation of the explorer of audio media files.
 * 
 * @stereotype  Presentation
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public interface AudioExplorerPresentation 
  {
    @Getter @Accessors(fluent = true)
    public static class Properties
      {
        private final StringProperty folderNameProperty = new SimpleStringProperty("");
        
        private final IntegerProperty selectedIndexProperty = new SimpleIntegerProperty(0);
      }
    
    /*******************************************************************************************************************
     *
     * Binds the UI with the callbacks.
     * 
     * @param   upAction    the action to go to the upper folder
     *
     ******************************************************************************************************************/
    public void bind (@Nonnull Properties properties, @Nonnull UserAction upAction);
    
    /*******************************************************************************************************************
     *
     * Shows this presentation on the screen.
     *
     ******************************************************************************************************************/
    public void showUp (@Nonnull Object control);
    
    /*******************************************************************************************************************
     *
     * Populates the presentation with a set of media files and select one.
     * 
     * @param   pm              the {@link PresentationModel}
     * @param   selectedIndex   the index of the item to select
     *
     ******************************************************************************************************************/
    public void populateAndSelect (@Nonnull PresentationModel pm, int selectedIndex);
    
    /*******************************************************************************************************************
     *
     * Puts the focus on the list to select media items.
     *
     ******************************************************************************************************************/
    public void focusOnMediaItems();
  }