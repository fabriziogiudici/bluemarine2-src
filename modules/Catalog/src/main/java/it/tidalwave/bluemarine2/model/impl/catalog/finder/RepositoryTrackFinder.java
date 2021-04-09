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
import it.tidalwave.bluemarine2.model.audio.Track;
import it.tidalwave.bluemarine2.model.finder.audio.TrackFinder;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@ToString
public class RepositoryTrackFinder extends RepositoryFinderSupport<Track, TrackFinder>
                                   implements TrackFinder
  {
    private static final long serialVersionUID = 770942161753738572L;

    private static final String QUERY_TRACKS = readSparql(RepositoryTrackFinder.class, "Tracks.sparql");

    @Nonnull
    protected final Optional<Id> makerId;

    @Nonnull
    protected final Optional<Id> recordId;

    /*******************************************************************************************************************
     *
     * Default constructor.
     *
     ******************************************************************************************************************/
    public RepositoryTrackFinder (@Nonnull final Repository repository)
      {
        this(repository, Optional.empty(), Optional.empty());
      }

    /*******************************************************************************************************************
     *
     * Clone constructor.
     *
     ******************************************************************************************************************/
    public RepositoryTrackFinder (@Nonnull final RepositoryTrackFinder other, @Nonnull final Object override)
      {
        super(other, override);
        final RepositoryTrackFinder source = getSource(RepositoryTrackFinder.class, other, override);
        this.makerId = source.makerId;
        this.recordId = source.recordId;
      }

    /*******************************************************************************************************************
     *
     * Override constructor.
     *
     ******************************************************************************************************************/
    private RepositoryTrackFinder (@Nonnull final Repository repository,
                                   @Nonnull final Optional<Id> makerId,
                                   @Nonnull final Optional<Id> recordId)
      {
        super(repository, "track");
        this.makerId = makerId;
        this.recordId = recordId;
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public TrackFinder madeBy (@Nonnull final Id artistId)
      {
        return clonedWith(new RepositoryTrackFinder(repository, Optional.of(artistId), recordId));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public TrackFinder inRecord (@Nonnull final Id recordId)
      {
        return clonedWith(new RepositoryTrackFinder(repository, makerId, Optional.of(recordId)));
      }

    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected QueryAndParameters prepareQuery()
      {
        return QueryAndParameters.withSparql(QUERY_TRACKS)
                                 .withParameter("artist", makerId.map(this::iriFor))
                                 .withParameter("record", recordId.map(this::iriFor));
      }
  }
