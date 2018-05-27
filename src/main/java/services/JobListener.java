package services;

import proto.hermes.FinishJobResult;
import proto.hermes.InitJobResult;
import proto.hermes.StartJobResult;
import roles.Client;

public interface JobListener {

    void onInit(Client client, InitJobResult result);

    void onStart(Client client, StartJobResult result);

    void onFinish(Client client, FinishJobResult result);
}
