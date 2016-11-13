package ru.ngtu.vst.sim;

public class Machine {
	private boolean busy = false;
	private Detail detail = null;

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy, Detail detail) {
		this.busy = busy;
		this.detail = detail;
	}
	
	public Detail getDetail()
	{
		return this.detail;
	}
}
