/**
 *  Smoke Detector
 *
 *  Author: jerod.mills@gmail.com
 *  Date: 2013-10-14
 */


// Automatically generated. Make future change here.
definition(
    name: "Smoke Detector",
    namespace: "",
    author: "jerod.mills@gmail.com",
    description: "Smoke Detector",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences {
	section("Smoke Detector..."){
		input "smoke", "capability.smokeDetector", title: "Which?"
	}
    section("Turn on Light..."){
		input "switch1", "capability.switch", multiple: true, title: "Which?"
	}
}

def installed()
{
    subscribe(smoke, "smoke.detected", presenceHandler)
	subscribe(smoke, "smoke.tested", presenceHandler)
	subscribe(smoke, "carbonMonoxide.detected", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(smoke, "smoke.detected", presenceHandler)
	subscribe(smoke, "smoke.tested", presenceHandler)
	subscribe(smoke, "carbonMonoxide.detected", presenceHandler)
}


def presenceHandler(evt)
{
			settings.switch1.on()
            sendPush("Smoke Detected")

}
