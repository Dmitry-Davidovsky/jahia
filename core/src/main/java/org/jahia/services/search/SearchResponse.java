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
package org.jahia.services.search;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * SearchResponse returned by the Jahia search service.
 *
 * @author Benjamin Papez
 *
 */
public class SearchResponse {

    /**
     * Information about particular search facet result, including its value (term) and aggregated count.
     */
    public static class Facet {

        private long count;
        private Object value;
        private String valueAsString;

        public Facet(Object value, String valueAsString, long count) {
            this.value = value;
            this.valueAsString = valueAsString;
            this.count = count;
        }

        public long getCount() {
            return count;
        }

        public Object getValue() {
            return value;
        }

        public String getValueAsString() {
            return valueAsString;
        }
        
        public boolean isValueEmpty() {
            return StringUtils.isEmpty(valueAsString);
        }
    }

    /**
     * Results, corresponding to a specific search facet.
     */
    public static class FacetedResult {

        private String id;

        private List<Facet> facets;

        public FacetedResult(String id, List<Facet> facets) {
            if (id == null || id.length() == 0) {
                throw new IllegalArgumentException("Facet definition ID should not be null or empty");
            }
            this.id = id;
            this.facets = facets != null ? Collections.unmodifiableList(facets) : null;
        }

        /**
         * Returns identifier of the corresponding facet definition to match this faceted result to.
         * 
         * @return identifier of the corresponding facet definition to match this faceted result to
         */
        public String getId() {
            return id;
        }

        /**
         * Facet, requested by its string value.
         * 
         * @param valueAsString the facet string value to retrieve facet for
         * 
         * @return Facet, requested by its string value, or <code>null</code> if there is no corresponding facet
         */
        public Facet getFacet(String valueAsString) {
            if (facets != null) {
                for (Facet f : facets) {
                    if (StringUtils.equals(valueAsString, f.getValueAsString())) {
                        return f;
                    }
                }
            }
            return null;
        }

        /**
         * A list of facets.
         * 
         * @return a list of of facets
         */
        public List<Facet> getFacets() {
            return facets;
        }

        /**
         * Checks that the faceted result is empty, i.e. either it has no facets or all facets have empty values.
         * 
         * @return <code>true</code> if the faceted result is considered as empty; <code>false</code> otherwise
         */
        public boolean isResultEmpty() {
            if (facets != null) {
                for (Facet f : facets) {
                    if (!f.isValueEmpty()) {
                        // at least one facet value is not empty
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private List<Hit<?>> results = Collections.emptyList();
    private Collection<FacetedResult> facetedResults;

    private long offset = 0;
    private long limit = -1;
    private boolean hasMore = false;
    private long approxCount = 0;

    /**
     * Initializes an instance of this class.
     */
    public SearchResponse() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param results the search result list
     */
    public SearchResponse(List<Hit<?>> results) {
        this();
        setResults(results);
    }

    /**
     * Holds a list of Hit objects matching the search criteria.
     *
     * @return list of Hit objects
     */
    public List<Hit<?>> getResults() {
        return results;
    }

    /**
     * Sets a list of Hits objects matching the search criteria. This setter is being used
     * by the Jahia search service.
     *
     * @param results List of Hit objects
     */
    public void setResults(List<Hit<?>> results) {
        this.results = results;
    }

    /**
     * @return Faceted results corresponding to facet definitions passed as a part of the search criteria if any, null otherwise
     */
    public Collection<FacetedResult> getFacetedResults() {
        return facetedResults;
    }

    /**
     * Retrieves the faceted result, which corresponds to the supplied facet definition ID.
     * 
     * @param facetDefinitionId the facet definition ID to retrieve results for
     * @return faceted result, which corresponds to the supplied facet definition ID, or <code>null</code> if there is no information about
     *         that facet (or no result for that facet is present)
     */
    public FacetedResult getFacetedResult(String facetDefinitionId) {
        if (facetedResults != null) {
            for (FacetedResult result : facetedResults) {
                if (result.getId().equals(facetDefinitionId)) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * @param facetedResults Faceted results corresponding to facet definitions passed as a part of the search criteria (if any)
     */
    public void setFacetedResults(Collection<FacetedResult> facetedResults) {
        this.facetedResults = facetedResults != null ? Collections.unmodifiableCollection(facetedResults) : null;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getApproxCount() {
        return approxCount;
    }

    public void setApproxCount(long approxCount) {
        this.approxCount = approxCount;
    }
}
