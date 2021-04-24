package yapilacaklarListesi.zamanlayicilar;

public class InitialState extends TaskState {

    public void run() {
        System.out.println( "starting...");
    }
    public TaskState next() {
        return new FinalState();
    }
}
