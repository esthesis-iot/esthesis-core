# Frequently Asked Quesstions

## I am using VirtualBox and the IP address my VM gets is not accessible from my machine
By default, VirtualBox creates a NATed VM. Switch your VM's network to 'Bridged':
![](/img/docs/dev-guide/vbox-bridged.png)

## When I change APISIX routes and I sync them with Helmfile, the routes do not change.
APISIX routes are implemented as CRDs and Helm [does not support updating or deleting CRDs](https://helm.sh/docs/chart_best_practices/custom_resource_definitions/). To update an existing CRD you need to
first manually delete it and `helmfile sync` again.

## On my dev cluster I get "Too many open files" when I try to follow logs.
1. SSH to your dev cluster.
2. `sudo vi /etc/sysctl.conf`
3. Add the following lines:
	```
	fs.inotify.max_user_instances = 131072
	fs.inotify.max_user_watches = 131072
	```
4. Reboot your dev cluster

## How to set a static IP in Ubuntu Server 23.04
1. Edit networking configuration:
	```
	sudo nano /etc/netplan/00-installer-config-yaml
	```
2. Match the IP address as well as the gateway and nameservers to your environment:
	```
	# This is the network config written by 'subiquity'
	network:
	 ethernets:
		eth0:
		  dhcp4: false
		  addresses: [192.168.2.12/24]
		  routes:
		   - to: default
			 via: 192.168.2.1
		  nameservers:
		   addresses: [8.8.8.8,8.8.4.4]
	 version: 2
	```
3. Update the configuration
	```
	sudo netplan apply
	```
4. Verify you got the IP defined above:
	```
	ip addr
	```

## How to connect IntelliJ to MongoDB
![](/img/docs/dev-guide/Data_Sources_and_Drivers.png)

## How to connect to MongoDB from MongoDB Compass
![](/img/docs/dev-guide/compass1.png)
![](/img/docs/dev-guide/compass2.png)

## How to create a token in InfluxDB
1. Open the InfluxDB UI, e.g. http://influxdb.esthesis:8086.
2. Switch to the organisiation you want to create the token for by clicking on the organisation name
in the top left corner.
3. Hover on the sidebar icon depicting an arrow pointing up.
4. Select 'API Tokens'.
5. Click on 'Generate API Token' button and choose "All Access API Token".
6. Give a description.
7. Copy the token and save it somewhere safe.
