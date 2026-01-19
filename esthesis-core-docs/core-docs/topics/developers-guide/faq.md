# Frequently Asked Questions

<deflist collapsible="true">
 	<def title="I am using VirtualBox and the IP address my VM gets is not accessible from my machine">
        By default, VirtualBox creates a NATed VM. Switch your VM's network to 'Bridged':<br/>
		<img src="vbox-bridged.png" alt="VirtualBox networking"/>
    </def>
	<def title="On my dev cluster I get 'Too many open files' when I try to follow logs.">
		<ol>
			<li>SSH to your dev cluster.</li>
			<li>`sudo vi /etc/sysctl.conf`</li>
			<li>Add the following lines:
				<code-block>
					fs.inotify.max_user_instances = 131072
					fs.inotify.max_user_watches = 131072
				</code-block>
			</li>
			<li>Reboot your dev cluster</li>
		</ol>
	</def>
	<def title="How to set a static IP in Ubuntu Server 23.04">
		<ol>
			<li>
				Edit networking configuration:
				<code-block>
				sudo nano /etc/netplan/00-installer-config-yaml
				</code-block>
			</li>
			<li>
				Match the IP address as well as the gateway and nameservers to your environment:
				<code-block>
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
				</code-block>
			</li>
			<li>
				Update the configuration
				<code-block>
				sudo netplan apply
				</code-block>
			</li>
			<li>
				Verify you got the IP defined above:
				<code-block>
				ip addr
				</code-block>
			</li>
		</ol>
	</def>
	<def title="How to connect IntelliJ to MongoDB">
		<img src="Data_Sources_and_Drivers.png" alt="IntelliJ MongodB"/>
	</def>
	<def title="How to connect to MongoDB from MongoDB Compass">
		<img src="compass1.png" alt="MongoDB Compass"/>
		<img src="compass2.png" alt="MongoDB Compass"/>
	</def>
	<def title="How to create a token in InfluxDB">
		<ol>
			<li>Open the InfluxDB UI, e.g. http://influxdb.esthesis:80.</li>
			<li>Switch to the organisiation you want to create the token for by clicking on the organisation name
			in the top left corner.</li>
			<li>Hover on the sidebar icon depicting an arrow pointing up.</li>
			<li>Select 'API Tokens'.</li>
			<li>Click on 'Generate API Token' button and choose "All Access API Token".</li>
			<li>Give a description.</li>
			<li>Copy the token and save it somewhere safe.</li>
		</ol>
	</def>
</deflist>
