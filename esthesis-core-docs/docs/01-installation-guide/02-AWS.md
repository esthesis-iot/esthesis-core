# Amazon Web Services (AWS)
The Helm charts for esthesis CORE can be deployed "as is" in AWS and no special configuration is
required. However, there are a few things that you may need to take into account, or tune, according
to what kind of AWS resources you are using.

## AWS EKS with EFS
If you are using AWS EKS with EFS, you will need to create a storage class that uses EFS. You can
create a default storage class that uses EFS by deploying the following descriptor:

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: efs-sc
provisioner: efs.csi.aws.com
parameters:
  provisioningMode: efs-ap
  fileSystemId: <file-system-id from EFS>
  directoryPerms: "700"
  reuseAccessPoint: "false" # optional
  ensureUniqueDirectory: "true" # optional
  basePath: /efs-dp # optional
  subPathPattern: ${.PVC.name} # optional
```

However, there are certain charts (specifically, the ones from Bitnami) that require a specific
uid and gid to be set in the storage class' underlying user. For that, you need to create an additional
storage class that uses EFS and sets the uid and gid to 1001. You can create such a storage class
by deploying the following descriptor:

```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: bitnami-sc
provisioner: efs.csi.aws.com
parameters:
  provisioningMode: efs-ap
  fileSystemId: <file-system-id from EFS>
  directoryPerms: "700"
  reuseAccessPoint: "false" # optional
  ensureUniqueDirectory: "true" # optional
  basePath: /efs-dp # optional
  subPathPattern: ${.PVC.name} # optional
  gid: "1001"
  uid: "1001"
```

When deploying esthesis CORE dependencies, you will need to specify the Bitnami-specific storage class
for MongoDB, and Keycloak by setting the following environment variables prior to running the Helmfile
install command:

```bash
MONGODB_STORAGE_CLASS=bitnami-sc
KEYCLOAK_STORAGE_CLASS=bitnami-sc
```
