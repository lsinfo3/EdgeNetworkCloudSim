package org.cloudbus.cloudsim.edge.counter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Counter class 
 * Basic class for all counting objects
 * 
 * @author David Hock, Alexander Klein, Dirk Staehle
 * @version 1.1.0
 * @since 2005-11-06
 */
abstract class Counter {
	/**
	 * Attribute: Sum
	 */
	double	sumPowerOne;
	/**
	 * Attribute: SumPowerTwo
	 */
	double	sumPowerTwo;
	/**
	 * Attribute: min counted value
	 */
	double	min;
	/**
	 * Attribute: max counted value
	 */
	double	max;
	/**
	 * Attribute: Name of the observed variable
	 */
	String	observedVariable	= "";
	
	static Logger logger = LogManager.getLogger(Counter.class.getName());
	
	/**
	 * Constructor uses the reset() method to initialize
	 */
	public Counter()
	{
		this.reset();
	}
	
	/**
	 * Constructor
	 * 
	 * @param name
	 *            of the observed variable
	 */
	public Counter(String name)
	{
		this.reset();
		this.observedVariable = name;
	}
	
	/**
	 * Abstract method getMean () used to define the interface of all counters
	 * 
	 * @return the mean corresponding to the counter
	 */
	public abstract double getMean();
	
	/**
	 * Abstract method getVariance () used to define the interface of all
	 * counters
	 * 
	 * @return the variance corresponding to the counter
	 */
	public abstract double getVariance();
	
	/**
	 * Function returns the minimum of all counted values
	 * 
	 * @return the minimum of all counted values
	 */
	public double getMin()
	{
		return this.min;
	}
	
	/**
	 * Function returns the maximum of all counted values
	 * 
	 * @return the maximum of all counted values
	 */
	public double getMax()
	{
		return this.max;
	}
	
	/**
	 * Function calculates and returns the standard deviation by using the
	 * getVariance() function.
	 * 
	 * @return the standard deviation
	 */
	public double getStdDeviation()
	{
		return Math.sqrt(this.getVariance());
	}
	
	/**
	 * Function calculates and returns the variation coefficient by using the
	 * getStdDeviation() and getMean() functions.
	 * 
	 * @return the variation coefficient
	 */
	public double getCvar()
	{
		double tmp = this.getMean();
		if (tmp == 0)
		{
			return (this.getStdDeviation() == 0 ? 0 : Double.MAX_VALUE);
		}
		return (this.getStdDeviation() / tmp);
	}
	
	/**
	 * Function resets all attributes needed to calculate the statistic values
	 */
	public void reset()
	{
		this.sumPowerOne = 0;
		this.sumPowerTwo = 0;
		this.min = Double.MAX_VALUE;
		this.max = Double.MIN_VALUE;
	}
	
	/**
	 * Function only sets the min and the max counted value. Extending classes
	 * have to implement additional functionality. Hint: Time dependent counting
	 * differs from time independent counting
	 * 
	 * @param x
	 *            the value to count
	 */
	public void count(double x)
	{
		this.min = (x < this.min ? x : this.min);
		this.max = (x > this.max ? x : this.max);
	}
	
	/**
	 * Function prints all statistic values
	 */
	public void report()
	{
		logger.info("observed random variable: " + this.observedVariable);
		logger.info("mean:                     " + this.getMean());
		logger.info("variance:                 " + this.getVariance());
		logger.info("standard deviation:       "
				+ this.getStdDeviation());
		logger.info("coefficient of variation: " + this.getCvar());
		logger.info("minimum:                  " + this.getMin());
		logger.info("maximum:                  " + this.getMax());
		
	}
	
}
