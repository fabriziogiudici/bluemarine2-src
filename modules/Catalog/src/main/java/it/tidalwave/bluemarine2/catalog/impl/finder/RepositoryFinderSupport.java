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
package it.tidalwave.bluemarine2.catalog.impl.finder;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.springframework.util.StreamUtils;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Configurable;
import it.tidalwave.util.Id;
import it.tidalwave.util.Finder8;
import it.tidalwave.util.Finder8Support;
import it.tidalwave.role.ContextManager;
import it.tidalwave.bluemarine2.model.Entity;
import it.tidalwave.bluemarine2.catalog.impl.RepositoryMusicArtist;
import it.tidalwave.bluemarine2.catalog.impl.RepositoryRecord;
import it.tidalwave.bluemarine2.catalog.impl.RepositoryTrack;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @stereotype      Finder
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@RequiredArgsConstructor @Configurable @Slf4j
public abstract class RepositoryFinderSupport<ENTITY, FINDER extends Finder8<ENTITY>>
              extends Finder8Support<ENTITY, FINDER> 
  {
    protected static final String PREFIXES =
                  "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>\n" 
                + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" 
                + "PREFIX rel:   <http://purl.org/vocab/relationship/>\n" 
                + "PREFIX bm:    <http://bluemarine.tidalwave.it/2015/04/mo/>\n" 
                + "PREFIX mo:    <http://purl.org/ontology/mo/>\n" 
                + "PREFIX vocab: <http://dbtune.org/musicbrainz/resource/vocab/>\n" 
                + "PREFIX xs:    <http://www.w3.org/2001/XMLSchema#>\n";

    @Nonnull
    private Repository repository;
    
    @Inject
    private ContextManager contextManager;
    
    // TODO: push this to generic FinderSupport
    @Nonnull
    private Optional<Object> context = Optional.empty();
    
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    @Override @Nonnull
    public RepositoryFinderSupport clone()
      {
        final RepositoryFinderSupport clone = (RepositoryFinderSupport)super.clone();
        clone.repository = this.repository;
        clone.context = this.context;

        return clone;
      }
    
    
    // FIXME: push to Finder
    /*******************************************************************************************************************
     *
     * {@inheritDoc}
     *
     ******************************************************************************************************************/
    /* @Override */ @Nonnull
    public FINDER withContext (final @Nonnull Object context)
      {
        final RepositoryFinderSupport clone = (RepositoryFinderSupport)clone();
        clone.context = Optional.of(context);
        return (FINDER)clone;
      }

    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    protected <E extends Entity> List<E> query (final @Nonnull Class<E> entityClass,
                                                final @Nonnull String originalSparql,
                                                final @Nonnull Object ... bindings)
      {
        try 
          {
            log.info("query({}, ...)", entityClass);
            
            final String sparql = PREFIXES + 
                                  Arrays.asList(originalSparql.split("\n")).stream()
                                    .filter(s -> matches(s, bindings))
                                    .map(s -> s.replaceAll("^@[A-Za-z0-9]*@", ""))
                                    .collect(Collectors.joining("\n"));
            
            final RepositoryConnection connection = repository.getConnection();
            final TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

            for (int i = 0; i < bindings.length; i += 2)
              {
                query.setBinding((String)bindings[i], (Value)bindings[i + 1]);
              }

            log(originalSparql, sparql);
            log.info(">>>> query parameters: {}", Arrays.toString(bindings));
            
            final TupleQueryResult result = query.evaluate();
            final List<E> entities = createEntities(repository, entityClass, result);
            result.close(); // FIXME: finally
            connection.close(); // FIXME: finally
            
            log.info(">>>> query returning {} entities", entities.size());

            return entities;
          }
        catch (RepositoryException | MalformedQueryException | QueryEvaluationException e)
          {
            throw new RuntimeException(e);
          }
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private static boolean matches (final @Nonnull String string, final @Nonnull Object[] parameters)
      {
        final Pattern p = Pattern.compile("^@([A-Za-z0-9]*)@.*");
        final Matcher matcher = p.matcher(string);
        
        if (!matcher.matches())
          {
            return true;  
          }
        
        final String tag = matcher.group(1);
        
        for (int i = 0; i < parameters.length; i+= 2)
          {
            if (tag.equals(parameters[i]))
              {
                return true;  
              }
          }
        
        return false;
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    protected Value uriFor (final @Nonnull Id id)
      {
        return ValueFactoryImpl.getInstance().createURI(id.stringValue());
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private <E extends Entity> List<E> createEntities (final @Nonnull Repository repository, 
                                                   final @Nonnull Class<E> entityClass,
                                                   final @Nonnull TupleQueryResult result) 
      throws QueryEvaluationException 
      {
        try
          {
            if (context.isPresent())
              {
                contextManager.addLocalContext(context.get());
              }

            final List<E> entities = new ArrayList<>();

            while (result.hasNext())
              {
                entities.add(createEntity(repository, entityClass, result.next()));
              }

            return entities;
          }
        finally
          {
            if (context.isPresent())
              {
                contextManager.removeLocalContext(context.get());
              }
          }
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    private static <E extends Entity> E createEntity (final @Nonnull Repository repository, 
                                                      final @Nonnull Class<E> entityClass,
                                                      final @Nonnull BindingSet bindingSet)
      {
        // FIXME
        if (entityClass.equals(RepositoryMusicArtist.class))
          {
            return (E)new RepositoryMusicArtist(repository, bindingSet);
          }
        
        if (entityClass.equals(RepositoryRecord.class))
          {
            return (E)new RepositoryRecord(repository, bindingSet);
          }
        
        if (entityClass.equals(RepositoryTrack.class))
          {
            return (E)new RepositoryTrack(repository, bindingSet);
          }
        
        throw new RuntimeException("Unknown entity: " + entityClass);
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    @Nonnull
    protected static String readSparql (final @Nonnull Class<?> clazz, final @Nonnull String name)
      {
        try 
          {
            @Cleanup InputStream is = clazz.getResourceAsStream(name);
            return StreamUtils.copyToString(is, Charset.forName("UTF-8"));
          } 
        catch (IOException e) 
          {
            throw new RuntimeException(e);
          }
      }
    
    /*******************************************************************************************************************
     *
     * 
     *
     ******************************************************************************************************************/
    private void log (final @Nonnull String originalSparql, final @Nonnull String sparql)
      {
        if (log.isDebugEnabled())
          {
            Arrays.asList(originalSparql.split("\n")).stream().forEach(s -> log.debug(">>>> original query: {}", s));
          }
        
        if (log.isInfoEnabled())
          {
            Arrays.asList(sparql.split("\n")).stream().forEach(s -> log.info(">>>> query: {}", s));
          }
     }
  }