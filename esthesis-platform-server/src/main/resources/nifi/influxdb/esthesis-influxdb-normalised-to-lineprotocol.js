/*
 * esthesis-influxdb-normalised-to-lineprotocol.js
 * NiFi ExecuteScript to convert normalised (via esthesis-normalise-data.js) esthesis
 * telemetry/metadata to InfluxDB line protocol.
 */

// Get references the the output stream to write results, the input stream to read the content of
// the FlowFile, and to the incoming FlowFile.
var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback");
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var IOUtils = Java.type("org.apache.commons.io.IOUtils");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var flowFile = session.get();

// If no FlowFile exists transition to the failure relationship, otherwise proceed with processing.
if (flowFile != null) {
  var output = "";

  // Read incoming FlowFile and convert data to InfluxDB line protocol.
  session.read(flowFile, new InputStreamCallback(function (inputStream) {
    // Parse incoming FlowFile content to JSON.
    var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    var json = JSON.parse(text);

    // Iterate through records.
    json.forEach(function (record) {
      output += record['measurement'] +
        ",hardware_id=" + record['tags']['hardware_id'] +
        ",type=" + record['tags']['type'] + " ";
      var currentField = 0;
      var totalFields = Object.keys(record['fields']).length;
      for (var f in record['fields']) {
        var fieldValue = typeof record["fields"][f] === "string" ? '"' + record["fields"][f] + '"'
          : record["fields"][f];

        output += f + "=" + fieldValue;
        currentField++;
        if (currentField < totalFields) {
          output += ",";
        }
      }

      if (record.timestamp) {
        output += " " + record.timestamp * 1000000;
      }
      output += "\n";
    });
  }));

  // Replace FlowFile's content with the data in InfluxDB line protocol.
  flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
    outputStream.write(output.getBytes(StandardCharsets.UTF_8))
  }));

  session.transfer(flowFile, REL_SUCCESS);
}
