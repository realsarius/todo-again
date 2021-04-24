package yapilacaklarListesi.muzik;

public class MuzikOynatici {

    private static OKMuzik okMuzik = new OKMuzik();
    private static CANCELMuzik cancelMuzik = new CANCELMuzik();
    private static DialogMuzik dialogMuzik = new DialogMuzik();

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
