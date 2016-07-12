package org.cloudbus.cloudsim.edge.counter;

/**
 * TimeIndependentCounter class 
 * Class just counts the given values without regarding
 * the time the system remained in this state.
 * 
 * @author David Hock, Alexander Klein, Dirk Staehle
 * @version 1.1.0
 * @since 2005-11-06
 *
 */
public class TimeIndependentCounter extends Counter {
	/**
	 * Attribute: Number of samples taken
	 */
	public long	numSamples;
	
	/**
	 * Constructor that uses super constructor to initialize the DiscreteCounter
	 * @param name name of the Counter
	 */
	public TimeIndependentCounter(String name)
	{
		super(name);
		this.numSamples = 0;
	}
	
	/**
	 * Method resets all attributes
	 */
	@Override
	public void reset()
	{
		super.reset();
		this.numSamples = 0;
	}
	
	/**
	 * Method calculates and returns the mean of the observed variable without
	 * regarding the counting time
	 * 
	 * @return the calculated time independent mean
	 */
	@Override
	public double getMean()
	{
		if (this.numSamples > 0)
		{
			return this.sumPowerOne / this.numSamples;
		}
		return 0;
	}
	
	/**
	 * Method calculates and returns the variance of the observed variable
	 * without regarding the counting time
	 * 
	 * @return the calculated time independent variance
	 */
	@Override
	public double getVariance()
	{
		if (this.numSamples > 1)
		{
			double tmp1 = this.getMean();
			double tmp2 = this.sumPowerTwo / this.numSamples;
			return this.numSamples / (double) (this.numSamples - 1)
					* (tmp2 - tmp1 * tmp1); // Equation 2.30
		}
		return 0;
	}
	
	/**
	 * Method counts the given argument by extending the "original" counting
	 * method.
	 * 
	 * @param x
	 *            the double value to count
	 */
	@Override
	public void count(double x)
	{
		super.count(x);
		this.sumPowerOne += x;
		this.sumPowerTwo += x * x;
		this.numSamples++;
	}
	
	/**
	 * Method visualizes the calculated statistics by using the report method of
	 * the super class.
	 */
	@Override
	public void report()
	{
		System.out.println("discrete counter\n");
		super.report();
		System.out.println("number of samples: " + this.numSamples);
	}
}
