#!/bin/sh
# Script to create a local AXACT controller with the dmtest tool

# ------- WEB SOCKET CONFIGURATION -----------------------------------------------------------------
./dmtest oa Device.LocalAgent.Controller.
INSTANCE=1
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Alias usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.EndpointID proto::usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Enable 1


# ------- MQTT CONFIGURATION -----------------------------------------------------------------------
./dmtest oa Device.LocalAgent.Controller.
INSTANCE=3
./dmtest oa Device.LocalAgent.Controller.${INSTANCE}.MTP.
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.MTP.1.Protocol MQTT
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.MTP.1.MQTT.Topic axact-agent-topic
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.MTP.1.Enable 1
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Enable 1

./dmtest oa Device.MQTT.Client.
INSTANCE=1
./dmtest sv Device.MQTT.Client.${INSTANCE}.BrokerAddress home.kempmobil.de
./dmtest sv Device.MQTT.Client.${INSTANCE}.BrokerPort 8883
./dmtest sv Device.MQTT.Client.${INSTANCE}.Username usp-demo
./dmtest sv Device.MQTT.Client.${INSTANCE}.Password secret
./dmtest sv Device.MQTT.Client.${INSTANCE}.Enable 1

