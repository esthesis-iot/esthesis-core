/*
 * esthesis-influxdb-create-query.js
 * NiFi ExecuteScript to convert incoming telemetry/metadata read requests to an InfluxDB query.
 *
 * All incoming parameters are expected to be already sanitised.
 */
var allowedOperations = ['query', 'count', 'max', 'min', 'mean', 'sum'];

/**
 * Utility method to set the field of a query.
 * @param flowFile The FlowFile to get the field from.
 * @param queryTemplate The query template in which field are updated.
 * @returns {void | string} The original query template updated with the field value.
 * @private
 */
function _updateField(flowFile, queryTemplate) {
  var field = flowFile.getAttribute('esthesis.param.field').trim();
  if (!field) {
    field = "*";
  }
  return queryTemplate.replace("$FIELD", field);
}

/**
 * Utility method to set the measurement of a query.
 * @param flowFile The FlowFile to get the measurement from.
 * @param queryTemplate The query template in which field are updated.
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
    hardwareIdQuery = "hardware_id =~ " + hardwareId;
  } else {
    hardwareIdQuery = "hardware_id = '" + hardwareId + "'";
  }

  return queryTemplate.replace("$TAGS",
    hardwareIdQuery + " and type = '" + flowFile.getAttribute('esthesis.type') + "'");
}

function getCalculateField(flowFile, operation) {
  // Create the field template (i.e. first part of the SELECT query).
  var fieldTemplate;
  var field = flowFile.getAttribute('esthesis.param.field');
  if (!field) {
    fieldTemplate = operation + "(*)";
  } else {
    fieldTemplate = operation + "(" + field + ") as " + operation + "_" + field;
  }

  return fieldTemplate;
}

/**
 * Create a generic query request.
 * @param flowFile The FlowFile to process information to create the request.
 * @param field The field that will be used in the query
 */
function createQueryRequest(flowFile, field) {
  // Set the template for this type of execution.
  var queryTemplate = "SELECT $FIELD FROM $MEASUREMENT WHERE $TAGS $TIME ORDER BY time $ORDER $PAGING";

  // Set field, measurement and tags.
  if (field) {
    queryTemplate = queryTemplate.replace("$FIELD", field);
  } else {
    queryTemplate = _updateField(flowFile, queryTemplate);
  }
  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  queryTemplate = _updateTags(flowFile, queryTemplate);

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
      queryTemplate = queryTemplate.replace("$ORDER", "desc");
    }
  } else {
    queryTemplate = queryTemplate.replace("$ORDER", "desc");
  }

  // Set paging.
  var limit = isMetadataQuery ? 1 : flowFile.getAttribute('esthesis.param.pageSize');
  var offset = flowFile.getAttribute('esthesis.param.page');
  if (limit && offset) {
    queryTemplate = queryTemplate.replace("$PAGING", "LIMIT " + limit + " OFFSET " + offset);
  } else if (limit) {
    queryTemplate = queryTemplate.replace("$PAGING", "LIMIT " + limit);
  } else if (offset) {
    queryTemplate = queryTemplate.replace("$PAGING", "OFFSET " + offset);
  } else {
    queryTemplate = queryTemplate.replace("$PAGING", "");
  }

  log.info("Created InfluxDB query: " + queryTemplate);
  return queryTemplate;
}

// Get references the the output stream to write results and to the incoming FlowFile.
var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
var flowFile = session.get();
var isMetadataQuery = flowFile.getAttribute('esthesis.type') === "metadata";

// If FlowFile exists proceed with processing.
if (flowFile != null) {
  try {
    // Define the query to be executed.
    var queryTemplate;

    // Call the appropriate handler for the the type of query requested.
    var operation = flowFile.getAttribute('esthesis.operation').trim().toLowerCase();
    var field = flowFile.getAttribute('esthesis.param.field');

    if (isMetadataQuery && operation !== 'query') {
      throw('Requested operation \'' + operation + "\' is not supported for metadata");
    }

    if (allowedOperations.indexOf(operation) > -1) {
      queryTemplate = operation === 'query' ? createQueryRequest(flowFile) : createQueryRequest(
        flowFile, getCalculateField(flowFile, operation));
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
