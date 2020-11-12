/*
 * esthesis-influxdb-results-transformer.js
 * NiFi ExecuteScript to convert InfluxDB data to esthesis schema:
 *
 * {"measurement": [
 *  {"time": , "field1": , "field2":, ...
 * ]}
 */

function parseToMilis(date) {
  var dotPosition = date.indexOf(".");
  var timezonePosition = date.length -1;
  var nanos = date.substring(dotPosition - 1, timezonePosition);
  var milis = (parseFloat(nanos).toFixed(3) + date.charAt(timezonePosition));
  return date.substring(0, dotPosition - 1) + milis;
}

// Get references the the output stream to write results, the input stream to read the content of
// the FlowFile, and to the incoming FlowFile.
var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback")
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var IOUtils = Java.type("org.apache.commons.io.IOUtils")
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets")
var flowFile = session.get();
var nonTimeOperations = ['count', 'mean', 'sum'];

if (flowFile != null) {
  var operation = flowFile.getAttribute('esthesis.operation');
  try {
    var output = {};
    session.read(flowFile, new InputStreamCallback(function (inputStream) {
      // Parse incoming FlowFile content to JSON.
      var text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      var json = JSON.parse(text);

      if (json.length > 0) {
        for (var i in json) {
          var series = json[i].results[0].series[0];
          var columns = series.columns;
          // Set the root-level key as the name of the measurement.
          if (!output[series.name]) {
            output[series.name] = [];
          }

          // Fill-in each value from the result set.
          for (var j in series.values) {
            var val = {};
            for (var key in series.values[j]) {
              if (columns[key] === 'time') {
                if (nonTimeOperations.indexOf(operation) == -1) {
                  val['timestamp'] = Date.parse(parseToMilis(series.values[j][key]));
                }
              } else {
                // Ignore null values.
                if (series.values[j][key] !== null) {
                  val[columns[key]] = series.values[j][key];
                }
              }
              if (columns.indexOf('type') == -1) {
                val['type'] = flowFile.getAttribute('esthesis.type');
              }
            }
            output[series.name].push(val);
          }
        }
      }

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
              if (nonTimeOperations.indexOf(operation) == -1) {
                val['timestamp'] = Date.parse(parseToMilis(series.values[i][key]));
              }
            } else {
              // Ignore null values.
              if (series.values[i][key] !== null) {
                val[columns[key]] = series.values[i][key];
              }
            }

            if (columns.indexOf('type') == -1) {
              val['type'] = flowFile.getAttribute('esthesis.type');
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
  } catch (e) {
    log.error(e);
    session.transfer(flowFile, REL_FAILURE);
  }
}
