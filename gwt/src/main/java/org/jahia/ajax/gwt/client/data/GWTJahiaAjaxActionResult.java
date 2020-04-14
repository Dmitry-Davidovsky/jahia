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

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResultError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: hollis
 * Date: 20 ao�t 2008
 * Time: 12:26:53
 * 
 */
public class GWTJahiaAjaxActionResult implements Serializable {

    private String value;
    private List<GWTJahiaAjaxActionResultError>errors = new ArrayList<GWTJahiaAjaxActionResultError>();

    public GWTJahiaAjaxActionResult() {
    }

    public GWTJahiaAjaxActionResult(String value) {
        this.value = value;
    }

    public GWTJahiaAjaxActionResult(String value, List<GWTJahiaAjaxActionResultError> errors) {
        this.value = value;
        this.errors = errors;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<GWTJahiaAjaxActionResultError> getErrors() {
        return errors;
    }

    public void setErrors(List<GWTJahiaAjaxActionResultError> errors) {
        this.errors = errors;
    }

    public void addError(String errorMsg){
        GWTJahiaAjaxActionResultError error = new GWTJahiaAjaxActionResultError(errorMsg);
        if (this.errors==null){
            this.errors = new ArrayList<GWTJahiaAjaxActionResultError>();
        }
        this.errors.add(error);
    }

}
