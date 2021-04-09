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
package it.tidalwave.bluemarine2.util;

import org.slf4j.bridge.SLF4JBridgeHandler;
import it.tidalwave.util.PreferencesHandler;
import lombok.NoArgsConstructor;
import static lombok.AccessLevel.PRIVATE;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@NoArgsConstructor(access = PRIVATE)
public final class SystemConfigurer
  {
    public static void setupSlf4jBridgeHandler()
      {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
      }

    public static void setSystemProperties()
      {
        final PreferencesHandler preferencesHandler = PreferencesHandler.getInstance();
        final String workspace = preferencesHandler.getAppFolder().toString();
        System.setProperty("blueMarine2.workspace", workspace);
        System.setProperty("blueMarine2.logFolder", preferencesHandler.getLogFolder().toString());
        System.setProperty("blueMarine2.logConfigOverride", workspace + "/config/logback-override.xml");
      }
  }
