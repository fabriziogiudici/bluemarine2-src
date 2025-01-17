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
package it.tidalwave.bluemarine2.rest.impl.resource;

import javax.annotation.Nonnull;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import it.tidalwave.util.Id;
import it.tidalwave.bluemarine2.model.audio.Record;
import lombok.Getter;
import static it.tidalwave.role.ui.Displayable._Displayable_;

/***********************************************************************************************************************
 *
 * An adapter for exporting {@link Record} in REST.
 *
 * @stereotype  Adapter
 *
 * @author  Fabrizio Giudici
 *
 **********************************************************************************************************************/
@Getter
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonPropertyOrder(alphabetic = true)
public class RecordResource extends ResourceSupport
  {
    private final String id;

    private final String displayName;

    private final Optional<Integer> diskCount;

    private final Optional<Integer> diskNumber;

    private final Optional<Integer> trackCount;

    private final Optional<String> source;

    private final Optional<String> asin;

    private final Optional<String> gtin;

    protected String details;

    public RecordResource (@Nonnull final Record record)
      {
        this.id          = record.getId().stringValue();
        this.displayName = record.as(_Displayable_).getDisplayName();
        this.diskCount   = record.getDiskCount();
        this.diskNumber  = record.getDiskNumber();
        this.trackCount  = record.getTrackCount();
        this.source      = record.getSource().map(Id::toString);
        this.asin        = record.getAsin();
        this.gtin        = record.getGtin();
        this.details     = resourceUri("record", record);
      }
  }
