/*
 * esthesis-mysql-create-query.js
 * NiFi ExecuteScript to convert incoming telemetry/metadata read requests to a MySQL query.
 *
 * All incoming parameters are expected to be already sanitised.
 */

// The list of allowed operations on data.
// 'mean' is converted to 'avg' for MySQL.
var allowedOperations = ['count', 'max', 'min', 'mean', 'sum'];

/**
 * Utility method to set the fields of a query.
 * @param flowFile The FlowFile to get the fields from.
 * @param queryTemplate The query template in which fields are updated.
 * @returns {void | string} The original query template updated with the fields value.
 * @private
 */
function _updateFields(flowFile, queryTemplate) {
  var fields = flowFile.getAttribute('esthesis.param.fields').trim();
  if (!fields) {
    return queryTemplate.replace("$FIELDS", "*");
  } else {
    return queryTemplate.replace("$FIELDS", fields + ", timestamp");
  }
}

/**
 * Utility method to set the measurement of a query.
 * @param flowFile The FlowFile to get the measurement from.
 * @param queryTemplate The query template in which fields are updated.
 * @returns {void | string} The original query template updated with the measurement value.
 * @private
 */
function _updateMeasurement(flowFile, queryTemplate) {
  var measurement = flowFile.getAttribute('esthesis.param.measurement');
  if (!measurement) {
    throw  "Measurement parameter can not be empty.";
    //session.transfer(flowFile, REL_FAILURE);
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
  var hardwareId = flowFile.getAttribute('esthesis.hardwareId');

  var hardwareIdQuery;
  if ((hardwareId.startsWith('/') && hardwareId.endsWith('/'))) {
    hardwareIdQuery = "hardwareId REGEXP " + hardwareId;
  } else {
    hardwareIdQuery = "hardwareId = '" + hardwareId + "'";
  }

  return queryTemplate.replace("$TAGS", hardwareIdQuery);
}

function getCalculateFields(flowFile, operation) {
  // Create the fields template (i.e. first part of the SELECT query).
  var fieldsTemplate;
  var fields = flowFile.getAttribute('esthesis.param.fields');
  if (!fields) {
    fieldsTemplate = operation + "(*)";
  } else {
    fieldsTemplate = fields.split(",").map(function (f) {
      return operation + "(" + f.trim() + ") as " + operation + "_" + f
    }).join(',')
  }

  return fieldsTemplate;
}

/**
 * Create a generic query request.
 * @param flowFile The FlowFile to process information to create the request.
 * @param fields A list of fields already resolved in a previous step.
 */
function createQueryRequest(flowFile, fields) {
  // Set the template for this type of execution.
  var queryTemplate = "SELECT $FIELDS FROM $MEASUREMENT WHERE $TAGS $TIME ORDER BY timestamp $ORDER $PAGING";

  // Set fields, measurement and tags.
  if (fields) {
    queryTemplate = queryTemplate.replace("$FIELDS", fields + ", timestamp");
  } else {
    queryTemplate = _updateFields(flowFile, queryTemplate);
  }

  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  queryTemplate = _updateTags(flowFile, queryTemplate);

  // Set time (incoming time is in msec, so it needs to be converted to nanoseconds for InfluxDB).
  var timeFrom = flowFile.getAttribute('esthesis.param.from');
  var timeTo = flowFile.getAttribute('esthesis.param.to');
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
  var order = flowFile.getAttribute('esthesis.param.order');
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

// If FlowFile exists proceed with processing.
if (flowFile != null) {
  try {
    // Define the query to be executed.
    var queryTemplate;

    // Call the appropriate handler for the the type of query requested.
    var operation = flowFile.getAttribute('esthesis.operation').trim().toLowerCase();
    if (operation === 'query') {
      queryTemplate = createQueryRequest(flowFile);
    } else if (allowedOperations.indexOf(operation) > -1) {
      if (operation == "mean") {
        operation = "avg";
      }
      queryTemplate = createQueryRequest(flowFile, getCalculateFields(flowFile, operation));
    } else {
      throw('Requested operation \'' + operation + "\' is not supported.");
    }

    // Replace FlowFile's content with the query to be executed.
    flowFile = session.write(flowFile, new OutputStreamCallback(function (outputStream) {
      outputStream.write(queryTemplate.getBytes(StandardCharsets.UTF_8))
    }));

    session.transfer(flowFile, REL_SUCCESS);
  } catch (e) {
    log.error(e);
    session.transfer(flowFile, REL_FAILURE);
  }
}
