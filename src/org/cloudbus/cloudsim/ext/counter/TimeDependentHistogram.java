package org.cloudbus.cloudsim.ext.counter;

/**
 * TimeDependentHistogram class 
 * Class representing a time dependent histogram
 * 
 * @author David Hock, Alexander Klein, Dirk Staehle
 * @version 1.1.0
 * @since 2005-11-06
 *
 */
public class TimeDependentHistogram extends Histogram {
	/**
	 * Attribute: Simulation time of the last sample
	 */
	public long	lastSampleTime;
	/**
	 * Attribute: Simulation time of the first sample
	 */
	public long	firstSampleTime;
	
	/**
	 * Constructor used by extending classes to initialize the histogram
	 * 
	 * @param oVariable
	 *            name of the observed variable
	 * @param fname 
	 *            filename where to save
	 * @param lB
	 *            lower bound of the histogram
	 * @param uB
	 *            upper bound of the histogram
	 * @param num 
	 *            number of bounds 
	 */ 
	public TimeDependentHistogram(String oVariable, String fname, double lB,
			double uB, int num)
	{
		super(oVariable, fname, lB, uB, num);
		this.type = "continuous";
		this.lastSampleTime = 0;
		this.firstSampleTime = 0;
	}
	
	/**
	 * Function sets all attributes to initial value
	 */
	@Override
	public void reset()
	{
		super.reset();
		this.lastSampleTime = SimState.getNow();
		this.firstSampleTime = SimState.getNow();
	}
	
	/**
	 * Function counts the given argument and sets the attributes according to
	 * the SimState
	 * 
	 * @param x
	 *            the argument to count
	 */
	@Override
	public void count(double x)
	{
		int bin_index = this.getBinNumber(x);
		double tdiff = SimState.getNow() - this.lastSampleTime;
		this.bins.set(bin_index, (double) this.bins.get(bin_index) + tdiff);
		this.lastSampleTime = SimState.getNow();
	}
	
	/**
	 * Method calculates the divisor required for histogram calculations
	 * 
	 * @return calculate divisor
	 */
	@Override
	public double divisor()
	{
		return this.lastSampleTime - this.firstSampleTime;
	}
	
	/**
	 * String representing the type of the histogram
	 * 
	 * @return type of the histogram
	 */
	public String type()
	{
		return "continuous";
	}
}
