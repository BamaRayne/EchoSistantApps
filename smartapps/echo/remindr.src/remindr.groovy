/*
 * RemindR - An EchoSistant

 *	7/19/2018 Version: 2.0 (R.0.0.13) Started rewrite of v2
 *
 *  Copyright 2016, 2017, 2018 Jason Headley & Bobby Dobrescu & Anthony Santilli
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
	name: "RemindR",
	namespace: "Echo",
	author: "JH/BD",
	description: "Never miss an important event",
	category: "My Apps",
	iconUrl: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR.png",
	iconX2Url: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png")

/**********************************************************************************************************************************************/
private appVersion() { return "2.0.0d" }
private appDate() { return "08/17/2018" }
private release() { return "R.0.0.13"}
/**********************************************************************************************************************************************/

preferences {
	page(name: "mainPage")
	page(name: "uninstallPage")
}

def appInfoSect()	{
	section() {
		def str = "Released: (${appDate()})"
		paragraph title: "${app?.name} (V${appVersion()})", str, image: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png"
	}
}

def mainPage() {
	dynamicPage (name: "mainPage", title: "RemindR Home Page", install: true, uninstall: false) {
		appInfoSect()
		def profApp = getChildApps()
		if(!profApp) {
			section(" ") {
				paragraph "You haven't created any Reminders yet!!\nTap Create New Reminder to get Started"
			}
		}
		section("Reminders: (${childApps?.size()})") {
			def i0 = "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-RemindR@2x.png"
			app(name: "profApp", appName: "RemindRProfiles", namespace: "Echo", title: "Create New Reminder", multiple: true, image: i0)
		}
		if (state?.activeRetrigger) {
			section("Retriggers:"){
				paragraph title: "Active Retrigger", "${state?.activeRetrigger}", state: "complete"
			}
		}
		section("Settings:") {
			input "debug", "bool", title: "Enable Debug Logging", defaultValue: false, submitOnChange: true, image: getAppImg("debug.png")
			if(!location?.zipCode) {
				input "wZipCode", "text", title: "Zip Code", required: false, submitOnChange: true, image: getAppImg("info.png")
			}
		}
		section(" ") {
			href "uninstallPage", title: "Remove App & All Reminders", description: "", image: getAppImg("uninstall.png")
		}
	}
}

def uninstallPage() {
	dynamicPage(name: "uninstallPage", title: "Uninstall", install: false, uninstall: true) {
		section() {
			paragraph title: "NOTICE", "This will completely uninstall this App and All Reminders.", required: true, state: null
		}
		remove("Remove this App and All (${getChildApps()?.size()}) Reminders!", "WARNING!!!", "Last Chance to Stop!\nThis action is not reversible\n\nThis App and All Reminders will be removed...")
	}
}
/************************************************************************************************************
		Base Process
************************************************************************************************************/
def installed() {
	if (debug) { log.debug "Installed with settings: ${settings}" }
	state.ParentRelease = release()
	initialize()
}
def updated() {
	if (debug) { log.debug "Updated with settings: ${settings}" }
	unsubscribe()
	initialize()
}
def initialize() {
	subscribe(app, appHandler)
	webCoRE_init()
	subscribe(location, "askAlexaMQ", askAlexaMQHandler)
	pushover_init()
	//Other Apps Events
	state.esEvent = [:]
	state.activeRetrigger
	//subscribe(location, "echoSistant", echoSistantHandler)
	state.esProfiles = state?.esProfiles ? state?.esProfiles : []
	//CoRE and other 3rd party apps
	sendLocationEvent(name: "remindR", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "RemindR list refresh")
	sendLocationEvent(name: "echoSistant", value: "refresh", data: [profiles: getProfileList()] , isStateChange: true, descriptionText: "RemindR list refresh")
	setChildStates()
}

private setChildStates() {
	getChildApps()?.each { 
		it?.setDebugVal((settings?.debug == true))
		it?.setZipCode(location?.zipCode ?: settings?.zipCode)
	}
}

def getAppImg(imgName)	{ return "https://echosistant.com/es5_content/images/$imgName" }
/************************************************************************************************************
		3RD Party Integrations
************************************************************************************************************/
private webCoRE_handle(){return'webCoRE'}
private webCoRE_init(pistonExecutedCbk){state.webCoRE=(state.webCoRE instanceof Map?state.webCoRE:[:])+(pistonExecutedCbk?[cbk:pistonExecutedCbk]:[:]);subscribe(location,"${webCoRE_handle()}.pistonList",webCoRE_handler);if(pistonExecutedCbk)subscribe(location,"${webCoRE_handle()}.pistonExecuted",webCoRE_handler);webCoRE_poll();}
private webCoRE_poll(){sendLocationEvent([name: webCoRE_handle(),value:'poll',isStateChange:true,displayed:false])}
public  webCoRE_execute(pistonIdOrName,Map data=[:]){def i=(state.webCoRE?.pistons?:[]).find{(it.name==pistonIdOrName)||(it.id==pistonIdOrName)}?.id;if(i){sendLocationEvent([name:i,value:app.label,isStateChange:true,displayed:false,data:data])}}
public  webCoRE_list(mode){def p=state.webCoRE?.pistons;if(p)p.collect{mode=='id'?it.id:(mode=='name'?it.name:[id:it.id,name:it.name])}}
public  webCoRE_handler(evt){switch(evt.value){case 'pistonList':List p=state.webCoRE?.pistons?:[];Map d=evt.jsonData?:[:];if(d.id&&d.pistons&&(d.pistons instanceof List)){p.removeAll{it.iid==d.id};p+=d.pistons.collect{[iid:d.id]+it}.sort{it.name};state.webCoRE = [updated:now(),pistons:p];};break;case 'pistonExecuted':def cbk=state.webCoRE?.cbk;if(cbk&&evt.jsonData)"$cbk"(evt.jsonData);break;}}

//PushOver-Manager Input Generation Functions
private getPushoverSounds(){return (Map) atomicState?.pushoverManager?.sounds?:[:]}
private getPushoverDevices(){List opts=[];Map pmd=atomicState?.pushoverManager?:[:];pmd?.apps?.each{k,v->if(v&&v?.devices&&v?.appId){Map dm=[:];v?.devices?.sort{}?.each{i->dm["${i}_${v?.appId}"]=i};addInputGrp(opts,v?.appName,dm);}};return opts;}
private inputOptGrp(List groups,String title){def group=[values:[],order:groups?.size()];group?.title=title?:"";groups<<group;return groups;}
private addInputValues(List groups,String key,String value){def lg=groups[-1];lg["values"]<<[key:key,value:value,order:lg["values"]?.size()];return groups;}
private listToMap(List original){original.inject([:]){r,v->r[v]=v;return r;}}
private addInputGrp(List groups,String title,values){if(values instanceof List){values=listToMap(values)};values.inject(inputOptGrp(groups,title)){r,k,v->return addInputValues(r,k,v)};return groups;}
private addInputGrp(values){addInputGrp([],null,values)}
//PushOver-Manager Location Event Subscription Events, Polling, and Handlers
public pushover_init(){subscribe(location,"pushoverManager",pushover_handler);pushover_poll()}
public pushover_cleanup(){state?.remove("pushoverManager");unsubscribe("pushoverManager");}
public pushover_poll(){sendLocationEvent(name:"pushoverManagerCmd",value:"poll",data:[empty:true],isStateChange:true,descriptionText:"Sending Poll Event to Pushover-Manager")}
public pushover_msg(List devs,Map data){if(devs&&data){sendLocationEvent(name:"pushoverManagerMsg",value:"sendMsg",data:data,isStateChange:true,descriptionText:"Sending Message to Pushover Devices: ${devs}");}}
public pushover_handler(evt){Map pmd=atomicState?.pushoverManager?:[:];switch(evt?.value){case"refresh":def ed = evt?.jsonData;String id = ed?.appId;Map pA = pmd?.apps?.size() ? pmd?.apps : [:];if(id){pA[id]=pA?."${id}"instanceof Map?pA[id]:[:];pA[id]?.devices=ed?.devices?:[];pA[id]?.appName=ed?.appName;pA[id]?.appId=id;pmd?.apps = pA;};pmd?.sounds=ed?.sounds;break;case "reset":pmd=[:];break;};atomicState?.pushoverManager=pmd;}
//Builds Map Message object to send to Pushover Manager
private buildPushMessage(List devices,Map msgData,timeStamp=false){if(!devices||!msgData){return};Map data=[:];data?.appId=app?.getId();data.devices=devices;data?.msgData=msgData;if(timeStamp){data?.msgData?.timeStamp=new Date().getTime()};pushover_msg(devices,data);}


def echoSistantHandler(evt) {
	def result
	if (!evt) { return }
	log.warn "received event from EchoSistant with data: ${evt?.data}"
	switch (evt?.value) {
		case "refresh":
			state.esProfiles = evt?.jsonData && evt?.jsonData?.profiles ? evt?.jsonData.profiles : []
			break
		case "runReport":
			result = runReport(evt?.jsonData)
			break
	}
	return result
}

def listEchoSistantProfiles() {
	log.warn "child requesting esProfiles"
	return state.esProfiles = state.esProfiles ? state.esProfiles : []
}

def getProfileList(){
	return getChildApps()*.getLabel()
}

def childUninstalled() {
	if (debug) { log.debug "Refreshing Profiles for 3rd party apps, ${getProfileList()}" }
	sendLocationEvent(name: "remindR", value: "refresh", data: [profiles: getProfileList()], isStateChange: true, descriptionText: "RemindR list refresh")
}

def childInitialized(message) {
	state.activeRetrigger = message
}

def askAlexaMQHandler(evt) {
   if (!evt) { return }
	switch (evt?.value) {
		case "refresh":
		state?.askAlexaMQ = evt?.jsonData && evt?.jsonData?.queues ? evt?.jsonData?.queues : []
		break
	}
}

def listaskAlexaMQHandler() {
	log.warn "child requesting askAlexa Message Queues"
	return state?.askAlexaMQ
}

def getAdHocReports() {
	log.warn "looking for as-hoc reports"
	def childList = []
	getChildApps()?.each { c ->
		log.warn "child ${c?.getLabel()} has actionType = ${state?.actionType}"
		if (c?.getStateVal("actionType") == "Ad-Hoc Report") { childList?.push(c?.getLabel() as String) }
	}
	log.warn "finished looking and found: $childList"
	return childList
}

/***********************************************************************************************************************
	RUN ADHOC REPORT
***********************************************************************************************************************/
def runReport(String profile) {
	def result = null
	childApps.each {c ->
		String ch = c?.getLabel()
		if (ch == profile) {
			if (debug) { log.debug "Found a profile, $profile" }
			result = c?.runProfile(ch)
		}
	}
	return result
}
/***********************************************************************************************************************
	CANCEL RETRIGGER
***********************************************************************************************************************/
def cancelRetrigger() {
	def result = null
	childApps.each { child ->
		def ch = child?.getLabel()
		def chMessage = child?.retriveMessage()
		if (chMessage == state?.activeRetrigger) {
			if (debug) { log.debug "Found a profile for the retrigger = $ch" }
			result = child?.cancelRetrigger()
		}
	}
	log.warn "retrigger cancelation was $result"
	return result
}

def appHandler(evt) {
	cancelRetrigger()
	log.debug "app event ${evt.name}:${evt.value} received"
}

public static List stVoicesList() {
	return [
		"da-DK Naja",
		"da-DK Mads",
		"de-DE Marlene",
		"de-DE Hans",
		"de-DE Vicki",
		"en-US Salli",
		"en-US Joey",
		"en-AU Nicole",
		"en-AU Russell",
		"en-GB Amy",
		"en-GB Brian",
		"en-GB Emma",
		"en-GB Gwyneth",
		"en-GB Geraint",
		"en-IN Raveena",
		"en-US Ivy",
		"en-US Joanna",
		"en-US Justin",
		"en-US Kendra",
		"en-US Kimberly",
		"en-US Matthew",
		"es-ES Conchita",
		"es-ES Enrique",
		"es-US Penelope",
		"es-US Miguel",
		"fr-CA Chantal",
		"fr-FR Celine",
		"fr-FR Mathieu",
		"is-IS Dora",
		"is-IS Karl",
		"it-IT Carla",
		"it-IT Giorgio",
		"ja-JP Mizuki",
		"ja-JP Takumi",
		"nb-NO Liv",
		"nl-NL Lotte",
		"nl-NL Ruben",
		"pl-PL Agnieszka",
		"pl-PL Jacek",
		"pl-PL Ewa",
		"pl-PL Jan",
		"pl-PL Maja",
		"pt-BR Vitoria",
		"pt-BR Ricardo",
		"pt-PT Cristiano",
		"pt-PT Ines",
		"ro-RO Carmen",
		"ru-RU Tatyana",
		"ru-RU Maxim",
		"sv-SE Astrid",
		"tr-TR Filiz"
	]
}

public static List custSoundList() {
	return [
		"Custom URI",
		"Alexa: Bada Bing Bada Boom",
		"Alexa: Beep Beep",
		"Alexa: Boing",
		"Alexa: Open Sesame",
		"Bell 1",
		"Bell 2",
		"Dogs Barking",
		"Fire Alarm",
		"The mail has arrived",
		"A door opened",
		"There is motion",
		"Smartthings detected a flood",
		"Smartthings detected smoke",
		"Soft Chime",
		"Someone is arriving",
		"Piano",
		"Lightsaber"
	]
}