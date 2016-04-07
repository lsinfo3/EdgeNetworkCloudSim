package org.cloudbus.cloudsim.ext.counter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * TimeDependentCounter class 
 * Class counts the given values REGARDING the simulation time
 * 
 * @author David Hock, Alexander Klein, Dirk Staehle
 * @version 1.1.0
 * @since 2005-11-06
 *
 */
public class TimeDependentCounter extends Counter {
	/**
	 * Attribute: Simulation time of the last sample
	 */
	public long	lastSampleTime;
	/**
	 * Attribute: Simulation time of the first sample
	 */
	public long	firstSampleTime;
	
	static Logger logger = LogManager.getLogger(TimeDependentCounter.class.getName());
	
	/**
	 * Constructor that uses the given argument and the super constructor to
	 * initialize the TDCounter
	 * @param name name of the Counter
	 */
	public TimeDependentCounter(String name)
	{
		super(name);
		this.lastSampleTime = 0;
		this.firstSampleTime = 0;
	}
	
	/**
	 * Method resets all attributs
	 */
	@Override
	public void reset()
	{
		super.reset();
		this.lastSampleTime = 0;
		this.firstSampleTime = 0;
	}
	
	/**
	 * Method calculates and returns the mean of the observed variable regarding
	 * the counting time
	 * 
	 * @return the calculated time dependent mean
	 */
	@Override
	public double getMean()
	{
		long tmp = this.lastSampleTime - this.firstSampleTime;
		if (tmp > 0)
		{
			return this.sumPowerOne / tmp;
		}
		return 0;
	}
	
	/**
	 * Method calculates and returns the var of the observed variable regarding
	 * the counting time
	 * 
	 * @return the calculated time dependent var
	 */
	@Override
	public double getVariance()
	{
		long tmp = this.lastSampleTime - this.firstSampleTime;
		double tmp2 = this.getMean();
		if (tmp > 0)
		{
			return this.sumPowerTwo / tmp - tmp2 * tmp2;
		}
		return 0;
	}
	
	/**
	 * Method counts the given argument (regarding the time the system remained
	 * in this state) by extending the "original" counting method.
	 * 
	 * @param x
	 *            the double value to count
	 */
	@Override
	public void count(double x)
	{
//		double tdiff = SimState.getNow() - this.lastSampleTime;
		double tdiff = CloudSim.clock() - this.lastSampleTime;
		if (tdiff < 0)
		{
//			logger.info("last = " + this.lastSampleTime + " now = "
//					+ SimState.getNow());
			logger.info("last = " + this.lastSampleTime + " now = "
					+ CloudSim.clock());
			System.exit(-1);
		}
		this.sumPowerOne += (x * tdiff);
		this.sumPowerTwo += (x * x * tdiff);
//		this.lastSampleTime = SimState.getNow();
		this.lastSampleTime = (long) CloudSim.clock();
	}
	
	/**
	 * Method visualizes the calculated statistics by using the report method of
	 * the super class.
	 */
	@Override
	public void report()
	{
		logger.info("continuous counter\n");
		super.report();
		logger.info("interval length: "
				+ (this.lastSampleTime - this.firstSampleTime));
	}
}
