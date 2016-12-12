package ru.ngtu.vst.sim;

public class Machine {
	private boolean busy = false;
	private Detail detail = null;
	private int workTime = 0;
	private int startTime = 0;

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy, Detail detail, int time) {
		this.busy = busy;
		this.detail = detail;
		if (this.busy) {
			this.startTime = time;
		} else {
			this.workTime += time - this.startTime;
		}
	}

	public Detail getDetail() {
		return this.detail;
	}
	
	public int getWorkTime()
	{
		return this.workTime;
	}
}
