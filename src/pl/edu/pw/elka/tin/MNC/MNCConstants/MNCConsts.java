package pl.edu.pw.elka.tin.MNC.MNCConstants;

import pl.edu.pw.elka.tin.MNC.MNCAddress;

import java.util.Random;

/**
 * Zbiór wszystkich stałych wykorzysywanych w projekcie
 * @author Maciek
 */
public interface MNCConsts {
    public static final String LOCAL_LINE_SEPARATOR = System.getProperty("line.separator");

    public static final MNCAddress MULTICAST_ADDR = new MNCAddress("ff02::1:1:1", MNCAddress.TYPE.MULTICAST_GROUP);
    public static final int MCAST_PORT = 4446;
    public static final int MAX_UDP_PACKET_SIZE = 1400;

    public static final int WAIT_FOR_TOKEN_TIMEOUT = 5000;
    public static final int WAIT_FOR_TMP_TOKEN = 5000;

    public static final Random rand = new Random(100);
    public static final int randRange = 1000;
}
