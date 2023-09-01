package servent.message;


import app.FileInfo;
import app.ServentInfo;

import java.util.Map;
import java.util.Set;


public class UpdateMessage extends BasicMessage {

    private static final long serialVersionUID = 2084490973699262440L;


    public UpdateMessage(ServentInfo sender, ServentInfo receiver, Map<Integer, Set<FileInfo>> updateFiles, String name, FileInfo fileInfo, Integer id) {

        super(MessageType.UPDATE, sender, receiver,updateFiles, id, fileInfo, name);
    }
}
