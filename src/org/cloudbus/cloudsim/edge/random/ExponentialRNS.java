package org.cloudbus.cloudsim.edge.random;

@SuppressWarnings("serial")
public class ExponentialRNS extends RNS {
	private double mean;

	public ExponentialRNS(double lambda) {
		super();
		mean = 1 / lambda;
	}

	public ExponentialRNS(double lambda, long seed) {
		super(seed);
		mean = 1 / lambda;
	}

	public void setParameters(double lambda) {
		mean = 1 / lambda;
	}

	@Override
	public void setMoments(double[] m) {
		mean = m[0];
	}

	public double next() {
		double x = -Math.log(super.nextDouble()) * mean;
		return x;
	}
	public static void main(String[] args) {
		ExponentialRNS er = new ExponentialRNS(1, 1);
		double t[]  ={5};
		er.setMoments(t);
		for (int i = 0; i < 10; i++) {
			System.out.println(er.next());
		}
	}
}
