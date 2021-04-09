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
package it.tidalwave.bluemarine2.ui.mainscreen.impl;

import javax.inject.Inject;
import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Collection;
import it.tidalwave.util.annotation.VisibleForTesting;
import org.springframework.beans.factory.ListableBeanFactory;
import it.tidalwave.role.ui.Displayable;
import it.tidalwave.role.ui.UserAction;
import it.tidalwave.messagebus.annotation.ListensTo;
import it.tidalwave.messagebus.annotation.SimpleMessageSubscriber;
import it.tidalwave.bluemarine2.message.PowerOnNotification;
import it.tidalwave.bluemarine2.ui.commons.flowcontroller.FlowController;
import it.tidalwave.bluemarine2.ui.mainscreen.MainScreenPresentation;
import lombok.extern.slf4j.Slf4j;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

/***********************************************************************************************************************
 *
 * The control of the {@link MainScreenPresentation}.
 *
 * @stereotype  Control
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@SimpleMessageSubscriber @Slf4j
public class DefaultMainScreenPresentationControl
  {
    @Inject
    private MainScreenPresentation presentation;

    @Inject
    private FlowController flowController;

    @Inject
    private ListableBeanFactory beanFactory;

    /*******************************************************************************************************************
     *
     * The action that powers off the application.
     *
     ******************************************************************************************************************/
    // FIXME: this fails because flowController is not injected yet.
//    private final UserAction powerOffAction = UserAction.of(flowController::powerOff,
//                                                            LocalizedDisplayable.fromBundle(getClass(), "powerOff"));
    private final UserAction powerOffAction = UserAction.of(() -> flowController.powerOff(),
                                                            Displayable.fromBundle(getClass(), "powerOff"));

    /*******************************************************************************************************************
     *
     * Populates the Presentation with the main menu.
     *
     ******************************************************************************************************************/
    @PostConstruct
    @VisibleForTesting void initialize()
      {
        final Collection<UserAction> mainMenuActions =  beanFactory.getBeansOfType(MainMenuItem.class)
                                                                   .values()
                                                                   .stream()
                                                                   .sorted(comparing(MainMenuItem::getPriority))
                                                                   .map(MainMenuItem::getAction)
                                                                   .collect(toList());
        presentation.bind(mainMenuActions, powerOffAction);
      }

    /*******************************************************************************************************************
     *
     *
     ******************************************************************************************************************/
    @VisibleForTesting void onPowerOnNotification (@Nonnull @ListensTo final PowerOnNotification notification)
      {
        presentation.showUp();
      }
  }
