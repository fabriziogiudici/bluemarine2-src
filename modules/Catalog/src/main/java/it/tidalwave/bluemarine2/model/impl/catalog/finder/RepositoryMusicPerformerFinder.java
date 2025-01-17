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
package it.tidalwave.bluemarine2.model.impl.catalog.finder;

import javax.annotation.Nonnull;
import java.util.Optional;
import org.eclipse.rdf4j.repository.Repository;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.model.audio.MusicPerformer;
import it.tidalwave.bluemarine2.model.finder.audio.MusicPerformerFinder;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ToString
public class RepositoryMusicPerformerFinder extends RepositoryFinderSupport<MusicPerformer, MusicPerformerFinder>
                                            implements MusicPerformerFinder
  {
    private static final long serialVersionUID = 4571966692541694627L;

    private static final String QUERY_ARTISTS = readSparql(RepositoryMusicPerformerFinder.class, "AllMusicArtists.sparql");

    @Nonnull
    private final Optional<Id> performanceId;

    /*******************************************************************************************************************
     *
     * Default constructor.
     *
     ******************************************************************************************************************/
    public RepositoryMusicPerformerFinder (@Nonnull final Repository repository)
      {
        this(repository, Optional.empty());
      }

    /*******************************************************************************************************************
     *
     * Clone constructor.
     *
     ******************************************************************************************************************/
    public RepositoryMusicPerformerFinder (@Nonnull final RepositoryMusicPerformerFinder other,
                                           @Nonnull final Object override)
      {
        super(other, override);
        final RepositoryMusicPerformerFinder source = getSource(RepositoryMusicPerformerFinder.class, other, override);
        this.performanceId = source.performanceId;
      }

    /*******************************************************************************************************************
     *
     * Override constructor.
     *
     ******************************************************************************************************************/
    private RepositoryMusicPerformerFinder (@Nonnull final Repository repository,
                                            @Nonnull final Optional<Id> performanceId)
      {
        super(repository, "artist");
        this.performanceId = performanceId;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public MusicPerformerFinder performerOf (@Nonnull final Id performanceId)
      {
        return clonedWith(new RepositoryMusicPerformerFinder(repository, Optional.of(performanceId)));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected QueryAndParameters prepareQuery()
      {
        return QueryAndParameters.withSparql(QUERY_ARTISTS)
                                 .withParameter("performance", performanceId.map(this::iriFor));
      }
  }
