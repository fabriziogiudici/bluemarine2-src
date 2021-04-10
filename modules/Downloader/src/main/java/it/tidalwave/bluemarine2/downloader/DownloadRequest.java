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
package it.tidalwave.bluemarine2.downloader;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.net.URL;
import lombok.Getter;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Immutable @Getter @ToString
public class DownloadRequest 
  {
    public enum Option
      {
        NO_OPTION,
        FOLLOW_REDIRECT
      }
    
    @Nonnull
    private final URL url;
    
    @Nonnull
    private final Set<Option> options;

    public DownloadRequest (@Nonnull final URL url, @Nonnull final Option ... options)
      {
        this.url = url;
        this.options = new HashSet<>(List.of(options));
      }
    
    public boolean isOptionPresent (@Nonnull final Option option)
      {
        return options.contains(option);
      }
  }
