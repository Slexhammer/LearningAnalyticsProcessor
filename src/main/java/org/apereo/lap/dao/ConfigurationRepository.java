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
package org.apereo.lap.dao;

import org.apereo.lap.dao.model.Configuration;
import org.apereo.lap.dao.model.RiskConfidence;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides data access via JPA to the RiskConfidence table.
 * The risk confidence table contains a historical record of each processing run.
 *
 */
public interface ConfigurationRepository extends JpaRepository<Configuration, Long>, ConfigurationRepositoryExtension  {
	
}
