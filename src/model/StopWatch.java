package model;

import java.math.BigDecimal;

public class StopWatch {
	private long start;
	private long end;
	
	public void start() {
		start = System.nanoTime();
	}
	
	public void stop(String str) {
		end = System.nanoTime();//ƒ…√Î
		BigDecimal diff = BigDecimal.valueOf(end - start,10);//√Îº∂≤Ó÷µ
		diff = diff.multiply(new BigDecimal(1000));
	    System.out.println(str + ": " + diff + "ms");
	}
}
