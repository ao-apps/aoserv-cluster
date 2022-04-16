/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aoserv-cluster.
 *
 * aoserv-cluster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aoserv-cluster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aoserv-cluster.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * @author  AO Industries, Inc.
 */
public class PhysicalVolume implements Comparable<PhysicalVolume>, Serializable {

	private static final long serialVersionUID = 2L;

	final String clusterName;
	final String dom0Hostname;
	final String device;
	final short partition;
	final long extents;

	/**
	 * @see Dom0Disk#addPhysicalVolume
	 */
	PhysicalVolume(String clusterName, String dom0Hostname, String device, short partition, long extents) {
		assert extents>0 : "extents<=0: "+extents;
		this.clusterName = clusterName;
		this.dom0Hostname = dom0Hostname;
		this.device = device;
		this.partition = partition;
		this.extents = extents;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getDom0Hostname() {
		return dom0Hostname;
	}

	public String getDevice() {
		return device;
	}

	public short getPartition() {
		return partition;
	}

	public long getExtents() {
		return extents;
	}

	@Override
	public String toString() {
		return toString(clusterName, dom0Hostname, device, partition);
	}

	static String toString(String clusterName, String dom0Hostname, String device, short partition) {
		return Dom0Disk.toString(clusterName, dom0Hostname, device)+partition;
	}

	/**
	 * Sorted ascending by:
	 * <ol>
	 *   <li>clusterName</li>
	 *   <li>dom0Hostname</li>
	 *   <li>device</li>
	 *   <li>partition</li>
	 * </ol>
	 */
	@Override
	public int compareTo(PhysicalVolume other) {
		if(this==other) return 0;

		int diff = clusterName.compareTo(other.clusterName);
		if(diff!=0) return diff;

		diff = dom0Hostname.compareTo(other.dom0Hostname);
		if(diff!=0) return diff;

		diff = device.compareTo(other.device);
		if(diff!=0) return diff;

		return other.partition - partition;
	}
}
