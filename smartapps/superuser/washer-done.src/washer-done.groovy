/**
 *  Washer Done
 *
 *  Author: Jerod
 *  Date: 2013-07-20
 */
preferences {
	section("Choose one multi-sensor, when..."){
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: true
	}
	section("This time period delay(in MS)"){
		input "delayTime", "number", title: "Delay Time"
	}
	section("Then send this message in a text notification"){
		input "messageText", "text", title: "Message Text"
	}
	section("And as text message to this number (optional)"){
		input "phone", "phone", title: "Phone Number", required: false
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(acceleration, "acceleration.inactive", checkMotionInactive)
	subscribe(acceleration, "acceleration.active", checkMotionActive)
}

def checkMotionInactive(evt) {
	schedule(util.cronExpression(now() + delayTime), "sendMessage")
}

def checkMotionActive(evt) {
	unschedule()
}

def sendMessage() {
	sendPush(messageText)
	if (phone) {
		sendSms(phone, messageText)
	}
}
