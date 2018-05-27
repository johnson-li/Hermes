package services;

import proto.hermes.InitJobResult;

public interface JobListener {

    void onInit(InitJobResult result);
}
