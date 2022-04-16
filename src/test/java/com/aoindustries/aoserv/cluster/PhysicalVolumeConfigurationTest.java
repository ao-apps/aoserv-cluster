/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.io.IoUtils;
import java.awt.Rectangle;
import java.security.SecureRandom;
import java.util.Random;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * @author  AO Industries, Inc.
 */
@SuppressWarnings("deprecation")
public class PhysicalVolumeConfigurationTest {

	/**
	 * A fast pseudo-random number generator for non-cryptographic purposes.
	 */
	private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(Long.BYTES)));

	@Test
	public void testOverlaps() {
		for(int c = 1; c < 100000; c++) {
			int start1 = fastRandom.nextInt(c);
			int extents1 = fastRandom.nextInt(c)+1;
			int start2 = fastRandom.nextInt(c);
			int extents2 = fastRandom.nextInt(c)+1;
			boolean overlaps = PhysicalVolumeConfiguration.overlaps(start1, extents1, start2, extents2);
			Rectangle r1 = new Rectangle(start1, 0, extents1, 1);
			Rectangle r2 = new Rectangle(start2, 0, extents2, 1);
			boolean overlaps2 = r1.intersects(r2);
			if(overlaps != overlaps2) {
				fail(
					"Not equal: start1 = " + start1
					+ ", extents1 = " + extents1
					+ ", start2 = " + start2
					+ ", extents2 = " + extents2
				);
			}
		}
	}
}
