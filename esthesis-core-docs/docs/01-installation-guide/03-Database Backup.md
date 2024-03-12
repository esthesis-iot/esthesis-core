# Database Backup

## Connect EKS with S3

### Prerequisites

- An existing AWS Identity and Access Management (IAM) OpenID Connect (OIDC) provider for your cluster. To determine whether you already have one, or to create one, see [Creating an IAM OIDC provider for your cluster](https://docs.aws.amazon.com/eks/latest/userguide/enable-iam-roles-for-service-accounts.html).

- Version 2.12.3 or later of the AWS CLI installed and configured on your device or AWS CloudShell.

- The kubectl command line tool is installed on your device or AWS CloudShell. The version can be the same as or up to one minor version earlier or later than the Kubernetes version of your cluster. For example, if your cluster version is 1.28, you can use kubectl version 1.27, 1.28, or 1.29 with it. To install or upgrade kubectl, see Installing or updating kubectl.

### Creating an IAM policy

The Mountpoint for Amazon S3 CSI driver requires Amazon S3 permissions to interact with your file system. This section shows how to create an IAM policy that grants the necessary permissions.

The following example policy follows the IAM permission recommendations for Mountpoint. Alternatively, you can use the AWS managed policy AmazonS3FullAccess, but this managed policy grants more permissions than are needed for Mountpoint.

For more information about the recommended permissions for Mountpoint, see Mountpoint IAM permissions on GitHub.

### Create an IAM policy with the IAM console

1. Open the IAM console at https://console.aws.amazon.com/iam/.
2. In the left navigation pane, choose Policies.
3. On the Policies page, choose Create policy.
4. For Policy editor, choose JSON.
5. Under Policy editor, copy and paste the following:

> 
**_IMPORTANT:_** Replace `` `DOC-EXAMPLE-BUCKET1` `` with your own Amazon S3 bucket name.

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

### Creating an IAM role

The Mountpoint for Amazon S3 CSI driver requires Amazon S3 permissions to interact with your file system. This section shows how to create an IAM role to delegate these permissions. To create this role, you can use  `eksctl`, the IAM console, or the AWS CLI.

The IAM policy  `AmazonS3CSIDriverPolicy`  was created in the previous section.

You can create the role needed with 3 methods

 1. eksctl
 2. IAM console
 3. AWS CLI

### To create your Mountpoint for Amazon S3 CSI driver IAM role with  `eksctl`

To create the IAM role and the Kubernetes service account, run the following commands. These commands also attach the  `AmazonS3CSIDriverPolicy`  IAM policy to the role, annotate the Kubernetes service account (`s3-csi-controller-sa`) with the IAM role's Amazon Resource Name (ARN), and add the Kubernetes service account name to the trust policy for the IAM role.

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

 ### To create your Mountpoint for Amazon S3 CSI driver IAM role with the AWS Management Console

1.  Open the IAM console at  [https://console.aws.amazon.com/iam/](https://console.aws.amazon.com/iam/). 
2.  In the left navigation pane, choose  **Roles**.  
3.  On the  **Roles**  page, choose  **Create role**.  
4.  On the  **Select trusted entity**  page, do the following:   
    1.  In the  **Trusted entity type**  section, choose  **Web identity**.   
    2.  For  **Identity provider**, choose the  **OpenID Connect provider URL**  for your cluster (as shown under  **Overview**  in Amazon EKS).  
        If no URLs are shown, review the  [Prerequisites](https://docs.aws.amazon.com/eks/latest/userguide/s3-csi.html#prereqs)  section.
    3.  For  **Audience**, choose  `sts.amazonaws.com`.  
    4.  Choose  **Next**.        
5.  On the  **Add permissions**  page, do the following:
    1.  In the  **Filter policies**  box, enter  `AmazonS3CSIDriverPolicy`.       
        This policy was created in the previous section.   
    2.  Select the check box to the left of the  `AmazonS3CSIDriverPolicy`  result that was returned in the search.  
    3.  Choose  **Next**.    
6.  On the  **Name, review, and create**  page, do the following:
    1.  For  **Role name**, enter a unique name for your role, such as  `AmazonEKS_S3_CSI_DriverRole`.   
    2.  Under  **Add tags (Optional)**, add metadata to the role by attaching tags as key-value pairs. For more information about using tags in IAM, see  [Tagging IAM resources](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_tags.html)  in the  _IAM User Guide_. 
    3.  Choose  **Create role**.      
7.  After the role is created, choose the role in the console to open it for editing.  
8.  Choose the  **Trust relationships**  tab, and then choose  **Edit trust policy**. 
9.  Find the line that looks similar to the following:

    `"oidc.eks.region-code.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE:aud": "sts.amazonaws.com"`
    
    Add a comma to the end of the previous line, and then add the following line after it.  Replace `region-code` with the AWS Region that your cluster is in.  Replace  `EXAMPLED539D4633E53DE1B71EXAMPLE` with your cluster's OIDC provider ID.
    
    `"oidc.eks.region-code.amazonaws.com/id/EXAMPLED539D4633E53DE1B71EXAMPLE:sub": "system:serviceaccount:kube-system:s3-csi-*"`   
10.  Change the  `Condition`  operator from  `"StringEquals"`  to  `"StringLike"`.  
11.  Choose  **Update policy**  to finish.

### To create your Mountpoint for Amazon S3 CSI driver IAM role with the AWS CLI

1.  View the OIDC provider URL for your cluster. Replace  `` `my-cluster` ``  with the name of your cluster. If the output from the command is  `None`, review the  [Prerequisites](https://docs.aws.amazon.com/eks/latest/userguide/s3-csi.html#prereqs).
    
```shell
aws eks describe-cluster --name `my-cluster` --query "cluster.identity.oidc.issuer" --output text
```
    
   **An example output is as follows.**
    
    https://oidc.eks.`region-code`.amazonaws.com/id/`EXAMPLED539D4633E53DE1B71EXAMPLE`

2.  Create the IAM role, granting the Kubernetes service account the  `AssumeRoleWithWebIdentity`  action.
    
    1.  Copy the following contents to a file named  `` `aws-s3-csi-driver-trust-policy`.json``. Replace  `` `111122223333` ``  with your account ID. Replace  `` `EXAMPLED539D4633E53DE1B71EXAMPLE` `` and  `` `region-code` ``  with the values returned in the previous step.
        
        ```json
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {
                "Federated": "arn:aws:iam::`111122223333`:oidc-provider/oidc.eks.`region-code`.amazonaws.com/id/`EXAMPLED539D4633E53DE1B71EXAMPLE`"
              },
              "Action": "sts:AssumeRoleWithWebIdentity",
              "Condition": {
                "StringLike": {
                  "oidc.eks.`region-code`.amazonaws.com/id/`EXAMPLED539D4633E53DE1B71EXAMPLE`:sub": "system:serviceaccount:kube-system:s3-csi-*",
                  "oidc.eks.`region-code`.amazonaws.com/id/`EXAMPLED539D4633E53DE1B71EXAMPLE`:aud": "sts.amazonaws.com"
                }
              }
            }
          ]
        }
        ```
        
    2.  Create the role. You can change  `` `AmazonEKS_S3_CSI_DriverRole` ``  to a different name, but if you do, make sure to change it in later steps too.
        
 ```shell
 aws iam create-role --role-name `AmazonEKS_S3_CSI_DriverRole` --assume-role-policy-document file://"`aws-s3-csi-driver-trust-policy`.json"
 ```
        
3.  Attach the previously created IAM policy to the role with the following command.
    
```shell
aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/`AmazonS3CSIDriverPolicy` --role-name `AmazonEKS_S3_CSI_DriverRole`
```
    
 **Note:** The IAM policy  `AmazonS3CSIDriverPolicy`  was created in the previous section.
    
4.  Skip this step if you're installing the driver as an Amazon EKS add-on. For self-managed installations of the driver, create Kubernetes service accounts that are annotated with the ARN of the IAM role that you created.
    
    1.  Save the following contents to a file named  `` `mountpoint-s3-service-account`.yaml``. Replace  `` `111122223333` ``  with your account ID.
        
        ```yaml
        ---
        apiVersion: v1
        kind: ServiceAccount
        metadata:
          labels:
            app.kubernetes.io/name: aws-mountpoint-s3-csi-driver
          name: mountpoint-s3-csi-controller-sa
          namespace: kube-system
          annotations:
            eks.amazonaws.com/role-arn: arn:aws:iam::`111122223333`:role/`AmazonEKS_S3_CSI_DriverRole` 
        ```
        
    2.  Create the Kubernetes service account on your cluster. The Kubernetes service account (`mountpoint-s3-csi-controller-sa`) is annotated with the IAM role that you created named  `` `AmazonEKS_S3_CSI_DriverRole` ``.
        
```shell
kubectl apply -f `mountpoint-s3-service-account`.yaml
```
        
**Note:** When you deploy the plugin in this procedure, it creates and is configured to use a service account named  `s3-csi-driver-sa`.


## Configuring Persistent Volume for Backups on S3
In this step, you will be creating a Persistent Volume that points to an S3 bucket:

This is the template to do this:

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

## Configure the values of the Helm chart
The values.yaml file of the helm chart contains the following options:
```yaml
global:
	namespace: "test-s3-backups"

mongodb:
	name: "mongodb-backup"
	mongouser: "root"
	secretName: "my-mongodb"
	host: "my-mongodb"
	port: "27017"
	schedule: "0 2 * * *"
	subPath: "backup/mongodb"
	restartPolicy: "OnFailure"

influxdb:
	name: "influxdb-backup"
	secretName: "my-influxdb"
	host: "my-influxdb"
	port: "8086"
	schedule: "0 2 * * *"
	subPath: "backup/influxdb"
	restartPolicy: "OnFailure"

backupPVC:
	claimName: "backup-s3-claim"
	volumeName: "backup-s3-pv"
	storageClassName: ""
	storage: "10Gi"
```
You will need to configure these values accordingly.

### For MongoDB

 - name: The name you want the cronjob to have
 - mongouser: The root user for mongodb
 - secretName: The name of the secret that contains mongo-root-password
 - host: The name of the mongodb service on kubernetes
 - port: The port of mongodb
 - schedule: When you want the cronjob to run
 - subPath: The path on the PV that the backups should be stored on

### For InfluxDB

 - name: The name you want the cronjob to have
 - secretName: The name of the secret that contains INFLUX_TOKEN
 - host: The name of the influxdb service on kubernetes
 - port: The port of influxdb
 - schedule: When you want the cronjob to run
 - subPath: The path on the PV that the backups should be stored on

### Backup PVC

 - claimName: The name you want the PVC to have
 - volumeName: The name of the PV that the PVC will be mounted on
 - storageClassName: If on S3, it should be left empty, otherwise it should be configured to the correct storageClassName
 - storage: How much storage you want you PVC to request
