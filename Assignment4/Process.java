import java.util.*;

public class Process {
	private char name;
	private int size; //5, 11, 17, 31
	private int duration; // 1, 2, 3, 4, or 5
	private int [] sizeChart = {5, 11, 17, 31};
	//When a process is created, we randomly generate the fields.
	public Process(char s) {
		this.name = s;
		Random generator = new Random();
		this.size = sizeChart[generator.nextInt(4)];// 0~3 index
		this.duration = (generator.nextInt(5) + 1); // 1~5
	}
	
	public char getName() {
		return name;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int i) {
		duration = i;
	}
	
}


