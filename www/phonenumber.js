/* global cordova:false */

// TODO Not all needed.
var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    cordova = require('cordova');

(function() {
	'use strict';

	var cordovaPhonenumber = {};

	// cordovaPhonenumber.Headers = Headers;
	// cordovaPhonenumber.Request = Request;
	// cordovaPhonenumber.Response = Response;

	cordovaPhonenumber.number = function(listener, options) {
		console.log("Executing 'number' request.");

		return new Promise(function(resolve, reject) {

			exec(function(response) {

				// TODO Clean up (all old copy/pastes)
				if (response.status === "Some error") {
					reject(new TypeError('Native Imprivata request reported error'))
					return
				}
				listener(response);
				resolve(response)
			
			}, function(response) {
				console.log(response)
        		reject(new TypeError('Native Imprivata request failed'))
			}, 
			"CordovaPhonenumber",
			"number",
			[]);
		})
	}

	cordovaPhonenumber.number.polyfill = true;

	module.exports = cordovaPhonenumber;

})();