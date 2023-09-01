package servent.message;


import app.FileInfo;
import app.ServentInfo;

public class ReturnFile extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;
    public ReturnFile(ServentInfo sender, ServentInfo receiver, FileInfo fileInfo, String name, Integer id) {

        super(MessageType.RETURN_FILE_INFO, sender, receiver, fileInfo, name, id);
    }
}
