package cn.edu.nju.logic.Helper;
import java.util.concurrent.Semaphore;

public class Notifier {
    private int user_count = 0;
    private int pair_count = 0;
    private int finishCount = 0;

    private Semaphore semaphore = null;

    public Notifier(int user_count){
        this.user_count = user_count;
        this.pair_count = user_count*(user_count-1)/2;
        finishCount =0;

        semaphore = new Semaphore(1);
    }

    public void notifyFinish(){
        try {
            semaphore.acquire();
            finishCount ++;

            if(finishCount % 100  == 0){
                System.out.println("total "+ pair_count + " : current : " + finishCount);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            semaphore.release();
        }
    }
}
