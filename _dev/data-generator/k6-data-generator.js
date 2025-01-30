import http from "k6/http";
import {check} from "k6";
import {FormData} from 'https://jslib.k6.io/formdata/0.0.2/index.js';

export const options = {
	discardResponseBodies: true
};

export const load = {
	tags: true,
	users: true,
	applications: true,
	cas: true,
	certificates: true,
	keystores: true,
	campaigns: true,
	provisioning: true,
	devices: true,
	commands: true
}

export const serviceUrl = {
	tagSrv: __ENV.K6_ESTHESIS_CORE_TAG_SRV || "http://localhost:59020",
	securitySrv: __ENV.K6_ESTHESIS_CORE_SECURITY_SRV || "http://localhost:59170",
	applicationSrv: __ENV.K6_ESTHESIS_CORE_APPLICATION_SRV || "http://localhost:59090",
	cryptoSrv: __ENV.K6_ESTHESIS_CORE_CRYPTO_SRV || "http://localhost:59040",
	campaignSrv: __ENV.K6_ESTHESIS_CORE_CAMPAIGN_SRV || "http://localhost:59150",
	provisioningSrv: __ENV.K6_ESTHESIS_CORE_PROVISIONING_SRV || "http://localhost:59100",
	deviceSrv: __ENV.K6_ESTHESIS_CORE_DEVICE_SRV || "http://localhost:59010",
	commandSrv: __ENV.K6_ESTHESIS_CORE_COMMAND_SRV || "http://localhost:59080"
}

export const auth = {
	keycloakUrl: __ENV.K6_ESTHESIS_CORE_KEYCLOAK_URL || "http://keycloak.esthesis-dev/realms/esthesis/protocol/openid-connect/token",
	clientId: __ENV.K6_ESTHESIS_CORE_CLIENT_ID || "esthesis",
	username: __ENV.K6_ESTHESIS_CORE_USERNAME || "esthesis-admin",
	password: __ENV.K6_ESTHESIS_CORE_PASSWORD || "esthesis-admin"
}

// Function to encode parameters manually
function encodeParams(params) {
	return Object.keys(params)
	.map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
	.join("&");
}

function rnd(stem, suffix) {
	return stem + "-" + Date.now() + "-" + __VU + (suffix ? "-"
		+ suffix : "");
}

export function setup() {
	// Get access token.
	const params = {
		headers: {
			"Content-Type": "application/x-www-form-urlencoded"
		},
		responseType: "text"
	};
	const payload = encodeParams({
		grant_type: "password",
		client_id: auth.clientId,
		username: auth.username,
		password: auth.password,
	});
	const response = http.post(auth.keycloakUrl, payload, params);
	const token = JSON.parse(response.body).access_token;
	check(response, {
		"Token status is OK": (r) => r.status === 200,
		"Received access token": () => token !== undefined,
	});
	if (token === undefined) {
		console.error("Failed to get access token.");
		return;
	}

	// Create a device (for campaigns and commands).
	let deviceId, deviceHardwareId;
	if (load.campaigns || load.commands) {
		const requestBody = JSON.stringify({
			"hardwareId": rnd("lt-campaign-device"),
			"type": "CORE"
		});
		const requestReply = http.post(`${serviceUrl.deviceSrv}/api/v1/preregister`,
			requestBody, {
				headers: {
					"Authorization": `Bearer ${token}`,
					"Content-Type": "application/json"
				}, responseType: "text"
			});
		check(requestReply, {
			"Create init device": (r) => r.status === 200,
		});
		deviceId = JSON.parse(requestReply.body)[0].id;
		deviceHardwareId = JSON.parse(requestReply.body)[0].hardwareId;
	}

	// Create CA/Certificate (for certificates and keystores).
	let parentCaName, parentCaId, certificateId;
	if (load.certificates || load.keystores) {
		const requestBody = JSON.stringify({
			"cn": rnd("lt-parent-ca"),
			"name": rnd("lt-parent-ca"),
			"validity": "2039-12-30T22:00:00.000Z"
		});
		const requestReply = http.post(`${serviceUrl.cryptoSrv}/api/ca/v1`,
			requestBody, {
				headers: {
					"Authorization": `Bearer ${token}`,
					"Content-Type": "application/json"
				}, responseType: "text"
			});
		check(requestReply, {
			"Create init parent CA": (r) => r.status === 200,
		});
		parentCaName = JSON.parse(requestReply.body).name;
		parentCaId = JSON.parse(requestReply.body).id;

		if (load.keystores) {
			// Generate certificate.
			const requestBody = JSON.stringify({
				"cn": rnd("lt-ks-cert-cn"),
				"name": rnd("lt-ks-cert-name"),
				"san": rnd("lt-ks-cert-san"),
				"validity": "2039-12-30T22:00:00.000Z",
				"issuer": parentCaName
			});
			const requestReply = http.post(
				`${serviceUrl.cryptoSrv}/api/certificate/v1`,
				requestBody, {
					headers: {
						"Authorization": `Bearer ${token}`,
						"Content-Type": "application/json"
					}, responseType: "text"
				});
			check(requestReply, {
				"Create certificate for keystore": (r) => r.status === 200,
			});
			certificateId = JSON.parse(requestReply.body).id;
		}
	}

	return {
		headers: {
			"Authorization": `Bearer ${token}`,
			"Content-Type": "application/json"
		},
		parentCa: {
			"id": parentCaId,
			"name": parentCaName
		},
		keystoreCertificate: {
			"id": certificateId
		},
		device: {
			"id": deviceId,
			"hardwareId": deviceHardwareId
		}
	}
}

export default function (init) {

	let requestBody;
	let requestReply;

	// Generate tags.
	if (load.tags) {
		requestBody = JSON.stringify({
			"name": rnd("lt-title"),
			"description": rnd("lt-desc")
		});
		requestReply = http.post(`${serviceUrl.tagSrv}/api/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create tag": (r) => r.status === 200,
		});
	}

	// Generate users.
	if (load.users) {
		requestBody = JSON.stringify({
			"username": rnd("lt-username"),
			"firstName": rnd("lt-name"),
			"lastName": rnd("lt-surname"),
			"email": rnd("lt-username", "@esthesis.com"),
			"description": rnd("lt-desc"),
			"groups": ["64180cded7edc1e1200904a0"],
			"policies": ["ern:esthesis:core:about:*:create:allow"]
		});
		requestReply = http.post(`${serviceUrl.securitySrv}/api/v1/users`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create user": (r) => r.status === 200,
		});
	}

	// Generate applications.
	if (load.applications) {
		requestBody = JSON.stringify({
			"name": rnd("lt-app"),
			"token": rnd("lt-token"),
			"state": true
		});
		requestReply = http.post(`${serviceUrl.applicationSrv}/api/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create application": (r) => r.status === 200,
		});
	}

	// Generate CAs.
	if (load.cas) {
		requestBody = JSON.stringify({
			"cn": rnd("lt-ca-cn"),
			"name": rnd("lt-ca-name"),
			"validity": "2039-12-30T22:00:00.000Z"
		});
		requestReply = http.post(`${serviceUrl.cryptoSrv}/api/ca/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create CA": (r) => r.status === 200,
		});
	}

	// Generate certificates.
	if (load.certificates) {
		requestBody = JSON.stringify({
			"cn": rnd("lt-cert-cn"),
			"name": rnd("lt-cert-name"),
			"san": rnd("lt-cert-san"),
			"validity": "2039-12-30T22:00:00.000Z",
			"issuer": init.parentCa.name
		});
		requestReply = http.post(`${serviceUrl.cryptoSrv}/api/certificate/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create certificate": (r) => r.status === 200,
		});
	}

	// Generate keystores.
	if (load.keystores) {
		requestBody = JSON.stringify({
			"name": rnd("lt-ks-name"),
			"description": rnd("lt-ks-desc"),
			"password": rnd("lt-ks-pass"),
			"entries": [{
				"id": init.keystoreCertificate.id,
				"resourceType": "CERT",
				"keyType": ["CERT", "PRIVATE"],
				"name": "esthesis platform",
				"password": rnd("lt-ks-epass"),
			}, {
				"id": init.parentCa.id,
				"resourceType": "CA",
				"keyType": ["CERT", "PRIVATE"],
				"name": "esthesis CA",
				"password": rnd("lt-ks-epass"),
			}],
			"type": "JKS/SUN",
			"version": "01"
		});
		requestReply = http.post(`${serviceUrl.cryptoSrv}/api/keystore/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create keystore": (r) => r.status === 200,
		});
	}

	// Campaigns.
	if (load.campaigns) {
		requestBody = JSON.stringify({
			"name": rnd("lt-cmp-name"),
			"description": rnd("lt-cmp-desc"),
			"type": "PING",
			"conditions": [],
			"members": [{
				"id": init.device.id,
				"type": "DEVICE",
				"identifier": "dash-dev-1",
				"group": 0
			}],
			"searchByHardwareId": "",
			"searchByTags": "",
			"advancedDateTimeRecheckTimer": "PT1S",
			"advancedPropertyRecheckTimer": "PT1S",
			"advancedUpdateRepliesTimer": "PT1S",
			"advancedUpdateRepliesFinalTimer": "PT1S"
		});
		requestReply = http.post(`${serviceUrl.campaignSrv}/api/v1`,
			requestBody, {headers: init.headers});
		check(requestReply, {
			"Create campaign": (r) => r.status === 204,
		});
	}

	// Provisioning.
	if (load.provisioning) {
		const fd = new FormData();
		fd.append("dto", JSON.stringify({
			"name": rnd("lt-prov-name"),
			"description": rnd("lt-prov-desc"),
			"version": "1.0.0",
			"type": "EXTERNAL",
			"available": true,
			"sha256": rnd("lt-prov-sha"),
			"url": rnd("https://lt-prov-name", ".com")
		}));

		requestReply = http.post(`${serviceUrl.provisioningSrv}/api/v1`,
			fd.body(), {
				headers: {
					"Authorization": `${init.headers.Authorization}`,
					"Content-Type": `multipart/form-data; boundary=${fd.boundary}`
				}
			});
		check(requestReply, {
			"Create provisioning": (r) => r.status === 200,
		});
	}

	// Devices.
	if (load.devices) {
		requestBody = JSON.stringify({
			"hardwareId": rnd("lt-hid"),
			"type": "CORE"
		});
		requestReply = http.post(`${serviceUrl.deviceSrv}/api/v1/preregister`,
			requestBody, {headers: init.headers, responseType: "text"});
		check(requestReply, {
			"Create device": (r) => r.status === 200,
		});
	}

	// Commands.
	if (load.commands) {
		requestBody = JSON.stringify({
			"hardwareIds": init.device.hardwareId,
			"commandType": "p",
			"executionType": "a",
			"description": rnd("lt-cmd-desc")
		});
		requestReply = http.post(`${serviceUrl.commandSrv}/api/v1`,
			requestBody, {headers: init.headers, responseType: "text"});
		check(requestReply, {
			"Create command": (r) => r.status === 200,
		});
	}
}
