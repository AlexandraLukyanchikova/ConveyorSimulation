package ru.ngtu.vst.sim;

import java.util.ArrayList;
import java.util.Collections;
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
	public static final int simulationTime = 8 * 60 * 5;
	public static final int firstMachinesCount = 2, secondMachinesCount = 2;
	public static final List<Queue<Detail>> queueList = new ArrayList<Queue<Detail>>();
	public static final Queue<Detail> buffer = new LinkedList<Detail>();
	public static final Random random = new Random(100000);
	public static final List<Machine> firstMachines = new ArrayList<Machine>();
	public static final List<Machine> secondMachines = new ArrayList<Machine>();
	public static final EventList eventList = new EventList();
	public static final List<Detail> readyDetails = new ArrayList<Detail>();
	public static int detailCount = 0;
	private static double r = 0.0;

	public static void main(String[] args) {
		for (int i = 0; i < firstMachinesCount; i++) {
			queueList.add(new LinkedList<Detail>());
		}
		for (int i = 0; i < firstMachinesCount; i++) {
			firstMachines.add(new Machine());
		}
		for (int i = 0; i < secondMachinesCount; i++) {
			secondMachines.add(new Machine());
		}
		eventList.plan(new Event(0, uniform(A, B)));
		eventList.plan(new Event(3, simulationTime));

		boolean finish = false;
		while (true) {
			Event currentEvent = eventList.getEvent();
			time = currentEvent.getTime();
			// 0 - getting of a detail
			// 1 - completion of the treatment in the 1st stage
			// 2 - completion of the treatment in the 2st stage
			// 3 - completion of the simulation
			switch (currentEvent.getCode()) {
			case 0:
				handleDetailGetting();
				break;
			case 1:
				handleTreatmentOnStage(1, currentEvent.getMachineNumber());
				break;
			case 2:
				handleTreatmentOnStage(2, currentEvent.getMachineNumber());
				break;
			case 3:
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
		for (int i = 0; i < firstMachines.size(); i++) {
			System.out.println("Work coef of machine #" + i + ": "
					+ (double) firstMachines.get(i).getWorkTime() / (double) simulationTime);
		}
		System.out.println(
				"Work coef of 2nd machine: " + (double) secondMachines.get(0).getWorkTime() / (double) simulationTime);
		System.out.println();

		profitsCalculation();
	}

	public static void profitsCalculation() {
		double expenses = S1 * firstMachinesCount * (simulationTime / 60)
				+ S2 * firstMachinesCount * (simulationTime / 60) + detailCount * S4 + detailCount * r * S5;
		double profit = readyDetails.size() * S3 - expenses;

		System.out.println("Expenses: " + expenses);
		System.out.println("Profit: " + profit);
	}

	public static void handleDetailGetting() {
		eventList.plan(new Event(0, time + uniform(A, B)));
		for (int i = 0; i < 3; i++) {
			List<Queue<Detail>> sortedList = new ArrayList<Queue<Detail>>(queueList);
			Collections.sort(sortedList, new Comparator<Queue<Detail>>() {
				public int compare(Queue<Detail> q1, Queue<Detail> q2) {
					return Integer.compare(q1.size(), q2.size());
				}
			});
			sortedList.get(0).add(new Detail());
			detailCount++;
		}

		for (int i = 0; i < firstMachinesCount; i++) {
			Machine machine = firstMachines.get(i);
			if (!machine.isBusy() && queueList.get(i).size() > 0) {
				machine.setBusy(true, queueList.get(i).poll(), time);
				eventList.plan(new Event(1, time + uniform(T[i % 2], dt[i % 2]), i));
			}
		}
	}

	public static void handleTreatmentOnStage(int stage, int machineNumber) {
		Machine machine;
		Detail detail;
		if (stage == 1) {
			machine = firstMachines.get(machineNumber);
			detail = machine.getDetail();
			machine.setBusy(false, null, time);

			int pos = random.nextInt(100) + 1;
			if (pos > K[machineNumber % 2] - r) {
				buffer.add(detail);
				if (buffer.size() > 3) {
					for (int i = 1; i < secondMachines.size(); i++) {
						Machine secondMachine = secondMachines.get(i);
						if (!secondMachine.isBusy()) {
							secondMachine.setBusy(true, buffer.poll(), time);
							eventList.plan(new Event(2, time + exponential(T3), i));
							break;
						}
					}
				} else {
					Machine secondMachine = secondMachines.get(0);
					if (!secondMachine.isBusy()) {
						secondMachine.setBusy(true, buffer.poll(), time);
						eventList.plan(new Event(2, time + exponential(T3), 0));
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
				machine.setBusy(true, queue.poll(), time);
				eventList
						.plan(new Event(1, time + uniform(T[machineNumber % 2], dt[machineNumber % 2]), machineNumber));
			}
		} else if (stage == 2) {
			machine = secondMachines.get(machineNumber);
			detail = machine.getDetail();
			machine.setBusy(false, null, time);

			readyDetails.add(detail);

			if (machineNumber == 0) {
				if (buffer.size() > 0) {
					machine.setBusy(true, buffer.poll(), time);
					eventList.plan(new Event(2, time + exponential(T3), machineNumber));
				}
			} else {
				if (buffer.size() > 3) {
					machine.setBusy(true, buffer.poll(), time);
					eventList.plan(new Event(2, time + exponential(T3), machineNumber));
				}
			}
		}
	}

	public static int uniform(int average, int deviation) {
		int res = (int) (average + random.nextDouble() * 2 * deviation - deviation);
		return res;
	}

	public static int exponential(int average) {
		int sign = random.nextInt(2);
		if (sign == 0) {
			return (int) (average - Math.log(random.nextDouble()));
		} else {
			return (int) (average + Math.log(random.nextDouble()));
		}

	}
}