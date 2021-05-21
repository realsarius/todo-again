package yapilacaklarListesi.muzik;

public class MuzikOynatici {

    private static OKMuzik okMuzik;
    private static CANCELMuzik cancelMuzik;
    private static DialogMuzik dialogMuzik;

    private MuzikOynatici(){
        this.okMuzik = new OKMuzik();
        this.cancelMuzik = new CANCELMuzik();
        this.dialogMuzik = new DialogMuzik();
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
