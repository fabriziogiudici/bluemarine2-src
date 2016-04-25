/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://tidalwave@bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2016 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.ui.impl.javafx;

import javax.annotation.Nonnull;
import java.io.File;
//import java.nio.file.Path;
import javafx.application.Platform;
//import it.tidalwave.accounting.util.DefaultPreferencesHandler;
//import it.tidalwave.accounting.util.PreferencesHandler;
import org.slf4j.bridge.SLF4JBridgeHandler;
import it.tidalwave.ui.javafx.JavaFXSpringApplication;

/***********************************************************************************************************************
 *
 * The main class initializes the logging facility and starts the JavaFX application.
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
public class Main extends JavaFXSpringApplication
  {
    public Main()
      {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
//        setMaximized(true);
        setFullScreen(true);
        setFullScreenLocked(true);
      }

    public static void main (final @Nonnull String ... args)
      {
        try
          {
            // FIXME
//            final PreferencesHandler preferenceHandler = new DefaultPreferencesHandler();
//            final Path logfolder = preferenceHandler.getLogFolder();
//            System.setProperty("it.tidalwave.bluemarine2.logFolder", logfolder.toFile().getAbsolutePath());
            System.setProperty("it.tidalwave.bluemarine2.logFolder",
                    new File(System.getProperty("user.home"), ".blueMarine2/logs").getAbsolutePath());

            Platform.setImplicitExit(true);
            launch(args);
          }
        catch (Throwable t)
          {
            // Don't use logging facilities here, they could be not initialized
            t.printStackTrace();
            System.exit(-1);
          }
      }
  }