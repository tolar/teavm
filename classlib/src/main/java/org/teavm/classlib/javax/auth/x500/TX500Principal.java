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
package org.teavm.classlib.javax.auth.x500;

import java.io.IOException;
import java.util.Map;
import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TInputStream;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TPrincipal;
import org.teavm.classlib.java.util.TCollections;
import org.teavm.classlib.java.util.TMap;
import org.teavm.classlib.sun.security.util.TDerValue;
import org.teavm.classlib.sun.security.x509.TX500Name;

public final class TX500Principal implements TPrincipal, java.io.Serializable {

    private static final long serialVersionUID = -500463348111345721L;

    /**
     * RFC 1779 String format of Distinguished Names.
     */
    public static final String RFC1779 = "RFC1779";
    /**
     * RFC 2253 String format of Distinguished Names.
     */
    public static final String RFC2253 = "RFC2253";
    /**
     * Canonical String format of Distinguished Names.
     */
    public static final String CANONICAL = "CANONICAL";

    /**
     * The X500Name representing this principal.
     *
     * NOTE: this field is reflectively accessed from within X500Name.
     */
    private transient TX500Name thisX500Name;

    /**
     * Creates an X500Principal by wrapping an X500Name.
     *
     * NOTE: The constructor is package private. It is intended to be accessed
     * using privileged reflection from classes in sun.security.*.
     * Currently referenced from sun.security.x509.X500Name.asX500Principal().
     */
    TX500Principal(TX500Name x500Name) {
        thisX500Name = x500Name;
    }

    /**
     * Creates an {@code X500Principal} from a string representation of
     * an X.500 distinguished name (ex:
     * "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US").
     * The distinguished name must be specified using the grammar defined in
     * RFC 1779 or RFC 2253 (either format is acceptable).
     *
     * <p>This constructor recognizes the attribute type keywords
     * defined in RFC 1779 and RFC 2253
     * (and listed in {@link #getName(String format) getName(String format)}),
     * as well as the T, DNQ or DNQUALIFIER, SURNAME, GIVENNAME, INITIALS,
     * GENERATION, EMAILADDRESS, and SERIALNUMBER keywords whose Object
     * Identifiers (OIDs) are defined in RFC 3280 and its successor.
     * Any other attribute type must be specified as an OID.
     *
     * <p>This implementation enforces a more restrictive OID syntax than
     * defined in RFC 1779 and 2253. It uses the more correct syntax defined in
     * <a href="http://www.ietf.org/rfc/rfc4512.txt">RFC 4512</a>, which
     * specifies that OIDs contain at least 2 digits:
     *
     * <p>{@code numericoid = number 1*( DOT number ) }
     *
     * @param name an X.500 distinguished name in RFC 1779 or RFC 2253 format
     * @exception NullPointerException if the {@code name}
     *                  is {@code null}
     * @exception IllegalArgumentException if the {@code name}
     *                  is improperly specified
     */
    public TX500Principal(TString name) {
        this(name, TCollections.<TString, TString>emptyMap());
    }

    public TX500Principal(TString name, TMap<TString, TString> keywordMap) {
        if (name == null) {
            throw new NullPointerException("provided.null.name");
        }
        if (keywordMap == null) {
            throw new NullPointerException("provided.null.keyword.map");
        }

        try {
            thisX500Name = new TX500Name(name, keywordMap);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                    ("improperly specified input name: " + name);
            iae.initCause(e);
            throw iae;
        }
    }

    /**
     * Creates an {@code X500Principal} from a distinguished name in
     * ASN.1 DER encoded form. The ASN.1 notation for this structure is as
     * follows.
     * <pre>{@code
     * Name ::= CHOICE {
     *   RDNSequence }
     *
     * RDNSequence ::= SEQUENCE OF RelativeDistinguishedName
     *
     * RelativeDistinguishedName ::=
     *   SET SIZE (1 .. MAX) OF AttributeTypeAndValue
     *
     * AttributeTypeAndValue ::= SEQUENCE {
     *   type     AttributeType,
     *   value    AttributeValue }
     *
     * AttributeType ::= OBJECT IDENTIFIER
     *
     * AttributeValue ::= ANY DEFINED BY AttributeType
     * ....
     * DirectoryString ::= CHOICE {
     *       teletexString           TeletexString (SIZE (1..MAX)),
     *       printableString         PrintableString (SIZE (1..MAX)),
     *       universalString         UniversalString (SIZE (1..MAX)),
     *       utf8String              UTF8String (SIZE (1.. MAX)),
     *       bmpString               BMPString (SIZE (1..MAX)) }
     * }</pre>
     *
     * @param name a byte array containing the distinguished name in ASN.1
     * DER encoded form
     * @throws IllegalArgumentException if an encoding error occurs
     *          (incorrect form for DN)
     */
    public TX500Principal(byte[] name) {
        try {
            thisX500Name = new TX500Name(name);
        } catch (Exception e) {
            IllegalArgumentException iae = new IllegalArgumentException
                    ("improperly specified input name");
            iae.initCause(e);
            throw iae;
        }
    }

    /**
     * Creates an {@code X500Principal} from an {@code InputStream}
     * containing the distinguished name in ASN.1 DER encoded form.
     * The ASN.1 notation for this structure is supplied in the
     * documentation for
     * {@link #TX500Principal(byte[] name) X500Principal(byte[] name)}.
     *
     * <p> The read position of the input stream is positioned
     * to the next available byte after the encoded distinguished name.
     *
     * @param is an {@code InputStream} containing the distinguished
     *          name in ASN.1 DER encoded form
     *
     * @exception NullPointerException if the {@code InputStream}
     *          is {@code null}
     * @exception IllegalArgumentException if an encoding error occurs
     *          (incorrect form for DN)
     */
    public TX500Principal(TInputStream is) {
        if (is == null) {
            throw new NullPointerException("provided null input stream");
        }

        try {
            if (is.markSupported())
                is.mark(is.available() + 1);
            TDerValue der = new TDerValue(is);
            thisX500Name = new TX500Name(der.data);
        } catch (Exception e) {
            if (is.markSupported()) {
                try {
                    is.reset();
                } catch (TIOException ioe) {
                    IllegalArgumentException iae = new IllegalArgumentException
                            ("improperly specified input stream " +
                                    ("and unable to reset input stream"));
                    iae.initCause(e);
                    throw iae;
                }
            }
            IllegalArgumentException iae = new IllegalArgumentException
                    ("improperly specified input stream");
            iae.initCause(e);
            throw iae;
        }
    }

    /**
     * Returns a string representation of the X.500 distinguished name using
     * the format defined in RFC 2253.
     *
     * <p>This method is equivalent to calling
     * {@code getName(X500Principal.RFC2253)}.
     *
     * @return the distinguished name of this {@code X500Principal}
     */
    public String getName() {
        return getName(TX500Principal.RFC2253);
    }

    /**
     * Returns a string representation of the X.500 distinguished name
     * using the specified format. Valid values for the format are
     * "RFC1779", "RFC2253", and "CANONICAL" (case insensitive).
     *
     * <p> If "RFC1779" is specified as the format,
     * this method emits the attribute type keywords defined in
     * RFC 1779 (CN, L, ST, O, OU, C, STREET).
     * Any other attribute type is emitted as an OID.
     *
     * <p> If "RFC2253" is specified as the format,
     * this method emits the attribute type keywords defined in
     * RFC 2253 (CN, L, ST, O, OU, C, STREET, DC, UID).
     * Any other attribute type is emitted as an OID.
     * Under a strict reading, RFC 2253 only specifies a UTF-8 string
     * representation. The String returned by this method is the
     * Unicode string achieved by decoding this UTF-8 representation.
     *
     * <p> If "CANONICAL" is specified as the format,
     * this method returns an RFC 2253 conformant string representation
     * with the following additional canonicalizations:
     *
     * <ol>
     * <li> Leading zeros are removed from attribute types
     *          that are encoded as dotted decimal OIDs
     * <li> DirectoryString attribute values of type
     *          PrintableString and UTF8String are not
     *          output in hexadecimal format
     * <li> DirectoryString attribute values of types
     *          other than PrintableString and UTF8String
     *          are output in hexadecimal format
     * <li> Leading and trailing white space characters
     *          are removed from non-hexadecimal attribute values
     *          (unless the value consists entirely of white space characters)
     * <li> Internal substrings of one or more white space characters are
     *          converted to a single space in non-hexadecimal
     *          attribute values
     * <li> Relative Distinguished Names containing more than one
     *          Attribute Value Assertion (AVA) are output in the
     *          following order: an alphabetical ordering of AVAs
     *          containing standard keywords, followed by a numeric
     *          ordering of AVAs containing OID keywords.
     * <li> The only characters in attribute values that are escaped are
     *          those which section 2.4 of RFC 2253 states must be escaped
     *          (they are escaped using a preceding backslash character)
     * <li> The entire name is converted to upper case
     *          using {@code String.toUpperCase(Locale.US)}
     * <li> The entire name is converted to lower case
     *          using {@code String.toLowerCase(Locale.US)}
     * <li> The name is finally normalized using normalization form KD,
     *          as described in the Unicode Standard and UAX #15
     * </ol>
     *
     * <p> Additional standard formats may be introduced in the future.
     *
     * @param format the format to use
     *
     * @return a string representation of this {@code X500Principal}
     *          using the specified format
     * @throws IllegalArgumentException if the specified format is invalid
     *          or null
     */
    public String getName(String format) {
        if (format != null) {
            if (format.equalsIgnoreCase(RFC1779)) {
                return thisX500Name.getRFC1779Name();
            } else if (format.equalsIgnoreCase(RFC2253)) {
                return thisX500Name.getRFC2253Name();
            } else if (format.equalsIgnoreCase(CANONICAL)) {
                return thisX500Name.getRFC2253CanonicalName();
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    /**
     * Returns a string representation of the X.500 distinguished name
     * using the specified format. Valid values for the format are
     * "RFC1779" and "RFC2253" (case insensitive). "CANONICAL" is not
     * permitted and an {@code IllegalArgumentException} will be thrown.
     *
     * <p>This method returns Strings in the format as specified in
     * {@link #getName(String)} and also emits additional attribute type
     * keywords for OIDs that have entries in the {@code oidMap}
     * parameter. OID entries in the oidMap take precedence over the default
     * OIDs recognized by {@code getName(String)}.
     * Improperly specified OIDs are ignored; however if an OID
     * in the name maps to an improperly specified keyword, an
     * {@code IllegalArgumentException} is thrown.
     *
     * <p> Additional standard formats may be introduced in the future.
     *
     * <p> Warning: additional attribute type keywords may not be recognized
     * by other implementations; therefore do not use this method if
     * you are unsure if these keywords will be recognized by other
     * implementations.
     *
     * @param format the format to use
     * @param oidMap an OID map, where each key is an object identifier in
     *  String form (a sequence of nonnegative integers separated by periods)
     *  that maps to a corresponding attribute type keyword String.
     *  The map may be empty but never {@code null}.
     * @return a string representation of this {@code X500Principal}
     *          using the specified format
     * @throws IllegalArgumentException if the specified format is invalid,
     *  null, or an OID in the name maps to an improperly specified keyword
     * @throws NullPointerException if {@code oidMap} is {@code null}
     * @since 1.6
     */
    public String getName(String format, Map<String, String> oidMap) {
        if (oidMap == null) {
            throw new NullPointerException("provided.null.OID.map");
        }
        if (format != null) {
            if (format.equalsIgnoreCase(RFC1779)) {
                return thisX500Name.getRFC1779Name(oidMap);
            } else if (format.equalsIgnoreCase(RFC2253)) {
                return thisX500Name.getRFC2253Name(oidMap);
            }
        }
        throw new IllegalArgumentException("invalid format specified");
    }

    /**
     * Returns the distinguished name in ASN.1 DER encoded form. The ASN.1
     * notation for this structure is supplied in the documentation for
     * {@link #TX500Principal(byte[] name) X500Principal(byte[] name)}.
     *
     * <p>Note that the byte array returned is cloned to protect against
     * subsequent modifications.
     *
     * @return a byte array containing the distinguished name in ASN.1 DER
     * encoded form
     */
    public byte[] getEncoded() {
        try {
            return thisX500Name.getEncoded();
        } catch (IOException e) {
            throw new RuntimeException("unable to get encoding", e);
        }
    }

    /**
     * Return a user-friendly string representation of this
     * {@code X500Principal}.
     *
     * @return a string representation of this {@code X500Principal}
     */
    public String toString() {
        return thisX500Name.toString();
    }

    /**
     * Compares the specified {@code Object} with this
     * {@code X500Principal} for equality.
     *
     * <p> Specifically, this method returns {@code true} if
     * the {@code Object} <i>o</i> is an {@code X500Principal}
     * and if the respective canonical string representations
     * (obtained via the {@code getName(X500Principal.CANONICAL)} method)
     * of this object and <i>o</i> are equal.
     *
     * <p> This implementation is compliant with the requirements of RFC 3280.
     *
     * @param o Object to be compared for equality with this
     *          {@code X500Principal}
     *
     * @return {@code true} if the specified {@code Object} is equal
     *          to this {@code X500Principal}, {@code false} otherwise
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof org.teavm.classlib.javax.auth.x500.TX500Principal == false) {
            return false;
        }
        org.teavm.classlib.javax.auth.x500.TX500Principal other = (org.teavm.classlib.javax.auth.x500.TX500Principal)o;
        return this.thisX500Name.equals(other.thisX500Name);
    }

    /**
     * Return a hash code for this {@code X500Principal}.
     *
     * <p> The hash code is calculated via:
     * {@code getName(X500Principal.CANONICAL).hashCode()}
     *
     * @return a hash code for this {@code X500Principal}
     */
    public int hashCode() {
        return thisX500Name.hashCode();
    }

    /**
     * Save the X500Principal object to a stream.
     *
     * @serialData this {@code X500Principal} is serialized
     *          by writing out its DER-encoded form
     *          (the value of {@code getEncoded} is serialized).
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        s.writeObject(thisX500Name.getEncodedInternal());
    }

    /**
     * Reads this object from a stream (i.e., deserializes it).
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException,
            java.io.NotActiveException,
            ClassNotFoundException {

        // re-create thisX500Name
        thisX500Name = new TX500Name((byte[])s.readObject());
    }
}
