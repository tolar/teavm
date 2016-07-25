/*
 *  Copyright 2016 vtolar.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.security;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.PrivilegedAction;

import org.teavm.classlib.java.io.TBufferedInputStream;
import org.teavm.classlib.java.io.TFile;
import org.teavm.classlib.java.io.TFileInputStream;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.reflect.TField;
import org.teavm.classlib.java.net.TURL;
import org.teavm.classlib.java.util.TEnumeration;
import org.teavm.classlib.java.util.TLinkedHashSet;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.java.util.TProperties;
import org.teavm.classlib.sun.security.util.TPropertyExpander;

public final class TSecurity {



    /* The java.security properties */
    private static TProperties props;

    // An element in the cache
    private static class ProviderProperty {
        String className;
        TProvider provider;
    }

    static {
        // doPrivileged here because there are multiple
        // things in initialize that might require privs.
        // (the FileInputStream call and the File.exists call,
        // the securityPropFile call, etc)
        TAccessController.doPrivileged(new TPrivilegedAction<Void>() {
            public Void run() {
                initialize();
                return null;
            }
        });
    }

    private static void initialize() {
        props = new TProperties();
        boolean loadedProps = false;
        boolean overrideAll = false;

        // first load the system properties file
        // to determine the value of security.overridePropertiesFile
        TFile propFile = securityPropFile("java.security");
        if (propFile.exists()) {
            TInputStream is = null;
            try {
                TFileInputStream fis = new FileInputStream(propFile);
                is = new BufferedInputStream(fis);
                props.load(is);
                loadedProps = true;


            } catch (TIOException e) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioe) {

                    }
                }
            }
        }

        if ("true".equalsIgnoreCase(props.getProperty
                ("security.overridePropertiesFile"))) {

            String extraPropFile = System.getProperty
                    ("java.security.properties");
            if (extraPropFile != null && extraPropFile.startsWith("=")) {
                overrideAll = true;
                extraPropFile = extraPropFile.substring(1);
            }

            if (overrideAll) {
                props = new TProperties();
                if (sdebug != null) {
                    sdebug.println
                            ("overriding other security properties files!");
                }
            }

            // now load the user-specified file so its values
            // will win if they conflict with the earlier values
            if (extraPropFile != null) {
                TBufferedInputStream bis = null;
                try {
                    TURL propURL;

                    extraPropFile = TPropertyExpander.expand(extraPropFile);
                    propFile = new TFile(extraPropFile);
                    if (propFile.exists()) {
                        propURL = new TURL
                                ("file:" + propFile.getCanonicalPath());
                    } else {
                        propURL = new TURL(extraPropFile);
                    }
                    bis = new TBufferedInputStream(propURL.openStream());
                    props.load(bis);
                    loadedProps = true;

                } catch (Exception e) {

                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (TIOException ioe) {

                        }
                    }
                }
            }
        }

        if (!loadedProps) {
            initializeStatic();

        }

    }

    /*
     * Initialize to default values, if <java.home>/lib/java.security
     * is not found.
     */
    private static void initializeStatic() {
        props.put("security.provider.1", "sun.security.provider.Sun");
        props.put("security.provider.2", "sun.security.rsa.SunRsaSign");
        props.put("security.provider.3", "com.sun.net.ssl.internal.ssl.Provider");
        props.put("security.provider.4", "com.sun.crypto.provider.SunJCE");
        props.put("security.provider.5", "sun.security.jgss.SunProvider");
        props.put("security.provider.6", "com.sun.security.sasl.Provider");
    }

    /**
     * Don't let anyone instantiate this.
     */
    private TSecurity() {
    }

    private static TFile securityPropFile(String filename) {
        // maybe check for a system property which will specify where to
        // look. Someday.
        String sep = TFile.separator;
        return new TFile(System.getProperty("java.home") + sep + "lib" + sep +
                "security" + sep + filename);
    }

    /**
     * Looks up providers, and returns the property (and its associated
     * provider) mapping the key, if any.
     * The order in which the providers are looked up is the
     * provider-preference order, as specificed in the security
     * properties file.
     */
    private static TSecurity.ProviderProperty getProviderProperty(String key) {
        TSecurity.ProviderProperty entry = null;

        TList<TProvider> providers = TProviders.getProviderList().providers();
        for (int i = 0; i < providers.size(); i++) {

            String matchKey = null;
            TProvider prov = providers.get(i);
            String prop = prov.getProperty(key);

            if (prop == null) {
                // Is there a match if we do a case-insensitive property name
                // comparison? Let's try ...
                for (TEnumeration<Object> e = prov.keys();
                     e.hasMoreElements() && prop == null; ) {
                    matchKey = (String)e.nextElement();
                    if (key.equalsIgnoreCase(matchKey)) {
                        prop = prov.getProperty(matchKey);
                        break;
                    }
                }
            }

            if (prop != null) {
                TSecurity.ProviderProperty newEntry = new TSecurity.ProviderProperty();
                newEntry.className = prop;
                newEntry.provider = prov;
                return newEntry;
            }
        }

        return entry;
    }

    /**
     * Returns the property (if any) mapping the key for the given provider.
     */
    private static String getProviderProperty(String key, TProvider provider) {
        String prop = provider.getProperty(key);
        if (prop == null) {
            // Is there a match if we do a case-insensitive property name
            // comparison? Let's try ...
            for (TEnumeration<Object> e = provider.keys();
                 e.hasMoreElements() && prop == null; ) {
                String matchKey = (String)e.nextElement();
                if (key.equalsIgnoreCase(matchKey)) {
                    prop = provider.getProperty(matchKey);
                    break;
                }
            }
        }
        return prop;
    }

    /**
     * Gets a specified property for an algorithm. The algorithm name
     * should be a standard name. See the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * One possible use is by specialized algorithm parsers, which may map
     * classes to algorithms which they understand (much like Key parsers
     * do).
     *
     * @param algName the algorithm name.
     *
     * @param propName the name of the property to get.
     *
     * @return the value of the specified property.
     *
     * @deprecated This method used to return the value of a proprietary
     * property in the master file of the "SUN" Cryptographic Service
     * Provider in order to determine how to parse algorithm-specific
     * parameters. Use the new provider-based and algorithm-independent
     * {@code AlgorithmParameters} and {@code KeyFactory} engine
     * classes (introduced in the J2SE version 1.2 platform) instead.
     */
    @Deprecated
    public static String getAlgorithmProperty(String algName,
            String propName) {
        TSecurity.ProviderProperty entry = getProviderProperty("Alg." + propName
                + "." + algName);
        if (entry != null) {
            return entry.className;
        } else {
            return null;
        }
    }

    /**
     * Adds a new provider, at a specified position. The position is
     * the preference order in which providers are searched for
     * requested algorithms.  The position is 1-based, that is,
     * 1 is most preferred, followed by 2, and so on.
     *
     * <p>If the given provider is installed at the requested position,
     * the provider that used to be at that position, and all providers
     * with a position greater than {@code position}, are shifted up
     * one position (towards the end of the list of installed providers).
     *
     * <p>A provider cannot be added if it is already installed.
     *
     * <p>If there is a security manager, the
     * {@link java.lang.SecurityManager#checkSecurityAccess} method is called
     * with the {@code "insertProvider"} permission target name to see if
     * it's ok to add a new provider. If this permission check is denied,
     * {@code checkSecurityAccess} is called again with the
     * {@code "insertProvider."+provider.getName()} permission target name. If
     * both checks are denied, a {@code SecurityException} is thrown.
     *
     * @param provider the provider to be added.
     *
     * @param position the preference position that the caller would
     * like for this provider.
     *
     * @return the actual preference position in which the provider was
     * added, or -1 if the provider was not added because it is
     * already installed.
     *
     * @throws  NullPointerException if provider is null
     * @throws  SecurityException
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkSecurityAccess} method
     *          denies access to add a new provider
     *
     * @see #getProvider
     * @see #removeProvider
     * @see java.security.SecurityPermission
     */
    public static synchronized int insertProviderAt(TProvider provider,
            int position) {
        String providerName = provider.getName();
        checkInsertProvider(providerName);
        TProviderList list = TProviders.getFullProviderList();
        TProviderList newList = TProviderList.insertAt(list, provider, position - 1);
        if (list == newList) {
            return -1;
        }
        TProviders.setProviderList(newList);
        return newList.getIndex(providerName) + 1;
    }

    /**
     * Adds a provider to the next position available.
     *
     * <p>If there is a security manager, the
     * {@link java.lang.SecurityManager#checkSecurityAccess} method is called
     * with the {@code "insertProvider"} permission target name to see if
     * it's ok to add a new provider. If this permission check is denied,
     * {@code checkSecurityAccess} is called again with the
     * {@code "insertProvider."+provider.getName()} permission target name. If
     * both checks are denied, a {@code SecurityException} is thrown.
     *
     * @param provider the provider to be added.
     *
     * @return the preference position in which the provider was
     * added, or -1 if the provider was not added because it is
     * already installed.
     *
     * @throws  NullPointerException if provider is null
     * @throws  SecurityException
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkSecurityAccess} method
     *          denies access to add a new provider
     *
     * @see #getProvider
     * @see #removeProvider
     * @see java.security.SecurityPermission
     */
    public static int addProvider(Provider provider) {
        /*
         * We can't assign a position here because the statically
         * registered providers may not have been installed yet.
         * insertProviderAt() will fix that value after it has
         * loaded the static providers.
         */
        return insertProviderAt(provider, 0);
    }

    /**
     * Removes the provider with the specified name.
     *
     * <p>When the specified provider is removed, all providers located
     * at a position greater than where the specified provider was are shifted
     * down one position (towards the head of the list of installed
     * providers).
     *
     * <p>This method returns silently if the provider is not installed or
     * if name is null.
     *
     * <p>First, if there is a security manager, its
     * {@code checkSecurityAccess}
     * method is called with the string {@code "removeProvider."+name}
     * to see if it's ok to remove the provider.
     * If the default implementation of {@code checkSecurityAccess}
     * is used (i.e., that method is not overriden), then this will result in
     * a call to the security manager's {@code checkPermission} method
     * with a {@code SecurityPermission("removeProvider."+name)}
     * permission.
     *
     * @param name the name of the provider to remove.
     *
     * @throws  SecurityException
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkSecurityAccess} method
     *          denies
     *          access to remove the provider
     *
     * @see #getProvider
     * @see #addProvider
     */
    public static synchronized void removeProvider(String name) {
        check("removeProvider." + name);
        ProviderList list = Providers.getFullProviderList();
        ProviderList newList = ProviderList.remove(list, name);
        Providers.setProviderList(newList);
    }

    /**
     * Returns an array containing all the installed providers. The order of
     * the providers in the array is their preference order.
     *
     * @return an array of all the installed providers.
     */
    public static TProvider[] getProviders() {
        return Providers.getFullProviderList().toArray();
    }

    /**
     * Returns the provider installed with the specified name, if
     * any. Returns null if no provider with the specified name is
     * installed or if name is null.
     *
     * @param name the name of the provider to get.
     *
     * @return the provider of the specified name.
     *
     * @see #removeProvider
     * @see #addProvider
     */
    public static Provider getProvider(String name) {
        return Providers.getProviderList().getProvider(name);
    }

    /**
     * Returns an array containing all installed providers that satisfy the
     * specified selection criterion, or null if no such providers have been
     * installed. The returned providers are ordered
     * according to their
     * {@linkplain #insertProviderAt(java.security.Provider, int) preference order}.
     *
     * <p> A cryptographic service is always associated with a particular
     * algorithm or type. For example, a digital signature service is
     * always associated with a particular algorithm (e.g., DSA),
     * and a CertificateFactory service is always associated with
     * a particular certificate type (e.g., X.509).
     *
     * <p>The selection criterion must be specified in one of the following two
     * formats:
     * <ul>
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>}</i>
     * <p> The cryptographic service name must not contain any dots.
     * <p> A
     * provider satisfies the specified selection criterion iff the provider
     * implements the
     * specified algorithm or type for the specified cryptographic service.
     * <p> For example, "CertificateFactory.X.509"
     * would be satisfied by any provider that supplied
     * a CertificateFactory implementation for X.509 certificates.
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>
     * <attribute_name>:<attribute_value>}</i>
     * <p> The cryptographic service name must not contain any dots. There
     * must be one or more space characters between the
     * <i>{@literal <algorithm_or_type>}</i> and the
     * <i>{@literal <attribute_name>}</i>.
     *  <p> A provider satisfies this selection criterion iff the
     * provider implements the specified algorithm or type for the specified
     * cryptographic service and its implementation meets the
     * constraint expressed by the specified attribute name/value pair.
     * <p> For example, "Signature.SHA1withDSA KeySize:1024" would be
     * satisfied by any provider that implemented
     * the SHA1withDSA signature algorithm with a keysize of 1024 (or larger).
     *
     * </ul>
     *
     * <p> See the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard cryptographic service names, standard
     * algorithm names and standard attribute names.
     *
     * @param filter the criterion for selecting
     * providers. The filter is case-insensitive.
     *
     * @return all the installed providers that satisfy the selection
     * criterion, or null if no such providers have been installed.
     *
     * @throws InvalidParameterException
     *         if the filter is not in the required format
     * @throws NullPointerException if filter is null
     *
     * @see #getProviders(java.util.Map)
     * @since 1.3
     */
    public static Provider[] getProviders(String filter) {
        String key = null;
        String value = null;
        int index = filter.indexOf(':');

        if (index == -1) {
            key = filter;
            value = "";
        } else {
            key = filter.substring(0, index);
            value = filter.substring(index + 1);
        }

        Hashtable<String, String> hashtableFilter = new Hashtable<>(1);
        hashtableFilter.put(key, value);

        return (getProviders(hashtableFilter));
    }

    /**
     * Returns an array containing all installed providers that satisfy the
     * specified* selection criteria, or null if no such providers have been
     * installed. The returned providers are ordered
     * according to their
     * {@linkplain #insertProviderAt(java.security.Provider, int)
     * preference order}.
     *
     * <p>The selection criteria are represented by a map.
     * Each map entry represents a selection criterion.
     * A provider is selected iff it satisfies all selection
     * criteria. The key for any entry in such a map must be in one of the
     * following two formats:
     * <ul>
     * <li> <i>{@literal <crypto_service>.<algorithm_or_type>}</i>
     * <p> The cryptographic service name must not contain any dots.
     * <p> The value associated with the key must be an empty string.
     * <p> A provider
     * satisfies this selection criterion iff the provider implements the
     * specified algorithm or type for the specified cryptographic service.
     * <li>  <i>{@literal <crypto_service>}.
     * {@literal <algorithm_or_type> <attribute_name>}</i>
     * <p> The cryptographic service name must not contain any dots. There
     * must be one or more space characters between the
     * <i>{@literal <algorithm_or_type>}</i>
     * and the <i>{@literal <attribute_name>}</i>.
     * <p> The value associated with the key must be a non-empty string.
     * A provider satisfies this selection criterion iff the
     * provider implements the specified algorithm or type for the specified
     * cryptographic service and its implementation meets the
     * constraint expressed by the specified attribute name/value pair.
     * </ul>
     *
     * <p> See the <a href=
     * "../../../technotes/guides/security/StandardNames.html">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard cryptographic service names, standard
     * algorithm names and standard attribute names.
     *
     * @param filter the criteria for selecting
     * providers. The filter is case-insensitive.
     *
     * @return all the installed providers that satisfy the selection
     * criteria, or null if no such providers have been installed.
     *
     * @throws InvalidParameterException
     *         if the filter is not in the required format
     * @throws NullPointerException if filter is null
     *
     * @see #getProviders(java.lang.String)
     * @since 1.3
     */
    public static Provider[] getProviders(TMap<String,String> filter) {
        // Get all installed providers first.
        // Then only return those providers who satisfy the selection criteria.
        TProvider[] allProviders = TSecurity.getProviders();
        Set<String> keySet = filter.keySet();
        LinkedHashSet<Provider> candidates = new LinkedHashSet<>(5);

        // Returns all installed providers
        // if the selection criteria is null.
        if ((keySet == null) || (allProviders == null)) {
            return allProviders;
        }

        boolean firstSearch = true;

        // For each selection criterion, remove providers
        // which don't satisfy the criterion from the candidate set.
        for (Iterator<String> ite = keySet.iterator(); ite.hasNext(); ) {
            String key = ite.next();
            String value = filter.get(key);

            LinkedHashSet<Provider> newCandidates = getAllQualifyingCandidates(key, value,
                    allProviders);
            if (firstSearch) {
                candidates = newCandidates;
                firstSearch = false;
            }

            if ((newCandidates != null) && !newCandidates.isEmpty()) {
                // For each provider in the candidates set, if it
                // isn't in the newCandidate set, we should remove
                // it from the candidate set.
                for (Iterator<Provider> cansIte = candidates.iterator();
                     cansIte.hasNext(); ) {
                    Provider prov = cansIte.next();
                    if (!newCandidates.contains(prov)) {
                        cansIte.remove();
                    }
                }
            } else {
                candidates = null;
                break;
            }
        }

        if ((candidates == null) || (candidates.isEmpty()))
            return null;

        Object[] candidatesArray = candidates.toArray();
        TProvider[] result = new TProvider[candidatesArray.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = (TProvider)candidatesArray[i];
        }

        return result;
    }

    // Map containing cached Spi Class objects of the specified type
    private static final TMap<String, Class<?>> spiMap =
            new TConcurrentHashMap<>();

    /**
     * Return the Class object for the given engine type
     * (e.g. "MessageDigest"). Works for Spis in the java.security package
     * only.
     */
    private static Class<?> getSpiClass(String type) {
        Class<?> clazz = spiMap.get(type);
        if (clazz != null) {
            return clazz;
        }
        try {
            clazz = Class.forName("java.security." + type + "Spi");
            spiMap.put(type, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Spi class not found", e);
        }
    }

    /*
     * Returns an array of objects: the first object in the array is
     * an instance of an implementation of the requested algorithm
     * and type, and the second object in the array identifies the provider
     * of that implementation.
     * The {@code provider} argument can be null, in which case all
     * configured providers will be searched in order of preference.
     */
    static Object[] getImpl(String algorithm, String type, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            return GetInstance.getInstance
                    (type, getSpiClass(type), algorithm).toArray();
        } else {
            return GetInstance.getInstance
                    (type, getSpiClass(type), algorithm, provider).toArray();
        }
    }

    static Object[] getImpl(String algorithm, String type, String provider,
            Object params) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        if (provider == null) {
            return GetInstance.getInstance
                    (type, getSpiClass(type), algorithm, params).toArray();
        } else {
            return GetInstance.getInstance
                    (type, getSpiClass(type), algorithm, params, provider).toArray();
        }
    }

    /*
     * Returns an array of objects: the first object in the array is
     * an instance of an implementation of the requested algorithm
     * and type, and the second object in the array identifies the provider
     * of that implementation.
     * The {@code provider} argument cannot be null.
     */
    static Object[] getImpl(String algorithm, String type, Provider provider)
            throws NoSuchAlgorithmException {
        return GetInstance.getInstance
                (type, getSpiClass(type), algorithm, provider).toArray();
    }

    static Object[] getImpl(String algorithm, String type, Provider provider,
            Object params) throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        return GetInstance.getInstance
                (type, getSpiClass(type), algorithm, params, provider).toArray();
    }

    /**
     * Gets a security property value.
     *
     * <p>First, if there is a security manager, its
     * {@code checkPermission}  method is called with a
     * {@code java.security.SecurityPermission("getProperty."+key)}
     * permission to see if it's ok to retrieve the specified
     * security property value..
     *
     * @param key the key of the property being retrieved.
     *
     * @return the value of the security property corresponding to key.
     *
     * @throws  SecurityException
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkPermission} method
     *          denies
     *          access to retrieve the specified security property value
     * @throws  NullPointerException is key is null
     *
     * @see #setProperty
     * @see java.security.SecurityPermission
     */
    public static String getProperty(String key) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SecurityPermission("getProperty."+
                    key));
        }
        String name = props.getProperty(key);
        if (name != null)
            name = name.trim(); // could be a class name with trailing ws
        return name;
    }

    /**
     * Sets a security property value.
     *
     * <p>First, if there is a security manager, its
     * {@code checkPermission} method is called with a
     * {@code java.security.SecurityPermission("setProperty."+key)}
     * permission to see if it's ok to set the specified
     * security property value.
     *
     * @param key the name of the property to be set.
     *
     * @param datum the value of the property to be set.
     *
     * @throws  SecurityException
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkPermission} method
     *          denies access to set the specified security property value
     * @throws  NullPointerException if key or datum is null
     *
     * @see #getProperty
     * @see java.security.SecurityPermission
     */
    public static void setProperty(String key, String datum) {
        check("setProperty."+key);
        props.put(key, datum);
        invalidateSMCache(key);  /* See below. */
    }

    /*
     * Implementation detail:  If the property we just set in
     * setProperty() was either "package.access" or
     * "package.definition", we need to signal to the SecurityManager
     * class that the value has just changed, and that it should
     * invalidate it's local cache values.
     *
     * Rather than create a new API entry for this function,
     * we use reflection to set a private variable.
     */
    private static void invalidateSMCache(String key) {

        final boolean pa = key.equals("package.access");
        final boolean pd = key.equals("package.definition");

        if (pa || pd) {
            TAccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        /* Get the class via the bootstrap class loader. */
                        Class<?> cl = Class.forName(
                                "java.lang.SecurityManager", false, null);
                        TField f = null;
                        boolean accessible = false;

                        if (pa) {
                            f = cl.getDeclaredField("packageAccessValid");
                            accessible = f.isAccessible();
                            f.setAccessible(true);
                        } else {
                            f = cl.getDeclaredField("packageDefinitionValid");
                            accessible = f.isAccessible();
                            f.setAccessible(true);
                        }
                        f.setBoolean(f, false);
                        f.setAccessible(accessible);
                    }
                    catch (Exception e1) {
                        /* If we couldn't get the class, it hasn't
                         * been loaded yet.  If there is no such
                         * field, we shouldn't try to set it.  There
                         * shouldn't be a security execption, as we
                         * are loaded by boot class loader, and we
                         * are inside a doPrivileged() here.
                         *
                         * NOOP: don't do anything...
                         */
                    }
                    return null;
                }  /* run */
            });  /* PrivilegedAction */
        }  /* if */
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }

    private static void checkInsertProvider(String name) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkSecurityAccess("insertProvider");
            } catch (SecurityException se1) {
                try {
                    security.checkSecurityAccess("insertProvider." + name);
                } catch (SecurityException se2) {
                    // throw first exception, but add second to suppressed
                    se1.addSuppressed(se2);
                    throw se1;
                }
            }
        }
    }

    /*
    * Returns all providers who satisfy the specified
    * criterion.
    */
    private static TLinkedHashSet<TProvider> getAllQualifyingCandidates(
            String filterKey,
            String filterValue,
            TProvider[] allProviders) {
        String[] filterComponents = getFilterComponents(filterKey,
                filterValue);

        // The first component is the service name.
        // The second is the algorithm name.
        // If the third isn't null, that is the attrinute name.
        String serviceName = filterComponents[0];
        String algName = filterComponents[1];
        String attrName = filterComponents[2];

        return getProvidersNotUsingCache(serviceName, algName, attrName,
                filterValue, allProviders);
    }

    private static TLinkedHashSet<TProvider> getProvidersNotUsingCache(
            String serviceName,
            String algName,
            String attrName,
            String filterValue,
            TProvider[] allProviders) {
        TLinkedHashSet<TProvider> candidates = new TLinkedHashSet<>(5);
        for (int i = 0; i < allProviders.length; i++) {
            if (isCriterionSatisfied(allProviders[i], serviceName,
                    algName,
                    attrName, filterValue)) {
                candidates.add(allProviders[i]);
            }
        }
        return candidates;
    }

    /*
     * Returns true if the given provider satisfies
     * the selection criterion key:value.
     */
    private static boolean isCriterionSatisfied(TProvider prov,
            String serviceName,
            String algName,
            String attrName,
            String filterValue) {
        String key = serviceName + '.' + algName;

        if (attrName != null) {
            key += ' ' + attrName;
        }
        // Check whether the provider has a property
        // whose key is the same as the given key.
        String propValue = getProviderProperty(key, prov);

        if (propValue == null) {
            // Check whether we have an alias instead
            // of a standard name in the key.
            String standardName = getProviderProperty("Alg.Alias." +
                            serviceName + "." +
                            algName,
                    prov);
            if (standardName != null) {
                key = serviceName + "." + standardName;

                if (attrName != null) {
                    key += ' ' + attrName;
                }

                propValue = getProviderProperty(key, prov);
            }

            if (propValue == null) {
                // The provider doesn't have the given
                // key in its property list.
                return false;
            }
        }

        // If the key is in the format of:
        // <crypto_service>.<algorithm_or_type>,
        // there is no need to check the value.

        if (attrName == null) {
            return true;
        }

        // If we get here, the key must be in the
        // format of <crypto_service>.<algorithm_or_provider> <attribute_name>.
        if (isStandardAttr(attrName)) {
            return isConstraintSatisfied(attrName, filterValue, propValue);
        } else {
            return filterValue.equalsIgnoreCase(propValue);
        }
    }

    /*
     * Returns true if the attribute is a standard attribute;
     * otherwise, returns false.
     */
    private static boolean isStandardAttr(String attribute) {
        // For now, we just have two standard attributes:
        // KeySize and ImplementedIn.
        if (attribute.equalsIgnoreCase("KeySize"))
            return true;

        if (attribute.equalsIgnoreCase("ImplementedIn"))
            return true;

        return false;
    }

    /*
     * Returns true if the requested attribute value is supported;
     * otherwise, returns false.
     */
    private static boolean isConstraintSatisfied(String attribute,
            String value,
            String prop) {
        // For KeySize, prop is the max key size the
        // provider supports for a specific <crypto_service>.<algorithm>.
        if (attribute.equalsIgnoreCase("KeySize")) {
            int requestedSize = Integer.parseInt(value);
            int maxSize = Integer.parseInt(prop);
            if (requestedSize <= maxSize) {
                return true;
            } else {
                return false;
            }
        }

        // For Type, prop is the type of the implementation
        // for a specific <crypto service>.<algorithm>.
        if (attribute.equalsIgnoreCase("ImplementedIn")) {
            return value.equalsIgnoreCase(prop);
        }

        return false;
    }

    static String[] getFilterComponents(String filterKey, String filterValue) {
        int algIndex = filterKey.indexOf('.');

        if (algIndex < 0) {
            // There must be a dot in the filter, and the dot
            // shouldn't be at the beginning of this string.
            throw new InvalidParameterException("Invalid filter");
        }

        String serviceName = filterKey.substring(0, algIndex);
        String algName = null;
        String attrName = null;

        if (filterValue.length() == 0) {
            // The filterValue is an empty string. So the filterKey
            // should be in the format of <crypto_service>.<algorithm_or_type>.
            algName = filterKey.substring(algIndex + 1).trim();
            if (algName.length() == 0) {
                // There must be a algorithm or type name.
                throw new InvalidParameterException("Invalid filter");
            }
        } else {
            // The filterValue is a non-empty string. So the filterKey must be
            // in the format of
            // <crypto_service>.<algorithm_or_type> <attribute_name>
            int attrIndex = filterKey.indexOf(' ');

            if (attrIndex == -1) {
                // There is no attribute name in the filter.
                throw new InvalidParameterException("Invalid filter");
            } else {
                attrName = filterKey.substring(attrIndex + 1).trim();
                if (attrName.length() == 0) {
                    // There is no attribute name in the filter.
                    throw new InvalidParameterException("Invalid filter");
                }
            }

            // There must be an algorithm name in the filter.
            if ((attrIndex < algIndex) ||
                    (algIndex == attrIndex - 1)) {
                throw new InvalidParameterException("Invalid filter");
            } else {
                algName = filterKey.substring(algIndex + 1, attrIndex);
            }
        }

        String[] result = new String[3];
        result[0] = serviceName;
        result[1] = algName;
        result[2] = attrName;

        return result;
    }

    /**
     * Returns a Set of Strings containing the names of all available
     * algorithms or types for the specified Java cryptographic service
     * (e.g., Signature, MessageDigest, Cipher, Mac, KeyStore). Returns
     * an empty Set if there is no provider that supports the
     * specified service or if serviceName is null. For a complete list
     * of Java cryptographic services, please see the
     * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">Java
     * Cryptography Architecture API Specification &amp; Reference</a>.
     * Note: the returned set is immutable.
     *
     * @param serviceName the name of the Java cryptographic
     * service (e.g., Signature, MessageDigest, Cipher, Mac, KeyStore).
     * Note: this parameter is case-insensitive.
     *
     * @return a Set of Strings containing the names of all available
     * algorithms or types for the specified Java cryptographic service
     * or an empty set if no provider supports the specified service.
     *
     * @since 1.4
     **/
    public static Set<String> getAlgorithms(String serviceName) {

        if ((serviceName == null) || (serviceName.length() == 0) ||
                (serviceName.endsWith("."))) {
            return Collections.emptySet();
        }

        HashSet<String> result = new HashSet<>();
        Provider[] providers = java.security.Security.getProviders();

        for (int i = 0; i < providers.length; i++) {
            // Check the keys for each provider.
            for (Enumeration<Object> e = providers[i].keys();
                 e.hasMoreElements(); ) {
                String currentKey =
                        ((String)e.nextElement()).toUpperCase(Locale.ENGLISH);
                if (currentKey.startsWith(
                        serviceName.toUpperCase(Locale.ENGLISH))) {
                    // We should skip the currentKey if it contains a
                    // whitespace. The reason is: such an entry in the
                    // provider property contains attributes for the
                    // implementation of an algorithm. We are only interested
                    // in entries which lead to the implementation
                    // classes.
                    if (currentKey.indexOf(" ") < 0) {
                        result.add(currentKey.substring(
                                serviceName.length() + 1));
                    }
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
