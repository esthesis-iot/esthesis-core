# Permissions and Security

esthesis Core secures access to resources via a permissions system. This system is based on the
Policies model, which is a set of rules that are evaluated to determine whether a user has access
to a resource.

A policy template contains the following parts:

```mermaid
graph LR
	classDef MainElement fill: red

	subgraph a[Prefix]
		subgraph ern[Resource identification number]
			A[ern]:::MainElement
			A1[Constant]
			A1 -.-> A
		end

		subgraph application[esthesis IOT platform]
			B[esthesis]:::MainElement
			B1[Constant]
			B1 -.-> B
		end
	end

	subgraph b[Service identification]
		subgraph module[Platform module]
			C[module]:::MainElement
			C1[core, uns, gateway, etc.]
			C1 -.-> C
		end

		subgraph service[Service name]
			D[service]:::MainElement
			D1[Variable]
			D1 -.-> D
		end
	end

	subgraph c[Object identification]
		subgraph objectID[Object identifier]
			E[object ID]:::MainElement
			E1[Variable]
			E1 -.-> E
		end
	end

	subgraph d[Operation identification]
		subgraph operation[Operation]
			F[operation]:::MainElement
			F1[create, read, update, delete, etc.]
			F1 -.-> F
		end

		subgraph allowdeny[Access type]
			G[access]:::MainElement
			G1[allow, deny]
			G1 -.-> G
		end
	end

	ern -->|:| application
	application -->|:| module
	module -->|:| service
	service -->|:| objectID
	objectID -->|:| operation
	operation -->|:| allowdeny
```

An example policy could be:

```text
ern:esthesis:core:ca:*:delete:allow
```

The above policy allows the deletion of any resources in the Certificate Authoridy service of the
esthesis Core module.

## Backend
