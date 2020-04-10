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
 * along with aoserv-cluster.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.cluster.optimize;

import com.aoindustries.aoserv.cluster.Dom0;
import com.aoindustries.aoserv.cluster.DomU;

/**
 * A swap between primary and secondary.
 *
 * @author  AO Industries, Inc.
 */
public class MoveSecondaryTransition extends Transition {

	private final DomU domU;
	private final Dom0 oldSecondaryDom0;
	private final Dom0 newSecondaryDom0;

	MoveSecondaryTransition(
		DomU domU,
		Dom0 oldSecondaryDom0,
		Dom0 newSecondaryDom0
	) {
		this.domU = domU;
		this.oldSecondaryDom0 = oldSecondaryDom0;
		this.newSecondaryDom0 = newSecondaryDom0;
	}

	public DomU getDomU() {
		return domU;
	}

	public Dom0 getOldSecondaryDom0() {
		return oldSecondaryDom0;
	}

	public Dom0 getNewSecondaryDom0() {
		return newSecondaryDom0;
	}

	@Override
	public String toString() {
		return "Move "+domU.getHostname()+" secondary from "+oldSecondaryDom0.getHostname()+" to "+newSecondaryDom0.getHostname();
	}
}
