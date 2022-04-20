/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2008-2011, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.lang.io.IoUtils;
import com.aoindustries.aoserv.cluster.ClusterConfiguration;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This simply returns a random number between 0 and 1.  The results may be different
 * for each call on the same configuration - this may have unexpected consequences.
 *
 * @author  AO Industries, Inc.
 */
public class RandomHeuristicFunction implements HeuristicFunction {

  /**
   * A fast pseudo-random number generator for non-cryptographic purposes.
   */
  private static final Random fastRandom = new Random(IoUtils.bufferToLong(new SecureRandom().generateSeed(Long.BYTES)));

  @Override
  public double getHeuristic(ClusterConfiguration clusterConfiguration, int g) {
    return fastRandom.nextDouble();
  }
}
