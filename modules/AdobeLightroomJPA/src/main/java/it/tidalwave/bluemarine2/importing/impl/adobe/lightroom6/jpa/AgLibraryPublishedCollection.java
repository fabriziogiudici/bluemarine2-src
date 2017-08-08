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
@Table(name = "AgLibraryPublishedCollection")
@NamedQueries({
    @NamedQuery(name = "AgLibraryPublishedCollection.findAll", query = "SELECT a FROM AgLibraryPublishedCollection a"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByIdLocal", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.idLocal = :idLocal"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByCreationId", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.creationId = :creationId"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByGenealogy", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.genealogy = :genealogy"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByImageCount", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.imageCount = :imageCount"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByIsDefaultCollection", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.isDefaultCollection = :isDefaultCollection"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByName", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.name = :name"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByParent", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.parent = :parent"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByPublishedUrl", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.publishedUrl = :publishedUrl"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findByRemoteCollectionId", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.remoteCollectionId = :remoteCollectionId"),
    @NamedQuery(name = "AgLibraryPublishedCollection.findBySystemOnly", query = "SELECT a FROM AgLibraryPublishedCollection a WHERE a.systemOnly = :systemOnly")})
public class AgLibraryPublishedCollection implements Serializable
  {

    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id_local")
    private Integer idLocal;
    @Basic(optional = false)
    @Column(name = "creationId")
    private String creationId;
    @Basic(optional = false)
    @Column(name = "genealogy")
    private String genealogy;
    @Column(name = "imageCount")
    private String imageCount;
    @Column(name = "isDefaultCollection")
    private String isDefaultCollection;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Column(name = "parent")
    private Integer parent;
    @Column(name = "publishedUrl")
    private String publishedUrl;
    @Column(name = "remoteCollectionId")
    private String remoteCollectionId;
    @Basic(optional = false)
    @Column(name = "systemOnly")
    private String systemOnly;

    public AgLibraryPublishedCollection() {
    }

    public AgLibraryPublishedCollection(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public AgLibraryPublishedCollection(Integer idLocal, String creationId, String genealogy, String name, String systemOnly) {
        this.idLocal = idLocal;
        this.creationId = creationId;
        this.genealogy = genealogy;
        this.name = name;
        this.systemOnly = systemOnly;
    }

    public Integer getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Integer idLocal) {
        this.idLocal = idLocal;
    }

    public String getCreationId() {
        return creationId;
    }

    public void setCreationId(String creationId) {
        this.creationId = creationId;
    }

    public String getGenealogy() {
        return genealogy;
    }

    public void setGenealogy(String genealogy) {
        this.genealogy = genealogy;
    }

    public String getImageCount() {
        return imageCount;
    }

    public void setImageCount(String imageCount) {
        this.imageCount = imageCount;
    }

    public String getIsDefaultCollection() {
        return isDefaultCollection;
    }

    public void setIsDefaultCollection(String isDefaultCollection) {
        this.isDefaultCollection = isDefaultCollection;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public String getPublishedUrl() {
        return publishedUrl;
    }

    public void setPublishedUrl(String publishedUrl) {
        this.publishedUrl = publishedUrl;
    }

    public String getRemoteCollectionId() {
        return remoteCollectionId;
    }

    public void setRemoteCollectionId(String remoteCollectionId) {
        this.remoteCollectionId = remoteCollectionId;
    }

    public String getSystemOnly() {
        return systemOnly;
    }

    public void setSystemOnly(String systemOnly) {
        this.systemOnly = systemOnly;
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
        if (!(object instanceof AgLibraryPublishedCollection)) {
            return false;
        }
        AgLibraryPublishedCollection other = (AgLibraryPublishedCollection) object;
        if ((this.idLocal == null && other.idLocal != null) || (this.idLocal != null && !this.idLocal.equals(other.idLocal))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "it.tidalwave.bluemarine2.model.impl.adobe.lightroom.AgLibraryPublishedCollection[ idLocal=" + idLocal + " ]";
    }
  }
