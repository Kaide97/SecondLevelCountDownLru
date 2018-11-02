package cn.kaispace.algorithm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author KAI
 * @CreateTime 2018/10/19
 * @Describe
 **/
public class Lru<K,V> {
    private static final float HASH_LOAD_FACTORY = 0.75f;
    private LinkedHashMap<K,V> map;
    private int cacheSize;

    public Lru(int cacheSize) {
        this.cacheSize = cacheSize;
        int capacity = (int)Math.ceil(cacheSize / HASH_LOAD_FACTORY) + 1;
        map = new LinkedHashMap<K,V>(capacity, HASH_LOAD_FACTORY, true){
            private static final long serialVersionUID = 1;

            //这个方法是在afterNodeInsertion后判断中调用，移除掉头部（最不常用的）的节点
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > Lru.this.cacheSize;
            }
        };
    }

    public synchronized V get(K key) {
        //这里之所以能实现lru是因为get里面有个afterNodeAccess,这个方法将刚刚访问的节点移动到最后
        return map.get(key);
    }

    public synchronized void remove(K key){
        map.remove(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized int usedSize() {
        return map.size();
    }

    public void print() {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.print(entry.getValue() + "--");
        }
        System.out.println();
    }
}
