/**
 *	Tesla Service Manager 2.0
 *
 *	Author: nkraus@thinkpyxl.com
 *	Date: 2015-12-14
 */

definition(
    name: "Tesla 2.0 (Connect)",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Integrate your Tesla car with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%402x.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/tesla-app%403x.png"
){
	appSetting "clientId"
	appSetting "clientSecret"
	appSetting "APIURL"
}

preferences {
	page(name: "loginToTesla", title: "Tesla")
    page(name: "selectCars", title: "Tesla")
}

def loginToTesla() {
	def showUninstall = state.TeslaAccessToken != null
	return dynamicPage(name: "loginToTesla", title: "Connect your Tesla", nextPage:"selectCars", uninstall:showUninstall) {
		if (state.TeslaAccessToken != null) {
        	section('Access granted. Please click "NEXT" to continue.') {}
        } else {
            section("Log in to your Tesla account:") {
                input "username", "text", title: "Username", required: true, autoCorrect:false
                input "password", "password", title: "Password", required: true, autoCorrect:false
            }
            section("To use Tesla, SmartThings encrypts and securely stores your Tesla credentials.") {}
        }
	}
}

def selectCars() {
	def loginResult = forceLogin()

	if(state.TeslaAccessToken) {
		def options = carsDiscovered() ?: []

		return dynamicPage(name: "selectCars", title: "Select Tesla to connect", install:true, uninstall:true) {
			section(){
				input(name: "selectedCars", type: "enum", required:true, multiple:true, options:options)
			}
		}
	}
	else
	{
		log.error "login result false"
        return dynamicPage(name: "selectCars", title: "Tesla", install:false, uninstall:true, nextPage:"") {
			section("") {
				paragraph "Please check your username and password"
			}
		}
	}
}


def installed() {
	log.debug "Installed"
	initialize()
}

def updated() {
	log.debug "Updated"

	unsubscribe()
	initialize()
}

def initialize() {

	if (selectCars) {
		addDevice()
	}

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !selectedCars }
	log.info delete
    //removeChildDevices(delete)
}

//CHILD DEVICE METHODS
def addDevice() {
    def devices = getcarList()
    log.trace "Adding childs $devices - $selectedCars"
	selectedCars.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newCar = devices.find { (it.dni) == dni }
			d = addChildDevice("smartthings", "Tesla", dni, null, [name:"Tesla", label:"Tesla"])
			log.trace "created ${d.name} with id $dni"
            poll(dni)
		} else {
			log.trace "found ${d.name} with id $key already exists"
		}
	}
}

def getcarList() {
	def devices = []

	def carListParams = [
		uri: "${appSettings.APIURL}/vehicles",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
   	]
    log.trace carListParams
	try {
        httpGet(carListParams) { resp ->
            log.debug "Getting car list $resp.status, $resp.data.response"
            if(resp.status == 200) {
                def vehicleName = resp.data.response.display_name[0].toString()
                def vehicleId = resp.data.response.id[0]
                def dni = "tesla:${vehicleId}"
                def name = "Tesla [${vehicleName}]"
                // CHECK HERE IF MOBILE IS ENABLE
                // path: "/vehicles/${vehicleId}/mobile_enabled",
                // if (enable)
                devices += ["name" : "${name}", "dni" : "${dni}"]
                // else return [errorMessage:"Mobile communication isn't enable on all of your vehicles."]
            } else if(resp.status == 302) {
                // Token expired or incorrect
                singleUrl = resp.headers.Location.value
            } else {
                // ERROR
                log.error "car list: unknown response"
            }
        }
    } catch (all) {
        log.trace all
    }
    return devices
}

Map carsDiscovered() {
	def devices =  getcarList()
    log.trace "Map $devices"
	def map = [:]
	if (devices instanceof java.util.Map) {
		devices.each {
			def value = "${it?.name}"
			def key = it?.dni
			map["${key}"] = value
		}
	} else { //backwards compatable
		devices.each {
			def value = "${it?.name}"
			def key = it?.dni
			map["${key}"] = value
		}
	}
	map
}

def removeChildFromSettings(child) {
	def device = child.device
	def dni = device.deviceNetworkId
	log.debug "removing child device $device with dni ${dni}"
	if(!state?.suppressDelete?.get(dni))
	{
		def newSettings = settings.cars?.findAll { it != dni } ?: []
		app.updateSetting("cars", newSettings)
	}
}

private forceLogin() {
	state.TeslaAccessToken = null
	login()
}


private login() {
	if(state.TeslaAccessToken != null) {
		return [success:true]
	}
	return doLogin()
}

private doLogin() {
	def Params = [ grant_type: "password", client_id: appSettings.clientId, client_secret: appSettings.clientSecret, email: username, password: password ]
	def loginParams = [
		uri: "https://owner-api.teslamotors.com/oauth/token",
		contentType: "application/json",
        body: Params
	]

	def result = [success:false]

    try {
    	httpPost(loginParams) { resp ->
            if (resp.status == 200) {
                state.TeslaAccessToken = resp.data.access_token
                if (state.TeslaAccessToken) {
                    log.debug "Access Token Granted"
                    result.success = true
                } else {
                    // ERROR: any more information we can give?
                    result.reason = "Bad login"
                }
            } else {
                // ERROR: any more information we can give?
                result.reason = "Bad login"
            }
    	}
	} catch (groovyx.net.http.HttpResponseException e) {
			result.reason = "Bad login"
	}
	return result
}

private command(String dni, String command, String value = '') {
	def id = getVehicleId(dni)
    def commandPath

	switch (command) {
		case "flash":
    		commandPath = "/vehicles/${id}/command/flash_lights"
            break;
		case "honk":
    		commandPath = "/vehicles/${id}/command/honk_horn"
            break;
		case "doorlock":
    		commandPath = "/vehicles/${id}/command/door_lock"
            break;
		case "doorunlock":
    		commandPath = "/vehicles/${id}/command/door_unlock"
            break;
		case "climaon":
    		commandPath = "/vehicles/${id}/command/auto_conditioning_start"
            break;
		case "climaoff":
    		commandPath = "/vehicles/${id}/command/auto_conditioning_stop"
            break;
		case "roof":
            def percent
            switch (value) {
                case "open":
                   percent=100
                   break;
                case "close":
                   percent=0
                   break;
                case "vent":
                   percent=15
                   break;
                case "comfort":
                   percent=80
                   break;
                default:
                   percent=0
            }
    		commandPath = "/vehicles/${id}/command/sun_roof_control?state=${value}&percent=${percent}"
            break;
		case "temp":
    		commandPath = "/vehicles/${id}/command/set_temps?driver_temp=${value}&passenger_temp=${value}"
            break;
        case "remotestart":
    		commandPath = "/vehicles/${id}/command/remote_start_drive?password=${settings.password}"
            break;  
        case "openchargeport":
    		commandPath = "/vehicles/${id}/command/charge_port_door_open"
            break;      
		default:
			break;
    }
    
	debugEvent (commandPath, true)
    
	def commandParams = [
		uri: appSettings.APIURL+commandPath,
        contentType: "application/json",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
	]

	def loginRequired = false

	try {
        httpPost(commandParams) { resp ->
            if(resp.status == 403) {
                loginRequired = true
            } else if (resp.status == 200) {
                def data = resp.data.response
                if (data.result) 
                	debugEvent("Command '${command}' sent successfully.", false)
                else
                	debugEvent("Command failed: $data.reason", true)
            } else {
                debugEvent("unknown response: ${resp.status} - ${resp.headers.'Content-Type'}", true)
            }
        }
    } catch (all) {
        log.trace all
    }
	if(loginRequired) { throw new Exception("Login Required") }
}

private poll(String dni) {

	poll_climate_state(dni)
    poll_vehicle_state(dni)
    poll_charge_state(dni)
    
}

private poll_climate_state(String dni) {
	def id = getVehicleId(dni)
	def childDevice = getChildDevice(dni)
    
	def pollParams1 = [
		uri: "${appSettings.APIURL}/vehicles/${id}/data_request/climate_state",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
	]

	def loginRequired = false

    try{
        httpGet(pollParams1) { resp ->
            if(resp.status == 403) {
                loginRequired = true
            } else if (resp.status == 200) {
                def data = resp.data.response
                childDevice?.sendEvent(name: 'temperature', value: cToF(data.driver_temp_setting).toString())
                if (data.is_auto_conditioning_on)
                    childDevice?.sendEvent(name: 'clima', value: 'on')
                else
                    childDevice?.sendEvent(name: 'clima', value: 'off')
                childDevice?.sendEvent(name: 'thermostatSetpoint', value: cToF(data.driver_temp_setting).toString())
            } else {
                log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
            }
        }
    } catch (all) {
        log.trace all
    }

	if(loginRequired) {
		throw new Exception("Login Required")
	}
}

private poll_vehicle_state(String dni) {
	def id = getVehicleId(dni)
	def childDevice = getChildDevice(dni)

	def pollParams2 = [
		uri: appSettings.APIURL,
		path: "/vehicles/${id}/data_request/vehicle_state",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
	]

	try {
        httpGet(pollParams2) { resp ->
            if(resp.status == 403) {
                loginRequired = true
            } else if (resp.status == 200) {
                def data = resp.data.response
                if (data.sun_roof_percent_open == 0)
                    childDevice?.sendEvent(name: 'roof', value: 'close')
                else if (data.sun_roof_percent_open > 0 && data.sun_roof_percent_open < 70)
                    childDevice?.sendEvent(name: 'roof', value: 'vent')
                else if (data.sun_roof_percent_open >= 70 && data.sun_roof_percent_open <= 80)
                    childDevice?.sendEvent(name: 'roof', value: 'comfort')
                else if (data.sun_roof_percent_open > 80 && data.sun_roof_percent_open <= 100)
                    childDevice?.sendEvent(name: 'roof', value: 'open')
                if (data.locked)
                    childDevice?.sendEvent(name: 'door', value: 'lock')
                else
                    childDevice?.sendEvent(name: 'door', value: 'unlock')
            } else {
                log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
            }
        }
	} catch (all) {
        log.trace all
    }

	if(loginRequired) {
		throw new Exception("Login Required")
	}
}

private poll_charge_state(String dni) {
	def id = getVehicleId(dni)
	def childDevice = getChildDevice(dni)
    
	def pollParams1 = [
		uri: "${appSettings.APIURL}/vehicles/${id}/data_request/climate_state",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
	]

	def loginRequired = false

	def pollParams3 = [
		uri: "${appSettings.APIURL}/vehicles/${id}/data_request/charge_state",
		headers: [Authorization: "Bearer ${state.TeslaAccessToken}"]
	]

	try {
        httpGet(pollParams3) { resp ->
            if(resp.status == 403) {
                loginRequired = true
            } else if (resp.status == 200) {
                def data = resp.data.response
                childDevice?.sendEvent(name: 'connected', value: data.charging_state.toString())
                childDevice?.sendEvent(name: 'miles', value: data.battery_range.toString())
                childDevice?.sendEvent(name: 'battery', value: data.battery_level.toString())
            } else {
                log.error "unknown response: ${resp.status} - ${resp.headers.'Content-Type'}"
            }
        }
    } catch (all) {
        log.trace all
    }

	if(loginRequired) {
		throw new Exception("Login Required")
	}
}

private getVehicleId(String dni) {
    return dni.split(":").last()
}

private Boolean getCookieValueIsValid()
{
	// TODO: make a call with the cookie to verify that it works
	return getCookieValue()
}

private updateCookie(String cookie) {
	state.cookie = cookie
}

private getCookieValue() {
	state.cookie
}

def cToF(temp) {
    return temp * 1.8 + 32
}

private validUserAgent() {
	"curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8x zlib/1.2.5"
}

def debugEvent(message, displayEvent) {

	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)

}