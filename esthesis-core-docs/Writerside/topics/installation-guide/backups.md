# Backups
The following sections provide instructions on how to configure backups for the esthesis CORE
services. The backup procedure is a combination of enabling the backup feature on the underlying
services, as well as configuring your deployment environment to store the backups. Accordingly,
instructions are targeting specific cloud environments.

## Backup with a custom storage class
<note>
To be completed
</note>

## Backup on AWS with EFS
<note>
To be completed
</note>

## Backup on AWS with S3
The following section provides instructions on how to configure backups when running esthesis CORE
on AWS. Backups are targeting S3 buckets, which have been mounted as a Persistent Volume (PV) on
the Kubernetes cluster. To follow the next steps you need:
- An existing AWS Identity and Access Management (IAM) OpenID Connect (OIDC) provider for your
cluster. To determine whether you already have one, or to create one, see
[Creating an IAM OIDC provider for your cluster](https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html).
- Version 2.12.3 or later of the AWS CLI installed and configured on your device or AWS CloudShell.
- The `kubectl` command line tool is installed on your device or AWS CloudShell.

### Creating an IAM policy
The mount point for Amazon S3 CSI driver requires Amazon S3 permissions to interact with your file
system. This section shows how to create an IAM policy that grants the necessary permissions. The
following example policy follows the IAM permission recommendations for mount point. Alternatively,
you can use the AWS managed policy `AmazonS3FullAccess`, but this managed policy grants more
permissions than are needed. To create the IAM policy follow these steps:

1. Open the IAM console at https://console.aws.amazon.com/iam/.
2. In the left navigation pane, choose Policies.
3. On the Policies page, choose Create policy.
4. For Policy editor, choose JSON.
5. Under Policy editor, copy and paste the following:

:::info
Replace `` `DOC-EXAMPLE-BUCKET1` `` with your own Amazon S3 bucket name.
:::

```json
{
   "Version": "2012-10-17",
   "Statement": [
        {
            "Sid": "MountpointFullBucketAccess",
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::DOC-EXAMPLE-BUCKET1"
            ]
        },
        {
            "Sid": "MountpointFullObjectAccess",
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:AbortMultipartUpload",
                "s3:DeleteObject"
            ],
            "Resource": [
                "arn:aws:s3:::DOC-EXAMPLE-BUCKET1/*"
            ]
        }
   ]
}
```
1.  Directory buckets, introduced with the S3 Express One Zone storage class, use a different authentication mechanism from general purpose buckets. Instead of using  `s3:*`  actions, you should use the  `s3express:CreateSession`  action. For information about directory buckets, see  [Directory buckets](https://docs.aws.amazon.com/AmazonS3/latest/userguide/directory-buckets-overview.html)  in the  _Amazon S3 User Guide_.

    Below is an example of least-privilege policy that you would use for a directory bucket.

    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": "s3express:CreateSession",
                "Resource": "arn:aws:s3express:`aws-region`:`111122223333`:bucket/`` `DOC-EXAMPLE-BUCKET1`--az_id--x-s3``"
            }
        ]
    }
    ```
2.  Choose  **Next**.
3.  On the  **Review and create**  page, name your policy. This example walkthrough uses the name  `AmazonS3CSIDriverPolicy`.
4.  Choose  **Create policy**.

### Create a service account and IAM role with  `eksctl`
To create the IAM role and the Kubernetes service account, run the following commands. These commands
also attach the  `AmazonS3CSIDriverPolicy` IAM policy to the role, annotate the Kubernetes service
account (`s3-csi-controller-sa`) with the IAM role's Amazon Resource Name (ARN), and add the
Kubernetes service account name to the trust policy for the IAM role.

```shell
CLUSTER_NAME=my-cluster
REGION=region-code
ROLE_NAME=AmazonEKS_S3_CSI_DriverRole
POLICY_ARN=AmazonEKS_S3_CSI_DriverRole_ARN
eksctl create iamserviceaccount \
    --name s3-csi-driver-sa \
    --namespace kube-system \
    --cluster $CLUSTER_NAME \
    --attach-policy-arn $POLICY_ARN \
    --approve \
    --role-name $ROLE_NAME \
    --region $REGION \
    --role-only
  ```

### Configure Persistent Volume for backups on S3
To create a Persistent Volume that points to an S3 bucket you can use the following example:

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: <the-name-of-the-PV>
spec:
  capacity:
    storage: 1200Gi # ignored, required
  accessModes:
    - ReadWriteMany # supported options: ReadWriteMany / ReadOnlyMany
  mountOptions:
    - allow-delete
    - region <the-aws-region-of-bucket>
  csi:
    driver: s3.csi.aws.com # required
    volumeHandle: s3-csi-driver-volume
    volumeAttributes:
      bucketName: <the-name-of-the-bucket>
   ```

You will have to replace the following:
 - **the-name-of-the-PV**: The name you want to give to you PV.
 - **the-aws-region-of-bucket**: The region on which you bucket is. (eg. eu-central-1)
 - **the-name-of-the-bucket**: The name of you bucket.

## Running the Helm chart
You can configure the provided Helm chart to align with the resources you have created above using
the following environment parameters:

🔹 `INFLUXDB_ENABLED`<br/>
Whether InfluxDB should be installed by this chart or not.<br/>
Default: `true`

For MongoDB:

- ESTHESIS_BACKUP_MONGODB_CRONJOB_NAME: The name you want the cronjob to have. Default value is `esthesis-mongodb-backup-cronjob`
- ESTHESIS_BACKUP_MONGODB_USER: The root user for mongodb. Default value is `root`
- ESTHESIS_BACKUP_MONGODB_SECRET: The name of the secret that contains mongo-root-password. Default value is `my-mongodb`
- ESTHESIS_BACKUP_MONGODB_HOST: The name of the mongodb service on kubernetes. Default value is `my-mongodb`
- ESTHESIS_BACKUP_MONGODB_PORT: The port of mongodb. Default value is `27017`
- ESTHESIS_BACKUP_MONGODB_SCHEDULE: When you want the cronjob to run. Default value is `0 2 * * *`
- ESTHESIS_BACKUP_MONGODB_PATH: The path on the PV that the backups should be stored on. Default value is `backup/mongodb`

For InfluxDB:

- ESTHESIS_BACKUP_INFLUXDB_CRONJOB_NAME: The name you want the cronjob to have. Default value is `esthesis-influxdb-backup-cronjob`
- ESTHESIS_BACKUP_INFLUXDB_SECRET: The name of the secret that contains INFLUX_TOKEN. Default value is `my-influxdb`
- ESTHESIS_BACKUP_INFLUXDB_HOST: The name of the influxdb service on kubernetes. Default value is `my-influxdb`
- ESTHESIS_BACKUP_INFLUXDB_PORT: The port of influxdb. Default value is `8086`
- ESTHESIS_BACKUP_INFLUXDB_SCHEDULE: When you want the cronjob to run. Default value is `0 2 * * *`
- ESTHESIS_BACKUP_INFLUXDB_PATH: The path on the PV that the backups should be stored on. Default value is `backup/influxdb`

Backup PVC:

- ESTHESIS_BACKUP_PVC_NAME: The name you want the PVC to have. Default value is `esthesis-backup-s3-claim`
- ESTHESIS_BACKUP_VOLUME_NAME: The name of the PV that the PVC will be mounted on. Default value is `esthesis-backup-s3-pv`
- ESTHESIS_BACKUP_STORAGE_CLASS_NAME: If on S3, it should be left empty, otherwise it should be configured to the correct storageClassName. Default value is empty.
- ESTHESIS_BACKUP_STORAGE: How much storage you want you PVC to request. Default value is `10Gi`
