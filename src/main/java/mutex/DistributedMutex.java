package mutex;



import app.FileInfo;
import app.ServentInfo;

import java.util.Map;
import java.util.Set;

public interface DistributedMutex {

    Object lock(Boolean flag,String path);
    void unlock();
    void update(Map<Integer,Set<FileInfo>>  updateFiles, ServentInfo serventInfo);

    void get(String name);

    void findFile(String name,int id);

    void gotFile(FileInfo fileInfo,int id);
}
