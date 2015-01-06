package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import java.io.Serializable;

/**
 * Obiekt reprezentujący pojedynczy parametr rozsyłany przez sterownik
 * @author Karol
 */
public class MNCDeviceParameter implements Serializable {
    private TYPE type;
    private Object value;
    private int index;
    private int parameterSetId;
    public MNCDeviceParameter(int index, TYPE t, Object o){
        this.index = index;
        type = t;
        value = o;
    }

    public String getText(){
        return (String) value;
    }

    public int getIndex() {
        return index;
    }

    public int getParameterSetId() {
        return parameterSetId;
    }

    public void setParameterSetId(int id){
        parameterSetId = id;
    }

    public int getNumber() {
        return (Integer) value;
    }
    public static enum TYPE{
        TEXT,NUMBER,BINARY,LOGIC
    }
}
