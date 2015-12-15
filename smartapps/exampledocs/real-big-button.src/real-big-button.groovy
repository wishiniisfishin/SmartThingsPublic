/**
 *  Real Big Button
 *
 *  Copyright 2015 Brad Marsh
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
 */
definition(
    name: "Real Big Button",
    namespace: "exampledocs",
    author: "Brad Marsh",
    description: "HTML Resource Module that uses a single card and handles incoming events as well as ST.requests",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
	section("Title") {
		// TODO: put inputs here
        input "button", "capability.switch", title: "Select a switch", required: true
	}
}

cards {
	card(name: "home", type: "html", action: "home", whitelist: []) {}   
}

mappings {
	path("/home") {
		action: [
			GET: "home"
		]
	}
    path("/getInitialData") {
        action:[
            GET: "getInitialData"
        ]
    }
    path("/toggleSwitch") {
        action:[
            GET: "toggleSwitch"
        ]
    }
    path("/consoleLog") {
    	action:[
            GET: "consoleLog"
        ]
    }	
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	createAccessToken()
 
	initialize()
}
 
def uninstalled() {
	revokeAccessToken()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(button, "switch", buttonEventHandler)
    
    updateSolutionSummary()
}


def updateSolutionSummary(){
	sendEvent([
            name           : "summary",
            linkText       : app.label,
            descriptionText: "Summary updated",
            eventType      : "SOLUTION_SUMMARY",
            data           : [:],
            displayed      : false,
            isStateChange  : true,
            value          : 'test',
        ])
}

def getInitialData() {
	[	
    	name: "initialData",
        currentState: button.currentValue("switch"),
        secTest: app.name
	]
}

def toggleSwitch() {
	if(button.currentValue("switch") == "on"){
    	button.off()
    }else{
    	button.on()
    }
    
    
    // Native App is expecting a JSON object to be returned from a call to an action
    return [status:"OK"]
}

def consoleLog(){
	log.debug "console log: ${params.str}"
}

def buttonEventHandler(evt) {
	sendEvent(name: "buttonState", value: button.currentValue("switch"))
}

def home() {
 
	log.debug "current mode: ${location.mode}"
 
	renderHTML("test", true) { 
		head {
		"""
        	<script src="${buildResourceUrl('javascript/jquery.min.js')}"></script>
            <link rel="stylesheet" href="${buildResourceUrl('css/app.css')}?v=3" type="text/css" media="screen" />
            <script src="https://ajax.googleapis.com/ajax/libs/threejs/r69/three.min.js"></script>
			<script>
    		function eventReceived(evt) {
      		    APP.eventReceiver(evt);
    		}
		  	</script>
		"""
		}
		body {
		""" 
        	<div id="square">
                <div id="button">

                </div>
            </div>
            
            <script src="${buildResourceUrl('javascript/app.js')}?v=1"></script>
        """
        }
	}
}
// TODO: implement event handlers