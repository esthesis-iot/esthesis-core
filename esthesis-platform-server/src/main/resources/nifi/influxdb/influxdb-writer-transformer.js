/*
 * NiFi ExecuteScript to convert incoming esthesis telemetry/metadata to InfluxDB line protocol.
 *
 * version: 1.0.0
 */

// Get references the the output stream to write results, the input stream to read the content of
// the FlowFile, and to the incoming FlowFile.
var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback")
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var IOUtils = Java.type("org.apache.commons.io.IOUtils")
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets")
var flowFile = session.get();

// If no FlowFile exists transition to the failure relationship, otherwise proceed with processing.
if (flowFile != null) {
  var output = "";

  // Read incoming FlowFile and convert data to InfluxDB line protocol.
  session.read(flowFile, new InputStreamCallback(function (inputStream) {
    // Parse incoming FlowFile content to JSON.
    var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    var json = JSON.parse(text);

    // Check if time exists and add it if missing.
    if (!json.t) {
      json.t = Date.now() * 1000000;
    } else {
      json.t = json.t * 1000000;
    }

    // Check whether a single or multiple values are received and create output in InfluxDB line protocol format.
    if (typeof json.v === 'object') {
      var keys = Object.keys(json.v).length;
      var i = 0;
      output = json.m + "," + "deviceId=" + flowFile.readAttribute('esthesis.deviceId')
        + ",type=" + flowFile.readAttribute('esthesis.dataType') + " ";
      for (key in json.v) {
        output += key + "=" + json.v[key];
        i++;
        if (i < keys) {
          output += ",";
        }
      }
      output += " " + json.t + "\n";
    } else {
      output = json.m + "," + "deviceId=" + flowFile.readAttribute('esthesis.deviceId')
        + ",type=" + flowFile.readAttribute('esthesis.dataType') + " " + "value=" + json.v + " " + json.t;
    }
  }));

  // Replace FlowFile's content with the data in InfluxDB line protocol.
  flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
    outputStream.write(output.getBytes(StandardCharsets.UTF_8))
  }));

  session.transfer(flowFile, REL_SUCCESS);
} else {
  session.transfer(flowFile, REL_FAILURE);
}
