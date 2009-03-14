/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

/**
 * Any type may be used, but higher RAID levels are preferred for non-optimal solutions.
 *
 * @author  AO Industries, Inc.
 */
public enum RaidType {
    JBOD,
    RAID0,
    RAID1,
    RAID5,
    RAID6
}
