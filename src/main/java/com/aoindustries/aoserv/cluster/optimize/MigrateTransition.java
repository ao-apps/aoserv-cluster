/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2020  AO Industries, Inc.
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

import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.DomU;

/**
 * A swap between primary and secondary.
 *
 * @author  AO Industries, Inc.
 */
public class MigrateTransition extends Transition {

	private final DomU domU;
	private final Dom0 oldPrimaryDom0;
	private final Dom0 oldSecondaryDom0;

	MigrateTransition(
		DomU domU,
		Dom0 oldPrimaryDom0,
		Dom0 oldSecondaryDom0
	) {
		this.domU = domU;
		this.oldPrimaryDom0 = oldPrimaryDom0;
		this.oldSecondaryDom0 = oldSecondaryDom0;
	}

	public DomU getDomU() {
		return domU;
	}

	public Dom0 getOldPrimaryDom0() {
		return oldPrimaryDom0;
	}

	public Dom0 getOldSecondaryDom0() {
		return oldSecondaryDom0;
	}

	@Override
	public String toString() {
		return "Migrate "+domU.getHostname()+" from "+oldPrimaryDom0.getHostname()+" to "+oldSecondaryDom0.getHostname();
	}
}
