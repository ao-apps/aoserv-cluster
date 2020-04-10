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
 * A 64-bit implementation of PhysicalVolumeConfiguration to be used
 * when any of the three extents fields do not fit into 32 bits.  This will be
 * the case when the logical or phyiscal volumes reach 128 EB.
 *
 * @author  AO Industries, Inc.
 */
public class PhysicalVolumeConfigurationLong extends PhysicalVolumeConfiguration implements Serializable {

	private static final long serialVersionUID = 1L;

	final long firstLogicalExtent;
	final long firstPhysicalExtent;
	final long extents;

	PhysicalVolumeConfigurationLong(
		PhysicalVolume physicalVolume,
		long firstLogicalExtent,
		long firstPhysicalExtent,
		long extents
	) {
		super(physicalVolume);
		assert firstLogicalExtent>=0 : "firstLogicalExtent<0: "+firstLogicalExtent;
		assert firstPhysicalExtent>=0 : "firstPhysicalExtent<0: "+firstPhysicalExtent;
		assert extents>0 : "extents<=0: "+extents;
		assert
			firstLogicalExtent>Integer.MAX_VALUE
			|| firstPhysicalExtent>Integer.MAX_VALUE
			|| extents>Integer.MAX_VALUE
			: "At least one of firstLogicalExtent, firstPhysicalExtent, and extents must be greater than Integer.MAX_VALUE";
		this.firstLogicalExtent = firstLogicalExtent;
		this.firstPhysicalExtent = firstPhysicalExtent;
		this.extents = extents;
	}

	@Override
	public long getFirstLogicalExtent() {
		return firstLogicalExtent;
	}

	@Override
	public long getFirstPhysicalExtent() {
		return firstPhysicalExtent;
	}

	@Override
	public long getExtents() {
		return extents;
	}
}
