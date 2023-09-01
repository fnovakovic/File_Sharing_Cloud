package servent.handler;


import app.AppConfig;
import mutex.DistributedMutex;
import mutex.MutexType;
import mutex.SuzukiTokenMutex;
import servent.message.Message;

public class ReturnFileHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiTokenMutex suzukiTokenMutex;

    public ReturnFileHandler(Message clientMessage, DistributedMutex tokenMutex) {
        this.clientMessage = clientMessage;
        if (AppConfig.MUTEX_TYPE == MutexType.SUZUKI_TOKEN) {
            this.suzukiTokenMutex = (SuzukiTokenMutex) tokenMutex;
        } else {
            AppConfig.timestampedErrorPrint("Handling token message in non-token mutex: " + AppConfig.MUTEX_TYPE);
        }
    }

    @Override
    public void run() {
        suzukiTokenMutex.gotFile(clientMessage.getFileInfo(),clientMessage.getOriginalSenderInfo().getId());
    }
}