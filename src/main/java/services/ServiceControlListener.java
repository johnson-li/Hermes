package services;

import proto.hermes.InitServiceRequest;

public interface ServiceControlListener {

    void onInit(InitServiceRequest request);

    void onStart();

    void onStop();
}
