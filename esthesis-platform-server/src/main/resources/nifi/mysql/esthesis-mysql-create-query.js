/*
 * esthesis-mysql-create-query.js
 * NiFi ExecuteScript to convert incoming telemetry/metadata read requests to a MySQL query.
 *
 * All incoming parameters are expected to be already sanitised.
 */

// The list of allowed operations on data.
// 'mean' is converted to 'avg' for MySQL.
var allowedOperations = ['query', 'count', 'max', 'min', 'mean', 'sum'];
var nonTimeOperations = ['count', 'mean', 'sum'];

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
    queryTemplate = queryTemplate.replace("$GROUPBY", "");
    return queryTemplate.replace("$FIELDS", "*");
  } else {
    queryTemplate = queryTemplate.replace("$GROUPBY", "GROUP BY " + fields + ", timestamp");
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
    hardwareIdQuery = "hardware_id REGEXP " + hardwareId;
  } else {
    hardwareIdQuery = "hardware_id = '" + hardwareId + "'";
  }

  return queryTemplate.replace("$TAGS", hardwareIdQuery);
}

function getCalculateFields(flowFile, operation) {
  // Create the fields template (i.e. first part of the SELECT query).
  var fieldsTemplate;
  var fields = flowFile.getAttribute('esthesis.param.fields');
  fieldsTemplate = operation + "(" + fields + ") as " + (operation === "avg" ? "mean" : operation)
    + "_" + fields;

  return fieldsTemplate;
}

function _updateTime(flowFile, queryTemplate) {
  var timeFrom = flowFile.getAttribute('esthesis.param.from');
  var timeTo = flowFile.getAttribute('esthesis.param.to');
  if (timeFrom && timeTo) {
    queryTemplate = queryTemplate.replace("$TIME",
      "and timestamp >= " + timeFrom + " and timestamp <= " + timeTo);
  } else if (timeFrom) {
    queryTemplate = queryTemplate.replace("$TIME", "and timestamp >= " + timeFrom);
  } else if (timeTo) {
    queryTemplate = queryTemplate.replace("$TIME", "and timestamp <= " + timeTo);
  } else {
    queryTemplate = queryTemplate.replace("$TIME", "");
  }
  return queryTemplate;
}

function _updateOrder(flowFile, queryTemplate) {
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
  return queryTemplate;
}

function addTimestampToOperation(queryTemplate) {
  var measurement = flowFile.getAttribute('esthesis.param.measurement');
  var fields = flowFile.getAttribute('esthesis.param.fields');
  return "SELECT " + fields +
  " as " + operation + "_" + fields + ", timestamp from " + measurement + " WHERE "
  + fields + " in " + "(" + queryTemplate + ")" + " ORDER BY  timestamp desc LIMIT 1;";
}

/**
 * Create a generic query request.
 * @param flowFile The FlowFile to process information to create the request.
 * @param fields A list of fields already resolved in a previous step.
 */
function createQueryRequest(flowFile, fields) {
  // Set the template for this type of execution.
  var queryTemplate = "SELECT $FIELDS FROM $MEASUREMENT WHERE $TAGS $TIME $GROUPBY ORDER BY timestamp $ORDER $PAGING";

  if (nonTimeOperations.indexOf(operation) > -1) {
    queryTemplate = queryTemplate.replace("ORDER BY timestamp $ORDER", "");
  }

  // Set fields, measurement and tags.
  if (fields) {
    queryTemplate = queryTemplate.replace("$FIELDS", fields);
    queryTemplate = queryTemplate.replace("$GROUPBY", "");
  } else {
    queryTemplate = _updateFields(flowFile, queryTemplate);
  }

  queryTemplate = _updateMeasurement(flowFile, queryTemplate);
  queryTemplate = _updateTags(flowFile, queryTemplate);
  queryTemplate = _updateTime(flowFile, queryTemplate);
  queryTemplate = _updateOrder(flowFile, queryTemplate);

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

  log.trace("Created MYSQL query: " + queryTemplate);

  if (operation === 'max' || operation === 'min') {
    queryTemplate = addTimestampToOperation(queryTemplate);
  }
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
    if (isMetadataQuery && operation !== 'query' ) {
      throw('Requested operation \'' + operation + "\' is not supported for metadata");
    }
    if (allowedOperations.indexOf(operation) > -1) {
      if (operation === 'query') {
        queryTemplate = createQueryRequest(flowFile);
      } else {
        if (operation == "mean") {
          operation = "avg";
        }
        queryTemplate = createQueryRequest(flowFile, getCalculateFields(flowFile, operation));
      }
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
