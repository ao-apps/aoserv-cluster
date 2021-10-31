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
package com.aoindustries.aoserv.cluster.analyze;

/**
 * For each check, assigns a level associated with any problems.  The recommended
 * uses for each level generally consider reliability problems as more significant
 * than performance problems.
 *
 * @author  AO Industries, Inc.
 */
public enum AlertLevel {

	/**
	 * Indicates the resource is optimal.
	 */
	NONE,

	/**
	 * Generally indicates the resource is runnable but nonoptimal in some way that only
	 * slightly degrades performance and has no affect on reliability.
	 */
	LOW,

	/**
	 * Generally indicates the resource is either runnable with significantly degraded performance
	 * or runnable with slightly degraded reliability.
	 */
	MEDIUM,

	/**
	 * Generally indicates the resources is runnable with significantly degraded reliability.
	 */
	HIGH,

	/**
	 * Indicates the resource is not runnable.
	 */
	CRITICAL
}
