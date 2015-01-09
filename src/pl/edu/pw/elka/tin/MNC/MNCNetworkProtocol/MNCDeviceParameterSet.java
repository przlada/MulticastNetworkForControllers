package pl.edu.pw.elka.tin.MNC.MNCNetworkProtocol;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCConsts;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Klasa reprezentująca cały zestaw parametrów danego sterownika
 * @author Maciek
 */
public class MNCDeviceParameterSet implements Serializable{
    private int parameterSetID;
    private MNCDeviceParameter[] parameters;

    private String group;

    public MNCDeviceParameterSet(String group){
        this.group = group;
        parameterSetID = 0;
        parameters = new MNCDeviceParameter[MNCConsts.PARAMETER_SET_SIZE];
    }
    public MNCDeviceParameterSet(String group, Hashtable<Integer, MNCDeviceParameter> table){
        this(group);
        int i=0;
        for(MNCDeviceParameter param : table.values()){
            parameters[i] = param;
            if(i ==0)
                parameterSetID =  param.getParameterSetId();
            i++;
            if(i>=MNCConsts.PARAMETER_SET_SIZE)
                break;
        }
    }

    public String getGroup() {
        return group;
    }
    public void setGroup(String group){
        this.group = group;
    }

    public int getParameterSetID(){
        return parameterSetID;
    }

    public void setParameterSetID(int id){
        parameterSetID = id;
        for(MNCDeviceParameter param : parameters){
            if(param != null)
                param.setParameterSetId(id);
        }
    }

    public MNCDeviceParameter[] getParameters(){
        return parameters;
    }
    public void populateSet(){
        for(int i=0; i<MNCConsts.PARAMETER_SET_SIZE; i++){
            parameters[i] = new MNCDeviceParameter(i,MNCDeviceParameter.TYPE.TEXT,"parametr"+i);
        }
    }

}
