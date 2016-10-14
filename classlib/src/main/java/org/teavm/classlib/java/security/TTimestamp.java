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

import java.io.ObjectInputStream;
import java.util.List;

import org.teavm.classlib.java.io.TIOException;
import org.teavm.classlib.java.io.TSerializable;
import org.teavm.classlib.java.security.cert.TCertPath;
import org.teavm.classlib.java.security.cert.TCertificate;
import org.teavm.classlib.java.util.TDate;

public final class TTimestamp implements TSerializable {

    private static final long serialVersionUID = -5502683707821851294L;

    /**
     * The timestamp's date and time
     *
     * @serial
     */
    private TDate timestamp;

    /**
     * The TSA's certificate path.
     *
     * @serial
     */
    private TCertPath signerCertPath;

    /*
     * Hash code for this timestamp.
     */
    private transient int myhash = -1;

    /**
     * Constructs a Timestamp.
     *
     * @param timestamp is the timestamp's date and time. It must not be null.
     * @param signerCertPath is the TSA's certificate path. It must not be null.
     * @throws NullPointerException if timestamp or signerCertPath is null.
     */
    public TTimestamp(TDate timestamp, TCertPath signerCertPath) {
        if (timestamp == null || signerCertPath == null) {
            throw new NullPointerException();
        }
        this.timestamp = new TDate(timestamp.getTime()); // clone
        this.signerCertPath = signerCertPath;
    }

    /**
     * Returns the date and time when the timestamp was generated.
     *
     * @return The timestamp's date and time.
     */
    public TDate getTimestamp() {
        return new TDate(timestamp.getTime()); // clone
    }

    /**
     * Returns the certificate path for the Timestamping Authority.
     *
     * @return The TSA's certificate path.
     */
    public TCertPath getSignerCertPath() {
        return signerCertPath;
    }

    /**
     * Returns the hash code value for this timestamp.
     * The hash code is generated using the date and time of the timestamp
     * and the TSA's certificate path.
     *
     * @return a hash code value for this timestamp.
     */
    public int hashCode() {
        if (myhash == -1) {
            myhash = timestamp.hashCode() + signerCertPath.hashCode();
        }
        return myhash;
    }

    /**
     * Tests for equality between the specified object and this
     * timestamp. Two timestamps are considered equal if the date and time of
     * their timestamp's and their signer's certificate paths are equal.
     *
     * @param obj the object to test for equality with this timestamp.
     *
     * @return true if the timestamp are considered equal, false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null || (!(obj instanceof java.security.Timestamp))) {
            return false;
        }
        TTimestamp that = (TTimestamp)obj;

        if (this == that) {
            return true;
        }
        return (timestamp.equals(that.getTimestamp()) &&
                signerCertPath.equals(that.getSignerCertPath()));
    }

    /**
     * Returns a string describing this timestamp.
     *
     * @return A string comprising the date and time of the timestamp and
     *         its signer's certificate.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("timestamp: " + timestamp);
        List<? extends TCertificate> certs = signerCertPath.getCertificates();
        if (!certs.isEmpty()) {
            sb.append("TSA: " + certs.get(0));
        } else {
            sb.append("TSA: <empty>");
        }
        sb.append(")");
        return sb.toString();
    }

    // Explicitly reset hash code value to -1
    private void readObject(ObjectInputStream ois)
            throws TIOException, ClassNotFoundException {
        ois.defaultReadObject();
        myhash = -1;
        timestamp = new TDate(timestamp.getTime());
    }
}