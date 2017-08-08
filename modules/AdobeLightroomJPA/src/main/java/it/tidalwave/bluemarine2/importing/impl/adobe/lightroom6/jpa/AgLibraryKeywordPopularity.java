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
package it.tidalwave.bluemarine2.importing.impl.adobe.lightroom6.jpa;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici (Fabrizio.Giudici@tidalwave.it)
 * @version $Id:$
 *
 **********************************************************************************************************************/
@Entity
@Table(name = "AgLibraryKeywordPopularity")
@NamedQueries({
    @NamedQuery(name = "AgLibraryKeywordPopularity.findAll", query = "SELECT a FROM AgLibraryKeywordPopularity a"),
    @NamedQuery(name = "AgLibraryKeywordPopularity.findByIdLocal", query = "SELECT a FROM AgLibraryKeywordPopularity a WHERE a.idLocal = :idLocal"),
    @NamedQuery(name = "AgLibraryKeywordPopularity.findByOccurrences", query = "SELECT a FROM AgLibraryKeywordPopularity a WHERE a.occurrences = :occurrences"),
    @NamedQuery(name = "AgLibraryKeywordPopularity.findByPopularity", query = "SELECT a FROM AgLibraryKeywordPopularity a WHERE a.popularity = :popularity"),
    @NamedQuery(name = "AgLibraryKeywordPopularity.findByTag", query = "SELECT a FROM AgLibraryKeywordPopularity a WHERE a.tag = :tag")})
public class AgLibraryKeywordPopularity implements Serializable
  {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id_local")
    private Integer idLocal;
    @Basic(optional = false)
    @Column(name = "occurrences")
    private String occurrences;
    @Basic(optional = false)
    @Column(name = "popularity")
    private String popularity;
    @Basic(optional = false)
    @Column(name = "tag")
    private String tag;

    public AgLibraryKeywordPopularity() {
    }

    public AgLibraryKeywordPopularity(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public AgLibraryKeywordPopularity(Integer idLocal, String occurrences, String popularity, String tag) {
        this.idLocal = idLocal;
        this.occurrences = occurrences;
        this.popularity = popularity;
        this.tag = tag;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public String getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(String occurrences) {
        this.occurrences = occurrences;
    }

    public String getPopularity() {
        return popularity;
    }

    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idLocal != null ? idLocal.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AgLibraryKeywordPopularity)) {
            return false;
        }
        AgLibraryKeywordPopularity other = (AgLibraryKeywordPopularity) object;
        if ((this.idLocal == null && other.idLocal != null) || (this.idLocal != null && !this.idLocal.equals(other.idLocal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.tidalwave.bluemarine2.model.impl.adobe.lightroom.AgLibraryKeywordPopularity[ idLocal=" + idLocal + " ]";
    }
  }
