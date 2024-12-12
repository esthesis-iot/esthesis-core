# Dashboard

[WORK IN PROGRESS]
Two main goals:
- Optimise message traffic
- Minimise latency

Three different types of dashboard messages:
- Generic, eventually delivered: Messages that do not target a specific user, providing general information about the 
system. Those messages are of low priority, and latency is not a concern. For example, the number of events audited;
the end-users wants to know how many events were audited, but it is not critical to know that information immediately.
- Generic, immediate delivery: Similar to the previous type, but with immediate delivery. For example, a message that
indicates which are the most recent devices that were registered. The end-user wants to know that information as soon as
possible, as it may be useful for them to take action, or to verify that the system is working as expected.
- User-specific, immediate delivery: Messages that are targeted to a specific user. For example, a message with the 
value of a sensor that the user is monitoring. The user wants to know that information as soon as possible, as it may
be critical for them to take action.