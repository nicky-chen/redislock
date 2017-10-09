package com.nicky.redislock;


import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nicky.redis.RedisUtil;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.JedisPool;

public class SecKillTest {

	private static Long commidityId1 = 10000001L;
	private static Long commidityId2 = 10000002L;
	private RedisClient client;
	public static String HOST = "127.0.0.1";
	private JedisPool jedisPool;
	@Before
	public synchronized void  beforeTest() throws IOException{
		
		
		jedisPool = new JedisPool("127.0.0.1");
		
	}

	@Test
    public void dd() {
        Deque<Integer> deque = new ArrayDeque<>(1<<1);
        deque.addFirst(1);
        deque.addLast(2);
		RedisUtil redisUtil= RedisUtil.getInstance();
		Gson gson = new GsonBuilder().create();
		redisUtil.set("question", gson.toJson(deque));
		Deque<Integer> q = gson.fromJson(redisUtil.get("question"), new TypeToken<Deque<Integer>>(){}.getType());
        for (Integer a : q){
            System.out.println(a);
        }

//        String [] a = {"a", "b"};
//        System.out.println(String.join(":", "a", "b"));
    }

    @Test public void teee() {
        String a = "3个月";
        System.out.println(a.replace("个月",""));

    }

	@Test
	public void testSecKill(){
		int threadCount = 1000;
		int splitPoint = 500;
		final CountDownLatch endCount = new CountDownLatch(threadCount);
		final CountDownLatch beginCount = new CountDownLatch(1);
		final SecKillImpl testClass = new SecKillImpl();
		
		Thread[] threads = new Thread[threadCount];
		//起500个线程，秒杀第一个商品
		for(int i= 0;i < splitPoint;i++){
			threads[i] = new Thread(new  Runnable() {
				public void run() {
					try {
						//等待在一个信号量上，挂起
						beginCount.await();
						//用动态代理的方式调用secKill方法
						SeckillInterface proxy = (SeckillInterface) Proxy.newProxyInstance(SeckillInterface.class.getClassLoader(), 
							new Class[]{SeckillInterface.class}, new CacheLockInterceptor(testClass));
						proxy.secKill("test", commidityId1);
						endCount.countDown();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			threads[i].start();

		}
		
		for(int i= splitPoint;i < threadCount;i++){
			threads[i] = new Thread(new  Runnable() {
				public void run() {
					try {
						//等待在一个信号量上，挂起
						beginCount.await();
						//用动态代理的方式调用secKill方法
						beginCount.await();
						SeckillInterface proxy = (SeckillInterface) Proxy.newProxyInstance(SeckillInterface.class.getClassLoader(), 
							new Class[]{SeckillInterface.class}, new CacheLockInterceptor(testClass));
						proxy.secKill("test", commidityId2);
						//testClass.testFunc("test", 10000001L);
						endCount.countDown();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			threads[i].start();

		}
		
		
		long startTime = System.currentTimeMillis();
		//主线程释放开始信号量，并等待结束信号量
		beginCount.countDown();
		
		try {
			//主线程等待结束信号量
			endCount.await();
			//观察秒杀结果是否正确
			System.out.println(SecKillImpl.inventory.get(commidityId1));
			System.out.println(SecKillImpl.inventory.get(commidityId2));
			System.out.println("error count" + CacheLockInterceptor.ERROR_COUNT);
			System.out.println("total cost " + (System.currentTimeMillis() - startTime));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
