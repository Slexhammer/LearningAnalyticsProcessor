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
package org.apereo.lap.dao.model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Represents a row in the risk_confidence table
 *
 */
@Entity(name="configuration")
public class Configuration extends BaseEntity {
    private static final long serialVersionUID = -8050631804690469935L;

    @Column(name="SSP_BASE_URL")
    private String sspBaseUrl;
    @Column(name="SSP_RISK_CONFIDENCE_THRESHOLD")
    private Float sspRiskConfidenceThreshold;
    @Column(name="SSP_ACTIVE")
    private boolean sspActive;
    
	public String getSspBaseUrl() {
		return sspBaseUrl;
	}

	public void setSspBaseUrl(String sspBaseUrl) {
		this.sspBaseUrl = sspBaseUrl;
	}

	public Float getSSPRiskConfidenceThreshold() {
		return sspRiskConfidenceThreshold;
	}

	public void setSSPRiskConfidenceThreshold(Float sspRiskConfidenceThreshold) {
		this.sspRiskConfidenceThreshold = sspRiskConfidenceThreshold;
	}

	public boolean isSSPActive() {
		return sspActive;
	}

	public void setSSPActive(boolean sspActive) {
		this.sspActive = sspActive;
	}

	@Override
	protected boolean matchesClassAndId(Object other) {
        return Configuration.class.isInstance(other) ? matchesId((Configuration)other) : false;
    }
}
