package org.cloudbus.cloudsim.edge.random;

import java.util.Random;

@SuppressWarnings("serial")
public abstract class RNS extends Random {
	
	public RNS()
	{
		super();
	}

	public RNS(long seed)
	{
		super(seed);
	}

	public double next()
	{
		return 0;
	}

	// defines parameters by mean m[0] (and variance m[1] if required)
	abstract public void setMoments(double[] m);
}
