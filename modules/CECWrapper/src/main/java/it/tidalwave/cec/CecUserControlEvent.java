/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueMarine2 - Semantic Media Center
 * http://bluemarine2.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluemarine2-src
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
package it.tidalwave.cec;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import it.tidalwave.util.NotFoundException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/***********************************************************************************************************************
 *
 * Represents a CEC event about a user control.
 * 
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Immutable @Getter @EqualsAndHashCode(callSuper=true) @ToString
public class CecUserControlEvent extends CecEvent
  {
    /*******************************************************************************************************************
     *
     * Defines the key codes. 
     *
     ******************************************************************************************************************/
    @RequiredArgsConstructor @Getter
    public enum UserControlCode
      {
        SELECT(0x00),
        UP(0x01),
        DOWN(0x02),
        LEFT(0x03),
        RIGHT(0x04),
        EXIT(0x0d),
        PLAY(0x44),
        STOP(0x45),
        PAUSE(0x46),
        FAST_FORWARD(0x4b),
        REWIND(0x4c);
        
        private final int code;  
        
        @Nonnull
        public static UserControlCode forCode (final int code) 
          throws NotFoundException
          {
            for (final UserControlCode userControlCode : values())
              {
                if (userControlCode.getCode() == code)
                  {
                    return userControlCode;  
                  }
              }
            
            throw new NotFoundException("CEC user control code: " + Integer.toHexString(code));
          } 
      }
    
    @Nonnull
    private final UserControlCode userControlCode;
    
    public CecUserControlEvent (final @Nonnull EventType eventType, final @Nonnull UserControlCode userControlCode) 
      {
        super(eventType);
        this.userControlCode = userControlCode;
      }
  }
