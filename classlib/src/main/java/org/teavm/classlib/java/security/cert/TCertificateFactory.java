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
package org.teavm.classlib.java.security.cert;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.security.TProvider;
import org.teavm.classlib.sun.security.jca.TGetInstance;

public class TCertificateFactory {

    // The certificate type
    private String type;

    // The provider
    private TProvider provider;

    // The provider implementation
    private TCertificateFactorySpi certFacSpi;

    /**
     * Creates a CertificateFactory object of the given type, and encapsulates
     * the given provider implementation (SPI object) in it.
     *
     * @param certFacSpi the provider implementation.
     * @param provider the provider.
     * @param type the certificate type.
     */
    protected TCertificateFactory(TCertificateFactorySpi certFacSpi,
            TProvider provider, String type)
    {
        this.certFacSpi = certFacSpi;
        this.provider = provider;
        this.type = type;
    }

    /**
     * Returns a certificate factory object that implements the
     * specified certificate type.
     *
     * <p> This method traverses the list of registered security Providers,
     * starting with the most preferred Provider.
     * A new CertificateFactory object encapsulating the
     * CertificateFactorySpi implementation from the first
     * Provider that supports the specified type is returned.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param type the name of the requested certificate type.
     * See the CertificateFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard certificate types.
     *
     * @return a certificate factory object for the specified type.
     *
     * @exception CertificateException if no Provider supports a
     *          CertificateFactorySpi implementation for the
     *          specified type.
     *
     * @see java.security.Provider
     */
    public static final TCertificateFactory getInstance(String type)
            throws CertificateException {
        try {
            TGetInstance.Instance instance = TGetInstance.getInstance("CertificateFactory",
                    TCertificateFactorySpi.class, type);
            return new TCertificateFactory((TCertificateFactorySpi)instance.impl,
                    instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    /**
     * Returns a certificate factory object for the specified
     * certificate type.
     *
     * <p> A new CertificateFactory object encapsulating the
     * CertificateFactorySpi implementation from the specified provider
     * is returned.  The specified provider must be registered
     * in the security provider list.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * @param type the certificate type.
     * See the CertificateFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard certificate types.
     *
     * @param provider the name of the provider.
     *
     * @return a certificate factory object for the specified type.
     *
     * @exception CertificateException if a CertificateFactorySpi
     *          implementation for the specified algorithm is not
     *          available from the specified provider.
     *
     * @exception NoSuchProviderException if the specified provider is not
     *          registered in the security provider list.
     *
     * @exception IllegalArgumentException if the provider name is null
     *          or empty.
     *
     * @see java.security.Provider
     */
    public static final TCertificateFactory getInstance(String type,
            String provider) throws CertificateException,
            NoSuchProviderException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory",
                    CertificateFactorySpi.class, type, provider);
            return new TCertificateFactory((CertificateFactorySpi)instance.impl,
                    instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    /**
     * Returns a certificate factory object for the specified
     * certificate type.
     *
     * <p> A new CertificateFactory object encapsulating the
     * CertificateFactorySpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * @param type the certificate type.
     * See the CertificateFactory section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertificateFactory">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard certificate types.
     * @param provider the provider.
     *
     * @return a certificate factory object for the specified type.
     *
     * @exception CertificateException if a CertificateFactorySpi
     *          implementation for the specified algorithm is not available
     *          from the specified Provider object.
     *
     * @exception IllegalArgumentException if the {@code provider} is
     *          null.
     *
     * @see java.security.Provider
     *
     * @since 1.4
     */
    public static final TCertificateFactory getInstance(String type,
            Provider provider) throws CertificateException {
        try {
            GetInstance.Instance instance = GetInstance.getInstance("CertificateFactory",
                    CertificateFactorySpi.class, type, provider);
            return new TCertificateFactory((CertificateFactorySpi)instance.impl,
                    instance.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(type + " not found", e);
        }
    }

    /**
     * Returns the provider of this certificate factory.
     *
     * @return the provider of this certificate factory.
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * Returns the name of the certificate type associated with this
     * certificate factory.
     *
     * @return the name of the certificate type associated with this
     * certificate factory.
     */
    public final String getType() {
        return this.type;
    }

    /**
     * Generates a certificate object and initializes it with
     * the data read from the input stream {@code inStream}.
     *
     * <p>In order to take advantage of the specialized certificate format
     * supported by this certificate factory,
     * the returned certificate object can be typecast to the corresponding
     * certificate class. For example, if this certificate
     * factory implements X.509 certificates, the returned certificate object
     * can be typecast to the {@code X509Certificate} class.
     *
     * <p>In the case of a certificate factory for X.509 certificates, the
     * certificate provided in {@code inStream} must be DER-encoded and
     * may be supplied in binary or printable (Base64) encoding. If the
     * certificate is provided in Base64 encoding, it must be bounded at
     * the beginning by -----BEGIN CERTIFICATE-----, and must be bounded at
     * the end by -----END CERTIFICATE-----.
     *
     * <p>Note that if the given input stream does not support
     * {@link java.io.InputStream#mark(int) mark} and
     * {@link java.io.InputStream#reset() reset}, this method will
     * consume the entire input stream. Otherwise, each call to this
     * method consumes one certificate and the read position of the
     * input stream is positioned to the next available byte after
     * the inherent end-of-certificate marker. If the data in the input stream
     * does not contain an inherent end-of-certificate marker (other
     * than EOF) and there is trailing data after the certificate is parsed, a
     * {@code CertificateException} is thrown.
     *
     * @param inStream an input stream with the certificate data.
     *
     * @return a certificate object initialized with the data
     * from the input stream.
     *
     * @exception CertificateException on parsing errors.
     */
    public final TCertificate generateCertificate(TInputStream inStream)
            throws TCertificateException
    {
        return certFacSpi.engineGenerateCertificate(inStream);
    }

    /**
     * Returns an iteration of the {@code CertPath} encodings supported
     * by this certificate factory, with the default encoding first. See
     * the CertPath Encodings section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard encoding names and their formats.
     * <p>
     * Attempts to modify the returned {@code Iterator} via its
     * {@code remove} method result in an
     * {@code UnsupportedOperationException}.
     *
     * @return an {@code Iterator} over the names of the supported
     *         {@code CertPath} encodings (as {@code String}s)
     * @since 1.4
     */
    public final Iterator<String> getCertPathEncodings() {
        return(certFacSpi.engineGetCertPathEncodings());
    }

    /**
     * Generates a {@code CertPath} object and initializes it with
     * the data read from the {@code InputStream} inStream. The data
     * is assumed to be in the default encoding. The name of the default
     * encoding is the first element of the {@code Iterator} returned by
     * the {@link #getCertPathEncodings getCertPathEncodings} method.
     *
     * @param inStream an {@code InputStream} containing the data
     * @return a {@code CertPath} initialized with the data from the
     *   {@code InputStream}
     * @exception CertificateException if an exception occurs while decoding
     * @since 1.4
     */
    public final CertPath generateCertPath(InputStream inStream)
            throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(inStream));
    }

    /**
     * Generates a {@code CertPath} object and initializes it with
     * the data read from the {@code InputStream} inStream. The data
     * is assumed to be in the specified encoding. See
     * the CertPath Encodings section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#CertPathEncodings">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard encoding names and their formats.
     *
     * @param inStream an {@code InputStream} containing the data
     * @param encoding the encoding used for the data
     * @return a {@code CertPath} initialized with the data from the
     *   {@code InputStream}
     * @exception CertificateException if an exception occurs while decoding or
     *   the encoding requested is not supported
     * @since 1.4
     */
    public final CertPath generateCertPath(InputStream inStream,
            String encoding) throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(inStream, encoding));
    }

    /**
     * Generates a {@code CertPath} object and initializes it with
     * a {@code List} of {@code Certificate}s.
     * <p>
     * The certificates supplied must be of a type supported by the
     * {@code CertificateFactory}. They will be copied out of the supplied
     * {@code List} object.
     *
     * @param certificates a {@code List} of {@code Certificate}s
     * @return a {@code CertPath} initialized with the supplied list of
     *   certificates
     * @exception CertificateException if an exception occurs
     * @since 1.4
     */
    public final CertPath
    generateCertPath(List<? extends Certificate> certificates)
            throws CertificateException
    {
        return(certFacSpi.engineGenerateCertPath(certificates));
    }

    /**
     * Returns a (possibly empty) collection view of the certificates read
     * from the given input stream {@code inStream}.
     *
     * <p>In order to take advantage of the specialized certificate format
     * supported by this certificate factory, each element in
     * the returned collection view can be typecast to the corresponding
     * certificate class. For example, if this certificate
     * factory implements X.509 certificates, the elements in the returned
     * collection can be typecast to the {@code X509Certificate} class.
     *
     * <p>In the case of a certificate factory for X.509 certificates,
     * {@code inStream} may contain a sequence of DER-encoded certificates
     * in the formats described for
     * {@link #generateCertificate(java.io.InputStream) generateCertificate}.
     * In addition, {@code inStream} may contain a PKCS#7 certificate
     * chain. This is a PKCS#7 <i>SignedData</i> object, with the only
     * significant field being <i>certificates</i>. In particular, the
     * signature and the contents are ignored. This format allows multiple
     * certificates to be downloaded at once. If no certificates are present,
     * an empty collection is returned.
     *
     * <p>Note that if the given input stream does not support
     * {@link java.io.InputStream#mark(int) mark} and
     * {@link java.io.InputStream#reset() reset}, this method will
     * consume the entire input stream.
     *
     * @param inStream the input stream with the certificates.
     *
     * @return a (possibly empty) collection view of
     * java.security.cert.Certificate objects
     * initialized with the data from the input stream.
     *
     * @exception CertificateException on parsing errors.
     */
    public final Collection<? extends Certificate> generateCertificates
    (InputStream inStream) throws CertificateException {
        return certFacSpi.engineGenerateCertificates(inStream);
    }

    /**
     * Generates a certificate revocation list (CRL) object and initializes it
     * with the data read from the input stream {@code inStream}.
     *
     * <p>In order to take advantage of the specialized CRL format
     * supported by this certificate factory,
     * the returned CRL object can be typecast to the corresponding
     * CRL class. For example, if this certificate
     * factory implements X.509 CRLs, the returned CRL object
     * can be typecast to the {@code X509CRL} class.
     *
     * <p>Note that if the given input stream does not support
     * {@link java.io.InputStream#mark(int) mark} and
     * {@link java.io.InputStream#reset() reset}, this method will
     * consume the entire input stream. Otherwise, each call to this
     * method consumes one CRL and the read position of the input stream
     * is positioned to the next available byte after the inherent
     * end-of-CRL marker. If the data in the
     * input stream does not contain an inherent end-of-CRL marker (other
     * than EOF) and there is trailing data after the CRL is parsed, a
     * {@code CRLException} is thrown.
     *
     * @param inStream an input stream with the CRL data.
     *
     * @return a CRL object initialized with the data
     * from the input stream.
     *
     * @exception CRLException on parsing errors.
     */
    public final CRL generateCRL(InputStream inStream)
            throws CRLException
    {
        return certFacSpi.engineGenerateCRL(inStream);
    }

    /**
     * Returns a (possibly empty) collection view of the CRLs read
     * from the given input stream {@code inStream}.
     *
     * <p>In order to take advantage of the specialized CRL format
     * supported by this certificate factory, each element in
     * the returned collection view can be typecast to the corresponding
     * CRL class. For example, if this certificate
     * factory implements X.509 CRLs, the elements in the returned
     * collection can be typecast to the {@code X509CRL} class.
     *
     * <p>In the case of a certificate factory for X.509 CRLs,
     * {@code inStream} may contain a sequence of DER-encoded CRLs.
     * In addition, {@code inStream} may contain a PKCS#7 CRL
     * set. This is a PKCS#7 <i>SignedData</i> object, with the only
     * significant field being <i>crls</i>. In particular, the
     * signature and the contents are ignored. This format allows multiple
     * CRLs to be downloaded at once. If no CRLs are present,
     * an empty collection is returned.
     *
     * <p>Note that if the given input stream does not support
     * {@link java.io.InputStream#mark(int) mark} and
     * {@link java.io.InputStream#reset() reset}, this method will
     * consume the entire input stream.
     *
     * @param inStream the input stream with the CRLs.
     *
     * @return a (possibly empty) collection view of
     * java.security.cert.CRL objects initialized with the data from the input
     * stream.
     *
     * @exception CRLException on parsing errors.
     */
    public final Collection<? extends CRL> generateCRLs(InputStream inStream)
            throws CRLException {
        return certFacSpi.engineGenerateCRLs(inStream);
    }
}
