import java.util.*;


public class ProcessSwapping {
	
	ArrayList<Character> array;
	HashSet<Process> processSet;
	
	/*statistics*/
	int ffCount = 0;
	int nfCount = 0;
	int bfCount = 0;
	
	int MINUTE = 60;
	int SIZE = 100;
	
	public enum ALG {
		FF, NF, BF;
	}
	
	public void processSwappingInit() {
		array = new ArrayList<Character>();
		for (int i = 0; i < SIZE; i++) {
			array.add('.');
		}
		processSet = new HashSet<Process>();
	}
	
	public void run(ALG alg ) {
		switch (alg) {
		case FF:
			System.out.println("Runing First Fit Simulation");
			break;
		case NF:
			System.out.println("Runing Next Fit Simulation");
			break;
		case BF:
			System.out.println("Runing Best Fit Simulation");
			break;	
		}
		processSwappingInit();
		for (int count = 0; count < MINUTE; count++) {
			System.out.println("Second: " + (count + 1));
			if (delTimeOut()) {
				System.out.print("Swapping Out:");
				printMemoryMap();
			}
			switch (alg) {
			case FF:
				if (fffill())
					System.out.print("Swapping In: ");
					printMemoryMap();
				break;
			case NF:
				if (nffill())
					System.out.print("Swapping In: ");
					printMemoryMap();
				break;
			case BF:
				if (bffill())
					System.out.print("Swapping In: ");
					printMemoryMap();
				break;	
			}
			
		}
	}
	
	public void printMemoryMap() {
		for (int i = 0; i < array.size(); i++) {
			System.out.print(array.get(i));
		}
		System.out.println();
	}
	
	public boolean delTimeOut() {
		boolean found = false;
		Iterator<Process> setIter = processSet.iterator();
		while(setIter.hasNext()) {
			Process p = setIter.next();
			p.setDuration(p.getDuration()-1);
			if (p.getDuration() == 0) {
				found = true;
				for (int i = 0; i < array.size(); i++) {
					if (array.get(i) == p.getName())
						array.set(i, '.');
				}
				setIter.remove();
			}
		}
		return found;
	}
	
	public boolean fffill() {
		boolean fill = false; // there is a large enough hole to fill in the next process
		boolean res = false;
		// find the first hole that can fit
		do {
			fill = false; 
			Process p = getNextProcess();
			OUT: for (int i = 0; i < array.size(); i++) {
				int count = 0;
				while(array.get(i) == '.') {
					count++;
					i++;
					if(i == array.size())
						break OUT;
					if (count == p.getSize()) { // if found, start from begining again
						fill = true;
						res = true;
						processSet.add(p);
						ffCount++;
						for (int j = i-count; j < i; j++)
							array.set(j, p.getName());
						break OUT;
					}
				}
			}
		} while (fill);
		return res;
	}
	
	public boolean nffill() {
		boolean fill = false; // there is a large enough hole to fill in the next process
		boolean res = false;
		int start = 0; // next time start from start, next fit
		// find the first hole that can fit
		do {
			fill = false; 
			Process p = getNextProcess();
			OUT:for (int i = start; i < array.size(); i++) {
				int count = 0;
				while(array.get(i) == '.') {
					count++;
					i++;
					if(i == array.size())
						break OUT;
					if (count == p.getSize()) {
						fill = true;
						res = true;
						processSet.add(p);
						for (int j = i-count; j < i; j++)
							array.set(j, p.getName());
						start = i;
						nfCount++;
						break OUT;
					}
				}
			}
		} while (fill);
		return res;
	}
	
	public boolean bffill() {
		boolean fill = false; // there is a large enough hole to fill in the next process
		boolean res = false;
		int minSize = SIZE;
		int minPos = 0;
		// find the smallest hole that can fit
		do {
			fill = false; 
			Process p = getNextProcess();
			for (int i = 0; i < array.size(); i++) {
				int count = 0;
				
				while(array.get(i) == '.') {
					count++;
					i++;
					if (i == array.size())
						break;
				}
				if (count >= p.getSize() && count <= minSize) {
					fill = true;
					minSize = count;
					minPos = i - count;
				}
			}
			
			if (fill) {
				res = true;
				processSet.add(p);
				bfCount++;
				for (int j = minPos; j < minPos+p.getSize(); j++)
					array.set(j, p.getName());
			}
		} while (fill);
		return res;
	}
	
	public Process getNextProcess() {
		if (processSet.isEmpty()) {
			return new Process('A');
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			boolean contains = false;
			for(Process p : processSet) {
				if (p.getName() == c) { // if contains c
					contains = true;
					break;
				}
			}
			if (!contains)
			return new Process(c);
		}
		return null; // should not get here
	}
	
	public static void main(String[] args) {
		ProcessSwapping ps = new ProcessSwapping();
		int NO_RUNS = 5;
		for (int i = 0; i < NO_RUNS; i++) {
			ps.run(ALG.FF);
			ps.run(ALG.NF);
			ps.run(ALG.BF);
		}
		
		System.out.println("First Fit Swap Avg: " + ps.ffCount/NO_RUNS);
		System.out.println("Next Fit Swap Avg: " + ps.nfCount/NO_RUNS);
		System.out.println("Best Fit Swap Avg: " + ps.bfCount/NO_RUNS);
	}
}



