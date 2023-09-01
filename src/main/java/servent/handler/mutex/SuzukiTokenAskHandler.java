package servent.handler.mutex;


import app.AppConfig;
import mutex.DistributedMutex;
import mutex.MutexType;
import mutex.SuzukiTokenMutex;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;


public class SuzukiTokenAskHandler implements MessageHandler {

    private Message clientMessage;
    private SuzukiTokenMutex suzukiTokenMutex;

    public SuzukiTokenAskHandler(Message clientMessage, DistributedMutex tokenMutex) {
        this.clientMessage = clientMessage;
        if (AppConfig.MUTEX_TYPE == MutexType.SUZUKI_TOKEN) {
            this.suzukiTokenMutex = (SuzukiTokenMutex) tokenMutex;
        } else {
            AppConfig.timestampedErrorPrint("Handling token message in non-token mutex: " + AppConfig.MUTEX_TYPE);
        }
    }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.SUZUKI_ASK_TOKEN) {


            for(String tokenWnt: clientMessage.getTokenWant()){
                System.out.println("Ovo je id koji dodajemo u nasu listu cekanja " + tokenWnt);
            }

            suzukiTokenMutex.getTokenWant().addAll(clientMessage.getTokenWant()); //dodajemo sve u nasu listu
            suzukiTokenMutex.unlock(); //radimo unlock
        } else {
            AppConfig.timestampedErrorPrint("Token handler for message: " + clientMessage);
        }
    }
}
