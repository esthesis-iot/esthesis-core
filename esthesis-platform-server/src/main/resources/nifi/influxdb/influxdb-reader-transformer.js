/*
 * NiFi ExecuteScript to convert incoming telemetry/metadata read requests to an InfluxDB query.
 *
 * version: 1.0.0
 */

/**
 * Utility method to set the fields of a query.
 * @param flowFile The FlowFile to get the fields from.
 * @param queryTemplate The query template in which fields are updated.
 * @returns {void | string} The original query template updated with the fields value.
 * @private
 */
function _updateFields(flowFile, queryTemplate) {
  var fields = flowFile.getAttribute('http.query.param.fields');
  if (!fields) {
    fields = "*";
  }
  return queryTemplate.replace("$FIELDS", fields);
}

/**
 * Utility method to set the measurement of a query.
 * @param flowFile The FlowFile to get the measurement from.
 * @param queryTemplate The query template in which fields are updated.
 * @returns {void | string} The original query template updated with the measurement value.
 * @private
 */
function _updateMeasurement(flowFile, queryTemplate) {
  var measurement = flowFile.getAttribute('http.query.param.measurement');
  if (!measurement) {
    log.error('Measurement parameter can not be empty.');
    session.transfer(flowFile, REL_FAILURE);
  }
  return queryTemplate.replace("$MEASUREMENT", measurement);
}

/**
 * Utility method to set the tags of a query.
 * @param flowFile The FlowFile to get the tags from.
 * @param queryTemplate The query template in which tags are updated.
 * @returns {void | string} The original query template updated with the tags value.
 * @private
 */
function _updateTags(flowFile, queryTemplate) {
  return queryTemplate.replace("$TAGS",
    "deviceId = '" + flowFile.getAttribute('esthesis.deviceId') +
    "' and type = '" + flowFile.getAttribute('esthesis.dataType') + "'");
}

function _createCalculation(flowFile, calculation) {

}

function createGetRequest(flowFile) {
  // Set the template for this type of execution.
  var queryTemplate = "SELECT $FIELDS FROM $MEASUREMENT WHERE $TAGS ORDER BY time $ORDER LIMIT 1";

  // Set fields, measurement and tags.
  queryTemplate = _updateFields(flowFile, queryTemplate);
  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  queryTemplate = _updateTags(flowFile, queryTemplate);

  // Set order by.
  var position = flowFile.getAttribute('http.query.param.position');
  if (!position) {
    position = 'last';
  }
  if (position.toLowerCase() === 'first') {
    queryTemplate = queryTemplate.replace("$ORDER", "ASC");
  } else if ((position.toLowerCase() === 'last')) {
    queryTemplate = queryTemplate.replace("$ORDER", "DESC");
  }

  log.trace("Created InfluxDB query: " + queryTemplate);
  return queryTemplate;
}

function createCalculateRequest(flowFile) {
  // Get the function to perform.
  var func = flowFile.getAttribute('http.query.param.function');
  if (!func) {
    func = 'count';
  }

  // Create the calculation query.
  queryTemplate = "SELECT $CALCULATION FROM $MEASUREMENT";
  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  var fields = flowFile.getAttribute('http.query.param.fields');
  if (!fields) {
    queryTemplate = queryTemplate.replace("$CALCULATION", func + "(*)");
  } else {
    var calculationQuery = "";
    var i = 0;
    for (var field in fields.split(",")) {
      calculationQuery += func + "(" + field.trim() + ") as " + func + "_" + field.trim();
      i++;
      if (i < fields.split(",")) {
        calculationQuery += ", ";
      } else {
        calculationQuery += " ";
      }
    }
    queryTemplate = queryTemplate.replace("$CALCULATION", calculationQuery);
  }

  log.trace("Created InfluxDB query: " + queryTemplate);
  return queryTemplate;
}

/**
 * Create a generic query request.
 * @param flowFile The FlowFile to process information to create the request.
 */
function createQueryRequest(flowFile) {
  // Set the template for this type of execution.
  var queryTemplate = "SELECT $FIELDS FROM $MEASUREMENT WHERE $TAGS $TIME ORDER BY time $ORDER $PAGING";

  // Set fields, measurement and tags.
  queryTemplate = _updateFields(flowFile, queryTemplate);
  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  queryTemplate = _updateTags(flowFile, queryTemplate);

  // Set time (incoming time is in msec, so it needs to be converted to nanoseconds for InfluxDB).
  var timeFrom = flowFile.getAttribute('http.query.param.from');
  var timeTo = flowFile.getAttribute('http.query.param.to');
  if (timeFrom && timeTo) {
    timeFrom = timeFrom * 1000000;
    timeTo = timeTo * 1000000;
    queryTemplate = queryTemplate.replace("$TIME",
      "and time >= " + timeFrom + " and time <= " + timeTo);
  } else if (timeFrom) {
    timeFrom = timeFrom * 1000000;
    queryTemplate = queryTemplate.replace("$TIME", "and time >= " + timeFrom);
  } else if (timeTo) {
    timeTo = timeTo * 1000000;
    queryTemplate = queryTemplate.replace("$TIME", "and time <= " + timeTo);
  } else {
    queryTemplate = queryTemplate.replace("$TIME", "");
  }

  // Set order.
  var order = flowFile.getAttribute('http.query.param.order');
  if (order) {
    order = order.toLowerCase();
    if ((order === 'asc' || order === 'desc')) {
      queryTemplate = queryTemplate.replace("$ORDER", order);
    } else {
      queryTemplate = queryTemplate.replace("$ORDER", "asc");
    }
  } else {
    queryTemplate = queryTemplate.replace("$ORDER", "asc");
  }

  // Set paging.
  var limit = flowFile.getAttribute('http.query.param.limit');
  var offset = flowFile.getAttribute('http.query.param.offset');
  if (limit && offset) {
    queryTemplate = queryTemplate.replace("$PAGING", "LIMIT " + limit + " OFFSET " + offset);
  } else if (limit) {
    queryTemplate = queryTemplate.replace("$PAGING", "LIMIT " + limit);
  } else if (offset) {
    queryTemplate = queryTemplate.replace("$PAGING", "OFFSET " + offset);
  } else {
    queryTemplate = queryTemplate.replace("$PAGING", "");
  }

  log.trace("Created InfluxDB query: " + queryTemplate);
  return queryTemplate;
}

// Get references the the output stream to write results and to the incoming FlowFile.
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var flowFile = session.get();

// If FlowFile does not exist, transition to the failure relationship, otherwise proceed with processing.
if (flowFile != null) {
  // Define the query to be executed.
  var queryTemplate;

  // Call the appropriate handler for the the type of query requested.
  switch (flowFile.getAttribute('esthesis.queryType')) {
    case "query": {
      queryTemplate = createQueryRequest(flowFile);
      break;
    }
    case "get": {
      queryTemplate = createGetRequest(flowFile);
      break;
    }
    case "calculate": {
      queryTemplate = createCalculateRequest(flowFile);
      break;
    }
    default: {
      log.error(
        'Requested query type \'' + flowFile.getAttribute('esthesis.queryType')
        + "\' is not supported.");
      session.transfer(flowFile, REL_FAILURE);
    }
  }

  // Replace FlowFile's content with the query to be executed.
  flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
    outputStream.write(queryTemplate.getBytes(StandardCharsets.UTF_8))
  }));

  session.transfer(flowFile, REL_SUCCESS);
} else {
  session.transfer(flowFile, REL_FAILURE);
}
