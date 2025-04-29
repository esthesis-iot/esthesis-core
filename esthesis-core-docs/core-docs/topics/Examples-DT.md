# Examples

In this section you can find examples of how to use the esthesis CORE Digital Twin API to interact with Digital Twins.

## Preparation
### Prerequisites
You must meet the following prerequisites to follow the examples below:
1. You must have created an `Application` in esthesis CORE. Make the application `Active` and note down the token
assigned to it:
![dt-application.png](dt-application.png)
2. You must have a shell with the `curl` command available.
3. You must have at least one device connected to esthesis CORE.
3. Optionally, you may have the `jq` utility installed to parse JSON responses.

### Authentication
Authentication to the Digital Twin API is taking place via a custom HTTP header `X-ESTHESIS-DT-APP`. You must include 
this header in every request to the API, with the value being the token of the application you created in esthesis CORE.
For example, if your token is `abc123`, you would include the header as follows:
```bash
curl -H "X-ESTHESIS-DT-APP: abc123" https://...
```

## Examples
Set the following environment variables, so that you do not have to repeat them in every example. Make sure you replace
the values with the actual values from your own esthesis CORE installation:
```bash
export ESTHESIS_DT_TOKEN=abc123
export ESTHESIS_CORE_DT_URL=http://my-esthesis-core-url/api/dt
```

Do not forget to replace `dash-dev-1` device hardware ID used in the following examples with an actual hardware ID of 
a device connected to your esthesis CORE installation.

### Example 1: Ping a device, synchronously
Request:
```bash
curl -s -X POST -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/command/device/dash-dev-1/ping" | jq
```
Reply:

![dt-example-1.png](dt-example-1.png)

### Example 2: Ping a device, asynchronously
Request:
```bash
curl -s -X POST -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/command/device/dash-dev-1/ping?async=true" | jq
```
Reply:
The reply is empty, as the command was sent asynchronously.

### Example 3: Send a command to a device, synchronously
Request:
```bash
curl -s -X POST -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/command/device/dash-dev-1/execute" --data "uname -a" | jq
```
Reply:

![dt-example3.png](dt-example3.png)

### Example 4: Send a command to a device and receive the reply asynchronously
#### Send command
Request:
```bash
curl -i -s -X POST -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/command/device/dash-dev-1/execute?async=true" --data "uname -a"
```
Reply:

![dt-example4.png](dt-example4.png)

#### Get reply
Request:

Pass the `correlation-id` from the previous response to get the reply:
```bash
curl -s -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/get/command/67972f23e0babf1c375d6fa2/reply" | jq
```

Reply:

![dt-example4b.png](dt-example4b.png)

### Example 5: Get all available measurements for a category
Request:
```bash
curl -w '\n' -s -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/get/dash-dev-1/demo"
```

Reply:

![dt-example5.png](dt-example5.png)

### Example 6: Get a specific measurement from a category
Request:
```bash
curl -w '\n' -s -H "X-ESTHESIS-DT-APP: $ESTHESIS_DT_TOKEN" \
    "$ESTHESIS_CORE_DT_URL/v1/get/dash-dev-1/demo/cpu_temperature"
```

Reply:

![dt-example6.png](dt-example6.png)