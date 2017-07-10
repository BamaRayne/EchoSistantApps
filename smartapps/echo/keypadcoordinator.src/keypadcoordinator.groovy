/* 
 * KeyPad Coordinator - From the creators of EchoSistant  
 
 *	6/24/2017		Version:1.0 R.0.0.1		initial release
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
    name		: "KeypadCoordinator",
    namespace	: "Echo",
    author		: "JH/BD",
    description	: "A SmartApp to bring out the power of your KeyPad",
    category	: "My Apps",
	iconUrl			: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Keypad.png",
	iconX2Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Keypad@2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/Keypad@2x.png")

/**********************************************************************************************************************************************/
private def textVersion() {
	def text = "1.0"
}
private release() {
    def text = "R.0.0.10"
}
/**********************************************************************************************************************************************/

preferences {
    page(name: "main")
    page(name: "profiles")
    page(name: "advanced")
    }
page name: "main"
    def main() {
    	dynamicPage (name: "main", title: "Keypad Coordinator Profiles (${childApps?.size()})", install: true, uninstall: true) {
    	if (childApps?.size()) {  
    		section("Keypad Coordinator",  uninstall: false){
    		app(name: "profiles", appName: "KeyPad Profiles", namespace: "Echo", title: "Create a new Profile", multiple: true,  uninstall: false)
    		}
    	}
    else {
    	section("Keypad Coordinator",  uninstall: false){
    	paragraph "NOTE: Looks like you haven't created any Profiles yet.\n \nPlease make sure you have installed the Echo : KeypadCoordinator app before creating your first Profile!"
    	app(name: "profiles", appName: "KeyPad Profiles", namespace: "Echo", title: "Create a new Profile", multiple: true,  uninstall: false)
    	}
    }
    section("Settings",  uninstall: false, hideable: true, hidden: true){
    	input "debug", "bool", title: "Enable Debug Logging", default: true, submitOnChange: true
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
//		subscribe(app, appHandler)
        webCoRE_init()
        subscribe(location, "askAlexaMQ", askAlexaMQHandler)
        //Other Apps Events
        state.esEvent = [:]
        subscribe(location, "echoSistant", echoSistantHandler)
		state.esProfiles = state.esProfiles ? state.esProfiles : []
        //CoRE and other 3rd party apps
        sendLocationEvent(name: "KeypadCoordinator", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "Keypad Coordinator list refresh")
//		sendLocationEvent(name: "echoSistant", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "RemindR list refresh")
        //def children = getChildApps()
}
/************************************************************************************************************
		3RD Party Integrations
************************************************************************************************************/
private webCoRE_handle(){return'webCoRE'}
private webCoRE_init(pistonExecutedCbk){state.webCoRE=(state.webCoRE instanceof Map?state.webCoRE:[:])+(pistonExecutedCbk?[cbk:pistonExecutedCbk]:[:]);subscribe(location,"${webCoRE_handle()}.pistonList",webCoRE_handler);if(pistonExecutedCbk)subscribe(location,"${webCoRE_handle()}.pistonExecuted",webCoRE_handler);webCoRE_poll();}
private webCoRE_poll(){sendLocationEvent([name: webCoRE_handle(),value:'poll',isStateChange:true,displayed:false])}
public  webCoRE_execute(pistonIdOrName,Map data=[:]){def i=(state.webCoRE?.pistons?:[]).find{(it.name==pistonIdOrName)||(it.id==pistonIdOrName)}?.id;if(i){sendLocationEvent([name:i,value:app.label,isStateChange:true,displayed:false,data:data])}}
public  webCoRE_list(mode){def p=state.webCoRE?.pistons;if(p)p.collect{mode=='id'?it.id:(mode=='name'?it.name:[id:it.id,name:it.name])}}
public  webCoRE_handler(evt){switch(evt.value){case 'pistonList':List p=state.webCoRE?.pistons?:[];Map d=evt.jsonData?:[:];if(d.id&&d.pistons&&(d.pistons instanceof List)){p.removeAll{it.iid==d.id};p+=d.pistons.collect{[iid:d.id]+it}.sort{it.name};state.webCoRE = [updated:now(),pistons:p];};break;case 'pistonExecuted':def cbk=state.webCoRE?.cbk;if(cbk&&evt.jsonData)"$cbk"(evt.jsonData);break;}}

def echoSistantHandler(evt) {
	def result
	if (!evt) return
    log.warn "received event from EchoSistant with data: $evt.data"
	switch (evt.value) {
		case "refresh":
		state.esProfiles = evt.jsonData && evt.jsonData?.profiles ? evt.jsonData.profiles : []
			break
		case "runReport":
			def profile = evt.jsonData
            	result = runReport(profile)
            break	
    }
    return result
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
    sendLocationEvent(name: "KeypadCoordinator", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "Keypad Coordinator list refresh")
} 
//def childInitialized(message) {
//	state.activeRetrigger = message
//}

def askAlexaMQHandler(evt) {
   if (!evt) return
      switch (evt.value) {
         case "refresh":
            state.askAlexaMQ = evt.jsonData && evt.jsonData?.queues ? evt.jsonData.queues : []
            break
      }
}
def listaskAlexaMQHandler() {
log.warn "child requesting askAlexa Message Queues"
	return state.askAlexaMQ
}
