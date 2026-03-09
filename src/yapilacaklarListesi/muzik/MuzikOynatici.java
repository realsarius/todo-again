package yapilacaklarListesi.muzik;

public class MuzikOynatici {

    private static final OKMuzik okMuzik = new OKMuzik();
    private static final CANCELMuzik cancelMuzik = new CANCELMuzik();
    private static final DialogMuzik dialogMuzik = new DialogMuzik();

    private MuzikOynatici(){
        // Utility class
    }

    public static void dialogMuzikOynat(){
        dialogMuzik.oynat();
    }

    public static void okMuzigiOynat(){
        okMuzik.oynat();
    }

    public static void cancelMuzigiOynat(){
        cancelMuzik.oynat();
    }

}
