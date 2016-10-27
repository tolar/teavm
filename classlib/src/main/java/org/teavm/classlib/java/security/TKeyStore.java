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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TX509Certificate;

/**
 * Created by vasek on 22. 10. 2016.
 */
public class TKeyStore {


    /*
     * Constant to lookup in the Security properties file to determine
     * the default keystore type.
     * In the Security properties file, the default keystore type is given as:
     * <pre>
     * keystore.type=jks
     * </pre>
     */
    private static final String KEYSTORE_TYPE = "keystore.type";

    // The keystore type
    private TString type;

    // The provider
    private TProvider provider;

    // The provider implementation
    private TKeyStoreSpi keyStoreSpi;

    // Has this keystore been initialized (loaded)?
    private boolean initialized = false;

    /**
     * A marker interface for {@code TKeyStore}
     * {@link #load(java.security.KeyStore.LoadStoreParameter) load}
     * and
     * {@link #store(java.security.KeyStore.LoadStoreParameter) store}
     * parameters.
     *
     * @since 1.5
     */
    public static interface LoadStoreParameter {
        /**
         * Gets the parameter used to protect keystore data.
         *
         * @return the parameter used to protect keystore data, or null
         */
        public TKeyStore.ProtectionParameter getProtectionParameter();
    }

    /**
     * A marker interface for keystore protection parameters.
     *
     * <p> The information stored in a {@code ProtectionParameter}
     * object protects the contents of a keystore.
     * For example, protection parameters may be used to check
     * the integrity of keystore data, or to protect the
     * confidentiality of sensitive keystore data
     * (such as a {@code PrivateKey}).
     *
     * @since 1.5
     */
    public static interface ProtectionParameter { }

    /**
     * A password-based implementation of {@code ProtectionParameter}.
     *
     * @since 1.5
     */
    public static class PasswordProtection implements
            java.security.KeyStore.ProtectionParameter, javax.security.auth.Destroyable {

        private final char[] password;
        private final String protectionAlgorithm;
        private final AlgorithmParameterSpec protectionParameters;
        private volatile boolean destroyed = false;

        /**
         * Creates a password parameter.
         *
         * <p> The specified {@code password} is cloned before it is stored
         * in the new {@code PasswordProtection} object.
         *
         * @param password the password, which may be {@code null}
         */
        public PasswordProtection(char[] password) {
            this.password = (password == null) ? null : password.clone();
            this.protectionAlgorithm = null;
            this.protectionParameters = null;
        }

        /**
         * Creates a password parameter and specifies the protection algorithm
         * and associated parameters to use when encrypting a keystore entry.
         * <p>
         * The specified {@code password} is cloned before it is stored in the
         * new {@code PasswordProtection} object.
         *
         * @param password the password, which may be {@code null}
         * @param protectionAlgorithm the encryption algorithm name, for
         *     example, {@code PBEWithHmacSHA256AndAES_256}.
         *     See the Cipher section in the <a href=
         * "{@docRoot}/../technotes/guides/security/StandardNames.html#Cipher">
         * Java Cryptography Architecture Standard Algorithm Name
         * Documentation</a>
         *     for information about standard encryption algorithm names.
         * @param protectionParameters the encryption algorithm parameter
         *     specification, which may be {@code null}
         * @exception NullPointerException if {@code protectionAlgorithm} is
         *     {@code null}
         *
         * @since 1.8
         */
        public PasswordProtection(char[] password, String protectionAlgorithm,
                AlgorithmParameterSpec protectionParameters) {
            if (protectionAlgorithm == null) {
                throw new NullPointerException("invalid null input");
            }
            this.password = (password == null) ? null : password.clone();
            this.protectionAlgorithm = protectionAlgorithm;
            this.protectionParameters = protectionParameters;
        }

        /**
         * Gets the name of the protection algorithm.
         * If none was set then the keystore provider will use its default
         * protection algorithm. The name of the default protection algorithm
         * for a given keystore type is set using the
         * {@code 'keystore.<type>.keyProtectionAlgorithm'} security property.
         * For example, the
         * {@code keystore.PKCS12.keyProtectionAlgorithm} property stores the
         * name of the default key protection algorithm used for PKCS12
         * keystores. If the security property is not set, an
         * implementation-specific algorithm will be used.
         *
         * @return the algorithm name, or {@code null} if none was set
         *
         * @since 1.8
         */
        public String getProtectionAlgorithm() {
            return protectionAlgorithm;
        }

        /**
         * Gets the parameters supplied for the protection algorithm.
         *
         * @return the algorithm parameter specification, or {@code  null},
         *     if none was set
         *
         * @since 1.8
         */
        public AlgorithmParameterSpec getProtectionParameters() {
            return protectionParameters;
        }

        /**
         * Gets the password.
         *
         * <p>Note that this method returns a reference to the password.
         * If a clone of the array is created it is the caller's
         * responsibility to zero out the password information
         * after it is no longer needed.
         *
         * @see #destroy()
         * @return the password, which may be {@code null}
         * @exception IllegalStateException if the password has
         *              been cleared (destroyed)
         */
        public synchronized char[] getPassword() {
            if (destroyed) {
                throw new IllegalStateException("password has been cleared");
            }
            return password;
        }

        /**
         * Clears the password.
         *
         * @exception DestroyFailedException if this method was unable
         *      to clear the password
         */
        public synchronized void destroy() throws DestroyFailedException {
            destroyed = true;
            if (password != null) {
                Arrays.fill(password, ' ');
            }
        }

        /**
         * Determines if password has been cleared.
         *
         * @return true if the password has been cleared, false otherwise
         */
        public synchronized boolean isDestroyed() {
            return destroyed;
        }
    }

    /**
     * A ProtectionParameter encapsulating a CallbackHandler.
     *
     * @since 1.5
     */
    public static class CallbackHandlerProtection
            implements java.security.KeyStore.ProtectionParameter {

        private final CallbackHandler handler;

        /**
         * Constructs a new CallbackHandlerProtection from a
         * CallbackHandler.
         *
         * @param handler the CallbackHandler
         * @exception NullPointerException if handler is null
         */
        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler must not be null");
            }
            this.handler = handler;
        }

        /**
         * Returns the CallbackHandler.
         *
         * @return the CallbackHandler.
         */
        public CallbackHandler getCallbackHandler() {
            return handler;
        }

    }

    /**
     * A marker interface for {@code TKeyStore} entry types.
     *
     * @since 1.5
     */
    public static interface Entry {

        /**
         * Retrieves the attributes associated with an entry.
         * <p>
         * The default implementation returns an empty {@code Set}.
         *
         * @return an unmodifiable {@code Set} of attributes, possibly empty
         *
         * @since 1.8
         */
        public default Set<TKeyStore.Entry.Attribute> getAttributes() {
            return Collections.<TKeyStore.Entry.Attribute>emptySet();
        }

        /**
         * An attribute associated with a keystore entry.
         * It comprises a name and one or more values.
         *
         * @since 1.8
         */
        public interface Attribute {
            /**
             * Returns the attribute's name.
             *
             * @return the attribute name
             */
            public String getName();

            /**
             * Returns the attribute's value.
             * Multi-valued attributes encode their values as a single string.
             *
             * @return the attribute value
             */
            public String getValue();
        }
    }

    /**
     * A {@code TKeyStore} entry that holds a {@code PrivateKey}
     * and corresponding certificate chain.
     *
     * @since 1.5
     */
    public static final class PrivateKeyEntry implements TKeyStore.Entry {

        private final TPrivateKey privKey;
        private final TCertificate[] chain;
        private final Set<Attribute> attributes;

        /**
         * Constructs a {@code PrivateKeyEntry} with a
         * {@code PrivateKey} and corresponding certificate chain.
         *
         * <p> The specified {@code chain} is cloned before it is stored
         * in the new {@code PrivateKeyEntry} object.
         *
         * @param privateKey the {@code PrivateKey}
         * @param chain an array of {@code Certificate}s
         *      representing the certificate chain.
         *      The chain must be ordered and contain a
         *      {@code Certificate} at index 0
         *      corresponding to the private key.
         *
         * @exception NullPointerException if
         *      {@code privateKey} or {@code chain}
         *      is {@code null}
         * @exception IllegalArgumentException if the specified chain has a
         *      length of 0, if the specified chain does not contain
         *      {@code Certificate}s of the same type,
         *      or if the {@code PrivateKey} algorithm
         *      does not match the algorithm of the {@code PublicKey}
         *      in the end entity {@code Certificate} (at index 0)
         */
        public PrivateKeyEntry(TPrivateKey privateKey, TCertificate[] chain) {
            this(privateKey, chain, Collections.<Attribute>emptySet());
        }

        /**
         * Constructs a {@code PrivateKeyEntry} with a {@code PrivateKey} and
         * corresponding certificate chain and associated entry attributes.
         *
         * <p> The specified {@code chain} and {@code attributes} are cloned
         * before they are stored in the new {@code PrivateKeyEntry} object.
         *
         * @param privateKey the {@code PrivateKey}
         * @param chain an array of {@code Certificate}s
         *      representing the certificate chain.
         *      The chain must be ordered and contain a
         *      {@code Certificate} at index 0
         *      corresponding to the private key.
         * @param attributes the attributes
         *
         * @exception NullPointerException if {@code privateKey}, {@code chain}
         *      or {@code attributes} is {@code null}
         * @exception IllegalArgumentException if the specified chain has a
         *      length of 0, if the specified chain does not contain
         *      {@code Certificate}s of the same type,
         *      or if the {@code PrivateKey} algorithm
         *      does not match the algorithm of the {@code PublicKey}
         *      in the end entity {@code Certificate} (at index 0)
         *
         * @since 1.8
         */
        public PrivateKeyEntry(TPrivateKey privateKey, TCertificate[] chain,
                Set<Attribute> attributes) {

            if (privateKey == null || chain == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            if (chain.length == 0) {
                throw new IllegalArgumentException
                        ("invalid zero-length input chain");
            }

            TCertificate[] clonedChain = chain.clone();
            String certType = clonedChain[0].getType();
            for (int i = 1; i < clonedChain.length; i++) {
                if (!certType.equals(clonedChain[i].getType())) {
                    throw new IllegalArgumentException
                            ("chain does not contain certificates " +
                                    "of the same type");
                }
            }
            if (!privateKey.getAlgorithm().equals
                    (clonedChain[0].getPublicKey().getAlgorithm())) {
                throw new IllegalArgumentException
                        ("private key algorithm does not match " +
                                "algorithm of public key in end entity " +
                                "certificate (at index 0)");
            }
            this.privKey = privateKey;

            if (clonedChain[0] instanceof TX509Certificate &&
                    !(clonedChain instanceof TX509Certificate[])) {

                this.chain = new TX509Certificate[clonedChain.length];
                System.arraycopy(clonedChain, 0,
                        this.chain, 0, clonedChain.length);
            } else {
                this.chain = clonedChain;
            }

            this.attributes =
                    Collections.unmodifiableSet(new HashSet<>(attributes));
        }

        /**
         * Gets the {@code PrivateKey} from this entry.
         *
         * @return the {@code PrivateKey} from this entry
         */
        public TPrivateKey getPrivateKey() {
            return privKey;
        }

        /**
         * Gets the {@code Certificate} chain from this entry.
         *
         * <p> The stored chain is cloned before being returned.
         *
         * @return an array of {@code Certificate}s corresponding
         *      to the certificate chain for the public key.
         *      If the certificates are of type X.509,
         *      the runtime type of the returned array is
         *      {@code X509Certificate[]}.
         */
        public TCertificate[] getCertificateChain() {
            return chain.clone();
        }

        /**
         * Gets the end entity {@code Certificate}
         * from the certificate chain in this entry.
         *
         * @return the end entity {@code Certificate} (at index 0)
         *      from the certificate chain in this entry.
         *      If the certificate is of type X.509,
         *      the runtime type of the returned certificate is
         *      {@code X509Certificate}.
         */
        public TCertificate getCertificate() {
            return chain[0];
        }

        /**
         * Retrieves the attributes associated with an entry.
         * <p>
         *
         * @return an unmodifiable {@code Set} of attributes, possibly empty
         *
         * @since 1.8
         */
        @Override
        public Set<Attribute> getAttributes() {
            return attributes;
        }

        /**
         * Returns a string representation of this PrivateKeyEntry.
         * @return a string representation of this PrivateKeyEntry.
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Private key entry and certificate chain with "
                    + chain.length + " elements:\r\n");
            for (TCertificate cert : chain) {
                sb.append(cert);
                sb.append("\r\n");
            }
            return sb.toString();
        }

    }

    /**
     * A {@code TKeyStore} entry that holds a {@code SecretKey}.
     *
     * @since 1.5
     */
    public static final class SecretKeyEntry implements java.security.KeyStore.Entry {

        private final SecretKey sKey;
        private final Set<Attribute> attributes;

        /**
         * Constructs a {@code SecretKeyEntry} with a
         * {@code SecretKey}.
         *
         * @param secretKey the {@code SecretKey}
         *
         * @exception NullPointerException if {@code secretKey}
         *      is {@code null}
         */
        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
            this.attributes = Collections.<Attribute>emptySet();
        }

        /**
         * Constructs a {@code SecretKeyEntry} with a {@code SecretKey} and
         * associated entry attributes.
         *
         * <p> The specified {@code attributes} is cloned before it is stored
         * in the new {@code SecretKeyEntry} object.
         *
         * @param secretKey the {@code SecretKey}
         * @param attributes the attributes
         *
         * @exception NullPointerException if {@code secretKey} or
         *     {@code attributes} is {@code null}
         *
         * @since 1.8
         */
        public SecretKeyEntry(SecretKey secretKey, Set<Attribute> attributes) {

            if (secretKey == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            this.sKey = secretKey;
            this.attributes =
                    Collections.unmodifiableSet(new HashSet<>(attributes));
        }

        /**
         * Gets the {@code SecretKey} from this entry.
         *
         * @return the {@code SecretKey} from this entry
         */
        public SecretKey getSecretKey() {
            return sKey;
        }

        /**
         * Retrieves the attributes associated with an entry.
         * <p>
         *
         * @return an unmodifiable {@code Set} of attributes, possibly empty
         *
         * @since 1.8
         */
        @Override
        public Set<Attribute> getAttributes() {
            return attributes;
        }

        /**
         * Returns a string representation of this SecretKeyEntry.
         * @return a string representation of this SecretKeyEntry.
         */
        public String toString() {
            return "Secret key entry with algorithm " + sKey.getAlgorithm();
        }
    }

    /**
     * A {@code TKeyStore} entry that holds a trusted
     * {@code Certificate}.
     *
     * @since 1.5
     */
    public static final class TrustedCertificateEntry implements java.security.KeyStore.Entry {

        private final Certificate cert;
        private final Set<Attribute> attributes;

        /**
         * Constructs a {@code TrustedCertificateEntry} with a
         * trusted {@code Certificate}.
         *
         * @param trustedCert the trusted {@code Certificate}
         *
         * @exception NullPointerException if
         *      {@code trustedCert} is {@code null}
         */
        public TrustedCertificateEntry(Certificate trustedCert) {
            if (trustedCert == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
            this.attributes = Collections.<Attribute>emptySet();
        }

        /**
         * Constructs a {@code TrustedCertificateEntry} with a
         * trusted {@code Certificate} and associated entry attributes.
         *
         * <p> The specified {@code attributes} is cloned before it is stored
         * in the new {@code TrustedCertificateEntry} object.
         *
         * @param trustedCert the trusted {@code Certificate}
         * @param attributes the attributes
         *
         * @exception NullPointerException if {@code trustedCert} or
         *     {@code attributes} is {@code null}
         *
         * @since 1.8
         */
        public TrustedCertificateEntry(Certificate trustedCert,
                Set<Attribute> attributes) {
            if (trustedCert == null || attributes == null) {
                throw new NullPointerException("invalid null input");
            }
            this.cert = trustedCert;
            this.attributes =
                    Collections.unmodifiableSet(new HashSet<>(attributes));
        }

        /**
         * Gets the trusted {@code Certficate} from this entry.
         *
         * @return the trusted {@code Certificate} from this entry
         */
        public Certificate getTrustedCertificate() {
            return cert;
        }

        /**
         * Retrieves the attributes associated with an entry.
         * <p>
         *
         * @return an unmodifiable {@code Set} of attributes, possibly empty
         *
         * @since 1.8
         */
        @Override
        public Set<Attribute> getAttributes() {
            return attributes;
        }

        /**
         * Returns a string representation of this TrustedCertificateEntry.
         * @return a string representation of this TrustedCertificateEntry.
         */
        public String toString() {
            return "Trusted certificate entry:\r\n" + cert.toString();
        }
    }

    /**
     * Creates a TKeyStore object of the given type, and encapsulates the given
     * provider implementation (SPI object) in it.
     *
     * @param keyStoreSpi the provider implementation.
     * @param provider the provider.
     * @param type the keystore type.
     */
    protected TKeyStore(TKeyStoreSpi keyStoreSpi, TProvider provider, TString type)
    {
        this.keyStoreSpi = keyStoreSpi;
        this.provider = provider;
        this.type = type;

    }

    /**
     * Returns a keystore object of the specified type.
     *
     * <p> This method traverses the list of registered security Providers,
     * starting with the most preferred Provider.
     * A new TKeyStore object encapsulating the
     * TKeyStoreSpi implementation from the first
     * Provider that supports the specified type is returned.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param type the type of keystore.
     * See the TKeyStore section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#TKeyStore">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard keystore types.
     *
     * @return a keystore object of the specified type.
     *
     * @exception KeyStoreException if no Provider supports a
     *          TKeyStoreSpi implementation for the
     *          specified type.
     *
     * @see Provider
     */
    public static TKeyStore getInstance(TString type)
            throws KeyStoreException
    {
        try {
            Object[] objs = TSecurity.getImpl(type, TString.wrap("TKeyStore"), (TString)null);
            return new TKeyStore((TKeyStoreSpi)objs[0], (TProvider)objs[1], type);
        } catch (TNoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        } catch (TNoSuchProviderException nspe) {
            throw new KeyStoreException(type + " not found", nspe);
        }
    }

    /**
     * Returns a keystore object of the specified type.
     *
     * <p> A new TKeyStore object encapsulating the
     * TKeyStoreSpi implementation from the specified provider
     * is returned.  The specified provider must be registered
     * in the security provider list.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param type the type of keystore.
     * See the TKeyStore section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#TKeyStore">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard keystore types.
     *
     * @param provider the name of the provider.
     *
     * @return a keystore object of the specified type.
     *
     * @exception KeyStoreException if a TKeyStoreSpi
     *          implementation for the specified type is not
     *          available from the specified provider.
     *
     * @exception NoSuchProviderException if the specified provider is not
     *          registered in the security provider list.
     *
     * @exception IllegalArgumentException if the provider name is null
     *          or empty.
     *
     * @see Provider
     */
    public static TKeyStore getInstance(TString type, TString provider)
            throws KeyStoreException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        try {
            Object[] objs = TSecurity.getImpl(type, TString.wrap("TKeyStore"), provider);
            return new TKeyStore((TKeyStoreSpi)objs[0], (TProvider)objs[1], type);
        } catch (TNoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    /**
     * Returns a keystore object of the specified type.
     *
     * <p> A new TKeyStore object encapsulating the
     * TKeyStoreSpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * @param type the type of keystore.
     * See the TKeyStore section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#TKeyStore">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard keystore types.
     *
     * @param provider the provider.
     *
     * @return a keystore object of the specified type.
     *
     * @exception KeyStoreException if TKeyStoreSpi
     *          implementation for the specified type is not available
     *          from the specified Provider object.
     *
     * @exception IllegalArgumentException if the specified provider is null.
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static TKeyStore getInstance(TString type, TProvider provider)
            throws KeyStoreException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        try {
            Object[] objs = TSecurity.getImpl(type, TString.wrap("TKeyStore"), provider);
            return new TKeyStore((TKeyStoreSpi)objs[0], (TProvider)objs[1], type);
        } catch (TNoSuchAlgorithmException nsae) {
            throw new KeyStoreException(type + " not found", nsae);
        }
    }

    /**
     * Returns the default keystore type as specified by the
     * {@code keystore.type} security property, or the string
     * {@literal "jks"} (acronym for {@literal "Java keystore"})
     * if no such property exists.
     *
     * <p>The default keystore type can be used by applications that do not
     * want to use a hard-coded keystore type when calling one of the
     * {@code getInstance} methods, and want to provide a default keystore
     * type in case a user does not specify its own.
     *
     * <p>The default keystore type can be changed by setting the value of the
     * {@code keystore.type} security property to the desired keystore type.
     *
     * @return the default keystore type as specified by the
     * {@code keystore.type} security property, or the string {@literal "jks"}
     * if no such property exists.
     * @see java.security.Security security properties
     */
    public final static String getDefaultType() {
        String kstype;
        kstype = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(KEYSTORE_TYPE);
            }
        });
        if (kstype == null) {
            kstype = "jks";
        }
        return kstype;
    }

    /**
     * Returns the provider of this keystore.
     *
     * @return the provider of this keystore.
     */
    public final TProvider getProvider()
    {
        return this.provider;
    }

    /**
     * Returns the type of this keystore.
     *
     * @return the type of this keystore.
     */
    public final TString getType()
    {
        return this.type;
    }

    /**
     * Returns the key associated with the given alias, using the given
     * password to recover it.  The key must have been associated with
     * the alias by a call to {@code setKeyEntry},
     * or by a call to {@code setEntry} with a
     * {@code PrivateKeyEntry} or {@code SecretKeyEntry}.
     *
     * @param alias the alias name
     * @param password the password for recovering the key
     *
     * @return the requested key, or null if the given alias does not exist
     * or does not identify a key-related entry.
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     * @exception NoSuchAlgorithmException if the algorithm for recovering the
     * key cannot be found
     * @exception UnrecoverableKeyException if the key cannot be recovered
     * (e.g., the given password is wrong).
     */
    public final Key getKey(String alias, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetKey(alias, password);
    }

    /**
     * Returns the certificate chain associated with the given alias.
     * The certificate chain must have been associated with the alias
     * by a call to {@code setKeyEntry},
     * or by a call to {@code setEntry} with a
     * {@code PrivateKeyEntry}.
     *
     * @param alias the alias name
     *
     * @return the certificate chain (ordered with the user's certificate first
     * followed by zero or more certificate authorities), or null if the given alias
     * does not exist or does not contain a certificate chain
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final TCertificate[] getCertificateChain(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificateChain(alias);
    }

    /**
     * Returns the certificate associated with the given alias.
     *
     * <p> If the given alias name identifies an entry
     * created by a call to {@code setCertificateEntry},
     * or created by a call to {@code setEntry} with a
     * {@code TrustedCertificateEntry},
     * then the trusted certificate contained in that entry is returned.
     *
     * <p> If the given alias name identifies an entry
     * created by a call to {@code setKeyEntry},
     * or created by a call to {@code setEntry} with a
     * {@code PrivateKeyEntry},
     * then the first element of the certificate chain in that entry
     * is returned.
     *
     * @param alias the alias name
     *
     * @return the certificate, or null if the given alias does not exist or
     * does not contain a certificate.
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final Certificate getCertificate(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificate(alias);
    }

    /**
     * Returns the creation date of the entry identified by the given alias.
     *
     * @param alias the alias name
     *
     * @return the creation date of this entry, or null if the given alias does
     * not exist
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final Date getCreationDate(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCreationDate(alias);
    }

    /**
     * Assigns the given key to the given alias, protecting it with the given
     * password.
     *
     * <p>If the given key is of type {@code java.security.PrivateKey},
     * it must be accompanied by a certificate chain certifying the
     * corresponding public key.
     *
     * <p>If the given alias already exists, the keystore information
     * associated with it is overridden by the given key (and possibly
     * certificate chain).
     *
     * @param alias the alias name
     * @param key the key to be associated with the alias
     * @param password the password to protect the key
     * @param chain the certificate chain for the corresponding public
     * key (only required if the given key is of type
     * {@code java.security.PrivateKey}).
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded), the given key cannot be protected, or this operation fails
     * for some other reason
     */
    public final void setKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        if ((key instanceof PrivateKey) &&
                (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException("Private key must be "
                    + "accompanied by certificate "
                    + "chain");
        }
        keyStoreSpi.engineSetKeyEntry(alias, key, password, chain);
    }

    /**
     * Assigns the given key (that has already been protected) to the given
     * alias.
     *
     * <p>If the protected key is of type
     * {@code java.security.PrivateKey}, it must be accompanied by a
     * certificate chain certifying the corresponding public key. If the
     * underlying keystore implementation is of type {@code jks},
     * {@code key} must be encoded as an
     * {@code EncryptedPrivateKeyInfo} as defined in the PKCS #8 standard.
     *
     * <p>If the given alias already exists, the keystore information
     * associated with it is overridden by the given key (and possibly
     * certificate chain).
     *
     * @param alias the alias name
     * @param key the key (in protected format) to be associated with the alias
     * @param chain the certificate chain for the corresponding public
     *          key (only useful if the protected key is of type
     *          {@code java.security.PrivateKey}).
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded), or if this operation fails for some other reason.
     */
    public final void setKeyEntry(String alias, byte[] key,
            Certificate[] chain)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetKeyEntry(alias, key, chain);
    }

    /**
     * Assigns the given trusted certificate to the given alias.
     *
     * <p> If the given alias identifies an existing entry
     * created by a call to {@code setCertificateEntry},
     * or created by a call to {@code setEntry} with a
     * {@code TrustedCertificateEntry},
     * the trusted certificate in the existing entry
     * is overridden by the given certificate.
     *
     * @param alias the alias name
     * @param cert the certificate
     *
     * @exception KeyStoreException if the keystore has not been initialized,
     * or the given alias already exists and does not identify an
     * entry containing a trusted certificate,
     * or this operation fails for some other reason.
     */
    public final void setCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetCertificateEntry(alias, cert);
    }

    /**
     * Deletes the entry identified by the given alias from this keystore.
     *
     * @param alias the alias name
     *
     * @exception KeyStoreException if the keystore has not been initialized,
     * or if the entry cannot be removed.
     */
    public final void deleteEntry(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineDeleteEntry(alias);
    }

    /**
     * Lists all the alias names of this keystore.
     *
     * @return enumeration of the alias names
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final Enumeration<String> aliases()
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineAliases();
    }

    /**
     * Checks if the given alias exists in this keystore.
     *
     * @param alias the alias name
     *
     * @return true if the alias exists, false otherwise
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final boolean containsAlias(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineContainsAlias(alias);
    }

    /**
     * Retrieves the number of entries in this keystore.
     *
     * @return the number of entries in this keystore
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final int size()
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineSize();
    }

    /**
     * Returns true if the entry identified by the given alias
     * was created by a call to {@code setKeyEntry},
     * or created by a call to {@code setEntry} with a
     * {@code PrivateKeyEntry} or a {@code SecretKeyEntry}.
     *
     * @param alias the alias for the keystore entry to be checked
     *
     * @return true if the entry identified by the given alias is a
     * key-related entry, false otherwise.
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final boolean isKeyEntry(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineIsKeyEntry(alias);
    }

    /**
     * Returns true if the entry identified by the given alias
     * was created by a call to {@code setCertificateEntry},
     * or created by a call to {@code setEntry} with a
     * {@code TrustedCertificateEntry}.
     *
     * @param alias the alias for the keystore entry to be checked
     *
     * @return true if the entry identified by the given alias contains a
     * trusted certificate, false otherwise.
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final boolean isCertificateEntry(String alias)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineIsCertificateEntry(alias);
    }

    /**
     * Returns the (alias) name of the first keystore entry whose certificate
     * matches the given certificate.
     *
     * <p> This method attempts to match the given certificate with each
     * keystore entry. If the entry being considered was
     * created by a call to {@code setCertificateEntry},
     * or created by a call to {@code setEntry} with a
     * {@code TrustedCertificateEntry},
     * then the given certificate is compared to that entry's certificate.
     *
     * <p> If the entry being considered was
     * created by a call to {@code setKeyEntry},
     * or created by a call to {@code setEntry} with a
     * {@code PrivateKeyEntry},
     * then the given certificate is compared to the first
     * element of that entry's certificate chain.
     *
     * @param cert the certificate to match with.
     *
     * @return the alias name of the first entry with a matching certificate,
     * or null if no such entry exists in this keystore.
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     */
    public final String getCertificateAlias(Certificate cert)
            throws KeyStoreException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetCertificateAlias(cert);
    }

    /**
     * Stores this keystore to the given output stream, and protects its
     * integrity with the given password.
     *
     * @param stream the output stream to which this keystore is written.
     * @param password the password to generate the keystore integrity check
     *
     * @exception KeyStoreException if the keystore has not been initialized
     * (loaded).
     * @exception IOException if there was an I/O problem with data
     * @exception NoSuchAlgorithmException if the appropriate data integrity
     * algorithm could not be found
     * @exception CertificateException if any of the certificates included in
     * the keystore data could not be stored
     */
    public final void store(OutputStream stream, char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException
    {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineStore(stream, password);
    }

    /**
     * Stores this keystore using the given {@code LoadStoreParameter}.
     *
     * @param param the {@code LoadStoreParameter}
     *          that specifies how to store the keystore,
     *          which may be {@code null}
     *
     * @exception IllegalArgumentException if the given
     *          {@code LoadStoreParameter}
     *          input is not recognized
     * @exception KeyStoreException if the keystore has not been initialized
     *          (loaded)
     * @exception IOException if there was an I/O problem with data
     * @exception NoSuchAlgorithmException if the appropriate data integrity
     *          algorithm could not be found
     * @exception CertificateException if any of the certificates included in
     *          the keystore data could not be stored
     *
     * @since 1.5
     */
    public final void store(java.security.KeyStore.LoadStoreParameter param)
            throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineStore(param);
    }

    /**
     * Loads this TKeyStore from the given input stream.
     *
     * <p>A password may be given to unlock the keystore
     * (e.g. the keystore resides on a hardware token device),
     * or to check the integrity of the keystore data.
     * If a password is not given for integrity checking,
     * then integrity checking is not performed.
     *
     * <p>In order to create an empty keystore, or if the keystore cannot
     * be initialized from a stream, pass {@code null}
     * as the {@code stream} argument.
     *
     * <p> Note that if this keystore has already been loaded, it is
     * reinitialized and loaded again from the given input stream.
     *
     * @param stream the input stream from which the keystore is loaded,
     * or {@code null}
     * @param password the password used to check the integrity of
     * the keystore, the password used to unlock the keystore,
     * or {@code null}
     *
     * @exception IOException if there is an I/O or format problem with the
     * keystore data, if a password is required but not given,
     * or if the given password was incorrect. If the error is due to a
     * wrong password, the {@link Throwable#getCause cause} of the
     * {@code IOException} should be an
     * {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException if the algorithm used to check
     * the integrity of the keystore cannot be found
     * @exception CertificateException if any of the certificates in the
     * keystore could not be loaded
     */
    public final void load(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException
    {
        keyStoreSpi.engineLoad(stream, password);
        initialized = true;
    }

    /**
     * Loads this keystore using the given {@code LoadStoreParameter}.
     *
     * <p> Note that if this TKeyStore has already been loaded, it is
     * reinitialized and loaded again from the given parameter.
     *
     * @param param the {@code LoadStoreParameter}
     *          that specifies how to load the keystore,
     *          which may be {@code null}
     *
     * @exception IllegalArgumentException if the given
     *          {@code LoadStoreParameter}
     *          input is not recognized
     * @exception IOException if there is an I/O or format problem with the
     *          keystore data. If the error is due to an incorrect
     *         {@code ProtectionParameter} (e.g. wrong password)
     *         the {@link Throwable#getCause cause} of the
     *         {@code IOException} should be an
     *         {@code UnrecoverableKeyException}
     * @exception NoSuchAlgorithmException if the algorithm used to check
     *          the integrity of the keystore cannot be found
     * @exception CertificateException if any of the certificates in the
     *          keystore could not be loaded
     *
     * @since 1.5
     */
    public final void load(java.security.KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException,
            CertificateException {

        keyStoreSpi.engineLoad(param);
        initialized = true;
    }

    /**
     * Gets a keystore {@code Entry} for the specified alias
     * with the specified protection parameter.
     *
     * @param alias get the keystore {@code Entry} for this alias
     * @param protParam the {@code ProtectionParameter}
     *          used to protect the {@code Entry},
     *          which may be {@code null}
     *
     * @return the keystore {@code Entry} for the specified alias,
     *          or {@code null} if there is no such entry
     *
     * @exception NullPointerException if
     *          {@code alias} is {@code null}
     * @exception NoSuchAlgorithmException if the algorithm for recovering the
     *          entry cannot be found
     * @exception UnrecoverableEntryException if the specified
     *          {@code protParam} were insufficient or invalid
     * @exception UnrecoverableKeyException if the entry is a
     *          {@code PrivateKeyEntry} or {@code SecretKeyEntry}
     *          and the specified {@code protParam} does not contain
     *          the information needed to recover the key (e.g. wrong password)
     * @exception KeyStoreException if the keystore has not been initialized
     *          (loaded).
     * @see #setEntry(String, java.security.KeyStore.Entry, java.security.KeyStore.ProtectionParameter)
     *
     * @since 1.5
     */
    public final java.security.KeyStore.Entry getEntry(String alias, java.security.KeyStore.ProtectionParameter protParam)
            throws NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException {

        if (alias == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineGetEntry(alias, protParam);
    }

    /**
     * Saves a keystore {@code Entry} under the specified alias.
     * The protection parameter is used to protect the
     * {@code Entry}.
     *
     * <p> If an entry already exists for the specified alias,
     * it is overridden.
     *
     * @param alias save the keystore {@code Entry} under this alias
     * @param entry the {@code Entry} to save
     * @param protParam the {@code ProtectionParameter}
     *          used to protect the {@code Entry},
     *          which may be {@code null}
     *
     * @exception NullPointerException if
     *          {@code alias} or {@code entry}
     *          is {@code null}
     * @exception KeyStoreException if the keystore has not been initialized
     *          (loaded), or if this operation fails for some other reason
     *
     * @see #getEntry(String, java.security.KeyStore.ProtectionParameter)
     *
     * @since 1.5
     */
    public final void setEntry(String alias, java.security.KeyStore.Entry entry,
            java.security.KeyStore.ProtectionParameter protParam)
            throws KeyStoreException {
        if (alias == null || entry == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        keyStoreSpi.engineSetEntry(alias, entry, protParam);
    }

    /**
     * Determines if the keystore {@code Entry} for the specified
     * {@code alias} is an instance or subclass of the specified
     * {@code entryClass}.
     *
     * @param alias the alias name
     * @param entryClass the entry class
     *
     * @return true if the keystore {@code Entry} for the specified
     *          {@code alias} is an instance or subclass of the
     *          specified {@code entryClass}, false otherwise
     *
     * @exception NullPointerException if
     *          {@code alias} or {@code entryClass}
     *          is {@code null}
     * @exception KeyStoreException if the keystore has not been
     *          initialized (loaded)
     *
     * @since 1.5
     */
    public final boolean
    entryInstanceOf(String alias,
            Class<? extends java.security.KeyStore.Entry> entryClass)
            throws KeyStoreException
    {

        if (alias == null || entryClass == null) {
            throw new NullPointerException("invalid null input");
        }
        if (!initialized) {
            throw new KeyStoreException("Uninitialized keystore");
        }
        return keyStoreSpi.engineEntryInstanceOf(alias, entryClass);
    }

    /**
     * A description of a to-be-instantiated TKeyStore object.
     *
     * <p>An instance of this class encapsulates the information needed to
     * instantiate and initialize a TKeyStore object. That process is
     * triggered when the {@linkplain #getKeyStore} method is called.
     *
     * <p>This makes it possible to decouple configuration from TKeyStore
     * object creation and e.g. delay a password prompt until it is
     * needed.
     *
     * @see java.security.KeyStore
     * @see javax.net.ssl.KeyStoreBuilderParameters
     * @since 1.5
     */
    public static abstract class Builder {

        // maximum times to try the callbackhandler if the password is wrong
        static final int MAX_CALLBACK_TRIES = 3;

        /**
         * Construct a new Builder.
         */
        protected Builder() {
            // empty
        }

        /**
         * Returns the TKeyStore described by this object.
         *
         * @return the {@code TKeyStore} described by this object
         * @exception KeyStoreException if an error occurred during the
         *   operation, for example if the TKeyStore could not be
         *   instantiated or loaded
         */
        public abstract TKeyStore getKeyStore() throws KeyStoreException;

        /**
         * Returns the ProtectionParameters that should be used to obtain
         * the {@link TKeyStore.Entry Entry} with the given alias.
         * The {@code getKeyStore} method must be invoked before this
         * method may be called.
         *
         * @return the ProtectionParameters that should be used to obtain
         *   the {@link TKeyStore.Entry Entry} with the given alias.
         * @param alias the alias of the TKeyStore entry
         * @throws NullPointerException if alias is null
         * @throws KeyStoreException if an error occurred during the
         *   operation
         * @throws IllegalStateException if the getKeyStore method has
         *   not been invoked prior to calling this method
         */
        public abstract TKeyStore.ProtectionParameter getProtectionParameter(String alias)
                throws KeyStoreException;

        /**
         * Returns a new Builder that encapsulates the given TKeyStore.
         * The {@linkplain #getKeyStore} method of the returned object
         * will return {@code keyStore}, the {@linkplain
         * #getProtectionParameter getProtectionParameter()} method will
         * return {@code protectionParameters}.
         *
         * <p> This is useful if an existing TKeyStore object needs to be
         * used with Builder-based APIs.
         *
         * @return a new Builder object
         * @param keyStore the TKeyStore to be encapsulated
         * @param protectionParameter the ProtectionParameter used to
         *   protect the TKeyStore entries
         * @throws NullPointerException if keyStore or
         *   protectionParameters is null
         * @throws IllegalArgumentException if the keyStore has not been
         *   initialized
         */
        public static TKeyStore.Builder newInstance(final TKeyStore keyStore,
                final TKeyStore.ProtectionParameter protectionParameter) {
            if ((keyStore == null) || (protectionParameter == null)) {
                throw new NullPointerException();
            }
            if (keyStore.initialized == false) {
                throw new IllegalArgumentException("TKeyStore not initialized");
            }
            return new TKeyStore.Builder() {
                private volatile boolean getCalled;

                public TKeyStore getKeyStore() {
                    getCalled = true;
                    return keyStore;
                }

                public TKeyStore.ProtectionParameter getProtectionParameter(String alias)
                {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (getCalled == false) {
                        throw new IllegalStateException
                                ("getKeyStore() must be called first");
                    }
                    return protectionParameter;
                }
            };
        }

        /**
         * Returns a new Builder object.
         *
         * <p>The first call to the {@link #getKeyStore} method on the returned
         * builder will create a TKeyStore of type {@code type} and call
         * its {@link TKeyStore#load load()} method.
         * The {@code inputStream} argument is constructed from
         * {@code file}.
         * If {@code protection} is a
         * {@code PasswordProtection}, the password is obtained by
         * calling the {@code getPassword} method.
         * Otherwise, if {@code protection} is a
         * {@code CallbackHandlerProtection}, the password is obtained
         * by invoking the CallbackHandler.
         *
         * <p>Subsequent calls to {@link #getKeyStore} return the same object
         * as the initial call. If the initial call to failed with a
         * KeyStoreException, subsequent calls also throw a
         * KeyStoreException.
         *
         * <p>The TKeyStore is instantiated from {@code provider} if
         * non-null. Otherwise, all installed providers are searched.
         *
         * <p>Calls to {@link #getProtectionParameter getProtectionParameter()}
         * will return a {@link TKeyStore.PasswordProtection PasswordProtection}
         * object encapsulating the password that was used to invoke the
         * {@code load} method.
         *
         * <p><em>Note</em> that the {@link #getKeyStore} method is executed
         * within the {@link AccessControlContext} of the code invoking this
         * method.
         *
         * @return a new Builder object
         * @param type the type of TKeyStore to be constructed
         * @param provider the provider from which the TKeyStore is to
         *   be instantiated (or null)
         * @param file the File that contains the TKeyStore data
         * @param protection the ProtectionParameter securing the TKeyStore data
         * @throws NullPointerException if type, file or protection is null
         * @throws IllegalArgumentException if protection is not an instance
         *   of either PasswordProtection or CallbackHandlerProtection; or
         *   if file does not exist or does not refer to a normal file
         */
        public static TKeyStore.Builder newInstance(String type, Provider provider,
                File file, TKeyStore.ProtectionParameter protection) {
            if ((type == null) || (file == null) || (protection == null)) {
                throw new NullPointerException();
            }
            if ((protection instanceof TKeyStore.PasswordProtection == false) &&
                    (protection instanceof TKeyStore.CallbackHandlerProtection == false)) {
                throw new IllegalArgumentException
                        ("Protection must be PasswordProtection or " +
                                "CallbackHandlerProtection");
            }
            if (file.isFile() == false) {
                throw new IllegalArgumentException
                        ("File does not exist or it does not refer " +
                                "to a normal file: " + file);
            }
            return new TKeyStore.Builder.FileBuilder(type, provider, file, protection,
                    AccessController.getContext());
        }

        private static final class FileBuilder extends TKeyStore.Builder {

            private final TString type;
            private final TProvider provider;
            private final File file;
            private TKeyStore.ProtectionParameter protection;
            private TKeyStore.ProtectionParameter keyProtection;
            private final AccessControlContext context;

            private TKeyStore keyStore;

            private Throwable oldException;

            FileBuilder(TString type, TProvider provider, File file,
                    TKeyStore.ProtectionParameter protection,
                    AccessControlContext context) {
                this.type = type;
                this.provider = provider;
                this.file = file;
                this.protection = protection;
                this.context = context;
            }

            public synchronized TKeyStore getKeyStore() throws KeyStoreException
            {
                if (keyStore != null) {
                    return keyStore;
                }
                if (oldException != null) {
                    throw new KeyStoreException
                            ("Previous TKeyStore instantiation failed",
                                    oldException);
                }
                PrivilegedExceptionAction<TKeyStore> action =
                        new PrivilegedExceptionAction<TKeyStore>() {
                            public TKeyStore run() throws Exception {
                                if (protection instanceof TKeyStore.CallbackHandlerProtection == false) {
                                    return run0();
                                }
                                // when using a CallbackHandler,
                                // reprompt if the password is wrong
                                int tries = 0;
                                while (true) {
                                    tries++;
                                    try {
                                        return run0();
                                    } catch (IOException e) {
                                        if ((tries < MAX_CALLBACK_TRIES)
                                                && (e.getCause() instanceof UnrecoverableKeyException)) {
                                            continue;
                                        }
                                        throw e;
                                    }
                                }
                            }
                            public TKeyStore run0() throws Exception {
                                TKeyStore ks;
                                if (provider == null) {
                                    ks = TKeyStore.getInstance(type);
                                } else {
                                    ks = TKeyStore.getInstance(type, provider);
                                }
                                InputStream in = null;
                                char[] password = null;
                                try {
                                    in = new FileInputStream(file);
                                    if (protection instanceof TKeyStore.PasswordProtection) {
                                        password =
                                                ((TKeyStore.PasswordProtection)protection).getPassword();
                                        keyProtection = protection;
                                    } else {
                                        CallbackHandler handler =
                                                ((TKeyStore.CallbackHandlerProtection)protection)
                                                        .getCallbackHandler();
                                        PasswordCallback callback = new PasswordCallback
                                                ("Password for keystore " + file.getName(),
                                                        false);
                                        handler.handle(new Callback[] {callback});
                                        password = callback.getPassword();
                                        if (password == null) {
                                            throw new KeyStoreException("No password" +
                                                    " provided");
                                        }
                                        callback.clearPassword();
                                        keyProtection = new TKeyStore.PasswordProtection(password);
                                    }
                                    ks.load(in, password);
                                    return ks;
                                } finally {
                                    if (in != null) {
                                        in.close();
                                    }
                                }
                            }
                        };
                try {
                    keyStore = AccessController.doPrivileged(action, context);
                    return keyStore;
                } catch (PrivilegedActionException e) {
                    oldException = e.getCause();
                    throw new KeyStoreException
                            ("TKeyStore instantiation failed", oldException);
                }
            }

            public synchronized TKeyStore.ProtectionParameter
            getProtectionParameter(String alias) {
                if (alias == null) {
                    throw new NullPointerException();
                }
                if (keyStore == null) {
                    throw new IllegalStateException
                            ("getKeyStore() must be called first");
                }
                return keyProtection;
            }
        }

        public static TKeyStore.Builder newInstance(final String type,
                final Provider provider, final TKeyStore.ProtectionParameter protection) {
            if ((type == null) || (protection == null)) {
                throw new NullPointerException();
            }
            final AccessControlContext context = AccessController.getContext();
            return new TKeyStore.Builder() {
                private volatile boolean getCalled;
                private IOException oldException;

                private final PrivilegedExceptionAction<TKeyStore> action
                        = new PrivilegedExceptionAction<TKeyStore>() {

                    public TKeyStore run() throws Exception {
                        TKeyStore ks;
                        if (provider == null) {
                            ks = TKeyStore.getInstance(type);
                        } else {
                            ks = TKeyStore.getInstance(type, provider);
                        }
                        TKeyStore.LoadStoreParameter
                                param = new TKeyStore.SimpleLoadStoreParameter(protection);
                        if (protection instanceof TKeyStore.CallbackHandlerProtection == false) {
                            ks.load(param);
                        } else {
                            // when using a CallbackHandler,
                            // reprompt if the password is wrong
                            int tries = 0;
                            while (true) {
                                tries++;
                                try {
                                    ks.load(param);
                                    break;
                                } catch (IOException e) {
                                    if (e.getCause() instanceof UnrecoverableKeyException) {
                                        if (tries < MAX_CALLBACK_TRIES) {
                                            continue;
                                        } else {
                                            oldException = e;
                                        }
                                    }
                                    throw e;
                                }
                            }
                        }
                        getCalled = true;
                        return ks;
                    }
                };

                public synchronized TKeyStore getKeyStore()
                        throws KeyStoreException {
                    if (oldException != null) {
                        throw new KeyStoreException
                                ("Previous TKeyStore instantiation failed",
                                        oldException);
                    }
                    try {
                        return AccessController.doPrivileged(action, context);
                    } catch (PrivilegedActionException e) {
                        Throwable cause = e.getCause();
                        throw new KeyStoreException
                                ("TKeyStore instantiation failed", cause);
                    }
                }

                public TKeyStore.ProtectionParameter getProtectionParameter(String alias)
                {
                    if (alias == null) {
                        throw new NullPointerException();
                    }
                    if (getCalled == false) {
                        throw new IllegalStateException
                                ("getKeyStore() must be called first");
                    }
                    return protection;
                }
            };
        }

    }

    static class SimpleLoadStoreParameter implements TKeyStore.LoadStoreParameter {

        private final TKeyStore.ProtectionParameter protection;

        SimpleLoadStoreParameter(TKeyStore.ProtectionParameter protection) {
            this.protection = protection;
        }

        public TKeyStore.ProtectionParameter getProtectionParameter() {
            return protection;
        }
    }

}
