/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020, 2021, 2022  AO Industries, Inc.
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
import java.util.Map;

/**
 * A physical disk that is used for LVM.  It is split into
 * partitions, each of which is a physical volume.
 *
 * @author  AO Industries, Inc.
 */
public class Dom0Disk implements Comparable<Dom0Disk>, Serializable {

	private static final long serialVersionUID = 2L;

	final String clusterName;
	final String dom0Hostname;
	final String device;
	final int diskSpeed;
	final Map<Short, PhysicalVolume> unmodifiablePhysicalVolumes;

	/**
	 * unmodifiablePhysicalVolumes MUST BE UNMODIFIABLE
	 *
	 * @see Cluster#addDom0Disk
	 */
	Dom0Disk(
		String clusterName,
		String dom0Hostname,
		String device,
		int diskSpeed,
		Map<Short, PhysicalVolume> unmodifiablePhysicalVolumes
	) {
		this.clusterName = clusterName;
		this.dom0Hostname = dom0Hostname;
		this.device = device;
		this.diskSpeed = diskSpeed;
		this.unmodifiablePhysicalVolumes = unmodifiablePhysicalVolumes;
	}

	public String getClusterName() {
		return clusterName;
	}

	public String getDom0Hostname() {
		return dom0Hostname;
	}

	/**
	 * Gets the per-Dom0 unique device name.
	 */
	public String getDevice() {
		return device;
	}

	public int getDiskSpeed() {
		return diskSpeed;
	}

	/**
	 * Gets the unmodifiable set of physical volumes for this disk.
	 */
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
	public Map<Short, PhysicalVolume> getPhysicalVolumes() {
		return unmodifiablePhysicalVolumes;
	}

	/**
	 * Gets the physical volume for the specified partition number of <code>null</code> if not found
	 */
	public PhysicalVolume getPhysicalVolume(short partition) {
		return unmodifiablePhysicalVolumes.get(partition);
	}

	@Override
	public String toString() {
		return toString(clusterName, dom0Hostname, device);
	}

	static String toString(String clusterName, String dom0Hostname, String device) {
		return Dom0.toString(clusterName, dom0Hostname)+':'+device;
	}

	/**
	 * Sorted ascending by:
	 * <ol>
	 *   <li>clusterName</li>
	 *   <li>dom0Hostname</li>
	 *   <li>diskSpeed</li>
	 *   <li>device</li>
	 * </ol>
	 */
	@Override
	public int compareTo(Dom0Disk other) {
		if(this==other) return 0;

		int diff = clusterName.compareTo(other.clusterName);
		if(diff!=0) return diff;

		diff = dom0Hostname.compareTo(other.dom0Hostname);
		if(diff!=0) return diff;

		diff = diskSpeed - other.diskSpeed;
		if(diff!=0) return diff;

		return device.compareTo(other.device);
	}
}
