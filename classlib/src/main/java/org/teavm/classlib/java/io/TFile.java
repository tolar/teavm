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
package org.teavm.classlib.java.io;

import java.io.File;
import java.io.IOException;

/**
 * Created by vasek on 14. 7. 2016.
 */
public class TFile {

    public TFile(String pathname) {
    }

    public boolean canRead() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public static File createTempFile(String prefix, String suffix,
            File directory)
            throws IOException
    {
        return null;
    }

    public String getName() {
        return null;
    }

}
