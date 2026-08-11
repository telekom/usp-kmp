#!/bin/sh
#
# SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
#
# SPDX-License-Identifier: Apache-2.0
#

# Script to create a local AXACT controller with the dmtest tool

rm ./prefix/var/lib/axact/db.xml
PASSWD=`cat ../usp-library/mqtt-passwd`

# Send a test message to the server (read from stdin)
# mosquitto_pub -h home.kempmobil.de -p 8883 -u usp-demo -P $PASSWD -t test.topic -s

# ------- WEB SOCKET CONFIGURATION -----------------------------------------------------------------
INSTANCE=`./dmtest oa Device.LocalAgent.Controller. | grep 'Instance .* was created for object' | egrep -o '[0-9]{1,4}' | head -1`
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Alias usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.EndpointID proto::usp-ws-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Enable 1
# Disable also encryption for testing purposes:
./dmtest sv Device.LocalAgent.MTP.${INSTANCE}.WebSocket.EnableEncryption 0


# ------- MQTT CONFIGURATION -----------------------------------------------------------------------
CLIENT=`./dmtest oa Device.MQTT.Client. | grep 'Instance .* was created for object' | egrep -o '[0-9]{1,4}' | head -1`
./dmtest sv Device.MQTT.Client.${CLIENT}.BrokerAddress home.kempmobil.de
./dmtest sv Device.MQTT.Client.${CLIENT}.BrokerPort 1883
./dmtest sv Device.MQTT.Client.${CLIENT}.Username usp-demo
./dmtest sv Device.MQTT.Client.${CLIENT}.Password ${PASSWD}
./dmtest sv Device.MQTT.Client.${CLIENT}.ProtocolVersion 5.0
./dmtest sv Device.MQTT.Client.${CLIENT}.ClientID axact-demo
./dmtest sv Device.MQTT.Client.${CLIENT}.Enable 1
SUBS=`./dmtest oa Device.MQTT.Client.${CLIENT}.Subscription. | grep 'Instance .* was created for object' | egrep -o '[0-9]{1,4}' | head -1`
./dmtest sv Device.MQTT.Client.${CLIENT}.Subscription.${SUBS}.Topic usp/agents/proto::AXACT
./dmtest sv Device.MQTT.Client.${CLIENT}.Subscription.${SUBS}.Enable 1

# Make sure to NOT append a '.' at the end of the reference (i.e. after ${CLIENT})!
./dmtest sv Device.LocalAgent.MTP.2.MQTT.Reference Device.MQTT.Client.${CLIENT}
./dmtest sv Device.LocalAgent.MTP.2.MQTT.ResponseTopicConfigured usp/agents/proto::AXACT

CTRL=`./dmtest oa Device.LocalAgent.Controller. | grep 'Instance .* was created for object' | egrep -o '[0-9]{1,4}' | head -1`
./dmtest sv Device.LocalAgent.Controller.${CTRL}.Enable 1
./dmtest sv Device.LocalAgent.Controller.${CTRL}.EndpointID proto::usp-mqtt-demo
MTP=`./dmtest oa Device.LocalAgent.Controller.${CTRL}.MTP. | grep 'Instance .* was created for object' | egrep -o '[0-9]{1,4}' | head -1`
./dmtest sv Device.LocalAgent.Controller.${CTRL}.MTP.${MTP}.Protocol MQTT
./dmtest sv Device.LocalAgent.Controller.${CTRL}.MTP.${MTP}.Enable 1
./dmtest sv Device.LocalAgent.Controller.${CTRL}.MTP.${MTP}.MQTT.Reference Device.MQTT.Client.${CLIENT}
# Important: this defines the topic the agent will reply to as the responseInfo value of the MQTT publish
# paket is ignored by this agent!
./dmtest sv Device.LocalAgent.Controller.${CTRL}.MTP.${MTP}.MQTT.Topic usp/controllers/proto::usp-mqtt-demo


