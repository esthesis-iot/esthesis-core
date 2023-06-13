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
