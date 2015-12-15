/**
 *  Garage Door SmartApp
 *
 *  Author: Jerod
 */


// Automatically generated. Make future change here.
definition(
    name: "Garage Door Presense",
    namespace: "",
    author: "jerod.mills@gmail.com",
    description: "Detects Presence of multiple people and closes the garage door when you leave(if open) and opens it when you come home(if closed).",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When I arrive and leave..."){
    	input "people", "capability.presenceSensor", multiple: true
	}
	section("Garage Door switch..."){
		input "switch1", "capability.switch", title: "Which?"
	}
    section("Garage Door Sensor...") {
		input "contact1", "capability.contactSensor", title: "Which?"
	}
    section("Turn on Light..."){
		input "switch2", "capability.switch", multiple: true, title: "Which?"
	}
}

def installed()
{
    subscribe(people, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(people, "presence", presenceHandler)
}


def presenceHandler(evt)
{
	
	def current = "$evt.value"
    def currentgarage = settings.contact1.latestValue("status")
    
	if (current == "present") {
  		if(currentgarage == "closed"){
			settings.switch1.on()
        	settings.switch1.off()
            settings.switch2.on()
            sendPush("Garage opened")
        }
	}
	else if (current == "not present") {
    	if(currentgarage == "open"){
			settings.switch1.on()
        	settings.switch1.off()
            settings.switch2.off()
            sendPush("Garage closed")
        }
	}
}
