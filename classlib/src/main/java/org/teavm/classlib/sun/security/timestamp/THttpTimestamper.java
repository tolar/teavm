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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.teavm.classlib.java.io.TBufferedInputStream;
import org.teavm.classlib.sun.misc.TIOUtils;
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
        HttpURLConnection var2 = (HttpURLConnection)this.tsaURI.toURL().openConnection();
        var2.setDoOutput(true);
        var2.setUseCaches(false);
        var2.setRequestProperty("Content-Type", "application/timestamp-query");
        var2.setRequestMethod("POST");
        var2.setConnectTimeout(15000);
        Map.Entry var5;
        if(debug != null) {
            Set var3 = var2.getRequestProperties().entrySet();
            debug.println(var2.getRequestMethod() + " " + this.tsaURI + " HTTP/1.1");
            Iterator var4 = var3.iterator();

            while(var4.hasNext()) {
                var5 = (Map.Entry)var4.next();
                debug.println("  " + var5);
            }

            debug.println();
        }

        var2.connect();
        DataOutputStream var16 = null;

        try {
            var16 = new DataOutputStream(var2.getOutputStream());
            byte[] var17 = var1.encode();
            var16.write(var17, 0, var17.length);
            var16.flush();
            if(debug != null) {
                debug.println("sent timestamp query (length=" + var17.length + ")");
            }
        } finally {
            if(var16 != null) {
                var16.close();
            }

        }

        TBufferedInputStream var18 = null;
        var5 = null;

        byte[] var19;
        try {
            var18 = new TBufferedInputStream(var2.getInputStream());
            if(debug != null) {
                String var6 = var2.getHeaderField(0);
                debug.println(var6);

                for(int var7 = 1; (var6 = var2.getHeaderField(var7)) != null; ++var7) {
                    String var8 = var2.getHeaderFieldKey(var7);
                    debug.println("  " + (var8 == null?"":var8 + ": ") + var6);
                }

                debug.println();
            }

            verifyMimeType(var2.getContentType());
            int var20 = var2.getContentLength();
            var19 = TIOUtils.readFully(var18, var20, false);
            if(debug != null) {
                debug.println("received timestamp response (length=" + var19.length + ")");
            }
        } finally {
            if(var18 != null) {
                var18.close();
            }

        }

        return new TTSResponse(var19);
    }

    private static void verifyMimeType(String var0) throws IOException {
        if(!"application/timestamp-reply".equalsIgnoreCase(var0)) {
            throw new IOException("MIME Content-Type is not application/timestamp-reply");
        }
    }
}
