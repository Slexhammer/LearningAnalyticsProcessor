/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.services.input.csv;

import java.io.File;

import au.com.bytecode.opencsv.CSVReader;

import org.apereo.lap.services.input.InputHandler;

/**
 * Handles the CSV input processing for a single CSV file
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface CSVInputHandler extends InputHandler {

    /**
     * @return the name of the CSV filePath this handler handles
     */
    File getFile();
    
    /**
     * Sets the filePath explicitly
     */
    void setFilePath(String filePath);
    
    /**
     * @return the name of the CSV file this handler handles
     */
    String getFileName();

    /**
     * Read in the CSV file (does not process it, only the header line)
     * NOTE: the implementation SHOULD cache this so it is not rebuilt every time this method is called (unless reRead is true)
     * @param reRead force reading the file again (otherwise it will use the cached copy)
     * @return the CSVReader
     * @throws IllegalStateException if we fail to produce the reader
     */
    CSVReader readCSV(boolean reRead);
    
    int getOrder();
}
