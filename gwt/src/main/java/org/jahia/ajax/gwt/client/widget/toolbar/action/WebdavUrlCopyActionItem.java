/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

public class WebdavUrlCopyActionItem extends NodeTypeAwareBaseActionItem {
    public void onComponentSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        JahiaContentManagementService.App.getInstance().getAbsolutePath(lh.getSingleSelection().getPath(), new BaseAsyncCallback<String>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.error(throwable.getMessage(), throwable);
                MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
            }

            public void onSuccess(String s) {
                final Dialog dl = new Dialog();
                dl.setHeadingHtml(Messages.get("label.webdav.url"));
                dl.setModal(true);
                dl.setHideOnButtonClick(true);
                dl.setLayout(new HBoxLayout());
                dl.setWidth(500);
                dl.setScrollMode(Style.Scroll.NONE);
                final TextBox textBox = new TextBox();
                textBox.setValue(s);
                textBox.setWidth("460px");
                textBox.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        textBox.selectAll();
                    }
                });
                dl.add(textBox);
                dl.add(new HTML("<span class=\"copy-to-clipboard\" title=\"" + Messages.get("label.copyToClipboard", "") + "\">" + s + "</span>"));
                dl.setHeight(102);
                dl.show();
                initClippy(JahiaGWTParameters.getContextPath());
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        setEnabled(lh.getSingleSelection() != null
                && isNodeTypeAllowed(lh.getSingleSelection()));
    }

    private native void initClippy(String contextPath) /*-{
        $wnd.jQuery('.copy-to-clipboard').clippy({clippy_path: contextPath + '/modules/assets/javascript/clippy/clippy.swf'});
    }-*/;
}
