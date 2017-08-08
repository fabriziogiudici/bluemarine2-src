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
@Table(name = "AgLibraryCollectionStackImage")
@NamedQueries({
    @NamedQuery(name = "AgLibraryCollectionStackImage.findAll", query = "SELECT a FROM AgLibraryCollectionStackImage a"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByIdLocal", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.idLocal = :idLocal"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByCollapsed", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.collapsed = :collapsed"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByCollection", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.collection = :collection"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByImage", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.image = :image"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByPosition", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.position = :position"),
    @NamedQuery(name = "AgLibraryCollectionStackImage.findByStack", query = "SELECT a FROM AgLibraryCollectionStackImage a WHERE a.stack = :stack")})
public class AgLibraryCollectionStackImage implements Serializable
  {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id_local")
    private Integer idLocal;
    @Basic(optional = false)
    @Column(name = "collapsed")
    private int collapsed;
    @Basic(optional = false)
    @Column(name = "collection")
    private int collection;
    @Basic(optional = false)
    @Column(name = "image")
    private int image;
    @Basic(optional = false)
    @Column(name = "position")
    private String position;
    @Basic(optional = false)
    @Column(name = "stack")
    private int stack;

    public AgLibraryCollectionStackImage() {
    }

    public AgLibraryCollectionStackImage(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public AgLibraryCollectionStackImage(Integer idLocal, int collapsed, int collection, int image, String position, int stack) {
        this.idLocal = idLocal;
        this.collapsed = collapsed;
        this.collection = collection;
        this.image = image;
        this.position = position;
        this.stack = stack;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getStack() {
        return stack;
    }

    public void setStack(int stack) {
        this.stack = stack;
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
        if (!(object instanceof AgLibraryCollectionStackImage)) {
            return false;
        }
        AgLibraryCollectionStackImage other = (AgLibraryCollectionStackImage) object;
        if ((this.idLocal == null && other.idLocal != null) || (this.idLocal != null && !this.idLocal.equals(other.idLocal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.tidalwave.bluemarine2.model.impl.adobe.lightroom.AgLibraryCollectionStackImage[ idLocal=" + idLocal + " ]";
    }
  }
