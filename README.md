# Discord Temperature [![Build Status](https://travis-ci.org/KennethWussmann/discord-temperature.svg?branch=master)](https://travis-ci.org/KennethWussmann/discord-temperature)
Show your room temperature as your Discord status.

![](https://i.imgur.com/oVRzFr6.png)

# Features
* Philips Hue Motion Sensor
* Netatmo Healthy Home Coach (Also supports humidity, noise and CO2)
* Show data as Discord Status or via Discord rich presence

# Download
Download the latest version as JAR or WAR from the [releases](https://github.com/KennethWussmann/discord-temperature/releases).

# Configuration
Download the [default configuration](https://github.com/KennethWussmann/discord-temperature/blob/master/src/main/resources/application.yml) and put it in the same folder as the JAR.
Setup modules to your likings. Be sure that you can only have one `broadcast`, `temperature` & `publish` source.
Restart application and may follow instructions in console.
