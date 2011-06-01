/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.LazySortedMap;
import org.apache.commons.collections.map.TransformedSortedMap;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;

/**
 * Template rendering context with the information about current request/response pair and optional template parameters.
 *
 * @author toto
 */
public class RenderContext {
    
    private static final Transformer LOW_CASE_TRANSFORMER = new Transformer() {
        public Object transform(Object input) {
            return input != null ? input.toString().toLowerCase() : null;
        }
    };

    private static final FastHashMap RANK;
    static {
        RANK = new FastHashMap();
        RANK.put("css", Integer.valueOf(1));
        RANK.put("inlinecss", Integer.valueOf(2));
        RANK.put("javascript", Integer.valueOf(3));
        RANK.put("inlinejavascript", Integer.valueOf(4));
        RANK.put("inline", Integer.valueOf(5));
        RANK.put("unknown", Integer.valueOf(6));
        RANK.setFast(true);
    }
    @SuppressWarnings("unchecked")
    private static final Comparator<String> ASSET_COMPARATOR = ComparatorUtils
            .transformedComparator(null, new Transformer() {
                public Object transform(Object input) {
                    Integer rank = null;
                    if (input != null) {
                        rank = (Integer) RANK.get(input.toString());
                    }

                    return rank != null ? rank : RANK.get("unknown");
                }
            });

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Resource mainResource;
    private JahiaUser user;
    private JCRSiteNode site;
    private URLGenerator URLGenerator;
    private Locale uiLocale;

    private Stack<Resource> resourcesStack = new Stack<Resource>();

    private boolean isContributionMode = false;
    private boolean isEditMode = false;
    private String editModeConfigName;
    private String servletPath;

    private Set<String> displayedModules = new HashSet<String>();
    
    private String redirect;
    
    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> staticAssets = LazySortedMap.decorate(
            TransformedSortedMap.decorate(new TreeMap<String, Set<String>>(ASSET_COMPARATOR), LOW_CASE_TRANSFORMER, NOPTransformer.INSTANCE), new SetFactory());

    /** Static asset options (e.g. link title) keyed by asset resource (URL) */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String>> staticAssetOptions = LazyMap.decorate(
            new HashMap<String, Map<String, Object>>(), new Factory() {
                public Object create() {
                    return new HashMap<String, Object>();
                }
            });

    private String contentType;

    private Map<String,Map <String, Integer>> templatesCacheExpiration = new HashMap<String, Map<String,Integer>>();
    private boolean liveMode = false;

    private Set<String> resourceFileNames = new LinkedHashSet<String>();
    private boolean previewMode = false;
    private boolean ajaxRequest = false;
    private Resource ajaxResource = null;

    public RenderContext(HttpServletRequest request, HttpServletResponse response, JahiaUser user) {
        this.request = request;
        this.response = response;
        this.user = user;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public JahiaUser getUser() {
        return user;
    }

    public JCRSiteNode getSite() {
        return site;
    }

    public void setSite(JCRSiteNode site) {
        this.site = site;
    }

    public URLGenerator getURLGenerator() {
        return URLGenerator;
    }

    public void setURLGenerator(URLGenerator URLGenerator) {
        this.URLGenerator = URLGenerator;
    }

    public Set<String> getDisplayedModules() {
        return displayedModules;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public String getEditModeConfigName() {
        return editModeConfigName;
    }

    public void setEditModeConfigName(String editModeConfigName) {
        this.editModeConfigName = editModeConfigName;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
        JCRSessionFactory.getInstance().setCurrentServletPath(servletPath);
    }

    public boolean isContributionMode() {
        return isContributionMode;
    }

    public void setContributionMode(boolean contributionMode) {
        isContributionMode = contributionMode;
    }

    public boolean isLoggedIn() {
        return JahiaUserManagerService.isNotGuest(getUser());
    }

    public Map<String, Map<String, Integer>> getTemplatesCacheExpiration() {
        return templatesCacheExpiration;
    }

    public void addStaticAsset(String assetType,String asset) {
        addStaticAsset(assetType, asset, false);
    }

    public void addStaticAsset(String assetType,String asset, boolean insert) {
        addStaticAsset(assetType,asset,insert,"");
    }
    public void addStaticAsset(String assetType,String asset, boolean insert, String key) {
        String type = assetType + key;
        Set<String> assets = getStaticAssets(type);
        boolean isInlined = "inline".equalsIgnoreCase(assetType);
        if (isInlined || !resourceFileNames.contains(asset)) {
            if (insert) {
                LinkedHashSet<String> my = new LinkedHashSet<String>();
                my.add(asset);
                my.addAll(assets);
                assets = my;
            } else {
                if(!"".equals(key)) {
                    assets.clear();
                    assets.add(asset);
                } else {
                assets.add(asset);
                }
            }
            staticAssets.put(type, assets);
            if (!isInlined) {
                resourceFileNames.add(asset);
            }
        }
    }

    public void addStaticAsset(Map<String, Set<String>> staticAssets) {
        for (Map.Entry<String, Set<String>> entry : staticAssets.entrySet()) {
            final Set<String> assets = getStaticAssets(entry.getKey());
            if(assets!=null) {
                assets.addAll(entry.getValue());
            } else {
                this.staticAssets.put(entry.getKey(),entry.getValue());
            }
        }
    }

    public Set<String> getStaticAssets(String assetType) {
        return staticAssets.get(assetType);
    }

    public Map<String, Set<String>> getStaticAssets() {
        return staticAssets;
    }

    public void setMainResource(Resource mainResource) {
        this.mainResource = mainResource;
    }

    public Resource getMainResource() {
        return mainResource;
    }

	public String getContentType() {
    	return contentType;
    }

	public void setContentType(String contentType) {
    	this.contentType = contentType;
    }

    public Stack<Resource> getResourcesStack() {
        return resourcesStack;
    }

    public Locale getMainResourceLocale() {
        return getMainResource().getLocale();
    }

    public Locale getUILocale() {
        if (uiLocale == null) {
            Locale locale = null;
            if(JahiaUserManagerService.isNotGuest(getUser())) {
                locale = UserPreferencesHelper.getPreferredLocale(getUser(), LanguageCodeConverters.resolveLocaleForGuest(request));
            }
            if (locale == null) {
                locale = getMainResourceLocale();
            }
            uiLocale = locale;
            request.getSession(false).setAttribute(Constants.SESSION_UI_LOCALE, uiLocale);
        }
        return uiLocale;
    }

    public Locale getFallbackLocale() {
        if (site != null) {
            return site.isMixLanguagesActive()? LanguageCodeConverters.languageCodeToLocale(site.getDefaultLanguage()):null;
        }
        return null;
    }

    public void setLiveMode(boolean liveMode) {
        this.liveMode = liveMode;
    }

    public boolean isLiveMode() {
        return liveMode;
    }

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public boolean isPreviewMode() {
        return previewMode;
    }

    public void setAjaxRequest(boolean ajaxRequest) {
        this.ajaxRequest = ajaxRequest;
    }

    public boolean isAjaxRequest() {
        return ajaxRequest;
    }

    public void setAjaxResource(Resource ajaxResource) {
        this.ajaxResource = ajaxResource;
    }

    public Resource getAjaxResource() {
        return ajaxResource;
    }

    /**
     * @return the redirect
     */
    public String getRedirect() {
        return redirect;
    }

    /**
     * @param redirect the redirect to set
     */
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    /**
     * @return the staticAssetOptions
     */
    public Map<String, Map<String, String>> getStaticAssetOptions() {
        return staticAssetOptions;
    }

    public void resetStaticAssets() {
        this.staticAssets.clear();
    }
    
    public boolean isEnterpriseEdition() {
        return Jahia.isEnterpriseEdition();
    }

    public SettingsBean getSettings() {
        return SettingsBean.getInstance();
    }
}