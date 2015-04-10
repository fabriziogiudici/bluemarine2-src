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
package it.tidalwave.bluemarine2.catalog.impl;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.catalog.Track;
import it.tidalwave.bluemarine2.catalog.TrackFinder;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class RepositoryTrackEntityFinder extends RepositoryFinderSupport<Track, TrackFinder>
                                         implements TrackFinder
  {
    @CheckForNull
    private Id makerId;

    @CheckForNull
    private Id recordId;

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    public RepositoryTrackEntityFinder (final @Nonnull Repository repository)  
      {
        super(repository);
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public TrackFinder withMaker (final @Nonnull Id makerId)  
      {
        final RepositoryTrackEntityFinder clone = clone();
        clone.makerId = makerId;
        return clone;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public TrackFinder inRecord (final @Nonnull Id recordId)  
      {
        final RepositoryTrackEntityFinder clone = clone();
        clone.recordId = recordId;
        return clone;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public RepositoryTrackEntityFinder clone()
      {
        final RepositoryTrackEntityFinder clone = (RepositoryTrackEntityFinder)super.clone();
        clone.makerId = this.makerId;
        clone.recordId = this.recordId;

        return clone;
      }
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    protected List<? extends Track> computeNeededResults() 
      {
        final String q =
              "SELECT *" 
            + "WHERE  {\n" 
            + "       ?track        a                       mo:Track.\n" 
            + "       ?track        rdfs:label              ?label.\n" 
            + "       ?track        mo:track_number         ?track_number.\n" 
                      
            + ((makerId == null)
                    ? ""
                    : "       {\n"
                    + "         ?track        foaf:maker              ?artist.\n"
                    + "       }\n"
                    + "         UNION\n" 
                    + "       {\n"
                    + "         ?track        foaf:maker              ?artistGroup.\n"
                    + "         ?artistGroup  rel:collaboratesWith    ?artist.\n"
                    + "       }\n")
                      
            + ((recordId == null)
                    ? ""
                    : "       {\n"
                    + "         ?record       mo:track                ?track.\n"
                    + "       }\n")
                      
            + "       ?signal       a                       mo:DigitalSignal.\n" 
            + "       ?signal       mo:published_as         ?track.\n"
            + "       ?signal       mo:duration             ?duration.\n" 
                      
            + "       ?audioFile    a                       mo:AudioFile.\n" 
            + "       ?audioFile    mo:encodes              ?signal.\n" 
            + "       ?audioFile    bm:path                 ?path.\n" 

            + "       ?record       a                       mo:Record.\n" 
            + "       ?record       mo:track                ?track.\n" 
            + "       ?record       rdfs:label              ?record_label.\n" 
            + "       }\n"
            + "ORDER BY ?record_label ?track_number ?label";
        
        final List<Object> parameters = new ArrayList<>();
        
        // FIXME: use Optional?
        if (makerId != null)
          {
            parameters.add("artist");
            parameters.add(ValueFactoryImpl.getInstance().createURI(makerId.stringValue()));
          }
        
        if (recordId != null)
          {
            parameters.add("record");
            parameters.add(ValueFactoryImpl.getInstance().createURI(recordId.stringValue()));
          }
        
        return query(RepositoryTrackEntity.class, q, parameters.toArray());
      }
  }
