/* 
 * RemindR - An EchoSistant Smart App 
 *
 *	5/30/2017		Version:1.0 R.0.0.5		app touch cancelation
 *	5/24/2017		Version:1.0 R.0.0.2		ad-hoc triggering
 *
 *
 *  Copyright 2017 Jason Headley & Bobby Dobrescu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
/**********************************************************************************************************************************************/
definition(
    name		: "RemindR",
    namespace	: "Echo",
    author		: "JH/BD",
    description	: "Never miss an important event",
    category	: "My Apps",
	iconUrl			: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR.png",
	iconX2Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png")

/**********************************************************************************************************************************************/
private def textVersion() {
	def text = "1.0"
}
private release() {
    def text = "R.0.0.5"
}
/**********************************************************************************************************************************************/

preferences {
	page(name: "main")
    page(name: "profiles")
    page(name: "advanced")
}

		page name: "main"
            def main() {
                dynamicPage (name: "main", title: "Reminders and Notifications (${childApps?.size()})", install: true, uninstall: true) {
                    if (childApps?.size()) {  
                        section("Reminders",  uninstall: false){
                            app(name: "profiles", appName: "RemindRProfiles", namespace: "Echo", title: "Create a new Reminder", multiple: true,  uninstall: false)
                        }
                    }
                    else {
                        section("Reminders",  uninstall: false){
                            paragraph "NOTE: Looks like you haven't created any reminders yet.\n \nPlease make sure you have installed the Echo : RemindRProfile app before creating your first reminder!"
                            app(name: "profiles", appName: "RemindRProfiles", namespace: "Echo", title: "Create a new Reminder", multiple: true,  uninstall: false)
                        }
                    }
					if (state.activeRetrigger) {
                    	section("Active Retrigger"){
                        	paragraph ("${state.activeRetrigger}")
						}
                    }
                    section("Settings",  uninstall: false, hideable: true, hidden: true){
						input "debug", "bool", title: "Enable Debug Logging", default: true, submitOnChange: true
            			input "wZipCode", "text", title: "Zip Code (If Location Not Set)", required: "false"
                        paragraph ("Version: ${textVersion()} | Release: ${release()}")
					}

             	}
	        }       
/************************************************************************************************************
		Base Process
************************************************************************************************************/
def installed() {
	if (debug) log.debug "Installed with settings: ${settings}"
    state.ParentRelease = release()
    initialize()
}
def updated() { 
	if (debug) log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}
def initialize() {
		subscribe(app, appHandler)
        //Other Apps Events
        state.esEvent = [:]
        state.activeRetrigger
        subscribe(location, "echoSistant", echoSistantHandler)
		state.esProfiles = state.esProfiles ? state.esProfiles : []
        //CoRE and other 3rd party apps
        sendLocationEvent(name: "remindR", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "RemindR list refresh")
        def children = getChildApps()


}
/************************************************************************************************************
		3RD Party Integrations
************************************************************************************************************/
def echoSistantHandler(evt) {
	if (!evt) return
    log.warn "received event from EchoSistant with data: $evt.data"
	switch (evt.value) {
		case "refresh":
		state.esProfiles = evt.jsonData && evt.jsonData?.profiles ? evt.jsonData.profiles : []
			break
	}
}
def listEchoSistantProfiles() {
log.warn "child requesting esProfiles"
	return state.esProfiles = state.esProfiles ? state.esProfiles : []
}

def getProfileList(){
		return getChildApps()*.label
}
def childUninstalled() {
	if (debug) log.debug "Refreshing Profiles for 3rd party apps, ${getChildApps()*.label}"
    sendLocationEvent(name: "remindR", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "RemindR list refresh")
} 
def childInitialized(message) {
	state.activeRetrigger = message
}
/***********************************************************************************************************************
    RUN ADHOC REPORT
***********************************************************************************************************************/
def runReport(profile) {
def result
           		childApps.each {child ->
                        def ch = child.label
                		if (ch == profile) { 
                    		if (debug) log.debug "Found a profile, $profile"
                            result = child.runProfile(ch)
						}
            	}
                return result
}
/***********************************************************************************************************************
    CANCEL RETRIGGER
***********************************************************************************************************************/
def cancelRetrigger() {
def result
           		childApps.each {child ->
                        def ch = child.label
                		def chMessage = child.retriveMessage()
                        if (chMessage == state.activeRetrigger) { 
                    		if (debug) log.debug "Found a profile for the retrigger = $ch"
                            result = child.cancelRetrigger()
						}
            	}
                //return result
                log.warn "retrigger cancelation was $result"
}
def appHandler(evt) {
    cancelRetrigger()
    log.debug "app event ${evt.name}:${evt.value} received"
}