package com.palfs.cameraxstable;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


class LRUCache {

    Map<Integer, Integer> cache;
    int recentKey = 0;
    int maxCapacity = 0;
    public LRUCache(int capacity){
        maxCapacity = capacity;
        cache = new HashMap<>(capacity);
    }

    public int get(int key) {

        if (cache.containsKey(key))
            return cache.get(key);

        return -1;
    }

    public void put(int key, int value) {

        if (cache.containsKey(key)) {
            cache.replace(key, value);
            return;
        }

        if (cache.size() == maxCapacity){
            cache.remove(recentKey);
        }

        cache.put(key, value);

        recentKey = key;
    }
}

class Solution2StackFromQueue<E> {
   /*

    Given two queues with their standard operations (<code>enqueue</code>, <code>dequeue</code>,
    <code>isempty</code>, <code>size</code>),
    implement a stack with its standard operations (<code>pop</code>, <code>push</code>,
    <code>isempty</code>, <code>size</code>). The stack should be efficient when pushing an item.</p>

    */

    Queue<E> queue1 = new ArrayDeque<>();
    Queue<E> queue2 = new ArrayDeque<>();

    public boolean push(E item){
        return queue1.add(item);
    }

    public E pop(){

        while (queue1.size() > 1){
            queue2.add(queue1.remove());
        }

        E item = queue1.remove();

        Queue<E> tempQ = queue1;
        queue1 = queue2;

        queue2 = tempQ;
        return item;
    }


}

class Solution1 {
    public static int calPoints(String[] ops) {
        int result = Integer.MIN_VALUE;

        List<Integer> res = new ArrayList();

        for(int i =0; i < ops.length; i++){
            try{
                int num = Integer.parseInt(ops[i]);

                res.add(num);
            } catch (Exception e){
                if (ops[i].equals("C")){
                    res.remove(res.size() - 1);
                } else if (ops[i].equals("D")) {
                    int prevScore = res.get(res.size() - 1);
                    prevScore *= 2;
                    res.add(prevScore);
                } else if (ops[i].equals("+")) {
                    int prevScore = res.get(res.size() - 1);
                    int prevScore2 = res.get(res.size() - 2);

                    int newScore = prevScore + prevScore2;
                    res.add(newScore);
                }
            }
        }

        result = 0;
        for (int i = 0; i < res.size(); i++) {
            result += res.get(i);
        }

        return result;
    }
}

public class ExampleUnitTest {

    @Test
    public void checkStack(){

        LRUCache lRUCache = new LRUCache(2);
        lRUCache.put(1, 1); // cache is {1=1}
        lRUCache.put(2, 2); // cache is {1=1, 2=2}
        System.out.println(lRUCache.get(1));    // return 1
        lRUCache.put(3, 3); // LRU key was 2, evicts key 2, cache is {1=1, 3=3}
        System.out.println(lRUCache.get(2));    // returns -1 (not found)
        lRUCache.put(4, 4); // LRU key was 1, evicts key 1, cache is {4=4, 3=3}
        System.out.println(lRUCache.get(1));    // return -1 (not found)
        System.out.println(lRUCache.get(3));    // return 3
        System.out.println(lRUCache.get(4));    // return 4


//        Solution2StackFromQueue<Integer> stack = new Solution2StackFromQueue<>();
//
//        stack.push(1);
//        stack.push(2);
//        stack.push(3);
//
//        System.out.println(stack.pop());
//        System.out.println(stack.pop());
//        System.out.println(stack.pop());

    }

    @Test
    public void addition_isCorrect() {
//        String[] ops = {"5", "2", "C", "D", "+"}; // Sum = 30
        String[] ops = {"5","-2","4","C","D","9","+","+"}; // Sum = 27
        System.out.println(Solution1.calPoints(ops));
    }
}