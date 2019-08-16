var InputStreamCallback =  Java.type("org.apache.nifi.processor.io.InputStreamCallback")
var OutputStreamCallback =  Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var IOUtils = Java.type("org.apache.commons.io.IOUtils")
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets")

var flowFile = session.get();

if (flowFile != null) {
  var output = "";

  // Read incomfing FlowFile.
  session.read(flowFile, new InputStreamCallback(function(inputStream) {
    var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    // Parse incoming string to JSON.
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
      output = json.m + "," + "tag=tag1 "
      for (key in json.v) {
        output += key + "=" + json.v[key];
        i++;
        if (i < keys) {
          output += ",";
        }
      }
      output += " " + json.t + "\n";
    } else {
      output = json.m + "," + "tag=tag1 " + "value=" + json.v + " " + json.t;
    }
  }));

  // Replace FlowFile.
  flowFile = session.write(flowFile,new OutputStreamCallback(function(outputStream) {
    outputStream.write(output.getBytes(StandardCharsets.UTF_8))
  }));

  session.transfer(flowFile, REL_SUCCESS)
} else {
  session.transfer(flowFile, REL_FAILURE)
}
