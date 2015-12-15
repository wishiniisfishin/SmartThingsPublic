/**
 *  Smoke Detector
 *
 *  Author: jerod.mills@gmail.com
 *  Date: 2013-10-14
 */

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
