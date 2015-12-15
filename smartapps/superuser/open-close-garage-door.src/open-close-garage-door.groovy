/**
 *  Open/Close Garage Door
 *
 *  Author: SmartThings
 */

// Automatically generated. Make future change here.
definition(
    name: "Open/Close Garage Door",
    namespace: "",
    author: "jerod.mills@gmail.com",
    description: "Fires an On/Off for a switch to open or close a garage door attached to 110v relay.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When I touch the app, turn off...") {
		input "switches", "capability.switch"
	}
}

def installed()
{
	subscribe(app, appTouch)
    
}

def updated()
{
	unsubscribe()
	subscribe(app, appTouch)
}


def appTouch(evt) {
	log.debug "appTouch: $evt"
    switches.on()
	switches.off()
}
