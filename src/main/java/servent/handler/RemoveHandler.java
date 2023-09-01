package servent.handler;

import app.AppConfig;
import mutex.DistributedMutex;
import mutex.MutexType;
import mutex.SuzukiTokenMutex;
import servent.message.Message;

public class RemoveHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiTokenMutex suzukiTokenMutex;

    public RemoveHandler(Message clientMessage, DistributedMutex tokenMutex) {
        this.clientMessage = clientMessage;
        if (AppConfig.MUTEX_TYPE == MutexType.SUZUKI_TOKEN) {
            this.suzukiTokenMutex = (SuzukiTokenMutex) tokenMutex;
        } else {
            AppConfig.timestampedErrorPrint("Handling token message in non-token mutex: " + AppConfig.MUTEX_TYPE);
        }
    }

    @Override
    public void run() {
        for(String token: suzukiTokenMutex.getTokenWant()){ //prolazimo kroz listu onih koji traze token i brisemo onog kod koga je poslat token od strane nekog drugog serventa
            if(token.equals(String.valueOf(clientMessage.getId()))){
                suzukiTokenMutex.getTokenWant().remove();
            }
        }

    }
}
