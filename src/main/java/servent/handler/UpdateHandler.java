package servent.handler;


import app.AppConfig;
import mutex.DistributedMutex;
import mutex.MutexType;
import mutex.SuzukiTokenMutex;
import servent.message.Message;

public class UpdateHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiTokenMutex suzukiTokenMutex;

    public UpdateHandler(Message clientMessage, DistributedMutex tokenMutex) {
        this.clientMessage = clientMessage;
        if (AppConfig.MUTEX_TYPE == MutexType.SUZUKI_TOKEN) {
            this.suzukiTokenMutex = (SuzukiTokenMutex) tokenMutex;
        } else {
            AppConfig.timestampedErrorPrint("Handling token message in non-token mutex: " + AppConfig.MUTEX_TYPE);
        }
    }

    @Override
    public void run() {
        //aktiviramo update metodu i saljemo joj fajlove za update, i info o tome ko nam je poslao poruku
        suzukiTokenMutex.update(clientMessage.getUpdateFiles(),clientMessage.getOriginalSenderInfo());
    }
}
