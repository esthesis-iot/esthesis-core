/**
 esthesis-normalise-data.js
 NiFi script to normalise incoming telemetry/metadata.

 Incoming records format:
 [
 {
    "m": "test",
    "v": 123
  },
 {
    "m": "test",
    "v": {
      "temperature": 32,
      "humidity": 33
    }
  }
 ]

 Outgoing records format:
 [
 {
    "measurement": "test",
    "timestamp": 1569483969551,
    "fields": {
      "value": 123
    },
    "tags": {
      "hardwareId": "device1",
      "type": "telemetry"
    }
  },
 {
    "measurement": "test",
    "timestamp": 1569483969564,
    "fields": {
      "temperature": 32,
      "humidity": 33
    },
    "tags": {
      "hardwareId": "device1",
      "type": "telemetry"
    }
  }
 ]

 Tags are derived from Flow File attributes resolved in previous steps.
 If incoming record is a single object it will still be wrapped into an array.
 **/

// Get references the the output stream to write results, the input stream to read the content of
// the FlowFile, and to the incoming FlowFile.
var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback");
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var IOUtils = Java.type("org.apache.commons.io.IOUtils");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var flowFile = session.get();

// If no FlowFile exists return, otherwise proceed with processing.
if (flowFile != null) {
  var dataPoints = [];

  // Read incoming FlowFile and convert data to records.
  session.read(flowFile, new InputStreamCallback(function (inputStream) {
    // Parse incoming FlowFile content to JSON.
    var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

    // Make sure we are dealing with an array to avoid checks later on.
    if (!text.startsWith('[')) {
      text = "[" + text + "]";
    }
    var json = JSON.parse(text);

    // Iterate through records.
    for (var i in json) {
      var jsonEntry = json[i];
      var dataPoint = {};

      // Check if time exists and add it if missing.
      if (!jsonEntry.t) {
        jsonEntry.t = Date.now();
      }

      // Check whether a single or multiple values are received and create output.
      dataPoint['measurement'] = jsonEntry.m;
      dataPoint['timestamp'] = jsonEntry.t;
      var fields = {};
      if (typeof jsonEntry.v === 'object') {
        for (var j in jsonEntry.v) {
          fields[j] = jsonEntry.v[j];
        }
      } else {
        fields['value'] = jsonEntry.v;
      }
      dataPoint['fields'] = fields;

      // Add tags.
      dataPoint['tags'] = {
        'hardwareId': flowFile.getAttribute('esthesis.hardwareId'),
        'type': flowFile.getAttribute('esthesis.type')
      };

      // Push record.
      dataPoints.push(dataPoint);
    }
  }));

  // Replace FlowFile's content with the data in InfluxDB line protocol.
  flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
    outputStream.write(JSON.stringify(dataPoints).getBytes(StandardCharsets.UTF_8));
  }));
  session.transfer(flowFile, REL_SUCCESS);
}
