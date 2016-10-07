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
package org.teavm.classlib.sun.security.timestamp;

import java.io.IOException;
import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.sun.security.pkcs.TPKCS7;
import org.teavm.classlib.sun.security.util.TDerValue;

/**
 * Created by vasek on 7. 10. 2016.
 */
public class TTSResponse {
    public static final int GRANTED = 0;
    public static final int GRANTED_WITH_MODS = 1;
    public static final int REJECTION = 2;
    public static final int WAITING = 3;
    public static final int REVOCATION_WARNING = 4;
    public static final int REVOCATION_NOTIFICATION = 5;
    public static final int BAD_ALG = 0;
    public static final int BAD_REQUEST = 2;
    public static final int BAD_DATA_FORMAT = 5;
    public static final int TIME_NOT_AVAILABLE = 14;
    public static final int UNACCEPTED_POLICY = 15;
    public static final int UNACCEPTED_EXTENSION = 16;
    public static final int ADD_INFO_NOT_AVAILABLE = 17;
    public static final int SYSTEM_FAILURE = 25;
    private int status;
    private TString[] statusString = null;
    private boolean[] failureInfo = null;
    private byte[] encodedTsToken = null;
    private TPKCS7 tsToken = null;
    private TTimestampToken tstInfo;

    TTSResponse(byte[] var1) throws IOException {
        this.parse(var1);
    }

    public int getStatusCode() {
        return this.status;
    }

    public TString[] getStatusMessages() {
        return this.statusString;
    }

    public boolean[] getFailureInfo() {
        return this.failureInfo;
    }

    public String getStatusCodeAsText() {
        switch(this.status) {
            case 0:
                return "the timestamp request was granted.";
            case 1:
                return "the timestamp request was granted with some modifications.";
            case 2:
                return "the timestamp request was rejected.";
            case 3:
                return "the timestamp request has not yet been processed.";
            case 4:
                return "warning: a certificate revocation is imminent.";
            case 5:
                return "notification: a certificate revocation has occurred.";
            default:
                return "unknown status code " + this.status + ".";
        }
    }

    private boolean isSet(int var1) {
        return this.failureInfo[var1];
    }

    public String getFailureCodeAsText() {
        if(this.failureInfo == null) {
            return "";
        } else {
            try {
                if(this.isSet(0)) {
                    return "Unrecognized or unsupported algorithm identifier.";
                }

                if(this.isSet(2)) {
                    return "The requested transaction is not permitted or supported.";
                }

                if(this.isSet(5)) {
                    return "The data submitted has the wrong format.";
                }

                if(this.isSet(14)) {
                    return "The TSA\'s time source is not available.";
                }

                if(this.isSet(15)) {
                    return "The requested TSA policy is not supported by the TSA.";
                }

                if(this.isSet(16)) {
                    return "The requested extension is not supported by the TSA.";
                }

                if(this.isSet(17)) {
                    return "The additional information requested could not be understood or is not available.";
                }

                if(this.isSet(25)) {
                    return "The request cannot be handled due to system failure.";
                }
            } catch (ArrayIndexOutOfBoundsException var2) {
                ;
            }

            return "unknown failure code";
        }
    }

    public TPKCS7 getToken() {
        return this.tsToken;
    }

    public TTimestampToken getTimestampToken() {
        return this.tstInfo;
    }

    public byte[] getEncodedToken() {
        return this.encodedTsToken;
    }

    private void parse(byte[] var1) throws IOException {
        TDerValue var2 = new TDerValue(var1);
        if(var2.tag != 48) {
            throw new IOException("Bad encoding for timestamp response");
        } else {
            TDerValue var3 = var2.data.getDerValue();
            this.status = var3.data.getInteger();

            if(var3.data.available() > 0) {
                byte var4 = (byte)var3.data.peekByte();
                if(var4 == 48) {
                    TDerValue[] var5 = var3.data.getSequence(1);
                    this.statusString = new TString[var5.length];

                    for(int var6 = 0; var6 < var5.length; ++var6) {
                        this.statusString[var6] = var5[var6].getUTF8String();
                    }
                }
            }

            if(var3.data.available() > 0) {
                this.failureInfo = var3.data.getUnalignedBitString().toBooleanArray();
            }

            if(var2.data.available() > 0) {
                TDerValue var7 = var2.data.getDerValue();
                this.encodedTsToken = var7.toByteArray();
                this.tsToken = new TPKCS7(this.encodedTsToken);
                this.tstInfo = new TTimestampToken(this.tsToken.getContentInfo().getData());
            }

            if(this.status != 0 && this.status != 1) {
                if(this.tsToken != null) {
                    throw new TTSResponse.TimestampException("Bad encoding for timestamp response: expected no timeStampToken element to be present");
                }
            } else if(this.tsToken == null) {
                throw new TTSResponse.TimestampException("Bad encoding for timestamp response: expected a timeStampToken element to be present");
            }

        }
    }

    static final class TimestampException extends IOException {
        private static final long serialVersionUID = -1631631794891940953L;

        TimestampException(String var1) {
            super(var1);
        }
    }
}
