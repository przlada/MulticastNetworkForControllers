package pl.edu.pw.elka.tin.MNC;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Obietk reprezentujący adress sterownika (lub monitora). Jest również używana przy to porównania priotytetu.
 * @author Przemek
 */
public class MNCAddress implements Serializable, Comparable<MNCAddress>{
    private String address;
    private TYPE type;

    public MNCAddress(String val, TYPE t) {
        address = val;
        type = t;
    }

    @Override
    public int compareTo(MNCAddress other) {
        return address.compareTo(other.address);
    }

    public InetAddress getJavaAddress() throws UnknownHostException {
        return InetAddress.getByName(address);
    }

    public TYPE getType(){
        return type;
    }

    public String toString(){
        if(type == TYPE.MULTICAST_GROUP)
            return address.substring(0,4);
        return address.substring(address.length()-5);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (!address.equals(((MNCAddress)obj).address))
            return false;
        return true;
    }

    public static enum TYPE {
        MONITOR,
        CONTROLLER,
        MULTICAST_GROUP
    }
}
