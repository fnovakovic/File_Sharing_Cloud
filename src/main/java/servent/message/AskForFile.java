package servent.message;


import app.FileInfo;
import app.ServentInfo;

public class AskForFile extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;
    public AskForFile(ServentInfo sender, ServentInfo receiver, String name, FileInfo fileInfo, Integer id) {

        super(MessageType.ASK_FOR_FILE, sender, receiver, fileInfo,name, id);
    }
}