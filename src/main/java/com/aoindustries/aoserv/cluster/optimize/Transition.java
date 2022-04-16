/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020, 2021, 2022  AO Industries, Inc.
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

package com.aoindustries.aoserv.cluster.optimize;

/**
 * A transition is one of the possible conversions of clusterConfiguration state.
 * Other transitions could include:
 *     pvmove
 *     vgextend -\
 *                if we allow a mapping where not all extents have been reached,
 *                this may be helpful when growing/shrinking existing VMs.
 *     vgreduce -/
 *     physically moving a hard drive
 *     adding more hardware/servers
 *
 * @author  AO Industries, Inc.
 */
public abstract class Transition {

	Transition() {
		// Do nothing
	}
}
