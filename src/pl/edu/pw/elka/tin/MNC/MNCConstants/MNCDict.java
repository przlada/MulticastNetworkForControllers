package pl.edu.pw.elka.tin.MNC.MNCConstants;

import java.util.Hashtable;

/**
 * Klasa przechowująca wszelkie słowniki wykorzystywane w projekcie.
 * @author Karol
 */
public class MNCDict {
    private static Hashtable<Langs, Hashtable<String, String>> Dicts;

    public static String getLangText(Langs lang, String textKey){
        String val = Dicts.get(lang).get(textKey);
        if(val == null)
            val = textKey;
        return val;
    }
    public static enum Langs{
        PL
    }
    static {
        Dicts = new Hashtable<Langs, Hashtable<String, String>>();
        Hashtable<String, String> PLDict = new Hashtable<String, String>();
        PLDict.put("MNCName", "System komunikacji rozgłoszeniowej dla grup sterowników");
        PLDict.put("ControllerStarted", "Uruchomiono sterownik MNC:");
        PLDict.put("NoNameController", "brak nazwy");
        PLDict.put("HaveNewTokenOwner", "Ustalono adres sterownika z tokenem grupy:");
        PLDict.put("SendByMulticast", "Ustalono adres sterownika z tokenem grupy:");


        Dicts.put(Langs.PL, PLDict);
    }
}
