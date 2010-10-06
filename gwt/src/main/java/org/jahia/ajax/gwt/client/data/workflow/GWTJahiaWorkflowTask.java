/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:07:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaWorkflowTask extends BaseModelData implements Serializable {
    private List<GWTJahiaWorkflowOutcome> outcomes;
    private List<GWTJahiaWorkflowComment> comments;

    public GWTJahiaWorkflowTask() {
    }

    public String getId() {
        return get("id");
    }

    public void setId(String id) {
        set("id",id);
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name",name);
    }

    public String getProvider() {
        return get("provider");
    }

    public void setProvider(String provider) {
        set("provider",provider);
    }
    
    public List<GWTJahiaWorkflowOutcome> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<GWTJahiaWorkflowOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public void setTaskComments(List<GWTJahiaWorkflowComment> comments) {
        this.comments = comments;
    }

    public List<GWTJahiaWorkflowComment> getTaskComments() {
        return comments;
    }

    public void setFormResourceName(String formResourceName) {
        set("formResourceName",formResourceName);
    }

    public String getFormResourceName() {
        return get("formResourceName");
    }

    public void setCreateTime(Date createTime) {
        set("createTime",createTime);
    }
    
    public Date getCreateTime() {
        return get("createTime");
    }

    public void setProcessId(String processId) {
        set("processId",processId);
    }

    public String getProcessId() {
        return get("processId");
    }

    public void setDisplayName(String displayName) {
        set("displayName",displayName);
    }

    public String getDisplayName(){
        return get("displayName");
    }
}
