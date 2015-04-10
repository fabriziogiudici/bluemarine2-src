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
package it.tidalwave.bluemarine2.catalog;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import it.tidalwave.bluemarine2.catalog.impl.RepositoryCatalog;
import it.tidalwave.bluemarine2.catalog.impl.RepositoryTrackEntity;
import it.tidalwave.messagebus.MessageBus;
import it.tidalwave.util.Id;
import it.tidalwave.util.Key;
import it.tidalwave.util.PowerOnNotification;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.testng.annotations.Test;
import lombok.extern.slf4j.Slf4j;
import static java.nio.file.Files.*;
import static it.tidalwave.util.test.FileComparisonUtils.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.DataProvider;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j
public class RepositoryCatalogTest 
  {
    private static final Path MODELS = Paths.get("src/test/resources");
            
    private static final Path TEST_RESULTS = Paths.get("target/test-results");
            
    private static final Path EXPECTED_TEST_RESULTS = Paths.get("src/test/resources/expected-results");
            
    private Repository repository;
    
    private ApplicationContext context;
    
    private MessageBus messageBus;
    
    @Test(dataProvider = "p")
    public void dumpCatalog (final @Nonnull String catalogName, final @Nonnull String dumpName) 
      throws RepositoryException, IOException, RDFParseException, MalformedQueryException, QueryEvaluationException
      {
        final String s1 = "classpath:/META-INF/CommonsAutoBeans.xml";
        final String s2 = "classpath:/META-INF/RepositoryCatalogTestBeans.xml";
        context = new ClassPathXmlApplicationContext(s1, s2);
        messageBus = context.getBean(MessageBus.class);
        
        final Map<Key<?>, Object> properties = new HashMap<>();
        properties.put(it.tidalwave.bluemarine2.model.PropertyNames.ROOT_PATH, Paths.get("/base/path"));
        final PowerOnNotification notification = new PowerOnNotification(properties);
        messageBus.publish(notification);
        
        repository = new SailRepository(new MemoryStore());
        repository.initialize();
        final File file = MODELS.resolve(catalogName).toFile();
        final RepositoryConnection connection = repository.getConnection();
        
        connection.add(file, null, RDFFormat.N3);
        connection.commit();
        connection.close();
        
    // https://bitbucket.org/openrdf/alibaba/src/master/object-repository/
//        ObjectRepositoryFactory factory = new ObjectRepositoryFactory();
//        ObjectRepository oRepository = factory.createRepository(repository);        
               
        final Catalog catalog = new RepositoryCatalog(repository);
        
        final Path actualResult = TEST_RESULTS.resolve(dumpName);
        final Path expectedResult = EXPECTED_TEST_RESULTS.resolve(dumpName);
        createDirectories(TEST_RESULTS);
        final PrintWriter pw = new PrintWriter(actualResult.toFile());
        
        pw.println("\n\nALL TRACKS:\n");
        final Map<String, RepositoryTrackEntity> allTracks = catalog.findTracks().results().stream()
                        .map(t -> (RepositoryTrackEntity)t)
                        .collect(Collectors.toMap(RepositoryTrackEntity::toString, Function.identity()));
        final Comparator<RepositoryTrackEntity> c = (o1, o2) -> o1.getRdfsLabel().compareTo(o2.getRdfsLabel());
        allTracks.values().stream().sorted(c).forEach(track -> pw.printf("  %s\n", track));
        
        pw.println("\n\n\nRECORDS:\n");
        catalog.findRecords().stream().forEach(artist -> pw.printf("%s\n", artist));
        
        pw.println("\n\n\nARTISTS:\n");
        final List<? extends MusicArtist> artists = catalog.findArtists().results();
        artists.forEach(artist -> pw.printf("%s\n", artist));
        
        // FIXME: not correctly sorted in many cases
        
        artists.forEach(artist -> 
          {
            pw.printf("\nTRACKS OF %s:\n", artist);
            artist.findTracks().stream().forEach(track -> 
              {
                pw.printf("  %s\n", track);
                allTracks.remove(track.toString());
              });
          });
        
        pw.println("\n\nORPHANED TRACKS:\n");
        allTracks.values().stream().sorted(c).forEach(track -> pw.printf("  %s\n", track));

        pw.close();
        
        assertSameContents(expectedResult.toFile(), actualResult.toFile());
      }
    
    @DataProvider(name = "p")
    private static Object[][] p()
      {
        return new Object[][]
          {
              { "tiny-model.n3",  "tiny-dump.txt"  },
              { "small-model.n3", "small-dump.txt" },
              { "large-model.n3", "large-dump.txt" }
          };
      }
  }
