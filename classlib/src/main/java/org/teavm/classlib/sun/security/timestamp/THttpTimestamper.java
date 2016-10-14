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
package org.teavm.classlib.sun.security.timestamp;

import java.io.IOException;
import java.net.URI;

import org.teavm.classlib.sun.security.util.TDebug;

public class THttpTimestamper implements TTimestamper {
    private static final int CONNECT_TIMEOUT = 15000;
    private static final String TS_QUERY_MIME_TYPE = "application/timestamp-query";
    private static final String TS_REPLY_MIME_TYPE = "application/timestamp-reply";
    private static final TDebug debug = TDebug.getInstance("ts");
    private URI tsaURI = null;

    public THttpTimestamper(URI var1) {
        if(!var1.getScheme().equalsIgnoreCase("http") && !var1.getScheme().equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("TSA must be an HTTP or HTTPS URI");
        } else {
            this.tsaURI = var1;
        }
    }

    public TTSResponse generateTimestamp(TTSRequest var1) throws IOException {
        return null;
    }

    private static void verifyMimeType(String var0) throws IOException {
        if(!"application/timestamp-reply".equalsIgnoreCase(var0)) {
            throw new IOException("MIME Content-Type is not application/timestamp-reply");
        }
    }
}
