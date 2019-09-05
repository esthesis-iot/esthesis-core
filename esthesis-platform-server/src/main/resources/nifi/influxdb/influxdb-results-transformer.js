/*
 * NiFi ExecuteScript to convert InfluxDB data to esthesis schema.
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

if (flowFile != null) {
  var output = {};
  session.read(flowFile, new InputStreamCallback(function (inputStream) {
    // Parse incoming FlowFile content to JSON.
    var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    var json = JSON.parse(text);

    // Convert content.
    if (json.results && json.results[0] && json.results[0].series && json.results[0].series[0]) {
      var series = json.results[0].series[0];
      var columns = series.columns;
      // Set the root-level key as the name of the measurement.
      output[series.name] = [];

      // Fill-in each value from the result set.
      for (i in series.values) {
        val = {};
        for (key in series.values[i]) {
          if (columns[key] === 'time') {
            val[columns[key]] = Math.round(series.values[i][key] / 1000000);
          } else {
            val[columns[key]] = series.values[i][key];
          }
        }
        output[series.name].push(val);
      }
    }
  }));

  // Replace FlowFile's content with the data in InfluxDB line protocol.
  flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
    outputStream.write(JSON.stringify(output).getBytes(StandardCharsets.UTF_8))
  }));

  session.transfer(flowFile, REL_SUCCESS);
} else {
  session.transfer(flowFile, REL_FAILURE);
}
