/**
 * 
 */
package org.cloudbus.cloudsim.edge.lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cloudbus.cloudsim.edge.util.Request;

/**
 * @author Brice Kamneng Kwam
 *
 */
public class RequestList {

	public static <T extends Request> void sortById(List<T> requestList) {
		Collections.sort(requestList, new Comparator<T>() {

			/**
			 * Compares two objects.
			 * 
			 * @param a the first Object to be compared
			 * @param b the second Object to be compared
			 * @return the value 0 if both Objects are numerically equal; a value less than 0 if the
			 *         first Object is numerically less than the second Object; and a value greater
			 *         than 0 if the first Object is numerically greater than the second Object.
			 * @throws ClassCastException <tt>a</tt> and <tt>b</tt> are expected to be of type
			 *             <tt>Cloudlet</tt>
			 * @pre a != null
			 * @pre b != null
			 * @post $none
			 */
			@Override
			public int compare(T a, T b) throws ClassCastException {
				Integer cla = Integer.valueOf(a.getId());
				Integer clb = Integer.valueOf(b.getId());
				return cla.compareTo(clb);
			}
		});
	}
}
