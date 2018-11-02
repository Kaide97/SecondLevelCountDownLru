package cn.kaispace.algorithm;

import java.util.Random;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @Author KAI
 * @CreateTime 2018/10/19
 * @Describe
 **/
public class LruTest {
    public static void main(String[] args) throws InterruptedException {
        LruK<LruKey,StringBuilder> lk = new LruK<>(3L,10);
        LruKey[] lkey = new LruKey[10];
        for(int i = 0;i<lkey.length;i++){
            lkey[i] = new LruKey<>(String.valueOf(i),TimeUnit.NANOSECONDS.convert(3,TimeUnit.SECONDS));
            lk.put(lkey[i],new StringBuilder("A"));
        }
        new LruTest().printData(lk,lkey);
}

/**
 *@描述 延时访问
 *@参数 
 *@返回值 
 *@创建人  KAI
 *@创建时间  2018/10/20
 *@修改人和其它信息
 */
public synchronized void printData(LruK lk,LruKey[] lkey) throws InterruptedException {
    Random r = new Random();
    StringBuilder value;
    while(true){
        this.wait(1000);
        r.setSeed(System.currentTimeMillis());
        int pos;
        pos = r.nextInt(10);
        if((value = (StringBuilder) lk.get(lkey[pos]))==null){
            System.out.println("Get: "+ String.valueOf(pos)+"已被移除");
        }else {
            System.out.println("Get: " + String.valueOf(pos) + " Return: " +value);
        }
    }
}

static class LruKey<T> implements Delayed{
    private T t;
    private long liveTime;
    private long removeTime;
    public LruKey(T t,long liveTime){
        this.t = t;
        this.liveTime = liveTime;
    }
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(removeTime - System.nanoTime(), unit);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {return  1;}
        if (o == this) {return  0;}
        if (o instanceof LruKey){
            LruKey<T> tmpDelayedItem = (LruKey<T>)o;
            if (liveTime > tmpDelayedItem.liveTime ) {
                return 1;
            }else if (liveTime == tmpDelayedItem.liveTime) {
                return 0;
            }else {
                return -1;
            }
        }
        long diff = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return diff > 0 ? 1:diff == 0? 0:-1;
    }

    @Override
    public String toString(){
        System.out.print(this.t+": ");
        return super.toString();
    }

    /**
     * 进入二层缓存才启动计时
     */
    public void active(){
        this.removeTime = TimeUnit.NANOSECONDS.convert(liveTime, TimeUnit.NANOSECONDS) + System.nanoTime();
    }

    public T get(){ return t; }
}
}