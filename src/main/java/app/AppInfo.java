package app;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppInfo {
    private String ipAddress;
    private int id;

    private int port;

    private List<Integer> neightbors;

    private Map<Integer, Set<FileInfo>> files = new ConcurrentHashMap<>();

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<Integer> getNeightbors() {
        return neightbors;
    }

    public void setNeightbors(List<Integer> neightbors) {
        this.neightbors = neightbors;
    }

    public Map<Integer, Set<FileInfo>> getFiles() {
        return files;
    }

    public void setFiles(Map<Integer, Set<FileInfo>> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "System{" +
                "ipAddress='" + ipAddress + '\'' +
                ", id=" + id +
                ", port='" + port + '\'' +
                ", neightbors=" + neightbors +
                ", files=" + files +
                '}';
    }

    public void setAppInfo(AppInfo appInfo){
        this.id = appInfo.getId();
        this.port = appInfo.getPort();
        this.files = appInfo.getFiles();
        this.ipAddress = appInfo.getIpAddress();
        this.neightbors = appInfo.getNeightbors();
    }
}
