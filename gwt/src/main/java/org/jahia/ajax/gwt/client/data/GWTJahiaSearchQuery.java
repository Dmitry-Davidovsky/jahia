/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * GWT bean for search criteria.
 * User: ktlili
 * Date: Feb 16, 2010
 * Time: 4:19:05 PM
 */
public class GWTJahiaSearchQuery implements Serializable {
    private String query;
    private List<GWTJahiaNode> pages;
    private GWTJahiaLanguage language;
    private boolean inName;
    private boolean inTags;
    private boolean inContents;
    private boolean inFiles;
    private boolean inMetadatas;
    private List<String> folderTypes;
    private List<String> nodeTypes;
    private List<String> filters;
    private List<String> mimeTypes;
    private List<String> sites;
    private String originSiteUuid;
    private String basePath;
    private Date startPublicationDate;
    private Date endPublicationDate;
    private Date startCreationDate;
    private Date endCreationDate;
    private Date startEditionDate;
    private Date endEditionDate;
    private String timeInDays;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<GWTJahiaNode> getPages() {
        return pages;
    }

    public void setPages(List<GWTJahiaNode> pages) {
        this.pages = pages;
    }

    public GWTJahiaLanguage getLanguage() {
        return language;
    }

    public void setLanguage(GWTJahiaLanguage language) {
        this.language = language;
    }

    public boolean isInName() {
        return inName;
    }

    public void setInName(boolean inName) {
        this.inName = inName;
    }

    public boolean isInTags() {
        return inTags;
    }

    public void setInTags(boolean inTags) {
        this.inTags = inTags;
    }

    public boolean isInContents() {
        return inContents;
    }

    public void setInContents(boolean inContents) {
        this.inContents = inContents;
    }

    public boolean isInFiles() {
        return inFiles;
    }

    public void setInFiles(boolean inFiles) {
        this.inFiles = inFiles;
    }

    public boolean isInMetadatas() {
        return inMetadatas;
    }

    public void setInMetadatas(boolean inMetadatas) {
        this.inMetadatas = inMetadatas;
    }

    public List<String> getFolderTypes() {
        return folderTypes;
    }

    public void setFolderTypes(List<String> folderTypes) {
        this.folderTypes = folderTypes;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(List<String> mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public List<String> getSites() {
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = sites;
    }

    /**
     * @return the originSiteUuid
     */
    public String getOriginSiteUuid() {
        return originSiteUuid;
    }

    /**
     * @param originSiteUuid the originSiteUuid to set
     */
    public void setOriginSiteUuid(String originSiteUuid) {
        this.originSiteUuid = originSiteUuid;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public Date getStartPublishedDate() {
        return startPublicationDate;
    }

    public void setStartPublicationDate(Date startPublicationDate) {
        this.startPublicationDate = startPublicationDate;
    }

    public Date getEndPublishedDate() {
        return endPublicationDate;
    }

    public void setEndPublicationDate(Date endPublicationDate) {
        this.endPublicationDate = endPublicationDate;
    }

    public Date getStartCreatedDate() {
        return startCreationDate;
    }

    public void setStartCreationDate(Date startCreationDate) {
        this.startCreationDate = startCreationDate;
    }

    public Date getEndCreatedDate() {
        return endCreationDate;
    }

    public void setEndCreationDate(Date endCreationDate) {
        this.endCreationDate = endCreationDate;
    }

    public Date getStartLastModifiedDate() {
        return startEditionDate;
    }

    public void setStartEditionDate(Date startEditionDate) {
        this.startEditionDate = startEditionDate;
    }

    public Date getEndLastModifiedDate() {
        return endEditionDate;
    }

    public void setEndEditionDate(Date endEditionDate) {
        this.endEditionDate = endEditionDate;
    }

    public String getTimeInDays() {
        return timeInDays;
    }

    public void setTimeInDays(String timeInDays) {
        this.timeInDays = timeInDays;
    }
}
