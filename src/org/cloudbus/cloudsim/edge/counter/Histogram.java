package org.cloudbus.cloudsim.edge.counter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Histogram class 
 * Basic abstract class for all histograms 
 * Defining interface and functions of histograms
 * 
 * @author David Hock, Alexander Klein, Dirk Staehle
 * @version 1.1.0
 * @since 2005-11-06
 *
 */
public abstract class Histogram {
	/**
	 * Attribute: Values below this value increment the "first" interval
	 */
	public double			lowerBound;
	/**
	 * Attribute: Values greater than this value increment the "last" interval
	 */
	public double			upperBound;
	/**
	 * Attribute: Interval size
	 */
	public double			delta;
	/**
	 * Attribute: Number of intervals
	 */
	public int				numIntervals;
	/**
	 * Attribute: Vector that stores the histogram data
	 */
	public Vector<Double>	bins;
	/**
	 * Attribute: name of the observed variable
	 */
	public String			observedVariable;
	/**
	 * Attribute: filename where to save the collected information
	 */
	public String			filename;
	/**
	 * Attribute: String representing the type of the histogram
	 */
	
	public String			type;
	/**
	 * Attribute: print format of doubles
	 */
	public DecimalFormat	df	= new DecimalFormat("0.00");
	
	static Logger logger = LogManager.getLogger(Histogram.class.getName());
	
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
	public Histogram(String oVariable, String fname, double lB, double uB,
			int num)
	{
		this.observedVariable = oVariable;
		this.filename = fname;
		this.lowerBound = lB;
		this.upperBound = uB;
		this.numIntervals = num;
		this.delta = (this.upperBound - this.lowerBound) / this.numIntervals;
		this.bins = new Vector<Double>(this.numIntervals);
		for (int i = 0; i < this.numIntervals; i++)
		{
			this.bins.add(i, new Double(0));
		}
		this.type = "This is the Base class";
	}
	
	/**
	 * Method initializes the histogram by preparing the vector for counting
	 */
	public void reset()
	{
		for (int i = 0; i < this.numIntervals; i++)
		{
			this.bins.set(i, new Double(0));
		}
	}
	
	/**
	 * Function returns the number of the interval that has to be incremented
	 * 
	 * @param x
	 *            the value to count
	 * @return the index of the interval
	 */
	public int getBinNumber(double x)
	{
		if (x < this.lowerBound)
		{
			return 0;
		} else if (x >= this.upperBound)
		{
			return this.numIntervals - 1;
		} else
		{
			return ((int) (Math.floor((x - this.lowerBound) / this.delta)));
		}
	}
	
	/**
	 * Function sets the number of intervals to the value of the given argument
	 * i and resets the vector.
	 * 
	 * @param i
	 *            the new number of intervals of the histogram
	 */
	public void setupNumIntervals(int i)
	{
		this.numIntervals = i;
		this.bins.setSize(this.numIntervals);
		this.delta = (this.upperBound - this.lowerBound) / this.numIntervals;
		this.reset();
	}
	
	/**
	 * Abstract function count(double x) that has to be implemented by extending
	 * classes
	 * @param x the x to count
	 */
	public abstract void count (double x);
	/**
	 * Abstract function divisor() that has to be implemented by extending
	 * classes
	 * @return the divisor necessary for histogram calculations
	 */
	public abstract double divisor();
	
	/**
	 * Function tries to write the counted values to three files. Filename is
	 * the name of the observed variable + extension. The first file with
	 * extension ".hist" represents the usual histogram. The second file with
	 * extension ".epdf" represents the probability density function. The third
	 * file with extension ".dist" represents the distribution.
	 * 
	 * @throws Exception
	 *             if the files can not be created or overwritten.
	 */
	public void report()
	{
		this.report(this.filename);
	}
	
	/**
	 * Save information to a file
	 * @param theFilename the name of the file
	 */
	public void report_lower_upper(String theFilename)
	{
		//System.out.println(theFilename);
		File fhist = new File(theFilename);
		fhist.delete();
		try
		{
			fhist.createNewFile();
		} catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		try
		{
			FileOutputStream ohist = new FileOutputStream(fhist);
			for (int i = 0; i < this.numIntervals; i++)
			{
				ohist.write(
						(""
								+ String.valueOf(this.lowerBound + i * this.delta)
								+ " , "
								+ String.valueOf(this.lowerBound + (i + 1) * this.delta) 
								+ " , "
								+ this.bins.get(i) 
								+ "\n"
						).getBytes());
			}
			ohist.close();
		} catch (Exception e)
		{
			logger.error(e.getMessage());
		}

	}

	public void report(String theFilename)
	{
		double sum=0;
		for (int i = 0; i < this.numIntervals; i++)
		{
			sum+=this.bins.get(i);
		}
		//System.out.println(theFilename);
		File fhist = new File(theFilename);
		fhist.delete();
		try
		{
			fhist.createNewFile();
		} catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		try
		{
			FileOutputStream ohist = new FileOutputStream(fhist);
			for (int i = 0; i < this.numIntervals; i++)
			{
				ohist.write(
						(""
								+ String.valueOf(this.lowerBound + (i+0.5) * this.delta) 
								+ " , "
								+ this.bins.get(i)/sum 
								+ "\n"
						).getBytes());
			}
			ohist.close();
		} catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		
	}
	
}