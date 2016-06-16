package org.cloudbus.cloudsim.ext;

/**
 * CloudSim does not execute events that are fired before the simulation has
 * started. Thus we need to buffer them and then refire when the simulation
 * starts.
 * 
 * @author nikolay.grozev
 * 
 */
public class PresetEvent {
    final int id;
    final int tag;
    final Object data;
    final double delay;

    public PresetEvent(final int id, final int tag, final Object data, final double delay) {
        super();
        this.id = id;
        this.tag = tag;
        this.data = data;
        this.delay = delay;
    }

	public int getId() {
		return id;
	}

	public int getTag() {
		return tag;
	}

	public Object getData() {
		return data;
	}

	public double getDelay() {
		return delay;
	}
}
