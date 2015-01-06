package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import pl.edu.pw.elka.tin.MNC.MNCAddress;
import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;

import java.io.*;

/**
 * Klasa repterentująca dane przesyłane poprzez siec
 * @author Karol
 */
public final class MNCDatagram implements Serializable {
    public final MNCAddress sender;
    public final MNCAddress receiver;
    public final String group;
    public final int datagramId;
    public final TYPE type;
    public final Object data;

    public MNCDatagram(MNCAddress sender, MNCAddress receiver, String group, TYPE type, Object data){
        this.sender = sender;
        this.receiver = receiver;
        this.group = group;
        this.type = type;
        this.data = data;
        this.datagramId = MNCConsts.rand.nextInt(MNCConsts.randRange);
    }

    public static byte[] toByteArray(MNCDatagram d) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(d);
        byte[] array = b.toByteArray();
        if(array.length > MNCConsts.MAX_UDP_PACKET_SIZE) {
            throw new IOException();
        }
        return array;
    }

    public static MNCDatagram toMNCDatagram(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return (MNCDatagram)o.readObject();
    }

    public MNCAddress getSender() {
        return sender;
    }

    public MNCAddress getReceiver() {
        return receiver;
    }

    public String getGroup() {
        return group;
    }

    public TYPE getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String toString(){
        if(type == TYPE.DATA_FRAGMENT)
            return "id:"+datagramId+" group:"+group+" from:"+sender.toString() + " to:"+receiver.toString() + " type:"+TYPE.getName(type) +":"+((MNCDeviceParameter)data).getParameterSetId()+":"+((MNCDeviceParameter)data).getText();
        return "id:"+datagramId+" group:"+group+" from:"+sender.toString() + " to:"+receiver.toString() + " type:"+TYPE.getName(type);
    }

    public static enum TYPE {
        //udp
        IS_THERE_TOKEN,
        I_HAVE_TMP_TOKEN,
        WHO_IN_GROUP,
        I_HAVE_TOKEN,
        DATA_FRAGMENT,
        //tcp
        IAM_IN_GROUP,
        GET_TOKEN,
        DATA_FULL,
        CONSUMPTION_CONFIRMATION;

        public static String getName(TYPE t){
            switch(t){
                case IS_THERE_TOKEN: return "IS_THERE_TOKEN";
                case I_HAVE_TOKEN: return "I_HAVE_TOKEN";
                case IAM_IN_GROUP: return "IAM_IN_GROUP";
                case I_HAVE_TMP_TOKEN: return "I_HAVE_TMP_TOKEN";
                case WHO_IN_GROUP: return "WHO_IN_GROUP";
                case DATA_FULL: return "DATA_FULL";
                case GET_TOKEN: return "GET_TOKEN";
                case CONSUMPTION_CONFIRMATION: return "CONSUMPTION_CONFIRMATION";
                case DATA_FRAGMENT: return "DATA_FRAGMENT";
            }
            return null;
        }
    }
}
