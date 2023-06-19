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

## How to set static IP Ubuntu Server 23.04
1. Remove all adapters
2. Reboot the vm
3. Add 1 adapter of type bridge
4. Reboot the vm
5. Install network utilities in the vm
	```
	sudo apt install net-tools
	```
6. Access the  configuration file
	``` 
	cd /etc/netplan 
	```
7. List the content
	``` 
	ls
	```
8. Access the configuration file
	```
	sudo nano 00-installer-config-yaml 
	```
9. Enter the addresses, routes and nameservers. Check Use space and not tab.
	```                                                                          /etc/netplan/00-installer-config.yaml
	# This is the network config written by 'subiquity'
	network:
	 ethernets:
		enp0s3:
		  dhcp4: false
		  addresses: [192.168.2.12/24]
		  routes:
		   - to: default
			 via: 192.168.2.1
		  nameservers:
		   addresses: [8.8.8.8,8.8.4.4]
	 version: 2
	```
Mind that, the addresses you see in the file do not match any environment, replace them with the ones on your environment:
For addresses, you can type anything like 192.168.2.x
For gateway, you need the IP of your modem (it can be something like 192.168.2.1 - check your environment).
For nameservers, use the ones in the file
10. Apply the changes with CTRL + O and exit the editor with CTRL + X
11. Update the configuration
```
sudo netplan apply
```
If any error exists in the configuration file the errors will be displayed here. Correct them and run again the command.
12. See the current IP
```
ip addr
```
13. Connect to vm by using the IP address from command line or any ssh client tool.