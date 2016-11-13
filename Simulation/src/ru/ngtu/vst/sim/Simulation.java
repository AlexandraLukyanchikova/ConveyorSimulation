package ru.ngtu.vst.sim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Simulation {

	private static int A = 50;
	private static int B = 10;

	private static int[] K = { 5, 10 };

	private static int[] T = { 40, 60 };
	private static int[] dt = { 10, 15 };
	private static int T3 = 25;

	private static double S1 = 4;
	private static double S2 = 1.5;
	private static double S3 = 200;
	private static double S4 = 45;
	private static double S5 = 4;

	public static int time = 0;
	public static final int simulationTime = 7 * 8 * 60;
	public static final int firstMachinesCount = 15, secondMachinesCount = 2;
	public static final List<Queue<Detail>> queueList = new ArrayList<Queue<Detail>>();
	public static final List<Integer> maxQueueSizes = new ArrayList<Integer>();
	public static final Queue<Detail> buffer = new LinkedList<Detail>();
	public static int maxBufferSize = 0;
	public static final Random random = new Random();
	public static final List<Machine> firstMachines = new ArrayList<Machine>();
	public static final List<Machine> secondMachines = new ArrayList<Machine>();
	public static final EventList eventList = new EventList();
	public static final List<Detail> readyDetails = new ArrayList<Detail>();
	public static int detailCount = 0;

	public static void main(String[] args) {
		for (int i = 0; i < firstMachinesCount; i++) {
			queueList.add(new LinkedList<Detail>());
			maxQueueSizes.add(0);
		}
		for (int i = 0; i < firstMachinesCount; i++) {
			firstMachines.add(new Machine());
		}
		for (int i = 0; i < secondMachinesCount; i++) {
			secondMachines.add(new Machine());
		}
		eventList.plan(new Event(0, uniform(A, B)));
		eventList.plan(new Event(4, simulationTime));

		boolean finish = false;
		while (true) {
			Event currentEvent = eventList.getEvent();
			time = currentEvent.getTime();
			// 0 - getting of a detail
			// 1 - completion of the treatment in the 1st stage
			// 2 - buffer overflow
			// 3 - completion of the treatment in the 2st stage
			// 4 - completion of the simulation
			switch (currentEvent.getCode()) {
			case 0:
				handleDetailGetting();
				break;
			case 1:
				handleTreatmentOnStage(1, currentEvent.getMachineNumber());
				break;
			case 2:
				handleBufferOverflow();
				break;
			case 3:
				handleTreatmentOnStage(2, currentEvent.getMachineNumber());
				break;
			case 4:
				finish = true;
				break;
			}

			if (finish) {
				break;
			}
		}

		System.out.println("Count of ready details: " + readyDetails.size());
		System.out.println("Count of not ready details: " + (detailCount - readyDetails.size()));
		System.out.println();
		System.out.println("Machines of 1st type: " + firstMachines.size());
		System.out.println("Machines of 2nd type: " + secondMachines.size());
		System.out.println();
		for (int i = 0; i < maxQueueSizes.size(); i++) {
			System.out.println("Max size of queue #" + i + ": " + maxQueueSizes.get(i));
		}
		System.out.println("Max buffer size: " + maxBufferSize);
	}

	public static void handleDetailGetting() {
		eventList.plan(new Event(0, uniform(time + A, B)));
		for (int i = 0; i < 3; i++) {
			List<Queue<Detail>> sortedList = new ArrayList<Queue<Detail>>(queueList);
			sortedList.sort(new Comparator<Queue<Detail>>() {
				public int compare(Queue<Detail> q1, Queue<Detail> q2) {
					return Integer.compare(q1.size(), q2.size());
				}
			});
			sortedList.get(0).add(new Detail());
			detailCount++;
		}

		for (int i = 0; i < queueList.size(); i++) {
			if (queueList.get(i).size() > maxQueueSizes.get(i)) {
				maxQueueSizes.set(i, queueList.get(i).size());
			}
		}

		for (int i = 0; i < firstMachinesCount; i++) {
			Machine machine = firstMachines.get(i);
			if (!machine.isBusy() && queueList.get(i).size() > 0) {
				machine.setBusy(true, queueList.get(i).poll());
				eventList.plan(new Event(1, uniform(time + T[i % 2], dt[i % 2]), i));
			}
		}
	}

	public static void handleTreatmentOnStage(int stage, int machineNumber) {
		Machine machine;
		Detail detail;
		if (stage == 1) {
			machine = firstMachines.get(machineNumber);
			detail = machine.getDetail();
			machine.setBusy(false, null);

			int pos = random.nextInt(100) + 1;
			if (pos > K[machineNumber % 2]) {
				buffer.add(detail);
				if (buffer.size() > maxBufferSize) {
					maxBufferSize = buffer.size();
				}
				if (buffer.size() > 3) {
					eventList.plan(new Event(2, time));
					for (int i = 0; i < secondMachines.size(); i++) {
						Machine secondMachine = secondMachines.get(i);
						if (!secondMachine.isBusy()) {
							secondMachine.setBusy(true, buffer.poll());
							eventList.plan(new Event(3, exponential(time + T3), i));
							break;
						}
					}
				} else {
					Machine secondMachine = secondMachines.get(0);
					if (!secondMachine.isBusy()) {
						secondMachine.setBusy(true, buffer.poll());
						eventList.plan(new Event(3, exponential(time + T3), 0));
					}
				}

			} else {
				if (detail.getTreatmentCount() > 1) {
					detail = null;
				} else {
					detail.incrementTreatmentCount();
					queueList.get(machineNumber).add(detail);
				}
			}

			Queue<Detail> queue = queueList.get(machineNumber);
			if (queue.size() > 0) {
				machine.setBusy(true, queue.poll());
			}
		} else if (stage == 2) {
			machine = secondMachines.get(machineNumber);
			detail = machine.getDetail();
			machine.setBusy(false, null);

			readyDetails.add(detail);

			if (machineNumber == 0) {
				if (buffer.size() > 0) {
					machine.setBusy(true, buffer.poll());
					eventList.plan(new Event(3, exponential(time + T3), machineNumber));
				}
			} else {
				if (buffer.size() > 3) {
					machine.setBusy(true, buffer.poll());
					eventList.plan(new Event(3, exponential(time + T3), machineNumber));
				}
			}
		}
	}

	public static void handleBufferOverflow() {
		secondMachines.add(new Machine());
	}

	public static int uniform(int average, int deviation) {
		return average + random.nextInt(deviation * 2) - deviation;
	}

	public static int exponential(int average) {
		return (int) (average - 1.0 * Math.log(random.nextDouble()));
	}
}