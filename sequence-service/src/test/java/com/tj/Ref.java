package com.tj;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tj.sequence.Sequence;


public class Ref {
	public static void main(String[] args)throws Exception {
		String config = "dubbo-ref-test.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.out.println("spring inited");
       
        Sequence seq = (Sequence)context.getBean("sequence");
        
        String name = "seq0";
        System.out.println(seq.nextValue(name));
	}
}
