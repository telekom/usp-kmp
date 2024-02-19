#!/bin/sh
# Script to create a local AXACT controller with the dmtest tool
./dmtest oa Device.LocalAgent.Controller.
INSTANCE=1
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Alias usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.EndpointID proto::usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Enable 1
