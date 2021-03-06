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
package org.apereo.lap.services;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apereo.lap.services.input.InputHandler;
import org.apereo.lap.services.input.csv.ActivityCSVInputHandler;
import org.apereo.lap.services.input.csv.BaseCSVInputHandler;
import org.apereo.lap.services.input.csv.CSVInputHandler;
import org.apereo.lap.services.input.csv.CourseCSVInputHandler;
import org.apereo.lap.services.input.csv.EnrollmentCSVInputHandler;
import org.apereo.lap.services.input.csv.GradeCSVInputHandler;
import org.apereo.lap.services.input.csv.PersonalCSVInputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the inputs by reading the data into the temporary data storage
 * Validates the inputs and ensures the data is available to the pipeline processor
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public class CSVInputHandlerService extends BaseInputHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(CSVInputHandlerService.class);

    public CSVInputHandlerService(ConfigurationService configuration, StorageService storage, HierarchicalConfiguration xmlConfig)
    {
    	super(xmlConfig);
    	this.configuration = configuration;
    	this.storage = storage;
    	this.init(xmlConfig);
    }

	@Override
	public Type getType() {
		return Type.CSV;
	}
 
    private Map<String, String> files = new HashMap<>();
    
    public void init(HierarchicalConfiguration xmlConfig) {
        super.init();

        
     // load the lists
        // sources
        List<HierarchicalConfiguration> sourceFields = xmlConfig.configurationsAt("params.files.file");
        for (HierarchicalConfiguration field : sourceFields) {
            try {
            	files.put(field.getString("type"), field.getString("path"));
            } catch (Exception e) {
                // skip this input and warn
                logger.warn("Unable to load input field ("+field.toString()+") (skipping it): "+e);
            }
        }
    }

    /**
     * @param type the class of input handlers we are looking for
     * @param <T> InputHandler type (e.g. CSVInputHandler)
     * @return all handlers for a given type mapped by their unique handled type of data OR empty map if there are none
     */
    public <T extends InputHandler> Map<String, T> findHandlers(Class<T> type) {
        assert type != null;
        Map<String, T> handlers = new HashMap<>(); // empty set by default
        if (CSVInputHandler.class.isAssignableFrom(type)) {
        	for(Entry<String, String> file : files.entrySet()) {
        		
        		CSVInputHandler handler = null;
        		
        		if(file.getKey().equalsIgnoreCase("PERSONAL"))
        		{
        			handler = new PersonalCSVInputHandler(configuration, storage.getTempJdbcTemplate());
        		}
        		
        		if(file.getKey().equalsIgnoreCase("ACTIVITY"))
        		{
        			handler = new ActivityCSVInputHandler(configuration, storage.getTempJdbcTemplate());
        		}
        		
        		if(file.getKey().equalsIgnoreCase("COURSE"))
        		{
        			handler = new CourseCSVInputHandler(configuration, storage.getTempJdbcTemplate());
        		}
        		
        		if(file.getKey().equalsIgnoreCase("GRADE"))
        		{
        			handler = new GradeCSVInputHandler(configuration, storage.getTempJdbcTemplate());
        		}
        		
        		if(file.getKey().equalsIgnoreCase("ENROLLMENT"))
        		{
        			handler = new EnrollmentCSVInputHandler(configuration, storage.getTempJdbcTemplate());
        		}
        		
        		if(handler != null)
        		{
        			handler.setFilePath(file.getValue());
        			handlers.put(handler.getFileName(), (T)handler);
        		}
        	}
        	
        } // add other types here
        return handlers;
    }

    /**
     * Loads and verifies the standard CSVs from the inputs directory
     * @param inputCollections all collections to load (empty indicates that all should be loaded, null indicates none should be loaded)
     * @return a map of all loaded collection types -> the results of the load
     */
    public Map<InputCollection, InputHandler.ReadResult> loadInputCollection(InputCollection... inputCollections) {
        Map<InputCollection, InputHandler.ReadResult> loaded = new HashMap<>();
        if (inputCollections == null) {
            logger.info("Not loading any CSV files (empty inputCollections param)");
        } else {
            logger.info("load CSV files from: "+configuration.inputDirectory.getAbsolutePath());
            try {
                // Initialize the CSV handlers
                Map<String, CSVInputHandler> csvInputHandlerMap = findHandlers(CSVInputHandler.class);
                Collection<CSVInputHandler> csvInputHandlers = new ArrayList<>(csvInputHandlerMap.values());
                if (inputCollections.length > 0) { // null or empty means include them all
                    for (CSVInputHandler entry : csvInputHandlers) {
                        if (!ArrayUtils.contains(inputCollections, entry.getHandledCollection())) {
                            // filtering this one out
                            csvInputHandlerMap.remove(entry.getFileName());
                        }
                    }
                    // rebuild it from whatever is left
                    csvInputHandlers = csvInputHandlerMap.values();
                }
                logger.info("Loaded "+csvInputHandlers.size()+" CSV InputHandlers: "+csvInputHandlerMap.keySet());

                // First we verify the CSV files
                for (CSVInputHandler csvInputHandler : csvInputHandlers) {
                    csvInputHandler.readCSV(true); // force it true just in case
                    logger.info(csvInputHandler.getFileName()+" file and header appear valid");
                }
                
                List<CSVInputHandler> csvInputList = new ArrayList<>(csvInputHandlers);
                
                Comparator<CSVInputHandler> comparator = new Comparator<CSVInputHandler>() {
                    public int compare(CSVInputHandler c1, CSVInputHandler c2) {
                        return c1.getOrder() < c2.getOrder() ? -1 : 1;
                    }
                };

                Collections.sort(csvInputList, comparator); // use the comparator as much as u wan
           
                // Next we load the data into the temp DB
                for (CSVInputHandler csvInputHandler : csvInputList) {
                    InputHandler.ReadResult result = csvInputHandler.readInputIntoDB();
                    if (!result.failures.isEmpty()) {
                        logger.error(result.failures.size()+" failures while parsing "+result.handledType+":\n"+ StringUtils.join(result.failures, "\n")+"\n");
                    }
                    logger.info(result.loaded+" lines from "+result.handledType+" (out of "+result.total+" lines) inserted into temp DB (with "+result.failed+" failures): "+result);
                    loaded.put(csvInputHandler.getHandledCollection(), result);
                    loadedInputCollections.put(csvInputHandler.getHandledCollection(), csvInputHandler);
                }

                logger.info("Loaded CSV files: "+loadedInputCollections.keySet());
            } catch (Exception e) {
                String msg = "Failed to load CSVs file(s): "+e;
                logger.error(msg);
                throw new RuntimeException(msg, e);
            }
        }
        return loaded;
    }
}
