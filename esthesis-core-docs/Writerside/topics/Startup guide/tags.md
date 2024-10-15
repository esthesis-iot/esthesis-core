# Tags
Tags is a powerful mechanism of esthesis CORE allowing to logically group different types of
resources. Using tags, you can easily filter resources by their type, purpose, or any other
criteria suitable for your project. You can also use tags to send different configurations to
devices being registered, as well as creating device campaigns and executing commands targeting
all devices with a specific tag.

## Device registration
When a device is about to be registered with esthesis CORE, it can optionally specify one or more
tags. They in which registration and tags creation/selection is handled is presented next:

- **Device does not specify any tag**<br />
When a device is registering without specifying any tag, esthesis CORE will assign a random
MQTT server to it. This is to make sure that your device is still registered and can be accessed
by esthesis CORE, even if you do not have any tags defined. It is strongly suggested that you
always specify a tag when registering your devices.
- **Device specifies a single tag that is defined in esthesis CORE**<br />
When a device specifies a single tag that is defined in esthesis CORE, the device will be assigned
the MQTT server that is associated with that tag.
- **Device specifies multiple tags that are all defined in esthesis CORE**<br />
When a device specifies multiple tags that are all defined in esthesis CORE, the device will be
assigned a random MQTT server associated with all defined tag.
- **Device specifies a single tag that is not defined in esthesis CORE**<br />
When a device specifies a single tag that is not defined in esthesis CORE, the tag can be
automatically created in esthesis CORE, or not, based on the "Device tags" configuration under
"Settings".
  - If allow new tags to be created during registration, the device will be registered, the provided
  tag will be created in esthesis CORE, however no MQTT server will be assigned to it. To be able to
  connect this device to esthesis CORE, you need to manually change the device's settings to define
  an MQTT server; this requires you having remote or physical access to the device.
  - If you do not allow new tags to be created during registration, the device will be registered,
  no new tag will be created, and a random MQTT server will be assigned to the device.
- **Device specifies multiple tags that are not defined in esthesis CORE**<br />
This is similar to the above case, however multiple tags will be created or ignored.
- **Device specifies multiple tags, some exists in esthesis CORE some they do not**<br />
	- If esthesis CORE is configured to allow new tags to be created during registration, the device will
	be registered, the provided tags that are not defined in esthesis CORE will be created, and the
	device will be assigned a random MQTT server out of the tags that already exist.
  - If esthesis CORE is configured to not allow new tags to be created during registration, the device
  will be registered, no new tags will be created, and the device will be assigned a random MQTT server
  out of the tags that already exist.

As you can see, tags is a powerful mechanism providing you a lot of flexibility, however not planning
your tags carefully may lead to unexpected behavior. It is strongly suggested that you always define
your tags before registering your devices, and that you have a well-thought tags strategy.
