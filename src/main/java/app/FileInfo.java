package app;

import java.io.Serial;
import java.io.Serializable;

public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 463426265374700139L;

    private final String path;
    private final String name;
    private final String content;

    public FileInfo(String path, String content,String name) {
        this.path = path;
        this.content = content;
        this.name = name;
    }




    public String getPath() { return path; }



    public String getContent() { return content; }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof FileInfo)
            return o.hashCode() == this.hashCode();

        return false;

    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
