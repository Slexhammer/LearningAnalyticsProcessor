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
package org.apereo.lap.services.pipeline;

import org.apereo.lap.model.Processor;

import java.util.ArrayList;

/**
 * Handles the pipeline processing for a specific type of processor (e.g. Kettle)
 *
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ vt.edu)
 */
public interface PipelineProcessor {

    /**
     * @return the name of the processor type handled by this processor
     */
    Processor.ProcessorType getProcessorType();

    /**
     * Do the actual processing for this processor (and populate the results data and temp store if needed)
     *
     * If there is a failure and the pipeline should be stopped then throw a RuntimeException
     *
     * @return the results of the processor run
     * @throws java.lang.RuntimeException if the processor fails
     */
    ProcessorResult process();

    public static class ProcessorResult {
        public Processor.ProcessorType type;
        public long totalTimeMS;
        public long startTimeMS;
        public long endTimeMS;
        public ArrayList<String> failures;

        public ProcessorResult(Processor.ProcessorType processorType) {
            this.type = processorType;
            startTimeMS = System.currentTimeMillis();
        }

        public void done(ArrayList<String> failures) {
            this.failures = failures;
            this.endTimeMS = System.currentTimeMillis();
            this.totalTimeMS = this.endTimeMS - this.startTimeMS;
        }
    }

}