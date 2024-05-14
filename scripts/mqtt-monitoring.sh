#!/bin/sh
USER=usp-demo
PASSWD=`cat ../mqtt-passwd`
CLIENT_ID=monitoring
HOST=home.kempmobil.de
PORT=1883

mosquitto_sub -v -F 'Topic:         %t\nPayload:       %p\nPayload (hex): %x' -i ${CLIENT_ID} -h ${HOST} -p ${PORT} -u ${USER} -P ${PASSWD} \
    -t '$SYS/broker/clients/connected' \
    -t 'proto::usp-mqtt-demo' \
    -t 'os::00040E-B0F20840FD2C'
