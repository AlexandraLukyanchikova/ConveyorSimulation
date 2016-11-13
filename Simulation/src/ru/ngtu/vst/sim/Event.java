package ru.ngtu.vst.sim;

public class Event {
	private int code;
	private int time;
	private int machineNumber;

	Event(int code, int time) {
		this.code = code;
		this.time = time;
		this.machineNumber = -1;
	}
	
	Event(int code, int time, int machineNumber)
	{
		this.code = code;
		this.time = time;
		this.machineNumber = machineNumber;
	}
	
	public int getCode()
	{
		return this.code;
	}
	
	public int getTime()
	{
		return this.time;
	}
	
	public int getMachineNumber()
	{
		return this.machineNumber;
	}
}
