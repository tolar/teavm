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

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;

import org.teavm.classlib.java.io.TByteArrayOutputStream;
import org.teavm.classlib.java.lang.TBoolean;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.nio.TByteBuffer;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.security.cert.TX509Certificate;
import org.teavm.classlib.java.security.spec.TAlgorithmParameterSpec;
import org.teavm.classlib.java.util.THashMap;
import org.teavm.classlib.java.util.TIterator;
import org.teavm.classlib.java.util.TList;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.javax.crypto.TBadPaddingException;
import org.teavm.classlib.javax.crypto.TCipher;
import org.teavm.classlib.javax.crypto.TIllegalBlockSizeException;
import org.teavm.classlib.javax.crypto.TNoSuchPaddingException;
import org.teavm.classlib.sun.security.jca.TGetInstance;
import org.teavm.classlib.sun.security.jca.TServiceId;

public abstract class TSignature extends TSignatureSpi {


    /*
     * The algorithm for this signature object.
     * This value is used to map an OID to the particular algorithm.
     * The mapping is done in AlgorithmObject.algOID(String algorithm)
     */
    private TString algorithm;

    // The provider
    TProvider provider;

    /**
     * Possible {@link #state} value, signifying that
     * this signature object has not yet been initialized.
     */
    protected final static int UNINITIALIZED = 0;

    /**
     * Possible {@link #state} value, signifying that
     * this signature object has been initialized for signing.
     */
    protected final static int SIGN = 2;

    /**
     * Possible {@link #state} value, signifying that
     * this signature object has been initialized for verification.
     */
    protected final static int VERIFY = 3;

    /**
     * Current state of this signature object.
     */
    protected int state = UNINITIALIZED;

    /**
     * Creates a Signature object for the specified algorithm.
     *
     * @param algorithm the standard string name of the algorithm.
     * See the Signature section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     */
    protected TSignature(TString algorithm) {
        this.algorithm = algorithm;
    }

    // name of the special signature alg
    private final static TString RSA_SIGNATURE = TString.wrap("NONEwithRSA");

    // name of the equivalent cipher alg
    private final static TString RSA_CIPHER = TString.wrap("RSA/ECB/PKCS1Padding");

    // all the services we need to lookup for compatibility with TCipher
    private final static List<TServiceId> rsaIds = Arrays.asList(
            new TServiceId[] {
                    new TServiceId("Signature", "NONEwithRSA"),
                    new TServiceId("TCipher", "RSA/ECB/PKCS1Padding"),
                    new TServiceId("TCipher", "RSA/ECB"),
                    new TServiceId("TCipher", "RSA//PKCS1Padding"),
                    new TServiceId("TCipher", "RSA"),
            }
    );

    public static TSignature getInstance(TString algorithm)
            throws TNoSuchAlgorithmException {
        TList<TProvider.Service> list;
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            list = TGetInstance.getServices(rsaIds);
        } else {
            list = TGetInstance.getServices(TString.wrap("Signature"), algorithm);
        }
        TIterator<TProvider.Service> t = list.iterator();
        if (t.hasNext() == false) {
            throw new TNoSuchAlgorithmException
                    (TString.wrap(algorithm + " Signature not available"));
        }
        // try services until we find an Spi or a working Signature subclass
        TNoSuchAlgorithmException failure;
        do {
            TProvider.Service s = t.next();
            if (isSpi(s)) {
                return new TSignature.Delegate(s, t, algorithm);
            } else {
                // must be a subclass of Signature, disable dynamic selection
                try {
                    TGetInstance.Instance instance =
                            TGetInstance.getInstance(s, TSignatureSpi.class);
                    return getInstance(instance, algorithm);
                } catch (NoSuchAlgorithmException e) {
                    failure = e;
                }
            }
        } while (t.hasNext());
        throw failure;
    }

    private static TSignature getInstance(TGetInstance.Instance instance, TString algorithm) {
        TSignature sig;
        if (instance.impl instanceof TSignature) {
            sig = (TSignature)instance.impl;
            sig.algorithm = algorithm;
        } else {
            TSignatureSpi spi = (TSignatureSpi)instance.impl;
            sig = new TSignature.Delegate(spi, algorithm);
        }
        sig.provider = instance.provider;
        return sig;
    }

    private final static TMap<TString,TBoolean> signatureInfo;

    static {
        signatureInfo = new THashMap();
        TBoolean TRUE = TBoolean.TRUE;
        // pre-initialize with values for our SignatureSpi implementations
        signatureInfo.put(TString.wrap("sun.security.provider.DSA$RawDSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.provider.DSA$SHA1withDSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$MD2withRSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$MD5withRSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$SHA1withRSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$SHA256withRSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$SHA384withRSA"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.rsa.RSASignature$SHA512withRSA"), TRUE);
        signatureInfo.put(TString.wrap("com.sun.net.ssl.internal.ssl.RSASignature"), TRUE);
        signatureInfo.put(TString.wrap("sun.security.pkcs11.P11Signature"), TRUE);
    }

    private static boolean isSpi(TProvider.Service s) {
        if (s.getType().equals("TCipher")) {
            // must be a TCipherSpi, which we can wrap with the CipherAdapter
            return true;
        }
        TString className = s.getClassName();
        TBoolean result = signatureInfo.get(className);
        if (result == null) {
            try {
                Object instance = s.newInstance(null);
                // Signature extends SignatureSpi
                // so it is a "real" Spi if it is an
                // instance of SignatureSpi but not Signature
                boolean r = (instance instanceof TSignatureSpi)
                        && (instance instanceof java.security.Signature == false);
                result = TBoolean.valueOf(r);
                signatureInfo.put(className, result);
            } catch (Exception e) {
                // something is wrong, assume not an SPI
                return false;
            }
        }
        return result.booleanValue();
    }

    public static TSignature getInstance(TString algorithm, TString provider)
            throws TNoSuchAlgorithmException, TNoSuchProviderException {
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            // exception compatibility with existing code
            if ((provider == null) || (provider.length() == 0)) {
                throw new IllegalArgumentException("missing provider");
            }
            TProvider p = TSecurity.getProvider(provider);
            if (p == null) {
                throw new TNoSuchProviderException
                        (TString.wrap("no such provider: " + provider));
            }
            return getInstanceRSA(p);
        }
        TGetInstance.Instance instance = TGetInstance.getInstance
                (TString.wrap("Signature"), TSignatureSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }

    /**
     * Returns a Signature object that implements the specified
     * signature algorithm.
     *
     * <p> A new Signature object encapsulating the
     * SignatureSpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * @param algorithm the name of the algorithm requested.
     * See the Signature section in the <a href=
     * "{@docRoot}/../technotes/guides/security/StandardNames.html#Signature">
     * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
     * for information about standard algorithm names.
     *
     * @param provider the provider.
     *
     * @return the new Signature object.
     *
     * @exception NoSuchAlgorithmException if a SignatureSpi
     *          implementation for the specified algorithm is not available
     *          from the specified Provider object.
     *
     * @exception IllegalArgumentException if the provider is null.
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static TSignature getInstance(TString algorithm, TProvider provider)
            throws TNoSuchAlgorithmException {
        if (algorithm.equalsIgnoreCase(RSA_SIGNATURE)) {
            // exception compatibility with existing code
            if (provider == null) {
                throw new IllegalArgumentException("missing provider");
            }
            return getInstanceRSA(provider);
        }
        TGetInstance.Instance instance = TGetInstance.getInstance
                (TString.wrap("Signature"), TSignatureSpi.class, algorithm, provider);
        return getInstance(instance, algorithm);
    }

    // return an implementation for NONEwithRSA, which is a special case
    // because of the TCipher.RSA/ECB/PKCS1Padding compatibility wrapper
    private static TSignature getInstanceRSA(TProvider p)
            throws NoSuchAlgorithmException {
        // try Signature first
        TProvider.Service s = p.getService(TString.wrap("Signature"), RSA_SIGNATURE);
        if (s != null) {
            TGetInstance.Instance instance = TGetInstance.getInstance(s, TSignatureSpi.class);
            return getInstance(instance, RSA_SIGNATURE);
        }
        // check TCipher
        try {
            TCipher c = TCipher.getInstance(RSA_CIPHER, p);
            return new TSignature.Delegate(new TSignature.CipherAdapter(c), RSA_SIGNATURE);
        } catch (TGeneralSecurityException e) {
            // throw Signature style exception message to avoid confusion,
            // but append TCipher exception as cause
            throw new NoSuchAlgorithmException("no such algorithm: "
                    + RSA_SIGNATURE + " for provider " + p.getName(), e);
        }
    }

    /**
     * Returns the provider of this signature object.
     *
     * @return the provider of this signature object
     */
    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }

    void chooseFirstProvider() {
        // empty, overridden in Delegate
    }

    /**
     * Initializes this object for verification. If this method is called
     * again with a different argument, it negates the effect
     * of this call.
     *
     * @param publicKey the public key of the identity whose signature is
     * going to be verified.
     *
     * @exception TInvalidKeyException if the key is invalid.
     */
    public final void initVerify(TPublicKey publicKey)
            throws TInvalidKeyException {
        engineInitVerify(publicKey);
        state = VERIFY;


    }

    /**
     * Initializes this object for verification, using the public key from
     * the given certificate.
     * <p>If the certificate is of type X.509 and has a <i>key usage</i>
     * extension field marked as critical, and the value of the <i>key usage</i>
     * extension field implies that the public key in
     * the certificate and its corresponding private key are not
     * supposed to be used for digital signatures, an
     * {@code InvalidKeyException} is thrown.
     *
     * @param certificate the certificate of the identity whose signature is
     * going to be verified.
     *
     * @exception TInvalidKeyException  if the public key in the certificate
     * is not encoded properly or does not include required  parameter
     * information or cannot be used for digital signature purposes.
     * @since 1.3
     */
    public final void initVerify(TCertificate certificate)
            throws TInvalidKeyException {
        // If the certificate is of type X509Certificate,
        // we should check whether it has a Key Usage
        // extension marked as critical.
        if (certificate instanceof TX509Certificate) {
            // Check whether the cert has a key usage extension
            // marked as a critical extension.
            // The OID for KeyUsage extension is 2.5.29.15.
            TX509Certificate cert = (TX509Certificate)certificate;
            Set<String> critSet = cert.getCriticalExtensionOIDs();

            if (critSet != null && !critSet.isEmpty()
                    && critSet.contains("2.5.29.15")) {
                boolean[] keyUsageInfo = cert.getKeyUsage();
                // keyUsageInfo[0] is for digitalSignature.
                if ((keyUsageInfo != null) && (keyUsageInfo[0] == false))
                    throw new TInvalidKeyException(TString.wrap("Wrong key usage"));
            }
        }

        TPublicKey publicKey = certificate.getPublicKey();
        engineInitVerify(publicKey);
        state = VERIFY;


    }

    /**
     * Initialize this object for signing. If this method is called
     * again with a different argument, it negates the effect
     * of this call.
     *
     * @param privateKey the private key of the identity whose signature
     * is going to be generated.
     *
     * @exception TInvalidKeyException if the key is invalid.
     */
    public final void initSign(TPrivateKey privateKey)
            throws TInvalidKeyException {
        engineInitSign(privateKey);
        state = SIGN;

    }

    /**
     * Initialize this object for signing. If this method is called
     * again with a different argument, it negates the effect
     * of this call.
     *
     * @param privateKey the private key of the identity whose signature
     * is going to be generated.
     *
     * @param random the source of randomness for this signature.
     *
     * @exception TInvalidKeyException if the key is invalid.
     */
    public final void initSign(TPrivateKey privateKey, TSecureRandom random)
            throws TInvalidKeyException {
        engineInitSign(privateKey, random);
        state = SIGN;

    }

    /**
     * Returns the signature bytes of all the data updated.
     * The format of the signature depends on the underlying
     * signature scheme.
     *
     * <p>A call to this method resets this signature object to the state
     * it was in when previously initialized for signing via a
     * call to {@code initSign(PrivateKey)}. That is, the object is
     * reset and available to generate another signature from the same
     * signer, if desired, via new calls to {@code update} and
     * {@code sign}.
     *
     * @return the signature bytes of the signing operation's result.
     *
     * @exception SignatureException if this signature object is not
     * initialized properly or if this signature algorithm is unable to
     * process the input data provided.
     */
    public final byte[] sign() throws TSignatureException {
        if (state == SIGN) {
            return engineSign();
        }
        throw new TSignatureException("object not initialized for " +
                "signing");
    }

    /**
     * Finishes the signature operation and stores the resulting signature
     * bytes in the provided buffer {@code outbuf}, starting at
     * {@code offset}.
     * The format of the signature depends on the underlying
     * signature scheme.
     *
     * <p>This signature object is reset to its initial state (the state it
     * was in after a call to one of the {@code initSign} methods) and
     * can be reused to generate further signatures with the same private key.
     *
     * @param outbuf buffer for the signature result.
     *
     * @param offset offset into {@code outbuf} where the signature is
     * stored.
     *
     * @param len number of bytes within {@code outbuf} allotted for the
     * signature.
     *
     * @return the number of bytes placed into {@code outbuf}.
     *
     * @exception SignatureException if this signature object is not
     * initialized properly, if this signature algorithm is unable to
     * process the input data provided, or if {@code len} is less
     * than the actual signature length.
     *
     * @since 1.2
     */
    public final int sign(byte[] outbuf, int offset, int len)
            throws TSignatureException {
        if (outbuf == null) {
            throw new IllegalArgumentException("No output buffer given");
        }
        if (offset < 0 || len < 0) {
            throw new IllegalArgumentException("offset or len is less than 0");
        }
        if (outbuf.length - offset < len) {
            throw new IllegalArgumentException
                    ("Output buffer too small for specified offset and length");
        }
        if (state != SIGN) {
            throw new TSignatureException(TString.wrap("object not initialized for " +
                    "signing"));
        }
        return engineSign(outbuf, offset, len);
    }

    public final boolean verify(byte[] signature) throws TSignatureException {
        if (state == VERIFY) {
            return engineVerify(signature);
        }
        throw new TSignatureException(TString.wrap("object not initialized for " +
                "verification"));
    }

    public final boolean verify(byte[] signature, int offset, int length)
            throws TSignatureException {
        if (state == VERIFY) {
            if (signature == null) {
                throw new IllegalArgumentException("signature is null");
            }
            if (offset < 0 || length < 0) {
                throw new IllegalArgumentException
                        ("offset or length is less than 0");
            }
            if (signature.length - offset < length) {
                throw new IllegalArgumentException
                        ("signature too small for specified offset and length");
            }

            return engineVerify(signature, offset, length);
        }
        throw new TSignatureException(TString.wrap("object not initialized for " +
                "verification"));
    }

    public final void update(byte b) throws TSignatureException {
        if (state == VERIFY || state == SIGN) {
            engineUpdate(b);
        } else {
            throw new TSignatureException(TString.wrap("object not initialized for "
                    + "signature or verification"));
        }
    }

    public final void update(byte[] data) throws TSignatureException {
        update(data, 0, data.length);
    }

    public final void update(byte[] data, int off, int len)
            throws TSignatureException {
        if (state == SIGN || state == VERIFY) {
            if (data == null) {
                throw new IllegalArgumentException("data is null");
            }
            if (off < 0 || len < 0) {
                throw new IllegalArgumentException("off or len is less than 0");
            }
            if (data.length - off < len) {
                throw new IllegalArgumentException
                        ("data too small for specified offset and length");
            }
            engineUpdate(data, off, len);
        } else {
            throw new TSignatureException(TString.wrap("object not initialized for "
                    + "signature or verification"));
        }
    }

    public final void update(TByteBuffer data) throws TSignatureException {
        if ((state != SIGN) && (state != VERIFY)) {
            throw new TSignatureException(TString.wrap("object not initialized for "
                    + "signature or verification"));
        }
        if (data == null) {
            throw new NullPointerException();
        }
        engineUpdate(data);
    }

    /**
     * Returns the name of the algorithm for this signature object.
     *
     * @return the name of the algorithm for this signature object.
     */
    public final String getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns a string representation of this signature object,
     * providing information that includes the state of the object
     * and the name of the algorithm used.
     *
     * @return a string representation of this signature object.
     */
    public String toString() {
        String initState = "";
        switch (state) {
            case UNINITIALIZED:
                initState = "<not initialized>";
                break;
            case VERIFY:
                initState = "<initialized for verifying>";
                break;
            case SIGN:
                initState = "<initialized for signing>";
                break;
        }
        return "Signature object: " + getAlgorithm() + initState;
    }

    /**
     * Sets the specified algorithm parameter to the specified value.
     * This method supplies a general-purpose mechanism through
     * which it is possible to set the various parameters of this object.
     * A parameter may be any settable parameter for the algorithm, such as
     * a parameter size, or a source of random bits for signature generation
     * (if appropriate), or an indication of whether or not to perform
     * a specific but optional computation. A uniform algorithm-specific
     * naming scheme for each parameter is desirable but left unspecified
     * at this time.
     *
     * @param param the string identifier of the parameter.
     * @param value the parameter value.
     *
     * @exception TInvalidParameterException if {@code param} is an
     * invalid parameter for this signature algorithm engine,
     * the parameter is already set
     * and cannot be set again, a security exception occurs, and so on.
     *
     * @see #getParameter
     *
     * @deprecated Use
     * {@link #setParameter(java.security.spec.AlgorithmParameterSpec)
     * setParameter}.
     */
    @Deprecated
    public final void setParameter(String param, Object value)
            throws TInvalidParameterException {
        engineSetParameter(param, value);
    }

    /**
     * Initializes this signature engine with the specified parameter set.
     *
     * @param params the parameters
     *
     * @exception InvalidAlgorithmParameterException if the given parameters
     * are inappropriate for this signature engine
     *
     * @see #getParameters
     */
    public final void setParameter(TAlgorithmParameterSpec params)
            throws TInvalidAlgorithmParameterException {
        engineSetParameter(params);
    }

    /**
     * Returns the parameters used with this signature object.
     *
     * <p>The returned parameters may be the same that were used to initialize
     * this signature, or may contain a combination of default and randomly
     * generated parameter values used by the underlying signature
     * implementation if this signature requires algorithm parameters but
     * was not initialized with any.
     *
     * @return the parameters used with this signature, or null if this
     * signature does not use any parameters.
     *
     * @see #setParameter(AlgorithmParameterSpec)
     * @since 1.4
     */
    public final TAlgorithmParameters getParameters() {
        return engineGetParameters();
    }

    /**
     * Gets the value of the specified algorithm parameter. This method
     * supplies a general-purpose mechanism through which it is possible to
     * get the various parameters of this object. A parameter may be any
     * settable parameter for the algorithm, such as a parameter size, or
     * a source of random bits for signature generation (if appropriate),
     * or an indication of whether or not to perform a specific but optional
     * computation. A uniform algorithm-specific naming scheme for each
     * parameter is desirable but left unspecified at this time.
     *
     * @param param the string name of the parameter.
     *
     * @return the object that represents the parameter value, or null if
     * there is none.
     *
     * @exception TInvalidParameterException if {@code param} is an invalid
     * parameter for this engine, or another exception occurs while
     * trying to get this parameter.
     *
     * @see #setParameter(String, Object)
     *
     * @deprecated
     */
    @Deprecated
    public final Object getParameter(String param)
            throws TInvalidParameterException {
        return engineGetParameter(param);
    }

    /**
     * Returns a clone if the implementation is cloneable.
     *
     * @return a clone if the implementation is cloneable.
     *
     * @exception CloneNotSupportedException if this is called
     * on an implementation that does not support {@code Cloneable}.
     */
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }

    /*
     * The following class allows providers to extend from SignatureSpi
     * rather than from Signature. It represents a Signature with an
     * encapsulated, provider-supplied SPI object (of type SignatureSpi).
     * If the provider implementation is an instance of SignatureSpi, the
     * getInstance() methods above return an instance of this class, with
     * the SPI object encapsulated.
     *
     * Note: All SPI methods from the original Signature class have been
     * moved up the hierarchy into a new class (SignatureSpi), which has
     * been interposed in the hierarchy between the API (Signature)
     * and its original parent (Object).
     */

    @SuppressWarnings("deprecation")
    private static class Delegate extends TSignature {

        // The provider implementation (delegate)
        // filled in once the provider is selected
        private TSignatureSpi sigSpi;

        // lock for mutex during provider selection
        private final Object lock;

        // next service to try in provider selection
        // null once provider is selected
        private TProvider.Service firstService;

        // remaining services to try in provider selection
        // null once provider is selected
        private TIterator<TProvider.Service> serviceIterator;

        // constructor
        Delegate(TSignatureSpi sigSpi, TString algorithm) {
            super(algorithm);
            this.sigSpi = sigSpi;
            this.lock = null; // no lock needed
        }

        // used with delayed provider selection
        Delegate(TProvider.Service service,
                TIterator<TProvider.Service> iterator, TString algorithm) {
            super(algorithm);
            this.firstService = service;
            this.serviceIterator = iterator;
            this.lock = new Object();
        }

        /**
         * Returns a clone if the delegate is cloneable.
         *
         * @return a clone if the delegate is cloneable.
         *
         * @exception CloneNotSupportedException if this is called on a
         * delegate that does not support {@code Cloneable}.
         */
        public Object clone() throws CloneNotSupportedException {
            chooseFirstProvider();
            if (sigSpi instanceof Cloneable) {
                TSignatureSpi sigSpiClone = (TSignatureSpi)sigSpi.clone();
                // Because 'algorithm' and 'provider' are private
                // members of our supertype, we must perform a cast to
                // access them.
                TSignature that =
                        new TSignature.Delegate(sigSpiClone, ((TSignature)this).algorithm);
                that.provider = ((TSignature)this).provider;
                return that;
            } else {
                throw new CloneNotSupportedException();
            }
        }

        private static TSignatureSpi newInstance(TProvider.Service s)
                throws NoSuchAlgorithmException {
            if (s.getType().equals("TCipher")) {
                // must be NONEwithRSA
                try {
                    TCipher c = TCipher.getInstance(RSA_CIPHER, s.getProvider());
                    return new TSignature.CipherAdapter(c);
                } catch (TNoSuchPaddingException e) {
                    throw new NoSuchAlgorithmException(e);
                }
            } else {
                Object o = s.newInstance(null);
                if (o instanceof TSignatureSpi == false) {
                    throw new NoSuchAlgorithmException
                            ("Not a SignatureSpi: " + o.getClass().getName());
                }
                return (TSignatureSpi)o;
            }
        }

        // max number of debug warnings to print from chooseFirstProvider()
        private static int warnCount = 10;

        /**
         * Choose the Spi from the first provider available. Used if
         * delayed provider selection is not possible because initSign()/
         * initVerify() is not the first method called.
         */
        void chooseFirstProvider() {
            if (sigSpi != null) {
                return;
            }
            synchronized (lock) {
                if (sigSpi != null) {
                    return;
                }
                Exception lastException = null;
                while ((firstService != null) || serviceIterator.hasNext()) {
                    Provider.Service s;
                    if (firstService != null) {
                        s = firstService;
                        firstService = null;
                    } else {
                        s = serviceIterator.next();
                    }
                    if (isSpi(s) == false) {
                        continue;
                    }
                    try {
                        sigSpi = newInstance(s);
                        provider = s.getProvider();
                        // not needed any more
                        firstService = null;
                        serviceIterator = null;
                        return;
                    } catch (NoSuchAlgorithmException e) {
                        lastException = e;
                    }
                }
                TProviderException e = new TProviderException
                        ("Could not construct SignatureSpi instance");
                if (lastException != null) {
                    e.initCause(lastException);
                }
                throw e;
            }
        }

        private void chooseProvider(int type, TKey key, TSecureRandom random)
                throws TInvalidKeyException {
            synchronized (lock) {
                if (sigSpi != null) {
                    init(sigSpi, type, key, random);
                    return;
                }
                Exception lastException = null;
                while ((firstService != null) || serviceIterator.hasNext()) {
                    Provider.Service s;
                    if (firstService != null) {
                        s = firstService;
                        firstService = null;
                    } else {
                        s = serviceIterator.next();
                    }
                    // if provider says it does not support this key, ignore it
                    if (s.supportsParameter(key) == false) {
                        continue;
                    }
                    // if instance is not a SignatureSpi, ignore it
                    if (isSpi(s) == false) {
                        continue;
                    }
                    try {
                        TSignatureSpi spi = newInstance(s);
                        init(spi, type, key, random);
                        provider = s.getProvider();
                        sigSpi = spi;
                        firstService = null;
                        serviceIterator = null;
                        return;
                    } catch (Exception e) {
                        // NoSuchAlgorithmException from newInstance()
                        // InvalidKeyException from init()
                        // RuntimeException (TProviderException) from init()
                        if (lastException == null) {
                            lastException = e;
                        }
                    }
                }
                // no working provider found, fail
                if (lastException instanceof TInvalidKeyException) {
                    throw (TInvalidKeyException)lastException;
                }
                if (lastException instanceof RuntimeException) {
                    throw (RuntimeException)lastException;
                }
                String k = (key != null) ? key.getClass().getName() : "(null)";
                throw new TInvalidKeyException
                        ("No installed provider supports this key: "
                                + k, lastException);
            }
        }

        private final static int I_PUB     = 1;
        private final static int I_PRIV    = 2;
        private final static int I_PRIV_SR = 3;

        private void init(TSignatureSpi spi, int type, TKey  key,
                TSecureRandom random) throws TInvalidKeyException {
            switch (type) {
                case I_PUB:
                    spi.engineInitVerify((TPublicKey)key);
                    break;
                case I_PRIV:
                    spi.engineInitSign((TPrivateKey)key);
                    break;
                case I_PRIV_SR:
                    spi.engineInitSign((TPrivateKey)key, random);
                    break;
                default:
                    throw new AssertionError("Internal error: " + type);
            }
        }

        protected void engineInitVerify(TPublicKey publicKey)
                throws TInvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitVerify(publicKey);
            } else {
                chooseProvider(I_PUB, publicKey, null);
            }
        }

        protected void engineInitSign(TPrivateKey privateKey)
                throws TInvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitSign(privateKey);
            } else {
                chooseProvider(I_PRIV, privateKey, null);
            }
        }

        protected void engineInitSign(TPrivateKey privateKey, TSecureRandom sr)
                throws TInvalidKeyException {
            if (sigSpi != null) {
                sigSpi.engineInitSign(privateKey, sr);
            } else {
                chooseProvider(I_PRIV_SR, privateKey, sr);
            }
        }

        protected void engineUpdate(byte b) throws TSignatureException {
            chooseFirstProvider();
            sigSpi.engineUpdate(b);
        }

        protected void engineUpdate(byte[] b, int off, int len)
                throws TSignatureException {
            chooseFirstProvider();
            sigSpi.engineUpdate(b, off, len);
        }

        protected void engineUpdate(TByteBuffer data) {
            chooseFirstProvider();
            sigSpi.engineUpdate(data);
        }

        protected byte[] engineSign() throws TSignatureException {
            chooseFirstProvider();
            return sigSpi.engineSign();
        }

        protected int engineSign(byte[] outbuf, int offset, int len)
                throws TSignatureException {
            chooseFirstProvider();
            return sigSpi.engineSign(outbuf, offset, len);
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws TSignatureException {
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes);
        }

        protected boolean engineVerify(byte[] sigBytes, int offset, int length)
                throws TSignatureException {
            chooseFirstProvider();
            return sigSpi.engineVerify(sigBytes, offset, length);
        }

        protected void engineSetParameter(String param, Object value)
                throws TInvalidParameterException {
            chooseFirstProvider();
            sigSpi.engineSetParameter(param, value);
        }

        protected void engineSetParameter(TAlgorithmParameterSpec params)
                throws TInvalidAlgorithmParameterException {
            chooseFirstProvider();
            sigSpi.engineSetParameter(params);
        }

        protected Object engineGetParameter(TString param)
                throws TInvalidParameterException {
            chooseFirstProvider();
            return sigSpi.engineGetParameter(param);
        }

        protected TAlgorithmParameters engineGetParameters() {
            chooseFirstProvider();
            return sigSpi.engineGetParameters();
        }
    }

    // adapter for RSA/ECB/PKCS1Padding ciphers
    @SuppressWarnings("deprecation")
    private static class CipherAdapter extends TSignatureSpi {

        private final TCipher cipher;

        private TByteArrayOutputStream data;

        CipherAdapter(TCipher cipher) {
            this.cipher = cipher;
        }

        protected void engineInitVerify(TPublicKey publicKey)
                throws TInvalidKeyException {
            cipher.init(TCipher.DECRYPT_MODE, publicKey);
            if (data == null) {
                data = new TByteArrayOutputStream(128);
            } else {
                data.reset();
            }
        }

        protected void engineInitSign(TPrivateKey privateKey)
                throws TInvalidKeyException {
            cipher.init(TCipher.ENCRYPT_MODE, privateKey);
            data = null;
        }

        protected void engineInitSign(TPrivateKey privateKey,
                TSecureRandom random) throws TInvalidKeyException {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey, random);
            data = null;
        }

        protected void engineUpdate(byte b) throws TSignatureException {
            engineUpdate(new byte[] {b}, 0, 1);
        }

        protected void engineUpdate(byte[] b, int off, int len)
                throws TSignatureException {
            if (data != null) {
                data.write(b, off, len);
                return;
            }
            byte[] out = cipher.update(b, off, len);
            if ((out != null) && (out.length != 0)) {
                throw new TSignatureException
                        (TString.wrap("TCipher unexpectedly returned data"));
            }
        }

        protected byte[] engineSign() throws TSignatureException {
            try {
                return cipher.doFinal();
            } catch (TIllegalBlockSizeException e) {
                throw new TSignatureException(TString.wrap("doFinal() failed"));
            } catch (TBadPaddingException e) {
                throw new TSignatureException(TString.wrap("doFinal() failed"));
            }
        }

        protected boolean engineVerify(byte[] sigBytes)
                throws TSignatureException {
            try {
                byte[] out = cipher.doFinal(sigBytes);
                byte[] dataBytes = data.toByteArray();
                data.reset();
                return TMessageDigest.isEqual(out, dataBytes);
            } catch (TBadPaddingException e) {
                // e.g. wrong public key used
                // return false rather than throwing exception
                return false;
            } catch (TIllegalBlockSizeException e) {
                throw new TSignatureException(TString.wrap("doFinal() failed"));
            }
        }

        protected void engineSetParameter(String param, Object value)
                throws TInvalidParameterException {
            throw new TInvalidParameterException(TString.wrap("Parameters not supported"));
        }

        protected Object engineGetParameter(String param)
                throws TInvalidParameterException {
            throw new TInvalidParameterException(TString.wrap("Parameters not supported"));
        }

    }

}
