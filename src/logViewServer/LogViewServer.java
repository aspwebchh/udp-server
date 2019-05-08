package logViewServer;

import org.javatuples.Pair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class LogViewServer {
    private static final int SERVER_PORT = 33000;

    public static void main(String[] args) throws IOException {
        byte[] buf = new byte[1024 * 1];
        DatagramSocket ds = new DatagramSocket(SERVER_PORT);
        DatagramPacket dp_receive = new DatagramPacket(buf, buf.length);
        while(true){
            ds.receive(dp_receive);
            byte[] data = dp_receive.getData();
            Pair<Integer,byte[]> dataWithNum = Common.readNumAndData(data);
            System.out.println(dataWithNum.getValue0() + "-" + new String(dataWithNum.getValue1(), 0,  dp_receive.getLength() - 4));

            byte[] ackPacketData = Common.intToBytes( dataWithNum.getValue0() );

            DatagramPacket dp_send= new DatagramPacket(ackPacketData,ackPacketData.length,dp_receive.getAddress(),dp_receive.getPort());
            ds.send(dp_send);
            dp_receive.setLength(1024 * 1);
        }
    }
}
