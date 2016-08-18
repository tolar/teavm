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
package org.teavm.classlib.sun.security.x509;

import java.io.IOException;
import org.teavm.classlib.sun.security.util.TDerOutputStream;

/**
 * Created by vasek on 18. 8. 2016.
 */
public interface TGeneralNameInterface {
    int NAME_ANY = 0;
    int NAME_RFC822 = 1;
    int NAME_DNS = 2;
    int NAME_X400 = 3;
    int NAME_DIRECTORY = 4;
    int NAME_EDI = 5;
    int NAME_URI = 6;
    int NAME_IP = 7;
    int NAME_OID = 8;
    int NAME_DIFF_TYPE = -1;
    int NAME_MATCH = 0;
    int NAME_NARROWS = 1;
    int NAME_WIDENS = 2;
    int NAME_SAME_TYPE = 3;

    int getType();

    void encode(TDerOutputStream var1) throws IOException;

    int constrains(TGeneralNameInterface var1) throws UnsupportedOperationException;

    int subtreeDepth() throws UnsupportedOperationException;
}
