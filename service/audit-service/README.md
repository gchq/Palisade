# Audit Service

<span style="color:red">**Note:** As noted in the [documentation root](../../README.md) Palisade is in an early stage of development, therefore the precise APIs contained in these service modules are likely to adapt as the project matures.</span>

## Overview

The audit service provides a central service which other Palisade components use to log what requests they have received how resources are being processed. A non-goal of the audit service is to provide general logging support. Therefore, debug logs and so-forth should not be routed to the audit service. The output of the audit service can be useful for policy auditors for compliance checks and to monitor how rules are being applied. It may also be useful for follow-on machine processing to generate metrics on what data is being accessed, how often, by whom and for what reasons (justifications).

Palisade is designed for flexible deployment, therefore it is not assumed that all nodes within a cluster have unfettered access to the external network. It may be the case, that only specific cluster "edge" nodes are able to communicate externally to/from the cluster. Consequently, another role of the audit service is to function as a central location for the remainder of the Palisade components which themselves may not have external communication. Other services can send audit events to the audit service which can collate and store the events for later review.

Note that while the audit service provides a central place for audit logging to occur, this doesn't imply that the audit service is a singleton, in a micro-services deployment it is likely that several instances of the same audit service are present in a deployment. It will not matter which particular instance receives an individual audit event.

It may also forward certain events on to another non-Palisade service. This allows for the creation of "auditing alerts". For example, organisational rules may require that an alert is sent when a particularly sensitive type of action is taken or resource accessed.

Implementations of the audit service may also include proxies to forward the messages to another Audit service, aggregator's to reduce the volumes of logging to be stored as well as implementations that actually write the logs to storage.

By splitting the functionality of the audit components in this way, where they all implement this interface but do some small processing before passing to the next component, for example proxy -> receiver -> aggregator -> storage.
It means that if we don't want to aggregate audit records then we just remove the aggregator implementation when building that micro-service.

## API Usage

Bearing in mind the notice above, the `AuditService` class currently only defines one method.

* `void audit(final AuditRequest request)`

Hopefully little explanation is needed of what this method does, but it is worth stating that the actual action taken by an implementation of `AuditService` may depend on what is being audited. For example, a simple note of a request being made could simply be written to the audit log, but repeated, denied requests for a resource by a specific user may result in an escalation whereby an alert is raised.