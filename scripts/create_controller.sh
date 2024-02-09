

./dmtest oa Device.LocalAgent.Controller.

./dmtest gv Device.LocalAgent.Controller.

INSTANCE=1
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Alias usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.EndpointID proto::usp-demo
./dmtest sv Device.LocalAgent.Controller.${INSTANCE}.Enable 1

./dmtest oa Device.LocalAgent.Controller.${INSTANCE}.MTP.1.