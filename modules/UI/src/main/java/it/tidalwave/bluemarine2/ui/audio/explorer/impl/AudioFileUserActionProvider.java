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
package it.tidalwave.bluemarine2.ui.audio.explorer.impl;

import javax.annotation.Nonnull;
import java.util.List;
import it.tidalwave.role.ui.UserAction;
import it.tidalwave.role.ui.spi.DefaultUserActionProvider;
import it.tidalwave.dci.annotation.DciRole;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.bluemarine2.model.audio.AudioFile;
import it.tidalwave.bluemarine2.model.role.AudioFileSupplier;
import it.tidalwave.bluemarine2.ui.commons.RenderAudioFileRequest;
import lombok.RequiredArgsConstructor;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * A role for {@link AudioFileSupplier} that provides a default action that fires a request to render the audio file.
 *
 * @stereotype  Role
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@DciRole(datumType = AudioFileSupplier.class, context = DefaultAudioExplorerPresentationControl.class)
@RequiredArgsConstructor
public class AudioFileUserActionProvider extends DefaultUserActionProvider
  {
    @Nonnull
    private final AudioFileSupplier audioFileSupplier;

    @Nonnull // FIXME: use the Spi interface, but this needs a change in RoleManagerSupport
    private final DefaultAudioExplorerPresentationControl control;

    @Nonnull
    private final MessageBus messageBus;

    @Override @Nonnull
    public UserAction getDefaultAction()
      {
        return UserAction.of(() ->
          {
            final List<AudioFile> audioFiles = control.getMediaItems().stream()
                    .filter(i -> i instanceof AudioFileSupplier)
                    .map(i -> ((AudioFileSupplier)i).getAudioFile())
                    .collect(toList());

            messageBus.publish(new RenderAudioFileRequest(audioFileSupplier.getAudioFile(), audioFiles));
          });
      }
  }
