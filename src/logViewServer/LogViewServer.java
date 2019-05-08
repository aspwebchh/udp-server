package logViewServer;

import org.javatuples.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

public class LogViewServer {
    private static final int SERVER_PORT = 33000;
    private static final int PACKET_LENGTH = 1024;


    private static String getSendContent() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 49999999; i++) {
            content.append(i);
            //content.append("\n");
        }
        return content.toString();
    }


    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[PACKET_LENGTH];
        DatagramSocket ds = new DatagramSocket(SERVER_PORT);
        //ds.setSoTimeout(500);
        DatagramPacket dp_receive = new DatagramPacket(buf, buf.length);
        ConcurrentHashMap<Integer,byte[]> group = Common.group(getSendContent());
        while (true) {
            dp_receive.setLength(PACKET_LENGTH);
            ds.receive(dp_receive);
            InetAddress clientAddr = dp_receive.getAddress();
            int clientPort = dp_receive.getPort();

            byte[] data = dp_receive.getData();
            Pair<Integer, byte[]> dataWithNum = Common.readNumAndData(data);
            int cmd = dataWithNum.getValue0();
            //byte[] bytes = dataWithNum.getValue1();
            if (cmd == Cmd.READ_PATH) {
                new Thread(() -> {
                    handleReadPath(ds, clientAddr, clientPort, group);
                }).start();
            } else {
                int ackNum = cmd;
                group.remove(ackNum);
                System.out.println("确认成功，ackNum=" + ackNum);
            }
        }
    }

    private static void handleReadPath(DatagramSocket datagramSocket, InetAddress clientAddr, int clientPort,  ConcurrentHashMap<Integer,byte[]> group) {
        while ( group.size() > 0 ) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            group.forEach((num, data) ->{
                DatagramPacket dp_send = new DatagramPacket(data, data.length, clientAddr, clientPort);
                try {
                    datagramSocket.send(dp_send);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
