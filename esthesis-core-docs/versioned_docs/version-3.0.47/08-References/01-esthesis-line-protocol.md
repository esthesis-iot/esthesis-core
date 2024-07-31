# esthesis Line Protocol (eLP)

The esthesis Line Protocol (eLP) is a simple, text-based protocol, allowing you to push data to
the official device agent, or if you have a custom agent, to a data broker from which an esthesis
Dataflow reads data form.

## Specification
eLP consists of text-based messages conveying information that should be processed by esthesis
Core. Each message may contain multiple lines, and each line may contain multiple values. Here is
an overview of a line following the eLP format:
```text
category measurement1=value1[,measurement2=value2...] [timestamp]
```

- **category**, is an arbitrary, alphanumeric value, specifying the category the measurements belong
to. It should start with a letter.

- **measurement**, is an arbitrary, alphanumeric value, specifying the name of a measurement. It should start with a letter.

- **value**, is an arbitrary, alphanumeric value, specifying the value of a measurement.

- **timestamp**, should be expressed as a string, following <a href="https://en.wikipedia.org/wiki/ISO_8601" target="_new">ISO-8601</a>.
The timestamp value is shared between all measurements of the same line. If you need to send
multiple measurements with each one having a unique timestamp, split them in different lines
and specify a separate timestamp for each. If a timestamp value is not present, the time at the time of processing the message will be used as a timestamp.

### Value typing
When an eLP message is received by an esthesis Dataflow, the dataflow will try to determine the type
of the value using reasonable defaults. However, you may find cases where automatic value type
discovery is not what you would expect. For those cases, you can specify the type of the value by
using suffixes. Here is the list of prefixes you can use:

- Integer: append a `i` to the value, e.g. 123i
- Float: append a `f` to the value, e.g. 123.456f
- Long: append a `l` to the value, e.g. 1234567890123456789l
- Double: append a `d` to the value, e.g. 123.456d
- Short: append a `s` to the value, e.g. 123s
- Byte: append a `b` to the value, e.g. 123b
- Boolean: `true`, or `false`
- String: enclose the value is single quotes, e.g. 'myval'

## Examples

#### Send the cpu load:
```text
cpu load=1
```

#### Send the cpu load for a specific point in time:
```text
cpu load=1 2022-01-01T01:02:03Z
```

#### Send the cpu load and temperature:
```text
cpu load=1,temperature=20
```

#### Send the cpu load and temperature for a specific point in time:
```text
cpu load=1,temperature=20 2022-01-01T01:02:03Z
```

#### Specifying a specific value type (i.e. float) for a measurement:
```text
cpu load=1f
```

#### Sending text data:
```text
net ip1="primary 192.168.1.1"
```

#### Sending multiple lines:
```text
cpu load=1
cpu load=1 2022-01-01T01:02:03Z
cpu temperature=20 2022-01-01T03:02:03Z
cpu threads=102,temperature=29
cpu threads=102,temperature=29 2022-01-01T01:02:03Z
```
