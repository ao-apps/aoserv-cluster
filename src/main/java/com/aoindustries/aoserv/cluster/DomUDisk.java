/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020  AO Industries, Inc.
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
 * along with aoserv-cluster.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * A single virtual disk device and its requirements.
 *
 * @author  AO Industries, Inc.
 */
public class DomUDisk implements Comparable<DomUDisk>, Serializable {

	private static final long serialVersionUID = 2L;

	/**
	 * This is the standard size of the extents in bytes.
	 */
	public static final int EXTENTS_SIZE = 33554432;

	final String clusterName;
	final String domUHostname;
	final String device;
	final int minimumDiskSpeed;
	final long extents;
	final short weight;

	DomUDisk(
		String clusterName,
		String domUHostname,
		String device,
		int minimumDiskSpeed,
		long extents,
		short weight
	) {
		if(minimumDiskSpeed!=-1 && minimumDiskSpeed<=0) throw new IllegalArgumentException(this+": Invalid value for minimumDiskSpeed: "+minimumDiskSpeed);
		if(extents<1) throw new IllegalArgumentException(this+": extents should be >=1: "+extents);
		if(weight<1 || weight>1024) throw new IllegalArgumentException(this+": Invalid value for weight, should be in range 1-1024: "+weight);

		this.clusterName = clusterName;
		this.domUHostname = domUHostname;
		this.device = device;
		this.minimumDiskSpeed = minimumDiskSpeed;
		this.extents = extents;
		this.weight = weight;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getDomUHostname() {
		return domUHostname;
	}

	/**
	 * Gets the per-DomU unique device ID (usually /dev/xvd[a-z]).
	 */
	public String getDevice() {
		return device;
	}

	/**
	 * Gets the minimum disk speed or <code>-1</code> if doesn't matter.
	 */
	public int getMinimumDiskSpeed() {
		return minimumDiskSpeed;
	}

	/**
	 * Gets the number of LVM extents this virtual disk requires.
	 */
	public long getExtents() {
		return extents;
	}

	/**
	 * Gets the allocation weight in the range of 1-1024.
	 */
	public short getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return toString(clusterName, domUHostname, device);
	}

	static String toString(String clusterName, String domUHostname, String device) {
		return DomU.toString(clusterName, domUHostname)+':'+device;
	}

	/**
	 * Sorted ascending by:
	 * <ol>
	 *   <li>clusterName</li>
	 *   <li>domUHostname</li>
	 *   <li>device</li>
	 * </ol>
	 */
	@Override
	public int compareTo(DomUDisk other) {
		if(this==other) return 0;

		int diff = clusterName.compareTo(other.clusterName);
		if(diff!=0) return diff;

		diff = domUHostname.compareTo(other.domUHostname);
		if(diff!=0) return diff;

		return device.compareTo(other.device);
	}
}
