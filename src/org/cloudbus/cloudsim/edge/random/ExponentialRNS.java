package org.cloudbus.cloudsim.edge.random;
public class ExponentialRNS extends RNS
{
	private double mean;

	public ExponentialRNS(double lambda)
	{
		super();
		mean=1/lambda;
	}

	public ExponentialRNS( double lambda, long seed)
	{
		super(seed);
		mean=1/lambda;
	}

	public void setParameters(double lambda)
	{
		mean=1/lambda;
	}

	public void setMoments(double[] m)
	{
		mean=m[0];
	}

	public double next()
	{
		double x=-Math.log(super.nextDouble())*mean;
		return x;
	}

}
