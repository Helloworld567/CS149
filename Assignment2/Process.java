import java.util.*;

public class Process implements Comparable<Process>{
	private String name;
	private float arrivalTime; //0 ~ 99
	private float runTime; // 0.1 ~ 10
	private int priority; //1,2,3,4(1 is highest)
	
	//When a process is created, we randomly generate the fields.
	public Process(String s) {
		this.name = s;
		Random generator = new Random();
		this.arrivalTime = (float) (generator.nextFloat() * 99.0);
		this.runTime = (float) (generator.nextFloat() * 9.9 + 0.1);
		this.priority = generator.nextInt(4) + 1;
	}
	//Copy constructor
	public Process(Process p) {
		this.name = p.getName();
		this.arrivalTime = p.getArrivalTime();
		this.runTime = p.getRunTime();
		this.priority = p.getPriority();
	}
	//For debug purpose
	public Process(String s, long seed) {
		this.name = s;
		Random generator = new Random(seed);
		this.arrivalTime = (float) (generator.nextFloat() * 99.0);
		this.runTime = (float) (generator.nextFloat() * 9.9 + 0.1);
		this.priority = generator.nextInt(4) + 1;
	}
	public int compareTo(Process p) {
		return java.lang.Float.compare(arrivalTime, p.getArrivalTime());
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public float getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(float arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public float getRunTime() {
		return runTime;
	}
	public void setRunTime(float runTime) {
		this.runTime = runTime;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	@Override
	public String toString() {
		return "Process [name=" + name + ", arrivalTime=" + arrivalTime
				+ ", runTime=" + runTime + ", priority=" + priority + "]";
	}
	
}


