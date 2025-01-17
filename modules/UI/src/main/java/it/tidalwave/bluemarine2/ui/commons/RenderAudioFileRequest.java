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
package it.tidalwave.bluemarine2.ui.commons;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.List;
import it.tidalwave.bluemarine2.model.PlayList;
import it.tidalwave.bluemarine2.model.audio.AudioFile;
import lombok.ToString;
import static java.util.Collections.emptyList;

/***********************************************************************************************************************
 *
 * A message that requests to render an {@link AudioFile} in a {@link Playlist}.
 *
 * @stereotype  Message
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Immutable @ToString
public class RenderAudioFileRequest
  {
    @Nonnull
    private final AudioFile audioFile;

    @Nonnull
    private final List<AudioFile> list;

    public RenderAudioFileRequest (@Nonnull final AudioFile audioFile)
      {
        this(audioFile, emptyList());
      }

    public RenderAudioFileRequest (@Nonnull final AudioFile audioFile, @Nonnull final List<AudioFile> list)
      {
        this.audioFile = audioFile;
        this.list = List.copyOf(list);
      }

    @Nonnull
    public PlayList<AudioFile> getPlayList()
      {
        return new PlayList(audioFile, list);
      }
  }
