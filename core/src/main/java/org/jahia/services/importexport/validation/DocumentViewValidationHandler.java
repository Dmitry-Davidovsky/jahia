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
package org.jahia.services.importexport.validation;

import java.util.Collections;
import java.util.List;

import org.apache.jackrabbit.util.ISO9075;
import org.jahia.services.importexport.BaseDocumentViewHandler;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SAX handler that performs validation of the JCR content, provided in a document format.
 *
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public class DocumentViewValidationHandler extends BaseDocumentViewHandler implements
        ModuleDependencyAware {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentViewValidationHandler.class);

    private List<String> modules = Collections.emptyList();

    private String templateSetName;

    private List<ImportValidator> validators = Collections.emptyList();

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (validators.isEmpty()) {
            return;
        }
        pathes.pop();
    }

    /**
     * Returns the overall validation results.
     *
     * @return the overall validation results
     */
    public ValidationResults getResults() {
        ValidationResults results = new ValidationResults();
        for (ImportValidator validator : validators) {
            ValidationResult result = validator.getResult();
            if (result != null && !result.isSuccessful()) {
                logger.warn("Import validation fail: " + result.toString());
            }
            results.addResult(result);
        }

        return results;
    }

    @Override
    public void initDependencies(String templateSetName, List<String> modules) {
        this.templateSetName = templateSetName;
        if (modules != null) {
            this.modules = modules;
        } else {
            this.modules = Collections.emptyList();
        }
        initValidators();
    }

    private void initValidators() {
        if (!validators.isEmpty() && templateSetName != null) {
            for (ImportValidator validator : validators) {
                if (validator instanceof ModuleDependencyAware) {
                    ((ModuleDependencyAware) validator).initDependencies(templateSetName, modules);
                }
            }
        }
    }

    public void setValidators(List<ImportValidator> validators) {
        if (validators != null) {
            this.validators = validators;
        } else {
            validators = Collections.emptyList();
        }
        initValidators();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (validators.isEmpty()) {
            return;
        }

        try {
            String decodedLocalName = ISO9075.decode(localName);

            String decodedQName = qName.replace(localName, decodedLocalName);

            pathes.push(pathes.peek() + "/" + decodedQName);

            for (ImportValidator validator : validators) {
                validator.validate(decodedLocalName, decodedQName, pathes.peek(), atts);
            }
        } catch (Exception re) {
            throw new SAXException(re);
        }
    }
}
