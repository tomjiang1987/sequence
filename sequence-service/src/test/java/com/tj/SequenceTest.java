package com.tj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tj.sequence.Sequence;


public class SequenceTest {
	public static void main(String[] args)throws Exception {
		String config = "applicationContext.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.out.println("spring inited");
       
        final Sequence sequence = (Sequence)context.getBean("sequence");
        
        String name = "seq0";
        List<Long> seqList = new ArrayList<Long>();
        for(int i = 0 ;i< 1000 ;i++){
        	Long next = sequence.nextValue(name);
        	System.out.println(next);
        	seqList.add(next);
        }
        
        System.out.println("after sort...");
        Collections.sort(seqList);
        for(Long i : seqList){
        	System.out.println(i);
        }
        
        System.out.println("spring closing");
        context.close();
	}
}
