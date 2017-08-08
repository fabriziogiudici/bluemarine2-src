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
@Table(name = "AgLibraryCollectionStack")
@NamedQueries({
    @NamedQuery(name = "AgLibraryCollectionStack.findAll", query = "SELECT a FROM AgLibraryCollectionStack a"),
    @NamedQuery(name = "AgLibraryCollectionStack.findByIdLocal", query = "SELECT a FROM AgLibraryCollectionStack a WHERE a.idLocal = :idLocal"),
    @NamedQuery(name = "AgLibraryCollectionStack.findByIdGlobal", query = "SELECT a FROM AgLibraryCollectionStack a WHERE a.idGlobal = :idGlobal"),
    @NamedQuery(name = "AgLibraryCollectionStack.findByCollapsed", query = "SELECT a FROM AgLibraryCollectionStack a WHERE a.collapsed = :collapsed"),
    @NamedQuery(name = "AgLibraryCollectionStack.findByCollection", query = "SELECT a FROM AgLibraryCollectionStack a WHERE a.collection = :collection"),
    @NamedQuery(name = "AgLibraryCollectionStack.findByText", query = "SELECT a FROM AgLibraryCollectionStack a WHERE a.text = :text")})
public class AgLibraryCollectionStack implements Serializable
  {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id_local")
    private Integer idLocal;
    @Basic(optional = false)
    @Column(name = "id_global")
    private String idGlobal;
    @Basic(optional = false)
    @Column(name = "collapsed")
    private int collapsed;
    @Basic(optional = false)
    @Column(name = "collection")
    private int collection;
    @Basic(optional = false)
    @Column(name = "text")
    private String text;

    public AgLibraryCollectionStack() {
    }

    public AgLibraryCollectionStack(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public AgLibraryCollectionStack(Integer idLocal, String idGlobal, int collapsed, int collection, String text) {
        this.idLocal = idLocal;
        this.idGlobal = idGlobal;
        this.collapsed = collapsed;
        this.collection = collection;
        this.text = text;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public String getIdGlobal() {
        return idGlobal;
    }

    public void setIdGlobal(String idGlobal) {
        this.idGlobal = idGlobal;
    }

    public int getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(int collapsed) {
        this.collapsed = collapsed;
    }

    public int getCollection() {
        return collection;
    }

    public void setCollection(int collection) {
        this.collection = collection;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        if (!(object instanceof AgLibraryCollectionStack)) {
            return false;
        }
        AgLibraryCollectionStack other = (AgLibraryCollectionStack) object;
        if ((this.idLocal == null && other.idLocal != null) || (this.idLocal != null && !this.idLocal.equals(other.idLocal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.tidalwave.bluemarine2.model.impl.adobe.lightroom.AgLibraryCollectionStack[ idLocal=" + idLocal + " ]";
    }
  }
