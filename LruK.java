package cn.kaispace.algorithm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;

/**
 * @Author KAI
 * @CreateTime 2018/10/19
 * @Describe
 **/
public class LruK<K extends Delayed,V> {
    private Lru<K,V> lruTemp;
    private volatile Lru<K,V> lru;
    private LinkedHashMap<K,Long> usedCountMap;
    private DelayQueue<K> secondLruJudge;
    private Thread secondLruJudgeThread;
    /**
    *k即是进入二级容器的阈值
    **/
    private Long k;


    public LruK(Long k,int cacheSize){
        this.k = k;
        lru = new Lru<>(cacheSize);
        lruTemp = new Lru<>(cacheSize);
        usedCountMap = new LinkedHashMap<>();
        secondLruJudge = new DelayQueue<>();
        secondLruJudgeThread = new Thread(){
            @Override
            public void run(){
                while (true){
                    K key = secondLruJudge.poll();
                    if(key != null){
                        lru.remove(key);
                        Method m = null;
                        try {
                            m = key.getClass().getMethod("get");
                            System.out.println("==================");
                            System.out.println("DelayedQueue Remove: "+m.invoke(key));
                            System.out.println("==================");
                        }catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        secondLruJudgeThread.start();
    }

    public synchronized void put(K key,V value) {
        lruTemp.put(key, value);
        usedCountMap.put(key, 1L);
        key.toString();
        System.out.println("AddToTheMap");
        afterAccess();
    }

    public synchronized V get(K key) {
        V values;
        Long usedCount = usedCountMap.get(key);
        if (usedCount != null) {
            usedCountMap.replace(key, ++usedCount);
            System.out.println("------------------------");
            key.toString();
            System.out.println("modCount+1 CurrentModCount: " + usedCount.toString());
        }else{
            System.out.println("------------------------");
            key.toString();
            System.out.println("AlreadInsideSecondLevelCache");
        }

        afterAccess();
        return (values = lru.get(key)) == null ? lruTemp.get(key) : values;
    }

    public synchronized void clear(){
        lruTemp.clear();
        lru.clear();
        usedCountMap.clear();
        secondLruJudge.clear();
    }

    private synchronized void afterAccess(){
        ArrayList<K> removeKey = new ArrayList<>();
        for (Map.Entry<K,Long> entry:usedCountMap.entrySet()
             ) {
            if(entry.getValue()>=k){
                K key = entry.getKey();
                lru.put(key,lruTemp.get(key));
                lruTemp.remove(key);
                removeKey.add(key);
                //启动计时
                try {
                    key.getClass().getMethod("active").invoke(key);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                secondLruJudge.put(key);

                key.toString();
                System.out.println("AddToSecondLevelCache");
            }
        }
        for (K key:removeKey
             ) {
            usedCountMap.remove(key);
        }
    }
}
