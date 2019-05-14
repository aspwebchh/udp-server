package logViewServer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogContentUtil {
    static class FileNode {
        public String name = "";
        public List<FileNode> children = new ArrayList<>();
        public boolean isFile;
        public String lastModifyTime;
        public long size;

        public String toString() {
            return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
        }
    }

    private static final String LOG_PATH = LogViewServer.config.getLogPath();

    public static String getPaths() {
        FileNode fileNode = getFileNodes(LOG_PATH);
        return fileNode.toString();
    }

    private static FileNode getFileNodes(String path) {
        File file = new File(path);
        FileNode fileNode = new FileNode();
        fileNode.name = file.getName();
        fileNode.lastModifyTime = Common.unixTimestamp2StrTime(file.lastModified());
        fileNode.size = file.length();
        if (file.isDirectory()) {
            fileNode.isFile = false;
            for (File item : file.listFiles()) {
                fileNode.children.add(getFileNodes(item.getPath()));
            }
        } else {
            fileNode.isFile = true;
        }
        return fileNode;
    }

    public static String getFileContent(String filePath) {
        String fullPath = LOG_PATH + "/" + filePath;
        return Common.getFileContent(fullPath);
    }
}
