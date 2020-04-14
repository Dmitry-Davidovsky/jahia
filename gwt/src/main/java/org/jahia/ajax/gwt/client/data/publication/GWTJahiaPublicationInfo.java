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
package org.jahia.ajax.gwt.client.data.publication;

import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.SerializableBaseModel;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * 
 */
public class GWTJahiaPublicationInfo extends SerializableBaseModel {
    private static final long serialVersionUID = -8549934950900000042L;

    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;
    public static final int DELETED = 11;
    public static final int MARKED_FOR_DELETION = 12;

    public static final Map<Integer,String> statusToLabel = new HashMap<Integer, String>();

    static {
        statusToLabel.put(GWTJahiaPublicationInfo.PUBLISHED,"published");
        statusToLabel.put(GWTJahiaPublicationInfo.MARKED_FOR_DELETION,"markedfordeletion");
        statusToLabel.put(GWTJahiaPublicationInfo.MODIFIED,"modified");
        statusToLabel.put(GWTJahiaPublicationInfo.NOT_PUBLISHED,"notpublished");
        statusToLabel.put(GWTJahiaPublicationInfo.UNPUBLISHED,"unpublished");
        statusToLabel.put(GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE,"mandatorylanguageunpublishable");
        statusToLabel.put(GWTJahiaPublicationInfo.LIVE_MODIFIED,"livemodified");
        statusToLabel.put(GWTJahiaPublicationInfo.LIVE_ONLY,"liveonly");
        statusToLabel.put(GWTJahiaPublicationInfo.CONFLICT,"conflict");
        statusToLabel.put(GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID,"mandatorylanguagevalid");
        statusToLabel.put(GWTJahiaPublicationInfo.DELETED,"deleted");
    }

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(String uuid, int status) {
        setUuid(uuid);
        setStatus(status);
        setLocked(false);
        setWorkInProgress(false);
        setIsAllowedToPublishWithoutWorkflow(false);
        setIsNonRootMarkedForDeletion(false);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String title) {
        set("title", title);
    }

    public String getNodetype() {
        return get("nodetype");
    }

    public void setNodetype(String nodetype) {
        set("nodetype", nodetype);
    }

    public String getUuid() {
        return get("uuid");
    }

    public void setUuid(String uuid) {
        set("uuid", uuid);
    }

    public String getI18nUuid() {
        return get("i18nUuid");
    }

    public void setI18NUuid(String uuid) {
        set("i18nUuid", uuid);
    }

    public String getDeletedI18nUuid() {
        return get("deletedI18nUuid");
    }

    public void setDeletedI18nUuid(String uuids) {
        set("deletedI18nUuid", uuids);
    }

    public Integer getStatus() {
        return get("status");
    }

    public void setStatus(Integer status) {
        set("status", status);
    }

    public Boolean isAllowedToPublishWithoutWorkflow() {
        return get("isAllowedToPublishWithoutWorkflow");
    }

    public void setIsAllowedToPublishWithoutWorkflow(Boolean canPublish) {
        set("isAllowedToPublishWithoutWorkflow", canPublish);
    }

    public Boolean getIsNonRootMarkedForDeletion() {
        return get("nonRootMarkedForDeletion");
    }

    public void setIsNonRootMarkedForDeletion(Boolean canPublish) {
        set("nonRootMarkedForDeletion", canPublish);
    }

    public Boolean isLocked() {
        return get("locked");
    }

    public void setLocked(Boolean locked) {
        set("locked", locked);
    }

    public Boolean isWorkInProgress() {
        return get("workInProgress");
    }

    public void setWorkInProgress(Boolean workInProgress) {
        set("workInProgress", workInProgress);
    }

    public String getMainPath() {
        return get("mainPath");
    }

    public void setMainPath(String mainTitle) {
        set("mainPath", mainTitle);
    }

    public Integer getMainPathIndex() {
        return get("mainPathIndex");
    }

    public void setMainPathIndex(Integer mainTitle) {
        set("mainPathIndex", mainTitle);
    }

    public String getWorkflowGroup() {
        return get("workflowGroup");
    }

    public void setWorkflowGroup(String workflowGroup) {
        set("workflowGroup", workflowGroup);
    }

    public String getWorkflowTitle() {
        return get("workflowTitle");
    }

    public void setWorkflowTitle(String workflowTitle) {
        set("workflowTitle", workflowTitle);
    }

    public String getWorkflowDefinition() {
        return get("workflowDefinition");
    }

    public void setWorkflowDefinition(String workflowDefinition) {
        set("workflowDefinition", workflowDefinition);
    }

    public String getLanguage() {
        return get("language");
    }

    public void setLanguage(String language) {
        set("language", language);
    }


    public static Image renderPublicationStatusImage(GWTJahiaPublicationInfo info) {
        return statusLabelToImage(info.isWorkInProgress() ? "workinprogress" : statusToLabel.get(info.getStatus()));
    }

    public static Image renderPublicationStatusImage(Integer status) {
        return statusLabelToImage(statusToLabel.get(status));
    }

    private static Image statusLabelToImage(String label) {
        String title = Messages.get("label.publication." + label, label);
        final Image image = ToolbarIconProvider.getInstance().getIcon("publication/" + label).createImage();
        image.setTitle(title);
        return image;
    }

    public boolean isPublishable() {
        return  !isLocked() &&
                getStatus() > GWTJahiaPublicationInfo.PUBLISHED &&
                getStatus() != GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE &&
                getStatus() != GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID &&
                !isWorkInProgress() && !getIsNonRootMarkedForDeletion();
    }

    public boolean isUnpublishable() {
        return  !isLocked() &&
                getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED && getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED && getStatus() != GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE;
    }


    public void setMainUUID(String uuid) {
        set("mainUUID", uuid);
    }

    public String getMainUUID() {
        return get("mainUUID");
    }

    public void setPath(String path) {
        set("path",path);
    }
    
    public String getPath() {
        return get("path");
    }
}
