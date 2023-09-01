package servent.message;


import app.FileInfo;
import app.ServentInfo;

public class RemoveWantTokenMessage extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;
    public RemoveWantTokenMessage(ServentInfo sender, ServentInfo receiver, FileInfo fileInfo, String name, Integer id) {

        super(MessageType.REMOVE, sender, receiver, fileInfo, name, id);
    }
}
