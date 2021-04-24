package yapilacaklarListesi.zamanlayicilar;

public class FinalState extends TaskState{
    public void run() {
        System.out.println("Finishing...");
        time++;
    }
    public TaskState next(){
        return new InitialState();
    }
}
