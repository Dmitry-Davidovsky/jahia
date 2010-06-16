package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 11:12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThumbsListView extends ListView<GWTJahiaNode> {
    public ThumbsListView() {
        setItemSelector("div.thumb-wrap");
        setOverStyle("x-view-over");
    }


    public ThumbsListView(boolean detailed) {
        this();
        if (detailed) {
            setTemplate(getDetailedTemplate());
        } else {
            setTemplate(getSimpleTemplate());
        }
    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        String s = model.getName();
        model.set("shortName", Format.ellipse(s, 14));
        model.set("nameLabel", Messages.get("label.name", "Name"));
        model.set("authorLabel", Messages.get("versioning_author", "Author"));
        model.set("tagsLabel", Messages.get("org.jahia.jcr.edit.tags.tab", "tags"));
        String width = model.get("j:width");
        if (width != null) {
            if (Integer.parseInt(width) < 80) {
                model.set("nodeImg", "<img src=\"" + model.getUrl() + "\" title=\"" + model.getName() + "\">");
            } else {
                model.set("nodeImg", "<img src=\"" + model.getPreview() + "\" title=\"" + model.getName() + "\">");
            }
            model.set("widthHTML", "<div><b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.width.label", "Width") + " </b>" + model.get("j:width") + " px</div>");
            model.set("heightHTML", "<div><b>" + Messages.get("org.jahia.engines.filemanager.Filemanager_Engine.height.label", "Height") + " </b>" + model.get("j:height") + " px</div>");
        } else if (model.getPreview() != null) {
            model.set("nodeImg", "<img src=\"" + model.getPreview() + "\" title=\"" + model.getName() + "\">");
        } else {
            model.set("nodeImg", ContentModelIconProvider.getInstance().getIcon(model, true).getHTML());
        }

        if (model.getTags() != null && model.getTags().length() > 0) {
            model.set("tagsHTML", "<div><b>" + model.get("tagsLabel") + ": </b>" + model.getTags() + "</div>");
        }
        return model;
    }

    public void setContextMenu(Menu menu) {
        super.setContextMenu(menu);
    }

    public native String getSimpleTemplate() /*-{
        return ['<tpl for=".">',
      '<div title="{name}" class="thumb-wrap" id="{name}">',
      '<div class="thumb">{nodeImg}</div>',
      '<span class="x-editable"> {shortName}</span>',
      '{widthHTML}',
      '{heightHTML}',
      '{tagsHTML}',
      '</div>',
      '</tpl>',
      '<div class="x-clear"></div>'].join("");

      }-*/;


    public native String getDetailedTemplate() /*-{
    return ['<tpl for=".">',
        '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
        '<div><div style="width: 140px; float: left; text-align: center;" class="thumb">{nodeImg}</div>',
        '<div style="margin-left: 160px; " class="thumbDetails">',
        '<div><b>{nameLabel}: </b>{name}</div>',
        '<div><b>{authorLabel}: </b>{createdBy}</div>',
        '{widthHTML}',
        '{heightHTML}',
        '{tagsHTML}',
        '</div>',
        '</div>',
        '<div style="padding-left: 10px; padding-top: 10px; clear: left">{description}</div></div></tpl>',
        '<div class="x-clear"></div>'].join("");
    }-*/;

}
