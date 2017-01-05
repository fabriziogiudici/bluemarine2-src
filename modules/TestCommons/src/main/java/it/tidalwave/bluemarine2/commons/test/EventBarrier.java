/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - git clone https://bitbucket.org/tidalwave/bluemarine2-src.git
 * %%
 * Copyright (C) 2015 - 2017 Tidalwave s.a.s. (http://tidalwave.it)
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
package it.tidalwave.bluemarine2.commons.test;

import javax.annotation.Nonnull;
import java.util.concurrent.CountDownLatch;
import it.tidalwave.messagebus.MessageBus;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id: Class.java,v 631568052e17 2013/02/19 15:45:02 fabrizio $
 *
 **********************************************************************************************************************/
@Slf4j
public class EventBarrier<TOPIC> implements MessageBus.Listener<TOPIC>
  {
    private final CountDownLatch latch = new CountDownLatch(1);

    @Nonnull
    private final Class<TOPIC> eventType;

    public EventBarrier (final @Nonnull Class<TOPIC> topic, final @Nonnull MessageBus messageBus)
      {
        this.eventType = topic;
        messageBus.subscribe(topic, this);
      }

    @Override
    public void notify (final @Nonnull TOPIC event)
      {
        latch.countDown();
      }

    public void await()
      throws InterruptedException
      {
//        final Class<EVENT> eventType = ReflectionUtils.getTypeArguments(EventBarrier.class, getClass()).get(0);
        log.info("Waiting for {}... ", eventType.getName());
        latch.await();
        log.info("Got {}", eventType.getName());
      }
  }