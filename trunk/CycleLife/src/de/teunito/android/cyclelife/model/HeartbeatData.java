/*******************************************************************************
 * Copyright 2010 Tobias Teunissen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.teunito.android.cyclelife.model;

public class HeartbeatData {

	public int currentPulse = 0;
	public int batteryCharge = 0;
	public int cadence = 0;
	
	public int getCurrentPulse() {
		return currentPulse;
	}
	public int getBatteryCharge() {
		return batteryCharge;
	}
	public int getCadence() {
		return cadence;
	}
	public void setCurrentPulse(int currentPulse) {
		this.currentPulse = currentPulse;
	}
	public void setBatteryCharge(int batteryCharge) {
		this.batteryCharge = batteryCharge;
	}
	public void setCadence(int cadence) {
		this.cadence = cadence;
	}

}
