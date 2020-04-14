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
package org.jahia.ajax.gwt.client.widget.portlet;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaPortletOutputBean;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * 
 * User: loom
 * Date: Dec 4, 2008
 * Time: 3:09:02 PM
 * 
 */
public class PortletRender extends HTML {
    private GWTJahiaPortletOutputBean gwtPortletOutputBean;

    public PortletRender(String windowID, String entryPointInstanceID, String pathInfo, String queryString) {
        setHTML("Loading...");
        JahiaContentManagementService.App.getInstance().drawPortletInstanceOutput(windowID, entryPointInstanceID, pathInfo, queryString, new BaseAsyncCallback<GWTJahiaPortletOutputBean>() {
            public void onSuccess(GWTJahiaPortletOutputBean result) {
                gwtPortletOutputBean = result;
                if (gwtPortletOutputBean.isInIFrame()) {
                    setHTML("");
                    Element iFrameElement = DOM.createElement("iframe");
                    DOM.setElementAttribute(iFrameElement, "frameBorder", "0");
                    DOM.setElementAttribute(iFrameElement, "border", "0");
                    DOM.setElementAttribute(iFrameElement, "scrolling", "no");
                    DOM.setElementAttribute(iFrameElement, "marginHeight", "0");
                    DOM.setElementAttribute(iFrameElement, "marginWidth", "0");
                    DOM.setElementAttribute(iFrameElement, "onload", "resizeIFrame(this)");
                    if (gwtPortletOutputBean.getIFrameWidth() != null) {
                        DOM.setElementAttribute(iFrameElement, "width", gwtPortletOutputBean.getIFrameWidth());
                    } else {
                        DOM.setElementAttribute(iFrameElement, "width", "100%");
                    }
                    if (gwtPortletOutputBean.getIFrameHeight() != null) {
                        DOM.setElementAttribute(iFrameElement, "height", gwtPortletOutputBean.getIFrameHeight());
                    } else {
                        DOM.setElementAttribute(iFrameElement, "height", "100");
                    }
                    getElement().appendChild(iFrameElement);
                    iFrameDocumentWrite(iFrameElement, gwtPortletOutputBean.getHtmlOutput(), Long.toString(gwtPortletOutputBean.getDelayedIFrameResizeTime()));
                } else {
                    setHTML(gwtPortletOutputBean.getHtmlOutput());
                    // we still need to execute the Javascript that can be generated in the HTML, as it is not executed
                    // by default.
                    // we must now make sure we execute all the scripts in the HTML.
                    for (String curScriptSrc : gwtPortletOutputBean.getScriptsWithSrc()) {
                        Element scriptElement = DOM.createElement("script");
                        DOM.setElementAttribute(scriptElement, "src", curScriptSrc);
                        getElement().appendChild(scriptElement);
                    }

                    for (String curScriptCode : gwtPortletOutputBean.getScriptsWithCode()) {
                        eval(curScriptCode);
                    }

                }

                Log.info("Portlet HTML successfully retrieved.");

                onRender();
            }

            public void onApplicationFailure(Throwable throwable) {
                setHTML("Unable to load portlet.");
                Log.error("Error modifying portlet HTML", throwable);
            }
        });

    }

    public static native void eval(String source) /*-{
       $wnd.eval(source);
    }-*/;

    public static native void iFrameDocumentWrite(Element iFrameElement, String content, String delayedIFrameResizeTime) /*-{
       if ($wnd.iFrameDocumentWrite) {
           $wnd.iFrameDocumentWrite(iFrameElement, content, delayedIFrameResizeTime);
       }
    }-*/;

    public void onRender() {

    }

  

}
