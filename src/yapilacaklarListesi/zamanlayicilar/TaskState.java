package yapilacaklarListesi.zamanlayicilar;

public abstract class TaskState  {
    public int time = 1500;
    public abstract void run();
    public abstract TaskState next();
}