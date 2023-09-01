package servent.message.mutex;

import app.FileInfo;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;


import java.util.Queue;

public class TokenAskMessage extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;
    public TokenAskMessage(ServentInfo sender, ServentInfo receiver, Queue<String> tokenWant, String name, FileInfo fileInfo, Integer id) {


        super(MessageType.SUZUKI_ASK_TOKEN, sender, receiver,tokenWant, id, fileInfo, name);

    }
}
