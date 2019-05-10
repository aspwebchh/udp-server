package logViewServer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;


public class Config {
    private String logPath;
    private int serverPort;

    public int getServerPort() {
        return serverPort;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }

    public static Config instance() {
        String content = Common.getFileContent("config.json");
        JSONObject jsonObject = JSONObject.parseObject(content);
        String logPath = jsonObject.getString("logPath");
        int serverPort = jsonObject.getInteger("serverPort");

        Config config = new Config();
        config.setLogPath(logPath);
        config.setServerPort(serverPort);
        return config;
    }
}
