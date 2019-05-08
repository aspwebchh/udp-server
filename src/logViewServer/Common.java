package logViewServer;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Common {
    public static byte[] intToBytes(int value) {
        byte[] src = new byte[4];
        src[3] = (byte) ((value >> 24) & 0xFF);
        src[2] = (byte) ((value >> 16) & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    private static byte[] list2Array(List<Byte> list) {
        if (list == null || list.size() < 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }

    private static int num = 0;

    public static List<Pair<Integer, byte[]>> group(String sendContent) {
        List<Pair<Integer, List<Byte>>> result = new ArrayList<>();
        byte[] bytes = sendContent.getBytes();
        int dataLen = 1024 * 1;
        List<Byte> item = null;
        for (int i = 0; i < bytes.length; i++) {
            if (i % (dataLen - 4) == 0) {
                item = new ArrayList<>();
                if( num >= Integer.MAX_VALUE) {
                    num = 0;
                }
                num++;
                byte[] nBytes = intToBytes(num);
                item.add(nBytes[0]);
                item.add(nBytes[1]);
                item.add(nBytes[2]);
                item.add(nBytes[3]);
                result.add(Pair.with(num, item));
            }
            item.add(bytes[i]);
        }
        List<Pair<Integer, byte[]>> re = result.stream().map(pair -> Pair.with(pair.getValue0(), list2Array(pair.getValue1()))).collect(Collectors.toList());
        return re;
    }

    public static Pair<Integer, byte[]> readNumAndData(byte[] bytes) {
        byte[] nBytes = new byte[]{
                bytes[0],bytes[1],bytes[2],bytes[3]
        };
        int num = Common.bytesToInt(nBytes,0);
        byte[] content = new byte[bytes.length - 4];
        for( int j = 4; j < bytes.length; j++) {
            content[j - 4] = bytes[j];
        }
        return Pair.with(num, content);
    }
}
