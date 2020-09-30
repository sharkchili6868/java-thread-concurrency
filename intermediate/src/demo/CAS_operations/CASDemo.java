package demo.CAS_operations;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicStampedReference;

public class CASDemo {
    private static final AtomicStampedReference<Integer> atomic = new AtomicStampedReference<>(100, 0);

    public static void main(String[] args) throws InterruptedException {
        Thread t0 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    boolean success = atomic.compareAndSet(100, 101,
                            atomic.getStamp(), atomic.getStamp() + 1);
                    System.out.println(Thread.currentThread().getName() + " set 100 >>> 101 : " + success);

                    success = atomic.compareAndSet(101, 100,
                            atomic.getStamp(), atomic.getStamp() + 1);
                    System.out.println(Thread.currentThread().getName() + " set 101 >>> 100 : " + success);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t0.start();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int stamp = atomic.getStamp();
                    System.out.println(Thread.currentThread().getName() + " before modify : " + stamp);
                    TimeUnit.SECONDS.sleep(2);

                    int stamp1 = atomic.getStamp();
                    System.out.println(Thread.currentThread().getName() +
                            " wait 2 seconds, stamp is modified by t0 thread to : " + stamp1);

                    boolean success = atomic.compareAndSet(100, 101, stamp, stamp + 1);
                    System.out.println(Thread.currentThread().getName() + " set 100>101 using WRONG stamp: " + success);

                    success = atomic.compareAndSet(101, 100, stamp, stamp + 1);
                    System.out.println(Thread.currentThread().getName() + " set 101>100 using WRONG stamp: " + success);

                    //Kevin提醒：以下修改是成功的,因为使用了正确的版本号,正确的期待值
                    success = atomic.compareAndSet(100, 101, stamp1, stamp1 + 1);
                    System.out.println(Thread.currentThread().getName() + " set 100>101 using CORRECT stamp: " + success);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();

        t0.join();
        t1.join();

        System.out.println("Main thread finished");
    }
}
