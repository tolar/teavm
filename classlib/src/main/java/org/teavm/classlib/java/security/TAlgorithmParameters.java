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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.lang.TString;

public class TAlgorithmParameters {

    // The provider
    private TProvider provider;

    // The provider implementation (delegate)
    private TAlgorithmParametersSpi paramSpi;

    // The algorithm
    private TString algorithm;

    // Has this object been initialized?
    private boolean initialized = false;

    /**
     * Creates an AlgorithmParameters object.
     *
     * @param paramSpi the delegate
     * @param provider the provider
     * @param algorithm the algorithm
     */
    protected TAlgorithmParameters(TAlgorithmParametersSpi paramSpi,
            TProvider provider, TString algorithm)
    {
        this.paramSpi = paramSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    /**
     * Returns the name of the algorithm associated with this parameter object.
     *
     * @return the algorithm name.
     */
    public final TString getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns a parameter object for the specified algorithm.
     *
     * <p> This method traverses the list of registered security Providers,
     * starting with the most preferred Provider.
     * A new AlgorithmParameters object encapsulating the
     * AlgorithmParametersSpi implementation from the first
     * Provider that supports the specified algorithm is returned.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * <p> The returned parameter object must be initialized via a call to
     * {@code init}, using an appropriate parameter specification or
     * parameter encoding.
     *
     * @param algorithm the name of the algorithm requested.
     * See the AlgorithmParameters section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @return the new parameter object.
     *
     * @exception NoSuchAlgorithmException if no Provider supports an
     *          AlgorithmParametersSpi implementation for the
     *          specified algorithm.
     *
     * @see Provider
     */
    public static TAlgorithmParameters getInstance(TString algorithm)
            throws TNoSuchAlgorithmException {
        try {
            Object[] objs = TSecurity.getImpl(algorithm, TString.wrap("TAlgorithmParameters"),
                    (TString)null);
            return new TAlgorithmParameters((TAlgorithmParametersSpi)objs[0],
                    (TProvider)objs[1],
                    algorithm);
        } catch(TNoSuchProviderException e) {
            throw new TNoSuchAlgorithmException(TString.wrap(algorithm + " not found"));
        }
    }

    /**
     * Returns a parameter object for the specified algorithm.
     *
     * <p> A new AlgorithmParameters object encapsulating the
     * AlgorithmParametersSpi implementation from the specified provider
     * is returned.  The specified provider must be registered
     * in the security provider list.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * <p>The returned parameter object must be initialized via a call to
     * {@code init}, using an appropriate parameter specification or
     * parameter encoding.
     *
     * @param algorithm the name of the algorithm requested.
     * See the AlgorithmParameters section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @param provider the name of the provider.
     *
     * @return the new parameter object.
     *
     * @exception NoSuchAlgorithmException if an AlgorithmParametersSpi
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
    public static TAlgorithmParameters getInstance(TString algorithm,
            TString provider)
            throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = TSecurity.getImpl(algorithm, TString.wrap("AlgorithmParameters"),
                provider);
        return new TAlgorithmParameters((TAlgorithmParametersSpi)objs[0],
                (TProvider)objs[1],
                algorithm);
    }

    /**
     * Returns a parameter object for the specified algorithm.
     *
     * <p> A new AlgorithmParameters object encapsulating the
     * AlgorithmParametersSpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * <p>The returned parameter object must be initialized via a call to
     * {@code init}, using an appropriate parameter specification or
     * parameter encoding.
     *
     * @param algorithm the name of the algorithm requested.
     * See the AlgorithmParameters section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#AlgorithmParameters">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @param provider the name of the provider.
     *
     * @return the new parameter object.
     *
     * @exception NoSuchAlgorithmException if an AlgorithmParameterGeneratorSpi
     *          implementation for the specified algorithm is not available
     *          from the specified Provider object.
     *
     * @exception IllegalArgumentException if the provider is null.
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static TAlgorithmParameters getInstance(TString algorithm,
            TProvider provider)
            throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = TSecurity.getImpl(algorithm, TString.wrap("TAlgorithmParameters"),
                provider);
        return new TAlgorithmParameters((TAlgorithmParametersSpi)objs[0],
                (TProvider)objs[1],
                algorithm);
    }

    /**
     * Returns the provider of this parameter object.
     *
     * @return the provider of this parameter object
     */
    public final TProvider getProvider() {
        return this.provider;
    }

    /**
     * Initializes this parameter object using the parameters
     * specified in {@code paramSpec}.
     *
     * @param paramSpec the parameter specification.
     *
     * @exception InvalidParameterSpecException if the given parameter
     * specification is inappropriate for the initialization of this parameter
     * object, or if this parameter object has already been initialized.
     */
    public final void init(AlgorithmParameterSpec paramSpec)
            throws InvalidParameterSpecException
    {
        if (this.initialized)
            throw new InvalidParameterSpecException("already initialized");
        paramSpi.engineInit(paramSpec);
        this.initialized = true;
    }

    /**
     * Imports the specified parameters and decodes them according to the
     * primary decoding format for parameters. The primary decoding
     * format for parameters is ASN.1, if an ASN.1 specification for this type
     * of parameters exists.
     *
     * @param params the encoded parameters.
     *
     * @exception IOException on decoding errors, or if this parameter object
     * has already been initialized.
     */
    public final void init(byte[] params) throws TIOException {
        if (this.initialized)
            throw new TIOException(TString.wrap("already initialized"));
        paramSpi.engineInit(params);
        this.initialized = true;
    }

    /**
     * Imports the parameters from {@code params} and decodes them
     * according to the specified decoding scheme.
     * If {@code format} is null, the
     * primary decoding format for parameters is used. The primary decoding
     * format is ASN.1, if an ASN.1 specification for these parameters
     * exists.
     *
     * @param params the encoded parameters.
     *
     * @param format the name of the decoding scheme.
     *
     * @exception IOException on decoding errors, or if this parameter object
     * has already been initialized.
     */
    public final void init(byte[] params, String format) throws IOException {
        if (this.initialized)
            throw new IOException("already initialized");
        paramSpi.engineInit(params, format);
        this.initialized = true;
    }

    /**
     * Returns a (transparent) specification of this parameter object.
     * {@code paramSpec} identifies the specification class in which
     * the parameters should be returned. It could, for example, be
     * {@code DSAParameterSpec.class}, to indicate that the
     * parameters should be returned in an instance of the
     * {@code DSAParameterSpec} class.
     *
     * @param <T> the type of the parameter specification to be returrned
     * @param paramSpec the specification class in which
     * the parameters should be returned.
     *
     * @return the parameter specification.
     *
     * @exception InvalidParameterSpecException if the requested parameter
     * specification is inappropriate for this parameter object, or if this
     * parameter object has not been initialized.
     */
    public final <T extends AlgorithmParameterSpec>
    T getParameterSpec(Class<T> paramSpec)
            throws InvalidParameterSpecException
    {
        if (this.initialized == false) {
            throw new InvalidParameterSpecException("not initialized");
        }
        return paramSpi.engineGetParameterSpec(paramSpec);
    }

    /**
     * Returns the parameters in their primary encoding format.
     * The primary encoding format for parameters is ASN.1, if an ASN.1
     * specification for this type of parameters exists.
     *
     * @return the parameters encoded using their primary encoding format.
     *
     * @exception IOException on encoding errors, or if this parameter object
     * has not been initialized.
     */
    public final byte[] getEncoded() throws TIOException
    {
        if (this.initialized == false) {
            throw new TIOException(TString.wrap("not initialized"));
        }
        return paramSpi.engineGetEncoded();
    }

    /**
     * Returns the parameters encoded in the specified scheme.
     * If {@code format} is null, the
     * primary encoding format for parameters is used. The primary encoding
     * format is ASN.1, if an ASN.1 specification for these parameters
     * exists.
     *
     * @param format the name of the encoding format.
     *
     * @return the parameters encoded using the specified encoding scheme.
     *
     * @exception IOException on encoding errors, or if this parameter object
     * has not been initialized.
     */
    public final byte[] getEncoded(String format) throws IOException
    {
        if (this.initialized == false) {
            throw new IOException("not initialized");
        }
        return paramSpi.engineGetEncoded(format);
    }

    /**
     * Returns a formatted string describing the parameters.
     *
     * @return a formatted string describing the parameters, or null if this
     * parameter object has not been initialized.
     */
    public final String toString() {
        if (this.initialized == false) {
            return null;
        }
        return paramSpi.engineToString();
    }
}