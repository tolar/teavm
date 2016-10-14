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

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Iterator;
import java.util.List;

import org.teavm.classlib.java.lang.TException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.spec.TInvalidKeySpecException;
import org.teavm.classlib.java.security.spec.TKeySpec;
import org.teavm.classlib.sun.security.jca.TGetInstance;

public class TKeyFactory {

    // The algorithm associated with this key factory
    private final TString algorithm;

    // The provider
    private TProvider provider;

    // The provider implementation (delegate)
    private volatile TKeyFactorySpi spi;

    // lock for mutex during provider selection
    private final Object lock = new Object();

    // remaining services to try in provider selection
    // null once provider is selected
    private Iterator<TProvider.Service> serviceIterator;

    /**
     * Creates a KeyFactory object.
     *
     * @param keyFacSpi the delegate
     * @param provider the provider
     * @param algorithm the name of the algorithm
     * to associate with this {@code KeyFactory}
     */
    protected TKeyFactory(TKeyFactorySpi keyFacSpi, TProvider provider,
            TString algorithm) {
        this.spi = keyFacSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    private TKeyFactory(TString algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        List<TProvider.Service> list = TGetInstance.getServices(TString.wrap("KeyFactory"), algorithm);
        serviceIterator = list.iterator();
        // fetch and instantiate initial spi
        if (nextSpi(null) == null) {
            throw new NoSuchAlgorithmException
                    (algorithm + " KeyFactory not available");
        }
    }

    /**
     * Returns a KeyFactory object that converts
     * public/private keys of the specified algorithm.
     *
     * <p> This method traverses the list of registered security Providers,
     * starting with the most preferred Provider.
     * A new KeyFactory object encapsulating the
     * KeyFactorySpi implementation from the first
     * Provider that supports the specified algorithm is returned.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param algorithm the name of the requested key algorithm.
     * See the KeyFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @return the new KeyFactory object.
     *
     * @exception NoSuchAlgorithmException if no Provider supports a
     *          KeyFactorySpi implementation for the
     *          specified algorithm.
     *
     * @see Provider
     */
    public static TKeyFactory getInstance(TString algorithm)
            throws NoSuchAlgorithmException {
        return new TKeyFactory(algorithm);
    }

    /**
     * Returns a KeyFactory object that converts
     * public/private keys of the specified algorithm.
     *
     * <p> A new KeyFactory object encapsulating the
     * KeyFactorySpi implementation from the specified provider
     * is returned.  The specified provider must be registered
     * in the security provider list.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param algorithm the name of the requested key algorithm.
     * See the KeyFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @param provider the name of the provider.
     *
     * @return the new KeyFactory object.
     *
     * @exception NoSuchAlgorithmException if a KeyFactorySpi
     *          implementation for the specified algorithm is not
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
    public static TKeyFactory getInstance(TString algorithm, TString provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        TGetInstance.Instance instance = TGetInstance.getInstance(TString.wrap("KeyFactory"),
                TKeyFactorySpi.class, algorithm, provider);
        return new TKeyFactory((TKeyFactorySpi)instance.impl,
                instance.provider, algorithm);
    }

    /**
     * Returns a KeyFactory object that converts
     * public/private keys of the specified algorithm.
     *
     * <p> A new KeyFactory object encapsulating the
     * KeyFactorySpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * @param algorithm the name of the requested key algorithm.
     * See the KeyFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#KeyFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @param provider the provider.
     *
     * @return the new KeyFactory object.
     *
     * @exception NoSuchAlgorithmException if a KeyFactorySpi
     *          implementation for the specified algorithm is not available
     *          from the specified Provider object.
     *
     * @exception IllegalArgumentException if the specified provider is null.
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static TKeyFactory getInstance(TString algorithm, TProvider provider)
            throws NoSuchAlgorithmException {
        TGetInstance.Instance instance = TGetInstance.getInstance(TString.wrap("KeyFactory"),
                KeyFactorySpi.class, algorithm, provider);
        return new TKeyFactory((TKeyFactorySpi)instance.impl,
                instance.provider, algorithm);
    }

    /**
     * Returns the provider of this key factory object.
     *
     * @return the provider of this key factory object
     */
    public final TProvider getProvider() {
        synchronized (lock) {
            // disable further failover after this call
            serviceIterator = null;
            return provider;
        }
    }

    /**
     * Gets the name of the algorithm
     * associated with this {@code KeyFactory}.
     *
     * @return the name of the algorithm associated with this
     * {@code KeyFactory}
     */
    public final TString getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Update the active KeyFactorySpi of this class and return the next
     * implementation for failover. If no more implemenations are
     * available, this method returns null. However, the active spi of
     * this class is never set to null.
     */
    private TKeyFactorySpi nextSpi(TKeyFactorySpi oldSpi) {
        synchronized (lock) {
            // somebody else did a failover concurrently
            // try that spi now
            if ((oldSpi != null) && (oldSpi != spi)) {
                return spi;
            }
            if (serviceIterator == null) {
                return null;
            }
            while (serviceIterator.hasNext()) {
                TProvider.Service s = serviceIterator.next();
                try {
                    Object obj = s.newInstance(null);
                    if (obj instanceof KeyFactorySpi == false) {
                        continue;
                    }
                    TKeyFactorySpi spi = (TKeyFactorySpi)obj;
                    provider = s.getProvider();
                    this.spi = spi;
                    return spi;
                } catch (NoSuchAlgorithmException e) {
                    // ignore
                }
            }
            serviceIterator = null;
            return null;
        }
    }

    /**
     * Generates a public key object from the provided key specification
     * (key material).
     *
     * @param keySpec the specification (key material) of the public key.
     *
     * @return the public key.
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a public key.
     */
    public final TPublicKey generatePublic(TKeySpec keySpec)
            throws TInvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGeneratePublic(keySpec);
        }
        TException failure = null;
        TKeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGeneratePublic(keySpec);
            } catch (TException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof TInvalidKeySpecException) {
            throw (TInvalidKeySpecException)failure;
        }
        throw new TInvalidKeySpecException
                (TString.wrap("Could not generate public key"), failure);
    }

    /**
     * Generates a private key object from the provided key specification
     * (key material).
     *
     * @param keySpec the specification (key material) of the private key.
     *
     * @return the private key.
     *
     * @exception InvalidKeySpecException if the given key specification
     * is inappropriate for this key factory to produce a private key.
     */
    public final TPrivateKey generatePrivate(TKeySpec keySpec)
            throws InvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGeneratePrivate(keySpec);
        }
        Exception failure = null;
        TKeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGeneratePrivate(keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeySpecException) {
            throw (InvalidKeySpecException)failure;
        }
        throw new InvalidKeySpecException
                ("Could not generate private key", failure);
    }

    /**
     * Returns a specification (key material) of the given key object.
     * {@code keySpec} identifies the specification class in which
     * the key material should be returned. It could, for example, be
     * {@code DSAPublicKeySpec.class}, to indicate that the
     * key material should be returned in an instance of the
     * {@code DSAPublicKeySpec} class.
     *
     * @param <T> the type of the key specification to be returned
     *
     * @param key the key.
     *
     * @param keySpec the specification class in which
     * the key material should be returned.
     *
     * @return the underlying key specification (key material) in an instance
     * of the requested specification class.
     *
     * @exception InvalidKeySpecException if the requested key specification is
     * inappropriate for the given key, or the given key cannot be processed
     * (e.g., the given key has an unrecognized algorithm or format).
     */
    public final <T extends KeySpec> T getKeySpec(TKey key, Class<T> keySpec)
            throws InvalidKeySpecException {
        if (serviceIterator == null) {
            return spi.engineGetKeySpec(key, keySpec);
        }
        Exception failure = null;
        TKeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineGetKeySpec(key, keySpec);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeySpecException) {
            throw (InvalidKeySpecException)failure;
        }
        throw new InvalidKeySpecException
                ("Could not get key spec", failure);
    }

    /**
     * Translates a key object, whose provider may be unknown or potentially
     * untrusted, into a corresponding key object of this key factory.
     *
     * @param key the key whose provider is unknown or untrusted.
     *
     * @return the translated key.
     *
     * @exception InvalidKeyException if the given key cannot be processed
     * by this key factory.
     */
    public final Key translateKey(TKey key) throws InvalidKeyException {
        if (serviceIterator == null) {
            return spi.engineTranslateKey(key);
        }
        Exception failure = null;
        TKeyFactorySpi mySpi = spi;
        do {
            try {
                return mySpi.engineTranslateKey(key);
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi);
            }
        } while (mySpi != null);
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        if (failure instanceof InvalidKeyException) {
            throw (InvalidKeyException)failure;
        }
        throw new InvalidKeyException
                ("Could not translate key", failure);
    }

}
