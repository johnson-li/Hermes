package services;

import proto.hermes.ServiceInfo;

public interface ServiceListener {

    void onRegister(ServiceInfo serviceInfo);
}
