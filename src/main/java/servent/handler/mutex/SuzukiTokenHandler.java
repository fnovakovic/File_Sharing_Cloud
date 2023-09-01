package servent.handler.mutex;

import app.AppConfig;
import mutex.DistributedMutex;
import mutex.MutexType;
import mutex.SuzukiTokenMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;


public class SuzukiTokenHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiTokenMutex suzukiTokenMutex;

    public SuzukiTokenHandler(Message clientMessage, DistributedMutex tokenMutex) {
        this.clientMessage = clientMessage;
        if (AppConfig.MUTEX_TYPE == MutexType.SUZUKI_TOKEN) {
            this.suzukiTokenMutex = (SuzukiTokenMutex) tokenMutex;
        } else {
            AppConfig.timestampedErrorPrint("Handling token message in non-token mutex: " + AppConfig.MUTEX_TYPE);
        }
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SUZUKI_TOKEN) {
            AppConfig.timestampedStandardPrint("Recieved token");
            suzukiTokenMutex.receiveToken(clientMessage.getTokenWant());

        } else {
            AppConfig.timestampedErrorPrint("Token handler for message: " + clientMessage);
        }
    }
}
