package ru.ngtu.vst.sim;

public class Detail {
	private int treatmentCount = 1;

	public void incrementTreatmentCount()
	{
		this.treatmentCount++;
	}
	
	public int getTreatmentCount()
	{
		return this.treatmentCount;
	}
}
