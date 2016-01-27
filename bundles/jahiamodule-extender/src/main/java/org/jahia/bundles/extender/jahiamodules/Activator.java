/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.poi.util.IOUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.cache.CacheHelper;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JCRModuleListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageDeployer;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Activator for Jahia Modules extender
 */
public class Activator implements BundleActivator {

    public static final String LEGACY_HANDLER_PREFIX = "legacydepends";

    static Logger logger = LoggerFactory.getLogger(Activator.class);

    private static final BundleURLScanner CND_SCANNER = new BundleURLScanner("META-INF", "*.cnd", false);
    private static final BundleURLScanner DSL_SCANNER = new BundleURLScanner("META-INF", "*.dsl", false);
    private static final BundleURLScanner DRL_SCANNER = new BundleURLScanner("META-INF", "*.drl", false);
    private static final BundleURLScanner URLREWRITE_SCANNER = new BundleURLScanner("META-INF", "*urlrewrite*.xml", false);
    private static final BundleURLScanner FLOW_SCANNER = new BundleURLScanner("/", "flow.xml", true);

    private static final Comparator<Resource> IMPORT_FILE_COMPARATOR = new Comparator<Resource>() {
        public int compare(Resource o1, Resource o2) {
            return StringUtils.substringBeforeLast(o1.getFilename(), ".").compareTo(StringUtils.substringBeforeLast(o2.getFilename(), "."));
        }
    };

    private CndBundleObserver cndBundleObserver = null;
    private List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<ServiceRegistration<?>>();
    private BundleListener bundleListener = null;
    private Set<Bundle> installedBundles;
    private Set<Bundle> initializedBundles;
    private Map<Bundle, JahiaTemplatesPackage> registeredBundles;
    private Map<Bundle, ServiceTracker<HttpService, HttpService>> bundleHttpServiceTrackers = new HashMap<Bundle, ServiceTracker<HttpService, HttpService>>();
    private JahiaTemplateManagerService templatesService;
    private TemplatePackageRegistry templatePackageRegistry = null;
    private TemplatePackageDeployer templatePackageDeployer = null;

    private Map<BundleURLScanner, BundleObserver<URL>> extensionObservers = new LinkedHashMap<BundleURLScanner, BundleObserver<URL>>();
    private Map<String, List<Bundle>> toBeParsed;

    private BundleStarter bundleStarter;

    private Map<Bundle, ModuleState> moduleStates;

    private static Activator instance = null;

    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configurationAdminConfigurationAdminServiceTracker = null;

    public Activator() {
        instance = this;
    }

    public static Activator getInstance() {
        return instance;
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        logger.info("== Starting Jahia Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        // obtain service instances
        templatesService = (JahiaTemplateManagerService) SpringContextSingleton.getBean("JahiaTemplateManagerService");
        templatePackageDeployer = templatesService.getTemplatePackageDeployer();
        templatePackageRegistry = templatesService.getTemplatePackageRegistry();

        // register rule observers
        RulesBundleObserver rulesBundleObserver = new RulesBundleObserver();
        extensionObservers.put(DSL_SCANNER, rulesBundleObserver);
        extensionObservers.put(DRL_SCANNER, rulesBundleObserver);

        // Get all module state information from the service
        registeredBundles = templatesService.getRegisteredBundles();
        installedBundles = templatesService.getInstalledBundles();
        initializedBundles = templatesService.getInitializedBundles();
        toBeParsed = templatesService.getToBeParsed();
        moduleStates = templatesService.getModuleStates();

        BundleScriptResolver bundleScriptResolver = (BundleScriptResolver) SpringContextSingleton.getBean("BundleScriptResolver");

        // register view script observers
        final ScriptBundleObserver scriptBundleObserver = new ScriptBundleObserver(bundleScriptResolver);
        // add scanners for all types of scripts of the views to register them in the BundleScriptResolver
        for (String scriptExtension : bundleScriptResolver.getScriptExtensionsOrdering()) {
            extensionObservers.put(new BundleURLScanner("/", "*." + scriptExtension, true), scriptBundleObserver);
        }

        bundleStarter = new BundleStarter();

        extensionObservers.put(FLOW_SCANNER, new BundleObserver<URL>() {
            @Override
            public void addingEntries(Bundle bundle, List<URL> entries) {
                for (URL entry : entries) {
                    try {
                        URL parent = new URL(entry.getProtocol(), entry.getHost(), entry.getPort(), new File(entry.getFile()).getParent());
                        scriptBundleObserver.addingEntries(bundle, Arrays.asList(parent));
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }

            @Override
            public void removingEntries(Bundle bundle, List<URL> entries) {
                for (URL entry : entries) {
                    try {
                        URL parent = new URL(entry.getProtocol(), entry.getHost(), entry.getPort(), new File(entry.getFile()).getParent());
                        scriptBundleObserver.removingEntries(bundle, Arrays.asList(parent));
                    } catch (MalformedURLException e) {
                        //
                    }
                }
            }
        });

        // observer for URL rewrite rules
        extensionObservers.put(URLREWRITE_SCANNER, new UrlRewriteBundleObserver());

        // we won't register CND observer, but will rather call it manually
        cndBundleObserver = new CndBundleObserver();

        // add listener for other bundle life cycle events
        setupBundleListener(context);

        // Add tranformer for jahia-depends capabilities
        registerLegacyTransformer(context);

        checkExistingModules(context);

        registerShellCommands(context);

        JCRModuleListener l = (JCRModuleListener) SpringContextSingleton.getBean("org.jahia.services.templates.JCRModuleListener");
        l.setListener(new JCRModuleListener.Listener() {
            @Override
            public void onModuleImported(JahiaTemplatesPackage pack) {
                if (pack.getState().getState() == ModuleState.State.WAITING_TO_BE_IMPORTED) {
                    start(pack.getBundle());
                }
            }
        });

        configurationAdminConfigurationAdminServiceTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(context, ConfigurationAdmin.class, new ConfigAdminServiceCustomizer(context));
        configurationAdminConfigurationAdminServiceTracker.open();

        logger.info("== Jahia Extender started in {}ms ============================================================== ", System.currentTimeMillis() - startTime);

    }

    private void checkExistingModules(BundleContext context) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, BundleException, IOException {
        List<Bundle> toStart = new ArrayList<>();
        // parse existing bundles
        for (Bundle bundle : context.getBundles()) {
            // Parse bundle if activator has not seen them before

            if (!registeredBundles.containsKey(bundle)) {
                if (BundleUtils.isJahiaModuleBundle(bundle) && bundle.getState() > Bundle.INSTALLED) {
                    String l = BeanUtils.getProperty(bundle,"location");
                    if (bundle.getState() == Bundle.ACTIVE) {
                        bundle.stop();
                        toStart.add(bundle);
                    }
                    try {
                        if (!l.startsWith(LEGACY_HANDLER_PREFIX)) {
                            bundle.update(new URL(LEGACY_HANDLER_PREFIX + ":" + l).openStream());
                        } else {
                            bundle.update();
                        }
                    } catch (BundleException e) {
                        logger.warn("Cannot update bundle : " + e.getMessage(), e);
                    }
                }
            }
        }
        for (Bundle bundle : toStart) {
            bundle.start();
        }
    }

    private void registerShellCommands(BundleContext context) {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put(CommandProcessor.COMMAND_SCOPE, "jahia");
        dict.put(CommandProcessor.COMMAND_FUNCTION, new String[]{"modules"});
        ShellCommands shellCommands = new ShellCommands(this);
        serviceRegistrations.add(context.registerService(ShellCommands.class.getName(), shellCommands, dict));
    }

    private synchronized void setupBundleListener(BundleContext context) {
        context.addFrameworkListener(bundleStarter);
        context.addBundleListener(bundleListener = new SynchronousBundleListener() {

                    public void bundleChanged(final BundleEvent bundleEvent) {
                        Bundle bundle = bundleEvent.getBundle();
                        if (bundle == null || !BundleUtils.isJahiaModuleBundle(bundle)) {
                            return;
                        }

                        if (logger.isDebugEnabled()) {
                            logger.debug("Received event {} for bundle {}", BundleUtils.bundleEventToString(bundleEvent.getType()),
                                    getDisplayName(bundleEvent.getBundle()));
                        }
                        try {
                            switch (bundleEvent.getType()) {
                                case BundleEvent.INSTALLED:
                                    setModuleState(bundle, ModuleState.State.INSTALLED, null);
                                    install(bundle);
                                    break;
                                case BundleEvent.UPDATED:
                                    setModuleState(bundle, ModuleState.State.UPDATED, null);
                                    update(bundle);
                                    break;
                                case BundleEvent.RESOLVED:
                                    conditionalSetModuleState(bundle, ModuleState.State.RESOLVED);
                                    resolve(bundle);
                                    break;
                                case BundleEvent.STARTING:
                                    conditionalSetModuleState(bundle, ModuleState.State.STARTING);
                                    starting(bundle);
                                    break;
                                case BundleEvent.STARTED:
                                    conditionalSetModuleState(bundle, ModuleState.State.STARTED);
                                    start(bundle);
                                    break;
                                case BundleEvent.STOPPING:
                                    conditionalSetModuleState(bundle, ModuleState.State.STOPPING);
                                    stopping(bundle);
                                    break;
                                case BundleEvent.STOPPED:
                                    conditionalSetModuleState(bundle, ModuleState.State.STOPPED);
                                    stopped(bundle);
                                    break;
                                case BundleEvent.UNRESOLVED:
                                    conditionalSetModuleState(bundle, ModuleState.State.UNRESOLVED);
                                    unresolve(bundle);
                                    break;
                                case BundleEvent.UNINSTALLED:
                                    moduleStates.remove(bundle);
                                    uninstall(bundle);
                                    break;
                            }
                        } catch (Exception e) {
                            logger.error("Error when handling event", e);
                        }
                    }

                }
        );
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        logger.info("== Stopping Jahia Extender ============================================================== ");
        long startTime = System.currentTimeMillis();

        if (configurationAdminConfigurationAdminServiceTracker != null) {
            configurationAdminConfigurationAdminServiceTracker.close();
        }

        context.removeBundleListener(bundleListener);
        context.removeFrameworkListener(bundleStarter);

        bundleListener = null;
        bundleStarter = null;

        for (ServiceRegistration<?> serviceRegistration : serviceRegistrations) {
            try {
                serviceRegistration.unregister();
            } catch (IllegalStateException e) {
                logger.warn(e.getMessage());
            }
        }

        // Ensure all trackers are correctly closed - should be empty now
        for (ServiceTracker<HttpService, HttpService> tracker : bundleHttpServiceTrackers.values()) {
            tracker.close();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("== Jahia Extender stopped in {}ms ============================================================== ", totalTime);

    }

    private synchronized void install(final Bundle bundle) {
        installedBundles.add(bundle);
    }

    private synchronized void update(final Bundle bundle) {
        BundleUtils.unregisterModule(bundle);
        installedBundles.add(bundle);
    }

    private synchronized void uninstall(Bundle bundle) {
        logger.info("--- Uninstalling Jahia OSGi bundle {} --", getDisplayName(bundle));
        BundleUtils.unregisterModule(bundle);

        long startTime = System.currentTimeMillis();

        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage != null) {
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatePackageDeployer.clearModuleNodes(jahiaTemplatesPackage, session);
                        return null;
                    }
                });
                if (templatePackageRegistry.getAvailableVersionsForModule(jahiaTemplatesPackage.getId()).equals(Collections.singleton(jahiaTemplatesPackage.getVersion()))) {
                    if (SettingsBean.getInstance().isDevelopmentMode() && SettingsBean.getInstance().isProcessingServer()
                            && !templatesService.checkExistingContent(bundle.getSymbolicName())) {
                        JCRStoreService jcrStoreService = (JCRStoreService) SpringContextSingleton.getBean("JCRStoreService");
                        jcrStoreService.undeployDefinitions(bundle.getSymbolicName());
                        NodeTypeRegistry.getInstance().unregisterNodeTypes(bundle.getSymbolicName());
                    }
                }
            } catch (IOException | RepositoryException e) {
                logger.error("Error while uninstalling module content for module " + jahiaTemplatesPackage, e);
            }
            templatePackageRegistry.unregisterPackageVersion(jahiaTemplatesPackage);
        }
        installedBundles.remove(bundle);
        initializedBundles.remove(bundle);

        deleteBundleFileIfNeeded(bundle);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished uninstalling Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void deleteBundleFileIfNeeded(Bundle bundle) {
        File bundleFile = null;
        try {
            URL bundleUrl = new URL(bundle.getLocation());
            if (bundleUrl.getProtocol().equals("file")) {
                bundleFile = new File(bundleUrl.getFile());
            }
        } catch (MalformedURLException e) {
            // not located in a file
        }
        if (bundleFile != null
                && bundleFile.getAbsolutePath().startsWith(
                SettingsBean.getInstance().getJahiaModulesDiskPath() + File.separatorChar)
                && bundleFile.exists()) {
            // remove bundle file from var/modules
            if (!bundleFile.delete()) {
                logger.warn("Unable to delete file for uninstalled bundle {}", bundleFile);
            }
        }
    }

    private void parseBundle(final Bundle bundle) {
        final JahiaTemplatesPackage pkg = BundleUtils.isJahiaModuleBundle(bundle) ? BundleUtils.getModule(bundle)
                : null;

        if (null == pkg) {
            // is not a Jahia module -> skip
            installedBundles.remove(bundle);
            moduleStates.remove(bundle);
            return;
        }

        pkg.setState(getModuleState(bundle));
        //Check required version
        String jahiaRequiredVersion = bundle.getHeaders().get("Jahia-Required-Version");
        if (!StringUtils.isEmpty(jahiaRequiredVersion) && new org.jahia.commons.Version(jahiaRequiredVersion).compareTo(new org.jahia.commons.Version(Jahia.VERSION)) > 0) {
            logger.error("Error while reading module, required version (" + jahiaRequiredVersion + ") is higher than your Jahia version (" + Jahia.VERSION + ")");
            setModuleState(bundle, ModuleState.State.INCOMPATIBLE_VERSION, jahiaRequiredVersion);
            return;
        }

        List<String> dependsList = pkg.getDepends();
        if (!dependsList.contains("default")
                && !dependsList.contains("Default Jahia Templates")
                && !ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency()
                .contains(pkg.getId())) {
            dependsList.add("default");
        }

        for (String depend : dependsList) {
            if (!templatePackageRegistry.areVersionsForModuleAvailable(depend)) {
                logger.debug("Delaying module {} parsing because it depends on module {} that is not yet parsed.",
                        bundle.getSymbolicName(), depend);
                addToBeParsed(bundle, depend);
                return;
            }
        }

        logger.info("--- Parsing Jahia OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

        registeredBundles.put(bundle, pkg);
        templatePackageRegistry.registerPackageVersion(pkg);

        try {
            List<URL> foundURLs = CND_SCANNER.scan(bundle);
            if (!foundURLs.isEmpty()) {
                cndBundleObserver.addingEntries(bundle, foundURLs);
            }
        } catch (Exception e) {
            logger.error("--- Error parsing definitions for Jahia OSGi bundle " + pkg.getId() + " v" + pkg.getVersion(), e);
            setModuleState(bundle, ModuleState.State.ERROR_WITH_DEFINITIONS, e);
            return;
        }

        logger.info("--- Done parsing Jahia OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

        if (installedBundles.remove(bundle) || !checkImported(bundle, pkg)) {
            logger.info("--- Installing Jahia OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());

            scanForImportFiles(bundle, pkg);

            if (SettingsBean.getInstance().isProcessingServer()) {
                try {
                    JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser() != null ? JCRSessionFactory.getInstance().getCurrentUser() : JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser();
                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            templatePackageDeployer.initializeModuleContent(pkg, session);
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    logger.error("Error while initializing module content for module " + pkg, e);
                }
                initializedBundles.add(bundle);
            }
            logger.info("--- Done installing Jahia OSGi bundle {} v{} --", pkg.getId(), pkg.getVersion());
        }

        parseDependantBundles(pkg.getId());
    }

    private void addToBeParsed(Bundle bundle, String missingDependency) {
        List<Bundle> bundlesWaitingForDepend = toBeParsed.get(missingDependency);
        if (bundlesWaitingForDepend == null) {
            bundlesWaitingForDepend = new ArrayList<Bundle>();
            toBeParsed.put(missingDependency, bundlesWaitingForDepend);
        }
        bundlesWaitingForDepend.add(bundle);
    }

    private void parseDependantBundles(String key) {
        final List<Bundle> toBeParsedForKey = toBeParsed.get(key);
        if (toBeParsedForKey != null) {
            for (Bundle bundle : toBeParsedForKey) {
                if (bundle.getState() != Bundle.UNINSTALLED) {
                    logger.debug("Parsing module " + bundle.getSymbolicName() + " since it is dependent on just parsed module " + key);
                    parseBundle(bundle);
                }
            }
            toBeParsed.remove(key);
        }
    }

    private void resolve(Bundle bundle) {
        parseBundle(bundle);
    }

    private void unresolve(Bundle bundle) {
        // do nothing
    }

    private synchronized void starting(Bundle bundle) {
        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupById(bundle.getSymbolicName());
        if (jahiaTemplatesPackage != null) {
            try {
                logger.info("Stopping module {} before activating new version...", getDisplayName(bundle));
                jahiaTemplatesPackage.getBundle().stop();
            } catch (BundleException e) {
                logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
            }
        }
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            if (entry.getKey().getSymbolicName().equals(bundle.getSymbolicName()) && entry.getKey() != bundle && entry.getValue().getState() == ModuleState.State.WAITING_TO_BE_STARTED) {
                try {
                    entry.getKey().stop();
                } catch (BundleException e) {
                    logger.info("--- Cannot stop previous version of module " + bundle.getSymbolicName(), e);
                }
            }
        }
    }

    private synchronized void start(final Bundle bundle) {
        final JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null) {
            logger.error("--- Bundle " + bundle + " is starting but has not yet been parsed");
            bundleStarter.stopBundle(bundle);
            return;
        }

        if (!checkImported(bundle, jahiaTemplatesPackage)) {
            bundleStarter.stopBundle(bundle);
            return;
        }


        logger.info("--- Start Jahia OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        templatePackageRegistry.register(jahiaTemplatesPackage);
        jahiaTemplatesPackage.setActiveVersion(true);
        templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

        // scan for resource and call observers
        for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
            List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
            if (!foundURLs.isEmpty()) {
                scannerAndObserver.getValue().addingEntries(bundle, foundURLs);
            }
        }

        registerHttpResources(bundle);

        long totalTime = System.currentTimeMillis() - startTime;

        if (initializedBundles.remove(bundle)) {
            //auto deploy bundle according to bundle configuration
            try {
                JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                    public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        templatesService.autoInstallModulesToSites(jahiaTemplatesPackage, session);
                        session.save();
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while initializing module content for module " + jahiaTemplatesPackage, e);
            }
        }

        logger.info("--- Finished starting Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);

        if (hasSpringFile(bundle)) {
            try {
                if (BundleUtils.getContextToStartForModule(bundle) != null) {
                    BundleUtils.getContextToStartForModule(bundle).refresh();
                }
            } catch (Exception e) {
                setModuleState(bundle, ModuleState.State.SPRING_NOT_STARTED, e);
            }
        }
    }

    private boolean checkImported(Bundle bundle, final JahiaTemplatesPackage jahiaTemplatesPackage) {
        try {
            boolean imported = JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return session.itemExists("/modules/" + jahiaTemplatesPackage.getId() + "/" + jahiaTemplatesPackage.getVersion());
                }
            });
            if (!imported) {
                conditionalSetModuleState(bundle, ModuleState.State.WAITING_TO_BE_IMPORTED);
                return false;
            }
        } catch (RepositoryException e) {
            logger.error("Error while reading module jcr content" + jahiaTemplatesPackage, e);
        }
        return true;
    }

    private boolean hasSpringFile(Bundle bundle) {
        Enumeration<String> entries = bundle.getEntryPaths("/META-INF/spring");
        if (entries != null) {
            while (entries.hasMoreElements()) {
                String s = entries.nextElement();
                if (s.toLowerCase().endsWith(".xml")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getDisplayName(Bundle bundle) {
        return BundleUtils.getDisplayName(bundle);
    }

    private synchronized void stopping(Bundle bundle) {
        logger.info("--- Stopping Jahia OSGi bundle {} --", getDisplayName(bundle));
        long startTime = System.currentTimeMillis();

        JahiaTemplatesPackage jahiaTemplatesPackage = templatePackageRegistry.lookupByBundle(bundle);
        if (jahiaTemplatesPackage == null || !jahiaTemplatesPackage.isActiveVersion()) {
            return;
        }

        if (JahiaContextLoaderListener.isRunning()) {
            flushOutputCachesForModule(bundle, jahiaTemplatesPackage);

            templatePackageRegistry.unregister(jahiaTemplatesPackage);
            jahiaTemplatesPackage.setActiveVersion(false);
            templatesService.fireTemplatePackageRedeployedEvent(jahiaTemplatesPackage);

            if (jahiaTemplatesPackage.getContext() != null) {
                jahiaTemplatesPackage.setContext(null);
            }
            jahiaTemplatesPackage.setClassLoader(null);

            // scan for resource and call observers
            for (Map.Entry<BundleURLScanner, BundleObserver<URL>> scannerAndObserver : extensionObservers.entrySet()) {
                List<URL> foundURLs = scannerAndObserver.getKey().scan(bundle);
                if (!foundURLs.isEmpty()) {
                    scannerAndObserver.getValue().removingEntries(bundle, foundURLs);
                }
            }

            if (bundleHttpServiceTrackers.containsKey(bundle)) {
                bundleHttpServiceTrackers.remove(bundle).close();
            }

            setModuleState(bundle, ModuleState.State.STOPPED, null);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("--- Finished stopping Jahia OSGi bundle {} in {}ms --", getDisplayName(bundle), totalTime);
    }

    private void flushOutputCachesForModule(Bundle bundle, final JahiaTemplatesPackage pkg) {
        if (pkg.getInitialImports().isEmpty()) {
            // check for initial imports
            Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
            if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                importXMLEntryEnum = bundle.findEntries("META-INF", "import*.zip", false);
                if (importXMLEntryEnum == null || !importXMLEntryEnum.hasMoreElements()) {
                    // no templates -> no need to flush caches
                    return;
                }
            }
        }
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<JCRSiteNode> sitesNodeList = JahiaSitesService.getInstance().getSitesNodeList(session);
                    Set<String> pathsToFlush = new HashSet<String>();
                    for (JCRSiteNode site : sitesNodeList) {
                        Set<String> installedModules = site.getInstalledModulesWithAllDependencies();
                        if (installedModules.contains(pkg.getId()) || installedModules.contains(pkg.getName())) {
                            pathsToFlush.add(site.getPath());
                        }
                    }
                    if (!pathsToFlush.isEmpty()) {
                        CacheHelper.flushOutputCachesForPaths(pathsToFlush, true);
                    }
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private synchronized void stopped(Bundle bundle) {
        // Ensure context is reset
        BundleUtils.setContextToStartForModule(bundle, null);
    }

    private void registerHttpResources(final Bundle bundle) {
        final String displayName = getDisplayName(bundle);

        if (!BundleHttpResourcesTracker.getStaticResources(bundle).isEmpty()
                || !BundleHttpResourcesTracker.getJsps(bundle).isEmpty()) {
            logger.debug("Found HTTP resources for bundle {}." + " Will launch service tracker for HttpService",
                    displayName);
            if (bundleHttpServiceTrackers.containsKey(bundle)) {
                bundleHttpServiceTrackers.remove(bundle).close();
            }
            if (bundle.getBundleContext() != null) {
                ServiceTracker<HttpService, HttpService> bundleServiceTracker = new BundleHttpResourcesTracker(bundle);
                bundleServiceTracker.open(true);
                bundleHttpServiceTrackers.put(bundle, bundleServiceTracker);
            }
        } else {
            logger.debug("No HTTP resources found for bundle {}", displayName);
        }
    }

    private void scanForImportFiles(Bundle bundle, JahiaTemplatesPackage jahiaTemplatesPackage) {
        List<Resource> importFiles = new ArrayList<Resource>();
        Enumeration<URL> importXMLEntryEnum = bundle.findEntries("META-INF", "import*.xml", false);
        if (importXMLEntryEnum != null) {
            while (importXMLEntryEnum.hasMoreElements()) {
                importFiles.add(new BundleResource(importXMLEntryEnum.nextElement(), bundle));
            }
        }
        Enumeration<URL> importZIPEntryEnum = bundle.findEntries("META-INF", "import*.zip", false);
        if (importZIPEntryEnum != null) {
            while (importZIPEntryEnum.hasMoreElements()) {
                importFiles.add(new BundleResource(importZIPEntryEnum.nextElement(), bundle));
            }
        }
        Collections.sort(importFiles, IMPORT_FILE_COMPARATOR);
        for (Resource importFile : importFiles) {
            try {
                jahiaTemplatesPackage.addInitialImport(importFile.getURL().getPath());
            } catch (IOException e) {
                logger.error("Error retrieving URL for resource " + importFile, e);
            }
        }
    }

    public Map<Bundle, JahiaTemplatesPackage> getRegisteredBundles() {
        return registeredBundles;
    }

    public Map<ModuleState.State, Set<Bundle>> getModulesByState() {
        Map<ModuleState.State, Set<Bundle>> modulesByState = new TreeMap<ModuleState.State, Set<Bundle>>();
        for (Bundle bundle : moduleStates.keySet()) {
            ModuleState.State moduleState = moduleStates.get(bundle).getState();
            Set<Bundle> bundlesInState = modulesByState.get(moduleState);
            if (bundlesInState == null) {
                bundlesInState = new TreeSet<Bundle>();
            }
            bundlesInState.add(bundle);
            modulesByState.put(moduleState, bundlesInState);
        }
        return modulesByState;
    }

    private class BundleStarter implements FrameworkListener {

        private List<Bundle> toStart = Collections.synchronizedList(new ArrayList<Bundle>());
        private List<Bundle> toStop = Collections.synchronizedList(new ArrayList<Bundle>());

        @Override
        public synchronized void frameworkEvent(FrameworkEvent event) {
            switch (event.getType()) {
                case FrameworkEvent.PACKAGES_REFRESHED:
                    startAllBundles();
                    stopAllBundles();
                    break;

                case FrameworkEvent.STARTED:
                    logger.info("Got started event from OSGi framework");
                    FrameworkService.notifyStarted();
                    break;
            }
        }

        public void stopBundle(Bundle bundle) {
            toStop.add(bundle);
        }

        public void startAllBundles() {
            List<Bundle> toStart = new ArrayList<Bundle>(this.toStart);
            this.toStart.removeAll(toStart);

            for (Bundle bundle : toStart) {
                try {
                    bundle.start();
                } catch (BundleException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        public void stopAllBundles() {
            List<Bundle> toStop = new ArrayList<Bundle>(this.toStop);
            this.toStop.removeAll(toStop);
            for (Bundle bundle : toStop) {
                try {
                    if (bundle.getState() != Bundle.UNINSTALLED) {
                        bundle.stop();
                    }
                } catch (BundleException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public ModuleState getModuleState(Bundle bundle) {
        if (!moduleStates.containsKey(bundle)) {
            moduleStates.put(bundle, new ModuleState());
        }
        return moduleStates.get(bundle);
    }

    private void conditionalSetModuleState(Bundle bundle, ModuleState.State state) {
        if (getModuleState(bundle).getState() != ModuleState.State.ERROR_WITH_DEFINITIONS &&
                getModuleState(bundle).getState() != ModuleState.State.WAITING_TO_BE_PARSED &&
                getModuleState(bundle).getState() != ModuleState.State.INCOMPATIBLE_VERSION) {
            setModuleState(bundle, state, null);
        }
    }

    public void setModuleState(Bundle bundle, ModuleState.State state, Object details) {
        ModuleState moduleState = getModuleState(bundle);
        moduleState.setState(state);
        moduleState.setDetails(details);
    }

    public class ConfigAdminServiceCustomizer implements ServiceTrackerCustomizer<ConfigurationAdmin, ConfigurationAdmin> {

        private BundleContext bundleContext;

        public ConfigAdminServiceCustomizer(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public ConfigurationAdmin addingService(ServiceReference<ConfigurationAdmin> reference) {
            ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);
            registerFileInstallConfiguration(configurationAdmin);
            return configurationAdmin;
        }

        @Override
        public void modifiedService(ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin configurationAdmin) {
            unregisterFileInstallConfiguration(configurationAdmin);
            registerFileInstallConfiguration(configurationAdmin);
        }

        @Override
        public void removedService(ServiceReference<ConfigurationAdmin> reference, ConfigurationAdmin configurationAdmin) {
            unregisterFileInstallConfiguration(configurationAdmin);
        }
    }

    private void registerFileInstallConfiguration(ConfigurationAdmin configurationAdmin) {
        // we create the FileInstall dynamically to make sure it is not used before this bundle is properly started.
        // this is not ideal but seemed like the surest way to ensure proper startup sequencing. Ideally sequencing
        // should not be needed and this bundle should be capable of starting after Jahia modules have been installed.
        Configuration moduleFileInstallConfiguration = findExistingFileInstallConfiguration(configurationAdmin);
        if (moduleFileInstallConfiguration == null) {
            try {
                moduleFileInstallConfiguration = configurationAdmin.createFactoryConfiguration("org.apache.felix.fileinstall");
                Dictionary<String, Object> properties = moduleFileInstallConfiguration.getProperties();
                if (properties == null) {
                    properties = new Hashtable<String, Object>();
                }
                Properties felixProperties = ((Properties) SpringContextSingleton.getBean("felixFileinstallProperties"));
                for (Map.Entry<Object, Object> entry : felixProperties.entrySet()) {
                    String key = entry.getKey().toString();
                    if (key.startsWith("felix.fileinstall")) {
                        properties.put(key, entry.getValue());
                    }
                }
                moduleFileInstallConfiguration.setBundleLocation(null);
                moduleFileInstallConfiguration.update(properties);
            } catch (IOException e) {
                logger.error("Cannot update fileinstall configuration",e);
            }
        }
    }

    private void unregisterFileInstallConfiguration(ConfigurationAdmin configurationAdmin) {
        Configuration moduleFileInstallConfiguration = findExistingFileInstallConfiguration(configurationAdmin);
        if (moduleFileInstallConfiguration != null) {
            try {
                moduleFileInstallConfiguration.delete();
            } catch (IOException e) {
                logger.error("Cannot delete fileinstall configuration",e);
            }
        }
    }

    private Configuration findExistingFileInstallConfiguration(ConfigurationAdmin configurationAdmin) {
        Configuration moduleFileInstallConfiguration = null;
        try {
            Configuration[] existingConfigurations = configurationAdmin.listConfigurations("(service.factoryPid=org.apache.felix.fileinstall)");
            if (existingConfigurations != null) {
                for (Configuration existingConfiguration : existingConfigurations) {
                    String fileInstallDir = (String) existingConfiguration.getProperties().get("felix.fileinstall.dir");
                    if (fileInstallDir.contains("digital-factory-data") && fileInstallDir.contains("modules")) {
                        moduleFileInstallConfiguration = existingConfiguration;
                    }
                }
            }
        } catch (InvalidSyntaxException | IOException e) {
            logger.error("Cannot get fileinstall configurations",e);
        }
        return moduleFileInstallConfiguration;
    }

    private void registerLegacyTransformer(BundleContext context) throws Exception {
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("url.handler.protocol", LEGACY_HANDLER_PREFIX);
        context.registerService(URLStreamHandlerService.class, new AbstractURLStreamHandlerService() {
            @Override
            public URLConnection openConnection(URL url) throws IOException {
                return new URLConnection(url) {
                    @Override
                    public void connect() throws IOException {
                        // Do nothing
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();

                        ZipInputStream zis = new ZipInputStream(new URL(url.getFile()).openConnection().getInputStream());
                        ZipOutputStream zos = new ZipOutputStream(out);

                        ZipEntry zipEntry;
                        while ((zipEntry = zis.getNextEntry()) != null) {
                            zos.putNextEntry(new ZipEntry(zipEntry.getName()));
                            if (zipEntry.getName().equals("META-INF/MANIFEST.MF")) {
                                addDependsCapabilitiesToManifest(zis, zos);
                            } else {
                                IOUtils.copy(zis, zos);
                            }
                        }
                        zos.close();
                        return new ByteArrayInputStream(out.toByteArray());
                    }
                };


            }

            private void addDependsCapabilitiesToManifest(InputStream is, OutputStream os) throws IOException {
                Manifest mf = new Manifest();
                mf.read(is);
                Attributes atts = mf.getMainAttributes();

                String provide = "";String bundleId = atts.getValue("Bundle-SymbolicName");
                String bundleName = atts.getValue("Bundle-Name");
                if (atts.containsKey(new Attributes.Name("Provide-Capability"))) {
                    provide = atts.getValue("Provide-Capability") + ",";
                }
                if (!provide.contains("com.jahia.modules.dependencies")) {
                    provide += "com.jahia.modules.dependencies;moduleIdentifier=\"" + bundleId + "\"";
                    provide += ",com.jahia.modules.dependencies;moduleIdentifier=\"" + bundleName + "\"";
                    atts.put(new Attributes.Name("Provide-Capability"), provide);
                }


                List<String> dependsList = new ArrayList<String>();
                String deps = atts.getValue("Jahia-Depends");
                if (StringUtils.isNotBlank(deps)) {
                    String[] dependencies = StringUtils.split(deps, ",");
                    for (String dependency : dependencies) {
                        dependsList.add(dependency.trim());
                    }
                }

                if (!dependsList.contains("default")
                        && !dependsList.contains("Default Jahia Templates")
                        && !ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithNoDefaultDependency()
                        .contains(bundleId)) {
                    dependsList.add("default");
                }

                if (!dependsList.isEmpty()) {
                    String require = "";
                    if (atts.containsKey(new Attributes.Name("Require-Capability"))) {
                        require = atts.getValue("Require-Capability") + ",";
                    }
                    if (!require.contains("com.jahia.modules.dependencies")) {
                        for (String depend : dependsList) {
                            require += "com.jahia.modules.dependencies;filter:=\"(moduleIdentifier=" + depend + ")\",";
                        }
                        require = StringUtils.substringBeforeLast(require, ",");
                        atts.put(new Attributes.Name("Require-Capability"), require);
                    }
                }
                mf.write(os);
            }
        }, props);

        context.registerService(new String[]{ArtifactUrlTransformer.class.getName()}, new ArtifactUrlTransformer() {
            @Override
            public URL transform(URL url) throws Exception {
                return new URL(LEGACY_HANDLER_PREFIX, null, url.toString());
            }

            @Override
            public boolean canHandle(File file) {
                return true;
            }
        }, null);

    }

}
