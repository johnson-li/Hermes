package services;

import proto.hermes.FinishJobResult;
import proto.hermes.InitJobResult;
import proto.hermes.StartJobResult;
import roles.Client;

public interface JobListener {

    default void onInit(Client client, InitJobResult result) {
    }

    default void onStart(Client client, StartJobResult result) {
    }

    default void onFinish(Client client, FinishJobResult result) {
    }
}
