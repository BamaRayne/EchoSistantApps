/* 
 * KeyPad CoOrdinator - Child app 
 ************************************ FOR INTERNAL USE ONLY ******************************************************
							
 *		6/24/2017		Version:1.0 R.0.0.1		Initial Release
 * 
 *  Copyright 2016 Jason Headley & Bobby Dobrescu
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
	name			: "KeyPad Profiles",
    namespace		: "Echo",
    author			: "JH/BD",
	description		: "KeyPad CoOrdinator Child App",
	category		: "My Apps",
    parent			: "Echo:KeyPadCoOrdinator",
	iconUrl			: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant.png",
	iconX2Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png",
	iconX3Url		: "https://raw.githubusercontent.com/BamaRayne/Echosistant/master/smartapps/bamarayne/echosistant.src/app-Echosistant@2x.png")
/**********************************************************************************************************************************************/
private release() {
	def text = "R.0.0.1"
}
/**********************************************************************************************************************************************/
preferences {

    page name: "mainProfilePage"
    		page name: "pSend"          
        	page name: "pActions"
        	page name: "pConfig"
            page name: "pGroups"
        	page name: "pRestrict"
  			page name: "pDeviceControl"
            page name: "pPerson"
            page name: "pVirPerAction"
}

//dynamic page methods
def mainProfilePage() {	
    dynamicPage(name: "mainProfilePage", title:"", install: true, uninstall: installed) {
        def deviceId = "${app.label}" 
        def d = getChildDevice("${app.label}")
		section ("Name Your Profile") {
 		   	label title:"Profile Name", required:false, defaultValue: "New Profile", submitOnChange: true 
        }
        if (app.label != null) {
        section("${app.label}'s Presence ") {
            input "kpVirPer", "bool", title: "Does ${app.label} need a virtual presence sensor?", refreshAfterSelection: true
            if (kpVirPer) {
                href "pPerson", title: "Create/Delete a Virtual Presence Device for ${app.label}"
                if(d) {
                    input "sLocksVP","capability.lockCodes", title: "${app.label} can check-in using these keypads", required: true, multiple: true, submitOnChange: true
                    input "vpCode", "number", title: "${app.label}'s check-in code (4 digits)", required: false, refreshAfterSelection: true
                    input "vpActions", "bool", title: "Perform these actions when ${app.label} arrives", required: false, submitOnChange: true
                    input "notifyVPArrive", "bool", title: "Notify me when ${app.label} Arrives", required: false, submitOnChange: true
                    input "notifyVPDepart", "bool", title: "Notify me when ${app.label} Departs", required: false, submitOnChange: true
                    if (notifyVPArrive || notifyVPDepart) {
                        href "pVPNotifyPage", title: "${app.label}'s Notification Settings"
                    }
                }
            }    
        }
        section("${app.label}'s Garage Doors ") {
            input "garageDoors", "bool", title: "Can ${app.label} control the garage doors?", refreshAfterSelection: true
            if (garageDoors) {
                input "sLocksGarage","capability.lockCodes", title: "Select the Keypads that ${app.label} can use for the garage door", required: false, multiple: true, submitOnChange: true
                href "pGarageDoorNotify",title: "Garage Door Notification Settings"//, description: notificationPageDescription(), state: notificationPageDescription() ? "complete" : "")
                input "sDoor1", "capability.garageDoorControl", title: "${app.label} can control these garage door(s)", multiple: true, required: false, submitOnChange: true
                input "doorCode1", "number", title: "Code (4 digits)", required: false, refreshAfterSelection: true
                input "gd1Actions", "bool", title: "Perform this profiles actions when this Garage Door is opened via Keypad", required: false, submitOnChange: true
                if (doorCode1) {
                    input "sDoor2", "capability.garageDoorControl", title: "Allow These Garage Door(s)...", multiple: true, required: false, submitOnChange: true
                    input "doorCode2", "number", title: "Code (4 digits)", required: false, refreshAfterSelection: true
                    input "gd2Actions", "bool", title: "Perform this profiles actions when this Garage Door is opened via Keypad", required: false, submitOnChange: true
                    if (doorCode2) {
                        input "gd3Actions", "bool", title: "Perform this profiles actions when this Garage Door is opened via Keypad", required: false, submitOnChange: true
                        input "sDoor3", "capability.garageDoorControl", title: "Allow These Garage Door(s)...", multiple: true, required: false, submitOnChange: true
                        input "doorCode3", "number", title: "Code (4 digits)", required: false, refreshAfterSelection: true
                    	}
                    }
                }
            }
        section("${app.label}'s SHM controls") {
            input "SHMConfigure", "bool", title: "Will ${app.label} have control of SHM?", refreshAfterSelection: true
            if (SHMConfigure) {
                input "sLocksSHM","capability.lockCodes", title: "Select Keypads", required: true, multiple: true, submitOnChange: true
                input "shmCode", "number", title: "Code (4 digits)", required: false, refreshAfterSelection: true
                input "keypadstatus", "bool", title: "Send status to keypad?", required: true, defaultValue: false
                href "pShmNotifyPage", title: "SHM Profile Notification Settings"//, description: notificationPageDescription(), state: notificationPageDescription() ? "complete" : "")
            }
        }    
        if (SHMConfigure) {
            def hhPhrases = location.getHelloHome()?.getPhrases()*.label
            hhPhrases?.sort()
            section("Execute These Routines") {
                input "armRoutine", "enum", title: "Arm/Away routine", options: hhPhrases, required: false
                input "disarmRoutine", "enum", title: "Disarm routine", options: hhPhrases, required: false
                input "stayRoutine", "enum", title: "Arm/Stay routine", options: hhPhrases, required: false
                input "armDelay", "number", title: "Arm Delay (in seconds)", required: false
                input "notifyIncorrectPin", "bool", title: "Notify you when incorrect code is used?", required: false, defaultValue: false, submitOnChange: true
            }
		}
        section("${app.label}'s Thermostats") {
        	input "thermostats", "bool", title: "Will ${app.label} be able to adjust the temperature?", refreshAfterSelection: true	
				if(thermostats) {
        			input "tempKeypad", "capability.lockCodes", title: "Enter temperature using these keypads", required: false, multiple: true, submitOnChange: true
        			input "tempStat", "capability.thermostat", title: "Change the temperature on these thermostats", required: false, multiple: true, submitOnChange: true
            		}
        		}
        section("General Keypad Settings") {
        	href "pGenSettings", title: "Configure the General Settings for ${app.label}"
        		}
        section("General Restrictions for ${app.label}") {
            href "pRestrict", title: "Does ${app.label} need to restricted?", description: pRestrictComplete(), state: pRestrictSettings()
			}
        }
    }   
}

//////////////////////////////////////////////////////////////////////////////
/////////// ACTIONS NOTIFICATIONS PAGES
//////////////////////////////////////////////////////////////////////////////
page name: "pVPNotifyPage"
def pVPNotifyPage() {
    dynamicPage(name: "pVPNotifyPage", title: "Notification Settings") {
        section {
            input "vpPhone", "phone", title: "Text This Number", description: "Phone number", required: false, submitOnChange: true
            paragraph "For multiple SMS recipients, separate phone numbers with a comma"
            input "vpNotification", "bool", title: "Send A Push Notification", description: "Notification", required: false, submitOnChange: true
        }
    }
}    
page name: "pShmNotifyPage"
def pShmNotifyPage() {
    dynamicPage(name: "pShmNotifyPage", title: "Notification Settings") {
        section {
            input "shmPhone", "phone", title: "Text This Number", description: "Phone number", required: false, submitOnChange: true
            paragraph "For multiple SMS recipients, separate phone numbers with a comma"
            input(name: "notifySHMArm", title: "Notify when arming SHM", type: "bool", required: false)
            input(name: "notifySHMDisarm", title: "Notify when disarming SHM", type: "bool", required: false)
            input "shmNotification", "bool", title: "Send A Push Notification", description: "Notification", required: false, submitOnChange: true
        }
    }
}        
page name: "pGarageDoorNotify"
def pGarageDoorNotify() {
    dynamicPage(name: "pGarageDoorNotify", title: "Notification Settings") {
        section {
            input(name: "garagePhone", type: "text", title: "Text This Number", description: "Phone number", required: false, submitOnChange: true)
            paragraph "For multiple SMS recipients, separate phone numbers with a semicolon(;)"
            input(name: "garagePush", type: "bool", title: "Send A Push Notification", description: "Notification", required: false, submitOnChange: true)
            if (phone != null || notification) {
                input(name: "notifyGdoorOpen", title: "Notify when opening", type: "bool", required: false)
                input(name: "notifyGdoorClose", title: "Notify when closing", type: "bool", required: false)
            }
        }
    }
}
//////////////////////////////////////////////////////////////////////////////
/////////// VIRTUAL PRESENCE PAGES       
//////////////////////////////////////////////////////////////////////////////
page name: "pPerson"
def pPerson(){
    dynamicPage(name: "pPerson", title: "", uninstall: false){
        section ("Manage the Profile Virtual Person Device", hideWhenEmpty: true){
            href "pPersonCreate", title: "Tap Here to Create the Virtual Person Device ~ '${app.label}'"
            href "pPersonDelete", title: "Tap Here to Delete the Virtual Person Device ~ '${app.label}'"
        }
    }
}
page name: "pPersonCreate"
def pPersonCreate(){
    dynamicPage(name: "pPersonCreate", title: "", uninstall: false) {
        section ("") {
            paragraph "You have created a virtual presence sensor device. You will now see this device in your 'Things' list " +
                " in the SmartThings Mobile App.  You will also see it in the 'MyDevices' tab of the IDE"
        }
        virtualPerson()
    }
}
//////////////////////////////////////////////////////////////////////////////
/////////// GENERAL SETTINGS PAGE      
//////////////////////////////////////////////////////////////////////////////
page name: "pGenSettings"
def pGenSettings() {
    dynamicPage(name: "pGenSettings", title: "Notification Settings") {
		section("Panic Button Actions") {
        	input "panicKeypad","capability.lockCodes", title: "When the panic button is pressed on these keypads...", required: false, multiple: true, submitOnChange: true
        	}
        section("Send Chime when contacts open") {
			input "chimeKeypad","capability.lockCodes", title: "These keypads will chime...", required: false, multiple: true, submitOnChange: true
			input "chimeContact", "capability.contactSensor", title: "...when these contacts open", required: false, multiple: true, submitOnChange: true
			}
    	}
	}    
//////////////////////////////////////////////////////////////////////////////
/////////// DEVICES SELECTION PAGE      
//////////////////////////////////////////////////////////////////////////////
    page name: "mDevices"    
        def mDevices(){
            dynamicPage(name: "mDevices", title: "",install: false, uninstall: false) {
                section ("Lights, Switches, and Fans", hideWhenEmpty: true){  
                    input "cSwitch", "capability.switch", title: "Allow these devices to be controlled...", multiple: true, required: false, submitOnChange: true                   
                    input "cFan", "capability.switchLevel", title: "Allow these devices that control fans...", multiple: true, required: false
                }     
                section ("Garage Doors, Window Coverings and Locks", hideWhenEmpty: true){ 
                	input "cLock", "capability.lock", title: "Allow These Lock(s)...", multiple: true, required: false, submitOnChange: true
                	input "cWindowCover", "capability.windowShade", title: "Allow These Window Covering Devices...", multiple: true, required: false, submitOnChange: true              
                    input "cDoor", "capability.garageDoorControl", title: "Allow These Garage Door(s)...", multiple: true, required: false, submitOnChange: true
					input "cRelay", "capability.switch", title: "Allow These Garage Door Relay(s)...", multiple: false, required: false, submitOnChange: true
                    if (cRelay) input "cContactRelay", "capability.contactSensor", title: "Allow This Contact Sensor to Monitor the Garage Door Relay(s)...", multiple: false, required: false                
                }    
                section ("Climate Control", hideWhenEmpty: true){ 
                 	input "cTstat", "capability.thermostat", title: "Allow These Thermostat(s)...", multiple: true, required: false
                    input "cIndoor", "capability.temperatureMeasurement", title: "Allow These Device(s) to Report the Indoor Temperature...", multiple: true, required: false
                 	input "cOutDoor", "capability.temperatureMeasurement", title: "Allow These Device(s) to Report the Outdoor Temperature...", multiple: true, required: false
                    input "cVent", "capability.switchLevel", title: "Allow These Smart Vent(s)...", multiple: true, required: false
                } 
                section ("Sensors"){//, hideWhenEmpty: true) {
                 	input "cMotion", "capability.motionSensor", title: "Allow These Motion Sensor(s)...", multiple: true, required: false
                    input "cHumidity", "capability.relativeHumidityMeasurement", title: "Allow These Humidity Sensor(s)...", multiple: true, required: false
                    input "cWindow", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are used on a Window(s)...", multiple: true, required: false      
                    input "cDoor1", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are used on a Door(s)...", multiple: true, required: false                     
                    input "cContact", "capability.contactSensor", title: "Allow These Contact Sensor(s) that are NOT used on Doors or Windows...", multiple: true, required: false      
            		input "cWater", "capability.waterSensor", title: "Allow These Water Sensor(s)...", multiple: true, required: false                       
                    input "cPresence", "capability.presenceSensor", title: "Allow These Presence Sensors(s)...", multiple: true, required: false
                }
                section ("Media" , hideWhenEmpty: true){
                    input "cSpeaker", "capability.musicPlayer", title: "Allow These Media Player Type Device(s)...", required: false, multiple: true
                    input "cSynth", "capability.speechSynthesis", title: "Allow These Speech Synthesis Capable Device(s)", multiple: true, required: false
                    input "cMedia", "capability.mediaController", title: "Allow These Media Controller(s)", multiple: true, required: false
                     if (cMedia?.size() > 1) {
                     paragraph "NOTE: only the first selected device is used by the Main intent. The additional devices MUST be used by Profiles"
                     }
                } 
                section ("Batteries", hideWhenEmpty: true ){
                    input "cBattery", "capability.battery", title: "Allow These Device(s) with Batteries...", required: false, multiple: true
                } 
         	}
    	}  
//////////////////////////////////////////////////////////////////////////////
/////////// NOTIFICATION OUTPUT PAGES       
//////////////////////////////////////////////////////////////////////////////
page name: "pSend"
    def pSend(){
        dynamicPage(name: "pSend", title: "", uninstall: false){
             section ("Speakers", hideWhenEmpty: true){
                input "synthDevice", "capability.speechSynthesis", title: "On this Speech Synthesis Type Devices", multiple: true, required: false
                input "sonosDevice", "capability.musicPlayer", title: "On this Sonos Type Devices", required: false, multiple: true, submitOnChange: true    
                if (sonosDevice) {
                    input "volume", "number", title: "Temporarily change volume", description: "0-100% (default value = 30%)", required: false
                }  
            }
            section ("Text Messages" ) {
            	input "sendContactText", "bool", title: "Enable Text Notifications to Contact Book (if available)", required: false, submitOnChange: true   
                if (sendContactText) input "recipients", "contact", title: "Send text notifications to (optional)", multiple: true, required: false
           			input "sendText", "bool", title: "Enable Text Notifications to non-contact book phone(s)", required: false, submitOnChange: true     
                if (sendText){      
                    paragraph "You may enter multiple phone numbers separated by comma to deliver the Alexa message as a text and a push notification. E.g. 8045551122,8046663344"
                    input name: "sms", title: "Send text notification to (optional):", type: "phone", required: false
                }
            }    
            section ("Push Messages") {
            	input "push", "bool", title: "Send Push Notification (optional)", required: false, defaultValue: false
            	input "notify", "bool", title: "Send message to Mobile App Notifications Tab (optional)", required: false, defaultValue: false
            }
            section ("Remote Speaker Settings") {
               	input "pRunMsg", "Text", title: "Play this predetermined message when this profile executes...", required: false
                input "pPreMsg", "text", title: "Play this message before your spoken message...", defaultValue: none, submitOnChange: true, required: false 
            }
            section ("Text and Push Notification Output") {
                input "pRunTextMsg", "Text", title: "Send this predetermined text when this profile executes...", required: false
                input "pPreTextMsg", "text", title: "Append this text before the text message...", defaultValue: none, required: false 
            }             
		}                 
    }   
//////////////////////////////////////////////////////////////////////////////
/////////// PROFILE ACTIONS PAGE       
//////////////////////////////////////////////////////////////////////////////
page name: "pActions"
    def pActions() {
        dynamicPage(name: "pActions", uninstall: false) {
        	def routines = location.helloHome?.getPhrases()*.label 
            if (routines) {routines.sort()}
            section ("Trigger these lights and/or execute these routines when the Profile runs...") {
                href "pDeviceControl", title: "Select Devices...", description: pDevicesComplete() , state: pDevicesSettings()
                input "pMode", "enum", title: "Choose Mode to change to...", options: location.modes.name.sort(), multiple: false, required: false 
            	def actions = location.helloHome?.getPhrases()*.label 
                if (actions) {
                    actions.sort()
            	input "pRoutine", "enum", title: "Select a Routine to execute", required: false, options: actions, multiple: false, submitOnChange: true
                if (pRoutine) {
                input "pRoutine2", "enum", title: "Select a Second Routine to execute", required: false, options: actions, multiple: false
            		}
                }
                input "shmState", "enum", title: "Set Smart Home Monitor to...", options:["stay":"Armed Stay","away":"Armed Away","off":"Disarmed"], multiple: false, required: false, submitOnChange: true
                	if (shmState) {
                    	input "shmStateKeypads", "capability.lockCodes",  title: "Send status change to these keypads...", multiple: true, required: false, submitOnChange: true
                        }
				input "pVirPer", "bool", title: "Toggle the Virtual Person State Automatically when this Profile Runs", default: false, submitOnChange: true, required: false
			}
        }
    }
//////////////////////////////////////////////////////////////////////////////
/////////// DEVICES AND GROUPS CONTROL PAGES    
//////////////////////////////////////////////////////////////////////////////
page name: "pDeviceControl"
    def pDeviceControl() {
            dynamicPage(name: "pDeviceControl", title: "",install: false, uninstall: false) {
                 section ("Switches", hideWhenEmpty: true){
                    input "sSwitches", "capability.switch", title: "Select Lights and Switches...", multiple: true, required: false, submitOnChange: true
                        if (sSwitches) {
                        	input "sSwitchCmd", "enum", title: "Command To Send",
                        		options:["on":"Turn on","off":"Turn off","toggle":"Toggle"], multiple: false, required: false, submitOnChange:true
                        	input "delaySwitches", "bool", title: "Delay Actions?", required: false, defaultValue: false, submitOnChange:true
                        	if (delaySwitches) {
                        		input "sSecondsOn", "number", title: "Turn on in Seconds?", defaultValue: none, required: false
                        		input "sSecondsOff", "number", title: "Turn off in Seconds?", defaultValue: none, required: false
                            }
                        	if (sSwitchCmd) input "sOtherSwitch", "capability.switch", title: "...and these other switches?", multiple: true, required: false, submitOnChange: true                        
                        	if (sOtherSwitch) input "sOtherSwitchCmd", "enum", title: "Command To Send to these other switches", 
                        					options: ["on1":"Turn on","off1":"Turn off","toggle1":"Toggle"], multiple: false, required: false, submitOnChange: true
                        	if (sOtherSwitchCmd)	input "delayOtherSwitches", "bool", title: "Delay Actions?", required: false, defaultValue: false, submitOnChange:true
                                if (delayOtherSwitches) {
                                    input "sOtherSecondsOn", "number", title: "Turn on in Seconds?", defaultValue: none, required: false
                                    input "sOtherSecondsOff", "number", title: "Turn off in Seconds?", defaultValue: none, required: false
                                }
                	}
                }
                section ("Dimmers", hideWhenEmpty: true){
                    input "sDimmers", "capability.switchLevel", title: "Select Dimmers...", multiple: true, required: false , submitOnChange:true
                        if (sDimmers) { input "sDimmersCmd", "enum", title: "Command To Send",
                        					options:["on":"Turn on","off":"Turn off", "set":"Set level"], multiple: false, required: false, submitOnChange:true
                        }
                        if (sDimmersCmd) {                       
                       		input "sDimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, submitOnChange: true	
                        	input "delayDimmers", "bool", title: "Delay Actions?", required: false, defaultValue: false, submitOnChange:true      
                            if (delayDimmers) {
                            	input "sSecondsDimmers", "number", title: "Turn on in Seconds?", defaultValue: none, required: false
                                input "sSecondsDimmersOff", "number", title: "Turn off in Seconds?", defaultValue: none, required: false                        
                            }
                       		input "sOtherDimmers", "capability.switchLevel", title: "...and these other Dimmers...", multiple: true, required: false , submitOnChange:true
                        		if (sOtherDimmers) { 
                                	input "sOtherDimmersCmd", "enum", title: "Command To Send to these other Dimmers", 
                        				options:["on":"Turn on","off":"Turn off","set":"Set level"], multiple: false, required: false, submitOnChange:true
                        		}
                        		if (sOtherDimmersCmd) {
                                   	input "sOtherDimmersLVL", "number", title: "Dimmers Level", description: "Set dimmer level", required: false, submitOnChange: true
									input "delayOtherDimmers", "bool", title: "Delay Actions?", required: false, defaultValue: false, submitOnChange: true
									if (delayOtherDimmers) {
                                       	input "sSecondsOtherDimmers", "number", title: "Turn on in Seconds?", defaultValue: none, required: false
                                        input "sSecondsOtherDimmersOff", "number", title: "Turn off in Seconds?", defaultValue: none, required: false                        
                                	}
                     			}
                		}
                }
				section ("Colored lights", hideWhenEmpty: true){
            		input "sHues", "capability.colorControl", title: "Select These Colored Lights...", multiple: true, required: false, submitOnChange:true
            			if (sHues) {
                        	input "sHuesCmd", "enum", title: "Command To Send ", 
                            				options:["on":"Turn on","off":"Turn off","setColor":"Set Color"], multiple: false, required: false, submitOnChange:true
							if(sHuesCmd == "setColor") {
                            input "sHuesColor", "enum", title: "Hue Color?", required: false, multiple:false, options: fillColorSettings().name
							}
                            if(sHuesCmd == "setColor" || sHuesCmd == "on") {
                            input "sHuesLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]], submitOnChange:true                        
        					}
                        }
                        if (sHuesLevel)	input "sHuesOther", "capability.colorControl", title: "...and these other Colored Lights?", multiple: true, required: false, submitOnChange:true
            			if (sHuesOther) {
                        	input "sHuesOtherCmd", "enum", title: "Command To Send to these other Colored Lights", options:["on":"Turn on","off":"Turn off","setColor":"Set Color"], multiple: false, required: false, submitOnChange:true
							if(sHuesOtherCmd == "setColor") {
                            input "sHuesOtherColor", "enum", title: "Which Color?", required: false, multiple:false, options: fillColorSettings().name
                            }
                            if(sHuesOtherCmd == "on" || sHuesOtherCmd == "setColor") {
							input "sHuesOtherLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]                       
        				}
					}
                }
                section ("Flash These Switches", hideWhenEmpty: true) {
                    input "sFlash", "capability.switch", title: "Flash Switch(es)", multiple: true, required: false, submitOnChange:true
                    if (sFlash) {
                    	input "numFlashes", "number", title: "This number of times (default 3)", required: false, submitOnChange:true
                    	input "onFor", "number", title: "On for (default 1 second)", required: false, submitOnChange:true			
                    	input "offFor", "number", title: "Off for (default 1 second)", required: false, submitOnChange:true
                    }
                }
			}
		}    
    page name: "pGroups"
        def pGroups() {
            dynamicPage(name: "pGroups", title: "",install: false, uninstall: false) {
                section ("Group These Switches", hideWhenEmpty: true){
                        input "gSwitches", "capability.switch", title: "Group Dimmers and Switches...", multiple: true, required: false, submitOnChange: true
                        if (gSwitches) {
                            paragraph "You can now control this group by speaking commands to Alexa:  \n" +
                            " E.G: Alexa, turn on/off the lights in the " + app.label
                        }
                        input "gDisable", "capability.switch", title: "Group Disable Automation Switches (disable = off, enable = on)", multiple: true, required: false, submitOnChange: true
						if (gDisable) {
                            input "reverseDisable", "bool", title: "Reverse Disable Command (disable = on, enable = off)", required: false, defaultValue: false
                            paragraph "You can now use this group by speaking commands to Alexa:  \n" +
                            " E.G: Disable Automation in the " + app.label
                        }
						input "gFans", "capability.switch", title: "Group Ceiling Fans...", multiple: true, required: false, submitOnChange: true
                        if (gFans) {
                            paragraph "You can now control this group by speaking commands to Alexa:  \n" +
                            " E.G: Alexa turn on/off the fan in the " + app.label
                        }
						input "gHues", "capability.colorControl", title: "Group Colored Lights...", multiple: true, required: false, submitOnChange: true
                        if (gHues) {
                            paragraph "You can now control this group by speaking commands to Alexa:  \n" +
                            " E.G: Alexa set the color to red in the " + app.label
                        }
                    	href "gCustom", title: "Create Custom Groups", description: "Tap to set"               
                }       
               section ("Vents and Window Coverings", hideWhenEmpty: true){ 
                    input "gVents", "capability.switchLevel", title: "Group Smart Vent(s)...", multiple: true, required: false, submitOnChange: true
						if (sVent) {
                            paragraph "You can now control this group by speaking commands to Alexa:  \n" +
                            " E.G: Alexa open/close the vents in the " + app.label
                        }
					input "gShades",  "capability.windowShade", title: "Group These Window Covering Devices...", multiple: true, required: false   
                    	if (gShades) {
                            paragraph "You can now control this group by speaking commands to Alexa:  \n" +
                            " E.G: Alexa open/close the blinds/curtains/window coverings in the " + app.label
                        }
                }                
                section ("Media" , hideWhenEmpty: true){
					input "sMedia", "capability.mediaController", title: "Use This Media Controller", multiple: false, required: false, submitOnChange: true
                    	if (sMedia) {
                            paragraph "You can now control this device by speaking commands to Alexa:  \n" +
                            " E.G: Alexa start < Harmony Activity Name > in the " + app.label
                        }
						if (sSpeaker || sSynth) {
                            paragraph "You can now control this device by speaking commands to Alexa:  \n" +
                            " E.G: Alexa mute/unmute < Media Device Name > in the " + app.label
                        }
                    input "sSpeaker", "capability.musicPlayer", title: "Use This Media Player Device For Volume Control", required: false, multiple: false, submitOnChange: true
					input "sSynth", "capability.speechSynthesis", title: "Use This Speech Synthesis Capable Device", multiple: false, required: false, submitOnChange: true
                }             
            }
      	}
//////////////////////////////////////////////////////////////////////////////
/////////// RESTRICTIONS PAGES       
//////////////////////////////////////////////////////////////////////////////
page name: "pRestrict"
    def pRestrict(){
        dynamicPage(name: "pRestrict", title: "", uninstall: false) {
			section ("Mode Restrictions") {
                input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            }        
            section ("Days - Audio only on these days"){	
                input "days", title: "Only on certain days of the week", multiple: true, required: false, submitOnChange: true,
                    "enum", options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
            section ("Time - Audio only during these times"){
                href "certainTime", title: "Only during a certain time", description: timeIntervalLabel ?: "Tap to set", state: timeIntervalLabel ? "complete" : null
            }   
	    }
	}
page name: "certainTime"
    def certainTime() {
        dynamicPage(name:"certainTime",title: "Only during a certain time", uninstall: false) {
            section("Beginning at....") {
                input "startingX", "enum", title: "Starting at...", options: ["A specific time", "Sunrise", "Sunset"], required: false , submitOnChange: true
                if(startingX in [null, "A specific time"]) input "starting", "time", title: "Start time", required: false, submitOnChange: true
                else {
                    if(startingX == "Sunrise") input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    else if(startingX == "Sunset") input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                }
            }
            section("Ending at....") {
                input "endingX", "enum", title: "Ending at...", options: ["A specific time", "Sunrise", "Sunset"], required: false, submitOnChange: true
                if(endingX in [null, "A specific time"]) input "ending", "time", title: "End time", required: false, submitOnChange: true
                else {
                    if(endingX == "Sunrise") input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                    else if(endingX == "Sunset") input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false, submitOnChange: true
                }
            }
        }
    }

/************************************************************************************************************
		Virtual Person Check In/Out Automatically Handler
************************************************************************************************************/    
// Check in VP when profile runs

private pVirToggle() {
	def vp = getChildDevice("${app.label}")
     if(vp) {
     if (vp?.currentValue('presence').contains('not')) {
            vp.arrived()
            }
        else if (vp?.currentValue('presence').contains('present')) {
            vp.departed()
            }
    	}
	}
/************************************************************************************************************
		Smart Home Monitor Status Change when Profile Executes
************************************************************************************************************/    
def shmStateChange() {
	if (shmState == "stay") {
    	sendArmStayCommand()
        }
    if (shmState == "away") {
    	sendArmAwayCommand()
        }
    if (shmState == "off") {
    	sendDisarmCommand()
        }
    }    

def sendArmAwayCommand() {
	if (shmStateKeypads) {
		shmStateKeypads?.each() { it.acknowledgeArmRequest(3) }
		}
		sendSHMEvent("away")
	}
    
def sendDisarmCommand() {
	if (shmStateKeypads) {
		shmStateKeypads?.each() { it.acknowledgeArmRequest(0) }
		}
		sendSHMEvent("off")
	}
    
def sendArmStayCommand() {
	if (shmStateKeypads) {
		shmStateKeypads?.each() { it.acknowledgeArmRequest(1) }
		}
		sendSHMEvent("stay")
	}

private sendSHMEvent(String shmState) {
	def event = [
		name:"alarmSystemStatus",
		value: shmState,
		displayed: true,
		description: "System Status is ${shmState}"
		]
	sendLocationEvent(event)
}
/************************************************************************************************************
		Base Process
************************************************************************************************************/    
def installed() {
	log.debug "Installed with settings: ${settings}, current app version: ${release()}"
    state.ProfileRelease ="Profile: "  + release()
}

def updated() {
	log.debug "Updated with settings: ${settings}, current app version: ${release()}"
    state.ProfileRelease = "Profile: " + release()
	unsubscribe()
	initialize()
}

def initialize() {
		if (sLocksGarage) { subscribe(sLocksGarage, "codeEntered", codeEntryHandler) 
        log.info "garage doors subscribed to"}
        
		if (tempKeypad) { subscribe(tempKeypad, "codeEntered", tempHandler) }
        if (chimeContact) { subscribe (chimeContact, "contact.open", chimeHandler) }
		if (panicKeypad) { subscribe (panicKeypad, "contact.open", panicHandler) }
}
def codeEntryHandler(evt) {
//	log.info "codeEntered = ${evt.value} and the data is ${evt.data}, doorCode1 is ${doorCode1}"
  //do stuff
//  log.debug "Caught code entry event! ${evt.value.value}"
  def codeEntered = evt.value as String
  def data = evt.data as String
  def armMode = ''
  def currentarmMode = sLocksGarage.currentValue("armMode")
  def changedMode = 0  
  	def message = " "
    def stamp = state.lastTime = new Date(now()).format("h:mm aa, dd-MMMM-yyyy", location.timeZone) 
	if ("${codeEntered}" == "${doorCode1}" ||"${codeEntered}" == "${doorCode2}" || "${codeEntered}" == "${doorCode3}") {
    	if ("${data}" == "0") {
        	if (sDoor1 != null) {
            	sDoor1.close() 
                message = "The ${sDoor1} was closed by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
        	if (sDoor2 != null) {
            	sDoor2.close() 
                message = "The ${sDoor2} was closed by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
        	if (sDoor3 != null) {
            	sDoor3.close() 
                message = "The ${sDoor3} was closed by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
            }
    	if ("${data}" == "3") {
        	if (sDoor1 != null) {
            	sDoor1.open() 
                message = "The ${sDoor1} was opened by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
        	if (sDoor2 != null) {
            	sDoor2.open() 
                message = "The ${sDoor2} was opened by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
        	if (sDoor3 != null) {
            	sDoor3.open() 
                message = "The ${sDoor3} was opened by ${app.label} using the ${evt.displayName} at ${stamp}"
                }
            }
            log.info "${message}"
       }     
   						                    
	if ("${codeEntered}" != "${doorCode1}" && "${codeEntered}" != "${doorCode2}" && "${codeEntered}" != "${doorCode3}" ) {
  if (data == '0') {
    armMode = 'off'
  }
  else if (data == '3') {
    armMode = 'away'
  }
  else if (data == '1') {
    armMode = 'stay'
  }
	else {
    log.error "${app.label}: Unexpected arm mode sent by keypad!: "+data
    return []
  }
  def i = settings.maxUsers
//  def message = " "
  while (i > 0) {
    log.debug "i =" + i
    def correctCode = settings."userCode${i}" as String
    if (codeEntered == correctCode) {
      log.debug "User Enabled: " + state."userState${i}".enabled
      if (state."userState${i}".enabled == true) {
        log.debug "Correct PIN entered. Change SHM state to ${armMode}"
       def unlockUserName = settings."userName${i}"
        if (data == "0") {
          runIn(0, "sendDisarmCommand")
          message = "${evt.displayName} was disarmed by ${unlockUserName}"
        }
        else if (data == "1") {
          if(armDelay && keypadstatus) {
            keypad?.each() { it.setExitDelay(armDelay) }
          }
          runIn(armDelay, "sendStayCommand")
          message = "${evt.displayName} was armed to 'Stay' by ${unlockUserName}"
        }
        else if (data == "3") {
          //log.debug "sendArmCommand"
          if(armDelay && keypadstatus) {
            keypad?.each() { it.setExitDelay(armDelay) }
          }
          runIn(armDelay, "sendArmCommand")
          message = "${evt.displayName} was armed to 'Away' by ${unlockUserName}"
        }
        if(settings."burnCode${i}") {
          state."userState${i}".enabled = false
          message += ".  Now burning code."
        }

        log.debug "${message}"
        state."userState${i}".usage = state."userState${i}".usage + 1
        send(message)
        i = 0
      } else if (state."userState${i}".enabled == false){
        log.debug "PIN Disabled"
      }
    }
    changedMode = 1
    i--
  }
  if (changedMode == 1 && i == 0) {
    def errorMsg = "Incorrect Code Entered: ${codeEntered}"
    if (notifyIncorrectPin) {
      log.debug "Incorrect PIN"
      send(errorMsg)
    }
    keypad.sendInvalidKeycodeResponse()
		}
	}
}
/***********************************************************************************************************************
    RESTRICTIONS HANDLER
***********************************************************************************************************************/
private getAllOk() {
	modeOk && daysOk && timeOk
}
private getModeOk() {
    def result = !modes || modes?.contains(location.mode)
	if(parent.debug) log.debug "modeOk = $result"
    result
} 
private getDayOk() {
    def result = true
if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	if(parent.debug) log.debug "daysOk = $result"
	result
}
private getTimeOk() {
	def result = true
	if ((starting && ending) ||
	(starting && endingX in ["Sunrise", "Sunset"]) ||
	(startingX in ["Sunrise", "Sunset"] && ending) ||
	(startingX in ["Sunrise", "Sunset"] && endingX in ["Sunrise", "Sunset"])) {
		def currTime = now()
		def start = null
		def stop = null
		def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if(startingX == "Sunrise") start = s.sunrise.time
		else if(startingX == "Sunset") start = s.sunset.time
		else if(starting) start = timeToday(starting,location.timeZone).time
		s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if(endingX == "Sunrise") stop = s.sunrise.time
		else if(endingX == "Sunset") stop = s.sunset.time
		else if(ending) stop = timeToday(ending,location.timeZone).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	if (parent.debug) log.trace "getTimeOk = $result."
    }
    return result
}
private hhmm(time, fmt = "h:mm a") {
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}
private offset(value) {
	def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}
private timeIntervalLabel() {
	def result = ""
	if      (startingX == "Sunrise" && endingX == "Sunrise") result = "Sunrise" + offset(startSunriseOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunrise" && endingX == "Sunset") result = "Sunrise" + offset(startSunriseOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunset" && endingX == "Sunrise") result = "Sunset" + offset(startSunsetOffset) + " to Sunrise" + offset(endSunriseOffset)
	else if (startingX == "Sunset" && endingX == "Sunset") result = "Sunset" + offset(startSunsetOffset) + " to Sunset" + offset(endSunsetOffset)
	else if (startingX == "Sunrise" && ending) result = "Sunrise" + offset(startSunriseOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (startingX == "Sunset" && ending) result = "Sunset" + offset(startSunsetOffset) + " to " + hhmm(ending, "h:mm a z")
	else if (starting && endingX == "Sunrise") result = hhmm(starting) + " to Sunrise" + offset(endSunriseOffset)
	else if (starting && endingX == "Sunset") result = hhmm(starting) + " to Sunset" + offset(endSunsetOffset)
	else if (starting && ending) result = hhmm(starting) + " to " + hhmm(ending, "h:mm a z")
}
/***********************************************************************************************************************
    SMS HANDLER
***********************************************************************************************************************/
private void sendtxt(message) {
    if (parent.debug) log.debug "Request to send sms received with message: '${message}'"
    if (sendContactText) { 
        sendNotificationToContacts(message, recipients)
            if (parent.debug) log.debug "Sending sms to selected reipients"
    } 
    else {
    	if (push) { 
    		sendPushMessage
            	if (parent.debug) log.debug "Sending push message to selected reipients"
        }
    } 
    if (notify) {
        sendNotificationEvent(message)
             	if (parent.debug) log.debug "Sending notification to mobile app"

    }
    if (sms) {
        sendText(sms, message)
        if (parent.debug) log.debug "Processing message for selected phones"
	}
}
private void sendText(number, message) {
    if (sms) {
        def phones = sms.split("\\,")
        for (phone in phones) {
            sendSms(phone, message)
            if (parent.debug) log.debug "Sending sms to selected phones"
        }
    }
}

/************************************************************************************************************
   Switch/Color/Dimmer/Toggle Handlers
************************************************************************************************************/
// Used for delayed devices
def turnOnSwitch() { sSwitches?.on() }  
def turnOffSwitch() { sSwitches?.off() }
def turnOnOtherSwitch() { sOtherSwitch?.on() }
def turnOffOtherSwitch() { sOtherSwitch?.off() }  
def turnOnDimmers() { def level = dimmersLVL < 0 || !dimmersLVL ?  0 : dimmersLVL >100 ? 100 : dimmersLVL as int
	sDimmers?.setLevel(sDimmersLVL) }
def turnOffDimmers() { sDimmers?.off() }
def turnOnOtherDimmers() { def otherlevel = otherDimmersLVL < 0 || !otherDimmersLVL ?  0 : otherDimmersLVL >100 ? 100 : otherDimmersLVL as int
	sOtherDimmers?.setLevel(sOtherDimmersLVL) }
def turnOffOtherDimmers() { sOtherDimmers?.off() }   

// Primary control of profile triggered lights/switches when delayed
def profileDeviceControl() {
	if (sSecondsOn) { runIn(sSecondsOn,turnOnSwitch)}
    if (sSecondsOff) { runIn(sSecondsOff,turnOffSwitch)}
    if (sOtherSecondsOn)  { runIn(sOtherSecondsOn,turnOnOtherSwitch)}
    if (sOtherSecondsOff) { runIn(sOtherSecondsOff,turnOffOtherSwitch)}
	if (sSecondsDimmers) { runIn(sSecondsDimmers,turnOnDimmers)}
	if (sSecondsDimmersOff) { runIn(sSecondsDimmersOff,turnOffDimmers)}
    if (sSecondsOtherDimmers) { runIn(sSecondsOtherDimmers,turnOnOtherDimmers)}
	if (sSecondsOtherDimmersOff) { runIn(sSecondsOtherDimmersOff,turnOffOtherDimmers)}
// Control of Lights and Switches when not delayed            
    if (!sSecondsOn) {
		if  (sSwitchCmd == "on") { sSwitches?.on() }
			else if (sSwitchCmd == "off") { sSwitches?.off() }
		if (sSwitchCmd == "toggle") { toggle() }
		if (sOtherSwitchCmd == "on") { sOtherSwitch?.on() }
			else if (sOtherSwitchCmd == "off") { sOtherSwitch?.off() }
		if (otherSwitchCmd == "toggle") { toggle() }
		
        if (sDimmersCmd == "set" && sDimmers) { def level = sDimmersLVL < 0 || !sDimmersLVL ?  0 : sDimmersLVL >100 ? 100 : sDimmersLVL as int
			sDimmers?.setLevel(level) }
		if (sOtherDimmersCmd == "set" && sOtherDimmers) { def otherLevel = sOtherDimmersLVL < 0 || !sOtherDimmersLVL ?  0 : sOtherDimmersLVL >100 ? 100 : sOtherDimmersLVL as int
			sOtherDimmers?.setLevel(otherLevel) }
	}
}

private toggle() {
	if (sSwitches) {
        if (sSwitches?.currentValue('switch').contains('on')) {
            sSwitches?.off()
            }
        else if (sSwitches?.currentValue('switch').contains('off')) {
            sSwitches?.on()
            }
    }
    if (sOtherSwitch) {
        if (sOtherSwitch?.currentValue('switch').contains('on')) {
            sOtherSwitch?.off()
        }
        else if (sOtherSwitch?.currentValue('switch').contains('off')) {
            sOtherSwitch?.on()
            }
	}
}
/************************************************************************************************************
   Flashing Lights Handler
************************************************************************************************************/
private flashLights() {
 	if (parent.debug) log.debug "The Flash Switches Option has been activated"
	def doFlash = true
	def onFor = onFor ?: 60000/60
	def offFor = offFor ?: 60000/60
	def numFlashes = numFlashes ?: 3
	
    if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
	}
	if (doFlash) {
		state.lastActivated = now()
		def initialActionOn = sFlash.collect{it.currentflashSwitch != "on"}
		def delay = 0L
		
        numFlashes.times {
			sFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.on(delay: delay)
                }
				else {
					s.off(delay:delay)                   
                } 
			}
			delay += onFor
			sFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
					s.off(delay: delay)
				}
				else {
					s.on(delay:delay)
                }
			}
			delay += offFor
		}
	}
}
//////////////////////////////////////////////////////////////////////////////
/////////// THERMOSTAT HANDLER       
//////////////////////////////////////////////////////////////////////////////
def tempHandler(evt) {
	def codeEntered = evt.value as String
	    if (codeEntered.startsWith("01")) {
        def newSetPoint = codeEntered
        newSetPoint = newSetPoint.replaceAll(/01/,"")
        log.info "Changing the cooling to ${newSetPoint} degrees"
    	tempStat.setCoolingSetpoint("${newSetPoint}")
        }
    	if (codeEntered.startsWith("02")) {
        def newSetPoint = codeEntered
        newSetPoint = newSetPoint.replaceAll(/02/,"")
        log.info "Changing the heat to ${newSetPoint} degrees"
    	tempStat.setHeatingSetpoint("${newSetPoint}")
        }
	}
//////////////////////////////////////////////////////////////////////////////
/////////// PANIC BUTTON HANDLER  
//////////////////////////////////////////////////////////////////////////////   
def panicHandler(evt) {
	log.info "The panic button was pressed on the ${evt.displayName}"
    }
//////////////////////////////////////////////////////////////////////////////
/////////// CONTACT CHIME HANDLER  
//////////////////////////////////////////////////////////////////////////////
def chimeHandler(evt) {
	chimeKeypad.beep()
    }
//// CREATE VIRTUAL PRESENCE
def virtualPerson() {
    log.trace "Creating KeyPad Coordinator Virtual Person Device"
    def deviceId = "${app.label}" 
    def d = getChildDevice("${app.label}")
    if(!d) {
        d = addChildDevice("KeyPad Coordinator", "KeyPad Coordinator Simulated Presence Sensor", deviceId, null, [label:"${app.label}"])
        log.trace "KeyPad Coordinator Virtual Person Device - Created ${app.label} "
    }
    else {
        log.trace "NOTICE!!! Found that the KVPD ${d.displayName} already exists. Only one device per profile permitted"
    }
} 
//// DELETE VIRTUAL PRESENCE
page name: "pPersonDelete"
def pPersonDelete(){
    dynamicPage(name: "pPersonDelete", title: "", uninstall: false) {
        section ("") {
            paragraph "You have deleted a virtual presence sensor device. You will no longer see this device in your " +
                " SmartThings Mobile App.  "
        }
        removeChildDevices(getAllChildDevices())
    }
}
private removeChildDevices(delete) {
    log.debug "The Virtual Person Device '${app.label}' has been deleted from your SmartThings environment"
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}  

/************************************************************************************************************
   Page status and descriptions 
************************************************************************************************************/       
def pSendSettings() {def result = ""
    if (synthDevice || sonosDevice || sendContactText || sendText || push) {
    	result = "complete"}
        result}
def pSendComplete() {def text = "Tap here to Configure" 
    if (synthDevice || sonosDevice || sendContactText || sendText || push) {
    	text = "Configured"}
        else text = "Tap here to Configure"
    	text}
def pConfigSettings() {def result = ""
    if (pAlexaCustResp || pAlexaRepeat || pContCmdsProfile || pRunMsg || pPreMsg || pDisableAlexaProfile || pDisableALLProfile || pRunTextMsg || pPreTextMsg) {
    	result = "complete"}
        result}
def pConfigComplete() {def text = "Tap here to Configure" 
    if (pAlexaCustResp || pAlexaRepeat || pContCmdsProfile || pRunMsg || pPreMsg || pDisableAlexaProfile || pDisableALLProfile || pRunTextMsg || pPreTextMsg) {
    	text = "Configured"}
    	else text = "Tap here to Configure"
		text}
def pDevicesSettings() {def result = ""
    if (sSwitches || sDimmers || sHues || sFlash) {
    	result = "complete"}
    	result}
def pDevicesComplete() {def text = "Tap here to Configure" 
    if (sSwitches || sDimmers || sHues || sFlash) {
    	text = "Configured"}
        else text = "Tap here to Configure"
        text}
def pActionsSettings(){def result = ""
	def pDevicesProc = ""
    if (sSwitches || sDimmers || sHues || sFlash || shmState) {
    	result = "complete"
        pDevicesProc = "complete"}
    	result}
def pActionsComplete() {def text = "Configured" 
	def pDevicesComplete = pDevicesComplete()
    if (pDevicesProc || pMode || pRoutine || shmState) {
    	text = "Configured"}
        else text = "Tap here to Configure"
        text}        
def pRestrictSettings(){ def result = "" 
	if (modes || runDay || hues ||startingX || endingX) {
    	result = "complete"}
        result}
def pRestrictComplete() {def text = "Tap here to configure" 
    if (modes || runDay || hues ||startingX || endingX) {
    	text = "Configured"}
    	else text = "Tap here to Configure"
        text}
def pGroupSettings() {def result = ""
    if (gSwitches || gFans || gHues || sVent || sMedia || sSpeaker) {
    	result = "complete"}
    	result}
def pGroupComplete() {def text = "Tap here to Configure" 
    if (gSwitches || gFans || gHues || sVent || sMedia || sSpeaker) {
    	text = "Configured"}
        else text = "Tap here to Configure"
        text}