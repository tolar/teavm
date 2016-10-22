/*
 *  Copyright 2016 vasek.
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

import static java.util.Locale.ENGLISH;
import java.lang.ref.Reference;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.util.TArrayList;
import org.teavm.classlib.java.util.TCollections;
import org.teavm.classlib.java.util.THashMap;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TLinkedHashMap;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.java.util.TProperties;

/**
 * Created by vasek on 29. 6. 2016.
 */
public abstract class TProvider extends TProperties {

    private transient boolean initialized;

    // ServiceKey from previous getService() call
    // by re-using it if possible we avoid allocating a new object
    // and the toUpperCase() call.
    // re-use will occur e.g. as the framework traverses the provider
    // list and queries each provider with the same values until it finds
    // a matching service
    private static volatile TProvider.ServiceKey previousKey =
            new TProvider.ServiceKey(TString.wrap(""), TString.wrap(""), false);

    // Map<ServiceKey,Service>
    // used for services added via putService(), initialized on demand
    private transient Map<TProvider.ServiceKey,TProvider.Service> serviceMap;
    private TString name;

    protected TProvider(TString name, double version, TString info) {
        this.name = name;
        initialized = true;
    }


    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException();
        }
    }

    public synchronized TProvider.Service getService(TString type, TString algorithm) {
        checkInitialized();
        // avoid allocating a new key object if possible
        TProvider.ServiceKey key = previousKey;
        if (key.matches(type, algorithm) == false) {
            key = new TProvider.ServiceKey(type, algorithm, false);
            previousKey = key;
        }
        if (serviceMap != null) {
            TProvider.Service service = serviceMap.get(key);
            if (service != null) {
                return service;
            }
        }
        ensureLegacyParsed();
        return (legacyMap != null) ? legacyMap.get(key) : null;
    }

    // legacy properties changed since last call to any services method?
    private transient boolean legacyChanged;

    // Map<ServiceKey,Service>
    // used for services added via legacy methods, init on demand
    private transient TMap<TProvider.ServiceKey,TProvider.Service> legacyMap;

    // Map<String,String>
    private transient TMap<TString,TString> legacyStrings;

    // Set<Service>
    // Unmodifiable set of all services. Initialized on demand.
    private transient Set<TProvider.Service> serviceSet;

    private final static String ALIAS_PREFIX = "Alg.Alias.";
    private final static String ALIAS_PREFIX_LOWER = "alg.alias.";
    private final static int ALIAS_LENGTH = ALIAS_PREFIX.length();

    private TString[] getTypeAndAlgorithm(TString key) {
        int i = key.indexOf(TString.wrap("."));
        if (i < 1) {
            return null;
        }
        TString type = key.substring(0, i);
        TString alg = key.substring(i + 1);
        return new TString[] {type, alg};
    }

    private void parseLegacyPut(TString name, TString value) {
        if (name.toLowerCase(ENGLISH).startsWith(TString.wrap(ALIAS_PREFIX_LOWER))) {
            // e.g. put("Alg.Alias.MessageDigest.SHA", "SHA-1");
            // aliasKey ~ MessageDigest.SHA
            TString stdAlg = value;
            TString aliasKey = name.substring(ALIAS_LENGTH);
            TString[] typeAndAlg = getTypeAndAlgorithm(aliasKey);
            if (typeAndAlg == null) {
                return;
            }
            TString type = getEngineName(TString.wrap(typeAndAlg[0].toString()));
            TString aliasAlg = typeAndAlg[1].intern();
            TProvider.ServiceKey key = new TProvider.ServiceKey(type, stdAlg, true);
            TProvider.Service s = legacyMap.get(key);
            if (s == null) {
                s = new TProvider.Service(this);
                s.type = type;
                s.algorithm = stdAlg;
                legacyMap.put(key, s);
            }
            legacyMap.put(new TProvider.ServiceKey(type, stdAlg, true), s);
            s.addAlias(aliasAlg);
        } else {
            TString[] typeAndAlg = getTypeAndAlgorithm(name);
            if (typeAndAlg == null) {
                return;
            }
            int i = typeAndAlg[1].indexOf(' ');
            if (i == -1) {
                // e.g. put("MessageDigest.SHA-1", "sun.security.provider.SHA");
                TString type = getEngineName(TString.wrap((typeAndAlg[0]).toString()));
                TString stdAlg = typeAndAlg[1].intern();
                TString className = value;
                TProvider.ServiceKey key = new TProvider.ServiceKey(TString.wrap(type.toString()), TString.wrap(stdAlg.toString()), true);
                TProvider.Service s = legacyMap.get(key);
                if (s == null) {
                    s = new TProvider.Service(this);
                    s.type = TString.wrap(type.toString());
                    s.algorithm = TString.wrap(stdAlg.toString());
                    legacyMap.put(key, s);
                }
                s.className = className;
            } else { // attribute
                // e.g. put("MessageDigest.SHA-1 ImplementedIn", "Software");
                TString attributeValue = value;
                String type = getEngineName(TString.wrap(typeAndAlg[0].toString())).toString();
                TString attributeString = typeAndAlg[1];
                TString stdAlg = attributeString.substring(0, i).intern();
                TString attributeName = attributeString.substring(i + 1);
                // kill additional spaces
                while (attributeName.startsWith(TString.wrap(" "))) {
                    attributeName = attributeName.substring(1);
                }
                attributeName = attributeName.intern();
                TProvider.ServiceKey key = new TProvider.ServiceKey(TString.wrap(type), TString.wrap(stdAlg.toString()), true);
                TProvider.Service s = legacyMap.get(key);
                if (s == null) {
                    s = new TProvider.Service(this);
                    s.type = TString.wrap(type);
                    s.algorithm = TString.wrap(stdAlg.toString());
                    legacyMap.put(key, s);
                }
                s.addAttribute(attributeName, attributeValue);
            }
        }
    }

    private static class EngineDescription {
        final String name;
        final boolean supportsParameter;
        final String constructorParameterClassName;
        private volatile Class<?> constructorParameterClass;

        EngineDescription(String name, boolean sp, String paramName) {
            this.name = name;
            this.supportsParameter = sp;
            this.constructorParameterClassName = paramName;
        }
        Class<?> getConstructorParameterClass() throws ClassNotFoundException {
            Class<?> clazz = constructorParameterClass;
            if (clazz == null) {
                clazz = Class.forName(constructorParameterClassName);
                constructorParameterClass = clazz;
            }
            return clazz;
        }
    }


    private static TString getEngineName(TString s) {
        // try original case first, usually correct
        TProvider.EngineDescription e = knownEngines.get(s);
        if (e == null) {
            e = knownEngines.get(s.toLowerCase(ENGLISH));
        }
        return (e == null) ? s : TString.wrap(e.name);
    }

    private static final Map<String,TProvider.EngineDescription> knownEngines;

    private static void addEngine(String name, boolean sp, String paramName) {
        TProvider.EngineDescription ed = new TProvider.EngineDescription(name, sp, paramName);
        // also index by canonical name to avoid toLowerCase() for some lookups
        knownEngines.put(name.toLowerCase(ENGLISH), ed);
        knownEngines.put(name, ed);
    }

    static {
        knownEngines = new HashMap<String,TProvider.EngineDescription>();
        // JCA
        addEngine("AlgorithmParameterGenerator",        false, null);
        addEngine("AlgorithmParameters",                false, null);
        addEngine("KeyFactory",                         false, null);
        addEngine("KeyPairGenerator",                   false, null);
        addEngine("TKeyStore",                           false, null);
        addEngine("MessageDigest",                      false, null);
        addEngine("SecureRandom",                       false, null);
        addEngine("Signature",                          true,  null);
        addEngine("CertificateFactory",                 false, null);
        addEngine("CertPathBuilder",                    false, null);
        addEngine("CertPathValidator",                  false, null);
        addEngine("CertStore",                          false,
                "java.security.cert.CertStoreParameters");
        // JCE
        addEngine("TCipher",                             true,  null);
        addEngine("ExemptionMechanism",                 false, null);
        addEngine("Mac",                                true,  null);
        addEngine("KeyAgreement",                       true,  null);
        addEngine("KeyGenerator",                       false, null);
        addEngine("SecretKeyFactory",                   false, null);
        // JSSE
        addEngine("KeyManagerFactory",                  false, null);
        addEngine("SSLContext",                         false, null);
        addEngine("TrustManagerFactory",                false, null);
        // JGSS
        addEngine("GssApiMechanism",                    false, null);
        // SASL
        addEngine("SaslClientFactory",                  false, null);
        addEngine("SaslServerFactory",                  false, null);
        // POLICY
        addEngine("Policy",                             false,
                "java.security.Policy$Parameters");
        // CONFIGURATION
        addEngine("Configuration",                      false,
                "javax.security.auth.login.Configuration$Parameters");
        // XML DSig
        addEngine("XMLSignatureFactory",                false, null);
        addEngine("KeyInfoFactory",                     false, null);
        addEngine("TransformService",                   false, null);
        // Smart Card I/O
        addEngine("TerminalFactory",                    false,
                "java.lang.Object");
    }



    private void ensureLegacyParsed() {
        if ((legacyChanged == false) || (legacyStrings == null)) {
            return;
        }
        serviceSet = null;
        if (legacyMap == null) {
            legacyMap = new TLinkedHashMap<ServiceKey,Service>();
        } else {
            legacyMap.clear();
        }
        for (TMap.Entry<TString,TString> entry : legacyStrings.entrySet()) {
            parseLegacyPut(entry.getKey(), entry.getValue());
        }
        removeInvalidServices(legacyMap);
        legacyChanged = false;
    }

    private void removeInvalidServices(TMap<TProvider.ServiceKey,TProvider.Service> map) {
        for (TIterator<TMap.Entry<ServiceKey, Service>> t =
             map.entrySet().iterator(); t.hasNext(); ) {
            TProvider.Service s = t.next().getValue();
            if (s.isValid() == false) {
                t.remove();
            }
        }
    }

    // Wrapped String that behaves in a case insensitive way for equals/hashCode
    private static class UString {
        final TString string;
        final TString lowerString;

        UString(TString s) {
            this.string = s;
            this.lowerString = s.toLowerCase(ENGLISH);
        }

        public int hashCode() {
            return lowerString.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TProvider.UString == false) {
                return false;
            }
            TProvider.UString other = (TProvider.UString)obj;
            return lowerString.equals(other.lowerString);
        }

        public String toString() {
            return string.toString();
        }
    }


    public static class Service {

        private TString type, algorithm, className;
        private final TProvider provider;
        private TList<TString> aliases;
        private TMap<UString,TString> attributes;

        // Reference to the cached implementation Class object
        private volatile Reference<Class<?>> classRef;

        // flag indicating whether this service has its attributes for
        // supportedKeyFormats or supportedKeyClasses set
        // if null, the values have not been initialized
        // if TRUE, at least one of supportedFormats/Classes is non null
        private volatile Boolean hasKeyAttributes;

        // supported encoding formats
        private String[] supportedFormats;

        // names of the supported key (super) classes
        private Class[] supportedClasses;

        // whether this service has been registered with the Provider
        private boolean registered;

        private static final Class<?>[] CLASS0 = new Class<?>[0];

        // this constructor and these methods are used for parsing
        // the legacy string properties.

        private Service(TProvider provider) {
            this.provider = provider;
            aliases = TCollections.<TString>emptyList();
            attributes = TCollections.<TProvider.UString,TString>emptyMap();
        }

        private boolean isValid() {
            return (type != null) && (algorithm != null) && (className != null);
        }

        private void addAlias(TString alias) {
            if (aliases.isEmpty()) {
                aliases = new TArrayList<TString>(2);
            }
            aliases.add(alias);
        }

        void addAttribute(TString type, TString value) {
            if (attributes.isEmpty()) {
                attributes = new THashMap<UString,TString>(8);
            }
            attributes.put(new TProvider.UString(type), value);
        }

        /**
         * Construct a new service.
         *
         * @param provider the provider that offers this service
         * @param type the type of this service
         * @param algorithm the algorithm name
         * @param className the name of the class implementing this service
         * @param aliases List of aliases or null if algorithm has no aliases
         * @param attributes Map of attributes or null if this implementation
         *                   has no attributes
         *
         * @throws NullPointerException if provider, type, algorithm, or
         * className is null
         */
        public Service(TProvider provider, String type, String algorithm,
                String className, TList<TString> aliases,
                TMap<String,String> attributes) {
            if ((provider == null) || (type == null) ||
                    (algorithm == null) || (className == null)) {
                throw new NullPointerException();
            }
            this.provider = provider;
            this.type = getEngineName(TString.wrap(type));
            this.algorithm = TString.wrap(algorithm);
            this.className = TString.wrap(className);
            if (aliases == null) {
                this.aliases = TCollections.<TString>emptyList();
            } else {
                this.aliases = new TArrayList<TString>(aliases);
            }
            if (attributes == null) {
                this.attributes = TCollections.<TProvider.UString,TString>emptyMap();
            } else {
                this.attributes = new THashMap<TProvider.UString,TString>();
                for (TMap.Entry<String,String> entry : attributes.entrySet()) {
                    this.attributes.put(new TProvider.UString(entry.getKey()), entry.getValue());
                }
            }
        }

        /**
         * Get the type of this service. For example, {@code MessageDigest}.
         *
         * @return the type of this service
         */
        public final String getType() {
            return type.toString();
        }

        /**
         * Return the name of the algorithm of this service. For example,
         * {@code SHA-1}.
         *
         * @return the algorithm of this service
         */
        public final String getAlgorithm() {
            return algorithm.toString();
        }

        /**
         * Return the Provider of this service.
         *
         * @return the Provider of this service
         */
        public final TProvider getProvider() {
            return provider;
        }

        /**
         * Return the name of the class implementing this service.
         *
         * @return the name of the class implementing this service
         */
        public final TString getClassName() {
            return className;
        }

        // internal only
        private final TList<TString> getAliases() {
            return aliases;
        }

        public String getName() {
            return name;
        }
        private String name;



        /**
         * Return the value of the specified attribute or null if this
         * attribute is not set for this Service.
         *
         * @param name the name of the requested attribute
         *
         * @return the value of the specified attribute or null if the
         *         attribute is not present
         *
         * @throws NullPointerException if name is null
         */
        public final TString getAttribute(TString name) {
            if (name == null) {
                throw new NullPointerException();
            }
            return attributes.get(new TProvider.UString(name));
        }

        /**
         * Return a new instance of the implementation described by this
         * service. The security provider framework uses this method to
         * construct implementations. Applications will typically not need
         * to call it.
         *
         * <p>The default implementation uses reflection to invoke the
         * standard constructor for this type of service.
         * Security providers can override this method to implement
         * instantiation in a different way.
         * For details and the values of constructorParameter that are
         * valid for the various types of services see the
         * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">
         * Java Cryptography Architecture API Specification &amp;
         * Reference</a>.
         *
         * @param constructorParameter the value to pass to the constructor,
         * or null if this type of service does not use a constructorParameter.
         *
         * @return a new implementation of this service
         *
         * @throws InvalidParameterException if the value of
         * constructorParameter is invalid for this type of service.
         * @throws NoSuchAlgorithmException if instantiation failed for
         * any other reason.
         */
//        public Object newInstance(Object constructorParameter)
//                throws TNoSuchAlgorithmException {
//            if (registered == false) {
//                if (provider.getService(type, algorithm) != this) {
//                    throw new TNoSuchAlgorithmException
//                            (TString.wrap("Service not registered with Provider "
//                                    + provider.getName() + ": "));
//                }
//                registered = true;
//            }
//            try {
//                TProvider.EngineDescription cap = knownEngines.get(type);
//                if (cap == null) {
//                    // unknown engine type, use generic code
//                    // this is the code path future for non-core
//                    // optional packages
//                    return newInstanceGeneric(constructorParameter);
//                }
//                if (cap.constructorParameterClassName == null) {
//                    if (constructorParameter != null) {
//                        throw new InvalidParameterException
//                                ("constructorParameter not used with " + type
//                                        + " engines");
//                    }
//                    Class<?> clazz = getImplClass();
//                    Class<?>[] empty = {};
//                    Constructor<?> con = clazz.getConstructor(empty);
//                    return con.newInstance();
//                } else {
//                    Class<?> paramClass = cap.getConstructorParameterClass();
//                    if (constructorParameter != null) {
//                        Class<?> argClass = constructorParameter.getClass();
//                        if (paramClass.isAssignableFrom(argClass) == false) {
//                            throw new InvalidParameterException
//                                    ("constructorParameter must be instanceof "
//                                            + cap.constructorParameterClassName.replace('$', '.')
//                                            + " for engine type " + type);
//                        }
//                    }
//                    Class<?> clazz = getImplClass();
//                    Constructor<?> cons = clazz.getConstructor(paramClass);
//                    return cons.newInstance(constructorParameter);
//                }
//            } catch (TNoSuchAlgorithmException e) {
//                throw e;
//            } catch (TException e) {
//                throw new TNoSuchAlgorithmException
//                        (TString.wrap("Error constructing implementation (algorithm: "
//                                + algorithm + ", provider: " + provider.getName()
//                                + ", class: " + className + ")"), e);
//            }
//        }
//
//        // return the implementation Class object for this service
//        private Class<?> getImplClass() throws NoSuchAlgorithmException {
//            try {
//                Reference<Class<?>> ref = classRef;
//                Class<?> clazz = (ref == null) ? null : ref.get();
//                if (clazz == null) {
//                    ClassLoader cl = provider.getClass().getClassLoader();
//                    if (cl == null) {
//                        clazz = Class.forName(className);
//                    } else {
//                        clazz = cl.loadClass(className);
//                    }
//                    if (!Modifier.isPublic(clazz.getModifiers())) {
//                        throw new NoSuchAlgorithmException
//                                ("class configured for " + type + " (provider: " +
//                                        provider.getName() + ") is not public.");
//                    }
//                    classRef = new WeakReference<Class<?>>(clazz);
//                }
//                return clazz;
//            } catch (ClassNotFoundException e) {
//                throw new NoSuchAlgorithmException
//                        ("class configured for " + type + " (provider: " +
//                                provider.getName() + ") cannot be found.", e);
//            }
//        }

        /**
         * Generic code path for unknown engine types. Call the
         * no-args constructor if constructorParameter is null, otherwise
         * use the first matching constructor.
         */
//        private Object newInstanceGeneric(Object constructorParameter)
//                throws Exception {
//            Class<?> clazz = getImplClass();
//            if (constructorParameter == null) {
//                // create instance with public no-arg constructor if it exists
//                try {
//                    Class<?>[] empty = {};
//                    Constructor<?> con = clazz.getConstructor(empty);
//                    return con.newInstance();
//                } catch (NoSuchMethodException e) {
//                    throw new NoSuchAlgorithmException("No public no-arg "
//                            + "constructor found in class " + className);
//                }
//            }
//            Class<?> argClass = constructorParameter.getClass();
//            Constructor[] cons = clazz.getConstructors();
//            // find first public constructor that can take the
//            // argument as parameter
//            for (Constructor<?> con : cons) {
//                Class<?>[] paramTypes = con.getParameterTypes();
//                if (paramTypes.length != 1) {
//                    continue;
//                }
//                if (paramTypes[0].isAssignableFrom(argClass) == false) {
//                    continue;
//                }
//                return con.newInstance(constructorParameter);
//            }
//            throw new NoSuchAlgorithmException("No public constructor matching "
//                    + argClass.getName() + " found in class " + className);
//        }

        /**
         * Test whether this Service can use the specified parameter.
         * Returns false if this service cannot use the parameter. Returns
         * true if this service can use the parameter, if a fast test is
         * infeasible, or if the status is unknown.
         *
         * <p>The security provider framework uses this method with
         * some types of services to quickly exclude non-matching
         * implementations for consideration.
         * Applications will typically not need to call it.
         *
         * <p>For details and the values of parameter that are valid for the
         * various types of services see the top of this class and the
         * <a href="../../../technotes/guides/security/crypto/CryptoSpec.html">
         * Java Cryptography Architecture API Specification &amp;
         * Reference</a>.
         * Security providers can override it to implement their own test.
         *
         * @param parameter the parameter to test
         *
         * @return false if this this service cannot use the specified
         * parameter; true if it can possibly use the parameter
         *
         * @throws InvalidParameterException if the value of parameter is
         * invalid for this type of service or if this method cannot be
         * used with this type of service
         */
        public boolean supportsParameter(Object parameter) {
            TProvider.EngineDescription cap = knownEngines.get(type);
            if (cap == null) {
                // unknown engine type, return true by default
                return true;
            }
            if (cap.supportsParameter == false) {
                throw new InvalidParameterException("supportsParameter() not "
                        + "used with " + type + " engines");
            }
            // allow null for keys without attributes for compatibility
            if ((parameter != null) && (parameter instanceof Key == false)) {
                throw new InvalidParameterException
                        ("Parameter must be instanceof Key for engine " + type);
            }
            if (hasKeyAttributes() == false) {
                return true;
            }
            if (parameter == null) {
                return false;
            }
            Key key = (Key)parameter;
            if (supportsKeyFormat(key)) {
                return true;
            }
            if (supportsKeyClass(key)) {
                return true;
            }
            return false;
        }

        /**
         * Return whether this service has its Supported* properties for
         * keys defined. Parses the attributes if not yet initialized.
         */
        private boolean hasKeyAttributes() {
            Boolean b = hasKeyAttributes;
            if (b == null) {
                synchronized (this) {
                    TString s;
                    s = getAttribute(TString.wrap("SupportedKeyFormats"));
                    if (s != null) {
                        supportedFormats = s.split("\\|");
                    }
                    s = getAttribute(TString.wrap("SupportedKeyClasses"));
                    if (s != null) {
                        String[] classNames = s.split("\\|");
                        List<Class<?>> classList =
                                new ArrayList<>(classNames.length);
                        for (String className : classNames) {
                            Class<?> clazz = getKeyClass(className);
                            if (clazz != null) {
                                classList.add(clazz);
                            }
                        }
                        supportedClasses = classList.toArray(CLASS0);
                    }
                    boolean bool = (supportedFormats != null)
                            || (supportedClasses != null);
                    b = Boolean.valueOf(bool);
                    hasKeyAttributes = b;
                }
            }
            return b.booleanValue();
        }

        // get the key class object of the specified name
        private Class<?> getKeyClass(String name) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                // ignore
            }
            try {
                ClassLoader cl = provider.getClass().getClassLoader();
                if (cl != null) {
                    return cl.loadClass(name);
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }
            return null;
        }

        private boolean supportsKeyFormat(Key key) {
            if (supportedFormats == null) {
                return false;
            }
            String format = key.getFormat();
            if (format == null) {
                return false;
            }
            for (String supportedFormat : supportedFormats) {
                if (supportedFormat.equals(format)) {
                    return true;
                }
            }
            return false;
        }

        private boolean supportsKeyClass(Key key) {
            if (supportedClasses == null) {
                return false;
            }
            Class<?> keyClass = key.getClass();
            for (Class<?> clazz : supportedClasses) {
                if (clazz.isAssignableFrom(keyClass)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Return a String representation of this service.
         *
         * @return a String representation of this service.
         */
        public String toString() {
            String aString = aliases.isEmpty()
                    ? "" : "\r\n  aliases: " + aliases.toString();
            String attrs = attributes.isEmpty()
                    ? "" : "\r\n  attributes: " + attributes.toString();
            return provider.getName() + ": " + type + "." + algorithm
                    + " -> " + className + aString + attrs + "\r\n";
        }

    }

    public TString getName() {
        return name;
    }

    private static class ServiceKey {
        private final TString type;
        private final TString algorithm;
        private final TString originalAlgorithm;
        private ServiceKey(TString type, TString algorithm, boolean intern) {
            this.type = type;
            this.originalAlgorithm = algorithm;
            algorithm = algorithm.toUpperCase(ENGLISH);
            this.algorithm = intern ? algorithm.intern() : algorithm;
        }
        public int hashCode() {
            return type.hashCode() + algorithm.hashCode();
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TProvider.ServiceKey == false) {
                return false;
            }
            TProvider.ServiceKey other = (TProvider.ServiceKey)obj;
            return this.type.equals(other.type)
                    && this.algorithm.equals(other.algorithm);
        }
        boolean matches(TString type, TString algorithm) {
            return (this.type == type) && (this.originalAlgorithm == algorithm);
        }
    }
}

