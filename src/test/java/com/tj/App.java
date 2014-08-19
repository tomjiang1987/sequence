package com.tj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tj.sequence.Sequence;
import com.tj.sequence.SequenceException;


public class App {
	public static void main(String[] args)throws Exception {
		String config = App.class.getPackage().getName().replace('.', '/') + "/app-test.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.out.println("spring inited");
        
        final Sequence sequence = (Sequence)context.getBean("sequence");
        
        final Map<Long,AtomicLong> count = new ConcurrentHashMap<Long,AtomicLong>();
        
        Runnable job = new Runnable(){

			@Override
			public void run() {
				for(int i =0 ;i < 30000; i++){
		        	
					try {
						Long value = sequence.nextValue("seq0");
						
						if(count.get(value) == null){
							count.put(value, new AtomicLong(1));
						}else{
							System.out.println(Thread.currentThread().getName() + ":confilict key " + value + ":count: " +count.get(value).getAndIncrement());
						}
			          
					} catch (SequenceException e) {
						e.printStackTrace();
					}
		            
		        }
				
				//System.out.println(Thread.currentThread().getName() + ": key count =" + count.size() );
			}
        };
        
        List<Thread> ts = new ArrayList<Thread>();
        Long start = System.currentTimeMillis();
        for(int j = 0;j < 10;j++){
        	Thread thread = new Thread(job,"thread"+j);
        	ts.add(thread);
        	thread.start();
        }
        
        for(int k=0 ;k < ts.size();k++){
        	ts.get(k).join();
        }
        
        System.out.println((System.currentTimeMillis() - start)/1000);
	}
}
