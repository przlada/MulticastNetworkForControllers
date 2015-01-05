package pl.edu.pw.elka.tin.MNC;

import pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict;

import java.sql.Timestamp;
import java.util.Date;

import static pl.edu.pw.elka.tin.MNC.MNCConstants.MNCDict.*;

/**
 * Klasa odpowiedzialna za odbieranie i wyświetlanie wszelkich zdarzeń.
 * @author Paweł
 */
public class MNCSystemLog {
    private Langs lang;
    private String controllerName = null;

    public MNCSystemLog(Langs l){
        this(l,getLangText(l,"NoNameController"));
    }
    public MNCSystemLog(Langs l, String controllerName){
        setLang(l);
        this.controllerName = controllerName;

        print(getLangText(lang,"ControllerStarted") + controllerName);
    }

    public synchronized void setLang(Langs l){
        lang = l;
    }
    public synchronized Langs getLang(){
        return lang;
    }

    private void print(String text){
        Date date= new Date();
        System.out.println(new Timestamp(date.getTime()) + " " + controllerName+ " " + text);
    }

    public void acction(String type){
        print(type);
    }

}
