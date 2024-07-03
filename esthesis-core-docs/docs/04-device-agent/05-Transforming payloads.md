# Transforming payloads

The embedded MQTT and HTTP endpoints allow the device agent to receive data from external sources
using the [eLP protocol](../08-References/01-esthesis-line-protocol.md). When the external sources
are under your control, eLP is a simple protocol you can easily work with. However, when the
external sources are not under your control, you may need to transform their payloads before they
are sent to the device agent.

esthesis device agent allows you to transform payloads using external Lua scripts, for both MQTT
and HTTP endpoints (see the `LUA*` parameters in [Configuration parameters](02-Configuration%20parameters.md)
for more details).

## Lua incoming payload variable
In your Lua script, you have access to the original payload in the `payload` variable; the variable
is a string. You can modify the payload as you wish, and simply `return` it at the end.

### Example Lua script
Let us consider an external data source that pushes data in the following format:
```text
cpu
temperature=20
load=2
```

The first line is always the category name, whereas the remaining lines contain individual measurements
for that category. Measurements are separated by an equal sign.

We want to transform this incoming payload to eLP format, such as:
```text
cpu temperature=20,load=2
```
The following Lua script could be used to achieve this:
```lua
-- Splitting the payload into lines
local lines = {}
for line in payload:gmatch("[^\r\n]+") do
    table.insert(lines, line)
end

-- Extracting the category and measurements
local category = lines[1]
local measurements = {}
for i = 2, #lines do
    local measurement = lines[i]:gsub("%s+", "")  -- Remove whitespace
    table.insert(measurements, measurement)
end

-- Constructing the single-line format
local transformedPayload = category .. " " .. table.concat(measurements, ",")

return transformedPayload
```
## Lua incoming endpoint variable
In your Lua script, you also have access to an additional variable called `endpoint`.
This variable contains the MQTT topic or HTTP URI, depending on the source of the incoming payload.
This can be used to help determine the logic to apply when modifying the payload based on the specific endpoint.

### Example Lua script making use of the endpoint variable
Let's say, different from the previous example, we don't have the category name on the first line of the payload,
but instead, this information is in the topic or URI name.
For example, on the endpoint:

`sensor/hardware/cpu`

We receive the cpu category payload, with the following structure:
```text
temperature=20
load=2
```
On the other hand, in the following endpoint:

`sensor/hardware/memory`

We have the memory category  with its data in the payload such as:
```text
free=80
used=20
loadAverage=50
```

Below is an example of how to utilize the `endpoint` variable in your Lua script to handle that:
```lua
-- Splitting the payload into lines

local lines = {}
for line in payload:gmatch("[^\r\n]+") do
    table.insert(lines, line)
end

-- Extracting the category and measurements
local measurements = {}
for i = 1, #lines do
    local measurement = lines[i]:gsub("%s+", "")  -- Remove whitespace
    table.insert(measurements, measurement)
end

-- Logic based on the endpoint
if endpoint == "sensor/hardware/cpu" then
    -- Specific logic for CPU sensor
    transformedPayload = "cpu " .. table.concat(measurements, ",")
elseif endpoint == "sensor/hardware/memory" then
    -- Specific logic for Chipset sensor
    transformedPayload = "memory " .. table.concat(measurements, ",")
else
    -- Default logic for other endpoints (if any)
    transformedPayload =  "hardware " .. table.concat(measurements, ",")
end

return transformedPayload
```
