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
package org.teavm.classlib.javax.crypto;

import org.teavm.classlib.java.lang.TString;
import org.teavm.classlib.java.security.TGeneralSecurityException;

/**
 * Created by vasek on 8. 10. 2016.
 */
public class TShortBufferException extends TGeneralSecurityException {
    private static final long serialVersionUID = 8427718640832943747L;

    public TShortBufferException() {
    }

    public TShortBufferException(TString var1) {
        super(var1);
    }
}
