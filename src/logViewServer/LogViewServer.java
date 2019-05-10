package logViewServer;

import org.javatuples.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class LogViewServer {
    public static final Config config =  Config.instance();

    private static final int MAX_RE_SEND_COUNT = 10;

    private static final int SERVER_PORT = config.getServerPort();
    private static final int PACKET_LENGTH = 1024;

    private static volatile boolean statusReadPath = false;
    private static volatile boolean statusReadFileContent = false;

    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[PACKET_LENGTH];
        DatagramSocket ds = new DatagramSocket(SERVER_PORT);
        DatagramPacket dp_receive = new DatagramPacket(buf, buf.length);
        ConcurrentHashMap<Integer,byte[]> group = null;
        while (true) {
            dp_receive.setLength(PACKET_LENGTH);
            ds.receive(dp_receive);
            InetAddress clientAddr = dp_receive.getAddress();
            int clientPort = dp_receive.getPort();

            byte[] data = dp_receive.getData();
            Pair<Integer, byte[]> dataWithNum = Common.readNumAndData(data, dp_receive.getLength());
            int cmd = dataWithNum.getValue0();
            if (cmd == Cmd.READ_PATH) {
                group = Common.group(LogContentUtil.getPaths());
                ConcurrentHashMap<Integer,byte[]> group1 = group;
                statusReadPath = true;
                new Thread(() -> {
                    handleReadPath(ds, clientAddr, clientPort, group1);
                }).start();
            } else if(cmd == Cmd.READ_FILE_CONTENT) {
                byte[] content = dataWithNum.getValue1();
                String path = new String(content);
                group = Common.group(LogContentUtil.getFileContent(path));
                ConcurrentHashMap<Integer,byte[]> group1 = group;
                statusReadFileContent = true;
                new Thread(() -> {
                    handleReadFileContent(ds, clientAddr, clientPort, group1);
                }).start();
            } else {
                int ackNum = cmd;
                group.remove(ackNum);
                //System.out.println("确认成功，ackNum=" + ackNum);
                if( statusReadPath && ackNum == Cmd.READ_PATH_FINISH ) {
                    statusReadPath = false;
                    System.out.println("目录传输完毕,st=" + System.currentTimeMillis());
                } else if( statusReadFileContent && ackNum == Cmd.READ_FILE_CONTENT_FINISH) {
                    statusReadFileContent = false;
                    System.out.println("文件传输完毕,st=" + System.currentTimeMillis());
                }
            }
        }
    }


    private static void handle(DatagramSocket datagramSocket, InetAddress clientAddr, int clientPort,  ConcurrentHashMap<Integer,byte[]> group) {
        int count = 0;
        while ( group.size() > 0 && count < MAX_RE_SEND_COUNT ) {
            group.forEach((num, data) ->{
                DatagramPacket dp_send = new DatagramPacket(data, data.length, clientAddr, clientPort);
                try {
                    datagramSocket.send(dp_send);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                count++;
            }
        }
        System.out.println("handle.count=" + count + ",st=" + System.currentTimeMillis());
    }

    private static void handleReadFileContent(DatagramSocket datagramSocket, InetAddress clientAddr, int clientPort,  ConcurrentHashMap<Integer,byte[]> group) {
        handle(datagramSocket, clientAddr, clientPort, group);

        int count = 0;
        while (statusReadFileContent && count < MAX_RE_SEND_COUNT) {
            byte[] finishCmd = Common.intToBytes(Cmd.READ_FILE_CONTENT_FINISH);
            DatagramPacket finishCmdDp = new DatagramPacket(finishCmd, finishCmd.length, clientAddr, clientPort);
            try {
                datagramSocket.send(finishCmdDp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
                if( !statusReadFileContent ) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                count++;
            }
        }
        System.out.println("handleReadFileContent.count=" + count + ",st=" + System.currentTimeMillis());
    }


    private static void handleReadPath(DatagramSocket datagramSocket, InetAddress clientAddr, int clientPort,  ConcurrentHashMap<Integer,byte[]> group) {
        handle(datagramSocket, clientAddr, clientPort, group);

        int count = 0;
        while (statusReadPath && count < MAX_RE_SEND_COUNT) {
            byte[] finishCmd = Common.intToBytes(Cmd.READ_PATH_FINISH);
            DatagramPacket finishCmdDp = new DatagramPacket(finishCmd, finishCmd.length, clientAddr, clientPort);
            try {
                datagramSocket.send(finishCmdDp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(500);
                if( !statusReadPath ) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                count++;
            }
        }
        System.out.println("handleReadPath.count=" + count + ",st=" + System.currentTimeMillis());
    }
}
