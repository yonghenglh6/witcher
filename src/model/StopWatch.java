package model;

import java.math.BigDecimal;

public class StopWatch {
	private long start;
	private long end;
	
	int defaultCount=1;
	public void start() {
		start = System.nanoTime();
	}
	
	public void stop(String str) {
		end = System.nanoTime();//纳秒
		BigDecimal diff = BigDecimal.valueOf(end - start,10);//秒级差值
		diff = diff.multiply(new BigDecimal(10000));
	    System.out.println(str + ": " + diff + "ms");
	    System.out.flush();
	}
	
	public void stop() {
		stop("["+defaultCount+++"]耗时：");
	}
	
	public void stopAndStart(){
		stop("["+defaultCount+++"]耗时：");
		start();
	}
	public void stopAndStart(String tip){
		stop(tip);
		start();
	}
	
}
