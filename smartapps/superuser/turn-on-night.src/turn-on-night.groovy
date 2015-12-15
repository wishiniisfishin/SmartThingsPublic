/**
 *  Rise and Shine
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */

 preferences {
	 section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	 section("after this time of day") {
		 input "timeOfDay", "time", title: "Time?"
	 }
	 section("Change to this mode") {
		 input "newMode", "mode", title: "Mode?"
	 }
	 section("and turn off this light") {
		 input "switch2", "capability.switch"
	 }
     section("and shut this garage door") {
		 input "switch1", "capability.switch", title: "Garage Switch?"
         input "contact1", "capability.contactSensor", title: "Garage Sensor?"
	 }
     
}

def installed() {
    subscribe(people, "presence", presence)
    schedule(time, nighttime)
}

def updated() {
	unsubscribe()
	subscribe(people, "presence", presence)
    unschedule()
    schedule(time, nighttime)
}

def presence(evt)
{
	def current = "$evt.value"
    def currentgarage = settings.contact1.latestValue("status")
    def startTime = timeToday(timeOfDay)
	if (current == "present" && now() > startTime.time) {
    	runIn(300, "takeactions")
		
	}
	
}
def nighttime()
{
	takeactions()
}

private takeactions(){
 def currentgarage = settings.contact1.latestValue("status")
	if (everyoneIsHome()) {
        	if(currentgarage == "open"){
				settings.switch1.on()
	        	settings.switch1.off()
        	    //sendPush("Garage Closed")
        	}
            settings.switch2.off()
            def message = "Goodnight! SmartThings changed the mode to '$newMode'"
			sendPush(message)
			setLocationMode(newMode)
    }
}

private everyoneIsHome()
{
	def result = true
	for (person in people) {
		if (person.currentPresence == "not present") {
			result = false
			break
		}
	}
	return result
}
