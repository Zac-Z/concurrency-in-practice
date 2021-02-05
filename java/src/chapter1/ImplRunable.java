package chapter1;

public class ImplRunable implements Runnable {
    Sequence sequence = new Sequence();
    @Override
    public void run() {
        int value = 0;
        for (int i = 0; i < 1000; i++) {
            value = sequence.getNext();
        }
        System.out.println(Thread.currentThread().getName()+" value:"+ value);
    }

    public static void main(String[] args) {
        ImplRunable implRunable = new ImplRunable();
        Thread thread = new Thread(implRunable);
        Thread thread1 = new Thread(implRunable);
        thread.start();
        thread1.start();
    }
}
