import java.util.*;


public class ProcessScheduling {
	private Queue<Process> processQueue; //Queue to schedule process, processes are added by arrival time
	private ArrayList<String> state; //Store state for time chart
	private Process[] processArray; //Unordered original process array
	private HashMap<String, Process> map; //Backup all processes
	private HashMap<String, Integer> completed;//Record complete time
	
	public ProcessScheduling(Process[] p) {
		this.processArray = p;
		state = new ArrayList<String>();
		map = new HashMap<String, Process>();
		completed = new HashMap<String, Integer>();
		for (int i = 0; i < p.length; i++) {
			map.put(p[i].getName(), p[i]);
		}
	}
	
	public void run(String s) {
		if (s.equals("FCFS"))
			firstComeFirstServed();
		else if (s.equals("SRT"))
			shortestRemainingTime();
		else if (s.equals("SJF"))
			shortestJobFirst();
		else if (s.equals("HPFNONP"))
			highestPriorityFirstNonP();
		else if (s.equals("RR"))
			roundRobin();
		else if (s.equals("HPFP"))
			highestPriorityFirstPreemptive();
		else if (s.equals("HPFPWP"))
			highestPriorityFirstPreemptiveWithPromotion();
		
	}
	
	private void firstComeFirstServed() {
		Process curr = processQueue.poll();
		for (int i = 0; i < 100; i++) {
			//No more process to service
			if (curr == null) {
				state.add("Idle");
				continue;
			}
			//Current process finished
			if (Float.compare(curr.getRunTime(), 0) < 0) {
				completed.put(curr.getName(), i - 1);
				curr = processQueue.poll();
				if (curr == null) {
					state.add("Idle");
					continue;
				}
			}
			if (Float.compare(curr.getArrivalTime(), i) <= 0 && Float.compare(curr.getRunTime(), 0) > 0) {
				curr.setRunTime(curr.getRunTime() - 1); //Current process is running, decrement 1
				state.add(curr.getName());
			}
			else if (Float.compare(curr.getArrivalTime(), i) > 0) { // idle
				state.add("Idle");
			}
		}
		//Let the last process finish even if it goes beyond 100 quanta
		while (curr != null && Float.compare(curr.getRunTime(), 0) > 0) {
			curr.setRunTime(curr.getRunTime() - 1);
			state.add(curr.getName());
		}
		if (state.size() > 100) //Meaning the last process finished after quantum 99
			completed.put(curr.getName(), state.size() - 1);
	}
	
	private void shortestRemainingTime() {
		//Maintain a min-heap of processes that have arrived in shortest remaining time first order
		PriorityQueue<Process> minHeap = new PriorityQueue<Process>(processQueue.size(), new Comparator<Process>() {
			public int compare(Process p1, Process p2) {
				return Float.compare(p1.getRunTime(), p2.getRunTime());
			}
		});
		Process curr = null;
		for (int i = 0; i < 100; i++) {
			//Put all processes that have arrived into min-heap
			while (!processQueue.isEmpty() && Float.compare(processQueue.peek().getArrivalTime(), i) < 0) 
				minHeap.add(processQueue.poll());

			//Pick the root of minHeap to service
			if (!minHeap.isEmpty()) {
				curr = minHeap.poll();
				//Decrement current process's runTime and put back to minHeap if not finished
				curr.setRunTime(curr.getRunTime() - 1);
				state.add(curr.getName());
				if (Float.compare(curr.getRunTime(), 0) > 0)
					minHeap.add(curr);
				else {//A process has completed
					completed.put(curr.getName(), i);
				}
				continue;
			}
			
			state.add("Idle");
		}
		//Let the last process finish even if it goes beyond 100 quanta
		while (curr != null && Float.compare(curr.getRunTime(), 0) > 0) {
			curr.setRunTime(curr.getRunTime() - 1);
			state.add(curr.getName());
		}
		if (state.size() > 100) //Meaning the last process finished after quantum 99
			completed.put(curr.getName(), state.size() - 1);
	}
	
	private void shortestJobFirst()
	{
		PriorityQueue<Process> minHeap = new PriorityQueue<Process>(processQueue.size(), new Comparator<Process>() {
			public int compare(Process p1, Process p2) {
				return Float.compare(p1.getRunTime(), p2.getRunTime());
			}
		});
		for (int i = 0; i < 100; i++) {
			while (!processQueue.isEmpty() && Float.compare(processQueue.peek().getArrivalTime(), i) < 0) 
				minHeap.add(processQueue.poll());
			if (!minHeap.isEmpty()) {
				Process curr = minHeap.poll();
				//Decrement current process's runTime and put back to minHeap if not finished
				while (Float.compare(curr.getRunTime(), 0) > 0)
				{
					curr.setRunTime(curr.getRunTime() - 1);
					state.add(curr.getName());
					i++;
				}
				completed.put(curr.getName(), i);
				i--;
			}
			else
			{
				state.add("Idle");
			}
		}
	}
	
	private void roundRobin()
	{
		ArrayList<Process> q = new ArrayList<Process>();
		int index = 0; // track which process in the q to run
		Process c = null;
		for (int i = 0; i < 100; i++)
		{
			while (!processQueue.isEmpty() && Float.compare(processQueue.peek().getArrivalTime(), i) < 0) 
				q.add(processQueue.poll());
			if (!q.isEmpty())
			{
				if (index < q.size())
				{
					c = q.get(index);
					float curr = c.getRunTime();
					state.add(q.get(index).getName());
					q.get(index).setRunTime(curr - 1);
					if (Float.compare(curr, 0) < 0)
					{
						completed.put(q.get(index).getName(), i);
						q.remove(index--);
					}
					if (index == q.size() - 1)
						index = 0;
					else
						index++;
				}
			}
			else
			{
				state.add("Idle");
			}
		}
		//Let the last process finish even if it goes beyond 100 quanta
		while (c != null && Float.compare(c.getRunTime(), 0) > 0) {
			c.setRunTime(c.getRunTime() - 1);
			state.add(c.getName());
		}
		if (state.size() > 100) //Meaning the last process finished after quantum 99
			completed.put(c.getName(), state.size() - 1);
	}
	
	private void highestPriorityFirstPreemptive()
	{
		ArrayList<Process> q1 = new ArrayList<Process>();
		ArrayList<Process> q2 = new ArrayList<Process>();
		ArrayList<Process> q3 = new ArrayList<Process>();
		ArrayList<Process> q4 = new ArrayList<Process>();
		for (int i = 0; i < 100; i++)
		{
			while (!processQueue.isEmpty() && Float.compare(processQueue.peek().getArrivalTime(), i) < 0) 
			{
				Process p = processQueue.poll();
				switch (p.getPriority())
				{
					case 1 :
						q1.add(p);
						break;
					case 2 :
						q2.add(p);
						break;
					case 3 :
						q3.add(p);
						break;
					case 4 :
						q4.add(p);
						break;
				}
			}
			
			if(!q1.isEmpty())
			{
				state.add(q1.get(0).getName());
				q1.get(0).setRunTime(q1.get(0).getRunTime() - 1);
				if (Float.compare(q1.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q1.get(0).getName(), i);
					q1.remove(0);
				}
			}
			else if (!q2.isEmpty())
			{
				state.add(q2.get(0).getName());
				q2.get(0).setRunTime(q2.get(0).getRunTime() - 1);
				if (Float.compare(q2.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q2.get(0).getName(), i);
					q2.remove(0);
				}
			}
			else if (!q3.isEmpty())
			{
				state.add(q3.get(0).getName());
				q3.get(0).setRunTime(q3.get(0).getRunTime() - 1);
				if (Float.compare(q3.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q3.get(0).getName(), i);
					q3.remove(0);
				}
			}
			else if (!q4.isEmpty())
			{
				state.add(q4.get(0).getName());
				q4.get(0).setRunTime(q4.get(0).getRunTime() - 1);
				if (Float.compare(q4.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q4.get(0).getName(), i);
					q4.remove(0);
				}
			}
			else
			{
				state.add("Idle");
			}
		}
	}
	
	private void highestPriorityFirstPreemptiveWithPromotion()
	{
		ArrayList<Process> q1 = new ArrayList<Process>();
		ArrayList<Process> q2 = new ArrayList<Process>();
		ArrayList<Process> q3 = new ArrayList<Process>();
		ArrayList<Process> q4 = new ArrayList<Process>();
		HashMap<Process, Integer> waitTime = new HashMap<Process, Integer>();
		for (int i = 0; i < 100; i++)
		{
			while (!processQueue.isEmpty() && Float.compare(processQueue.peek().getArrivalTime(), i) < 0) 
			{
				Process p = processQueue.poll();
				switch (p.getPriority())
				{
					case 1 :
						q1.add(p);
						break;
					case 2 :
						q2.add(p);
						waitTime.put(p, 0);
						break;
					case 3 :
						q3.add(p);
						waitTime.put(p, 0);
						break;
					case 4 :
						q4.add(p);
						waitTime.put(p, 0);
						break;
				}
			}
			
			if(!q1.isEmpty())
			{
				state.add(q1.get(0).getName());
				q1.get(0).setRunTime(q1.get(0).getRunTime() - 1);
				if (Float.compare(q1.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q1.get(0).getName(), i);
					q1.remove(0);
				}
				
			}
			else if (!q2.isEmpty())
			{
				state.add(q2.get(0).getName());
				q2.get(0).setRunTime(q2.get(0).getRunTime() - 1);
				if (Float.compare(q2.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q2.get(0).getName(), i);
					q2.remove(0);
				}
				
			}
			else if (!q3.isEmpty())
			{
				state.add(q3.get(0).getName());
				q3.get(0).setRunTime(q3.get(0).getRunTime() - 1);
				if (Float.compare(q3.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q3.get(0).getName(), i);
					q3.remove(0);
				}
				
			}
			else if (!q4.isEmpty())
			{
				state.add(q4.get(0).getName());
				q4.get(0).setRunTime(q4.get(0).getRunTime() - 1);
				if (Float.compare(q4.get(0).getRunTime(), 0) < 0)
				{
					completed.put(q4.get(0).getName(), i);
					q4.remove(0);
				}
			}
			else
			{
				state.add("Idle");
			}
			
			if (!q2.isEmpty())
			{
				for (int j = 0; j < q2.size(); j++)
				{
					Process p = q2.get(j);
					waitTime.put(p, waitTime.get(p)+1);
					if (waitTime.get(p) == 5)
					{
						q1.add(p);
						q2.remove(p);
						waitTime.put(p,0);
						j--;
					}
				}
			}
			
			if (!q3.isEmpty())
			{
				for (int j = 0; j < q3.size(); j++)
				{
					Process p = q3.get(j);
					waitTime.put(p, waitTime.get(p)+1);
					if (waitTime.get(p) == 5)
					{
						q2.add(p);
						q3.remove(p);
						waitTime.put(p,0);
						j--;
					}
				}
			}
			
			if (!q4.isEmpty())
			{
				for (int j = 0; j < q4.size(); j++)
				{
					Process p = q4.get(j);
					waitTime.put(p, waitTime.get(p)+1);
					if (waitTime.get(p) == 5)
					{
						q3.add(p);
						q4.remove(p);
						waitTime.put(p,0);
						j--;
					}
				}
			}
			
		}
	}
	
	public void highestPriorityFirstNonP() {
		Queue<Process> p1 = new LinkedList<Process>();
		Queue<Process> p2 = new LinkedList<Process>();
		Queue<Process> p3 = new LinkedList<Process>();
		Queue<Process> p4 = new LinkedList<Process>();
		//Fill in 4 queues, sort processArray by arrivalTime
		Arrays.sort(processArray);
		for (int i = 0; i < processArray.length; i++) {
			int p = processArray[i].getPriority();
			if (p == 1) p1.add(processArray[i]);
			else if (p == 2) p2.add(processArray[i]);
			else if (p == 3) p3.add(processArray[i]);
			else if (p == 4) p4.add(processArray[i]);
		}
		Process curr = null;
		
		int i = 0;
		while (i < 100) {
			if (!p1.isEmpty() && Float.compare(p1.peek().getArrivalTime(), i) <= 0)
				curr = p1.poll();
			else if (!p2.isEmpty() && Float.compare(p2.peek().getArrivalTime(), i) <= 0)
				curr = p2.poll();
			else if (!p3.isEmpty() && Float.compare(p3.peek().getArrivalTime(), i) <= 0)
				curr = p3.poll();
			else if (!p4.isEmpty() && Float.compare(p4.peek().getArrivalTime(), i) <= 0)
				curr = p4.poll();
			else 
				curr = null;
			if (curr == null) {
				state.add("Idle");
				i++;
				continue;
			}

			//Curr process is ready to run
			while (Float.compare(curr.getRunTime(), 0) > 0) {
				curr.setRunTime(curr.getRunTime() - 1); //Current process is running, decrement 1
				state.add(curr.getName());
				i++;
			}
			//Curr process is done, need switch process
			completed.put(curr.getName(), i);
		}
		//Let the last process finish even if it goes beyond 100 quanta
		while (curr != null && Float.compare(curr.getRunTime(), 0) > 0) {
			curr.setRunTime(curr.getRunTime() - 1);
			state.add(curr.getName());
		}
		if (state.size() > 100) //Meaning the last process finished after quantum 99
			completed.put(curr.getName(), state.size() - 1);
	}
	
	public void printTimeChart() {
		for (int i = 0; i < state.size(); i++) {
			System.out.printf("%5d", i);
			System.out.printf("%5s", state.get(i));
			if ((i+1) % 10 == 0)
				System.out.println();
		}
		System.out.println();
	}
	
	public float calcAvgTurnAroundTime() {
		//Turnaround time for a process is the elapsed time between when it first arrives and when it completes
		float sum = 0;
		for (Map.Entry<String, Integer> entry : completed.entrySet()) {
			sum += entry.getValue() - map.get(entry.getKey()).getArrivalTime();
		}

		return sum / completed.size();
	}
	
	public float calcAvgWaitingTime() {
		//waiting time is the sum of all the time a process spends waiting from its arrival time to when it finishes.
		//Total waiting time = finishT - ArrivalT - burstT;
		float sum = 0;
		for (Map.Entry<String, Integer> entry : completed.entrySet()) {
			sum += entry.getValue() - map.get(entry.getKey()).getArrivalTime() - map.get(entry.getKey()).getRunTime();
		}
		return sum / completed.size();
	}
	
	public float calcAvgResponseTime() {
		//Response time for a process is the elapsed time between when it first arrives and when it first runs
		//Find when the process first runs
		HashMap<String, Integer> start = new HashMap<String, Integer>();
		for (int i = 0; i < state.size(); i++) {
			if (state.get(i).equals("Idle")) continue;
			Process p = map.get(state.get(i));
			if (!start.containsKey(p.getName()) && completed.containsKey(p.getName())) {
				start.put(p.getName(), i);
			}
		}
		float sum = 0;
		for (Map.Entry<String, Integer> entry : start.entrySet()) {
			//System.out.println("first run = "+entry.getValue() + "Arrival = " + map.get(entry.getKey()).getArrivalTime());
			sum += entry.getValue() - map.get(entry.getKey()).getArrivalTime();
		}
		return sum / start.size();
	}
	
	public int calcThroughput() {
		//Find how many processes completed during each run of an algorithm
		return completed.size();
	}
	
	public void enQueueByArrivalTime() {
		processQueue = new LinkedList<Process>();
		Arrays.sort(processArray);
		for (int i = 0; i < processArray.length; i++) {
			System.out.println(processArray[i].toString());
			processQueue.offer(new Process(processArray[i]));
		}
	}
	
	public static void main(String[] args) {
		int times = 5;
		float avgTurnAroundTime = 0, avgWaitingTime = 0;
		float avgResponseTime = 0, throughput = 0;
		//Test each algorithm for 5 times
		for (int j = 0; j < times; j++) {
			//Create processes
			Process[] p = new Process[100];
			for (int i = 0; i < p.length; i++) {
				StringBuilder name = new StringBuilder("p");
				name.append(i);
				p[i] = new Process(name.toString());
			}
			//Create process scheduler
			ProcessScheduling scheduler = new ProcessScheduling(p);
		
			scheduler.enQueueByArrivalTime();
			//scheduler.run("FCFS");
			//scheduler.run("SJF");
			//scheduler.run("SRT");
			//scheduler.run("RR");
			//scheduler.run("HPFNONP");
			scheduler.run("HPFP");
			//scheduler.run("HPFPWP");
			scheduler.printTimeChart();
			avgTurnAroundTime += scheduler.calcAvgTurnAroundTime();
			avgWaitingTime += scheduler.calcAvgWaitingTime();
			avgResponseTime += scheduler.calcAvgResponseTime();
			throughput += scheduler.calcThroughput();
		}
		System.out.println("Avg Turnaround time = " + avgTurnAroundTime / times);
		System.out.println("Avg Waiting time    = " + avgWaitingTime / times);
		System.out.println("Avg Response time   = " + avgResponseTime / times);
		System.out.println("Throughput           = " + throughput / times);
	}
}



