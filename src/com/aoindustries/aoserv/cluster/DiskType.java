package com.aoindustries.aoserv.cluster;

/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */

/**
 * Any type may be used, but faster types are preferred for non-optimal solutions.
 *
 * @author  AO Industries, Inc.
 */
public enum DiskType {
    IDE,
    SATA,
    SCSI,
    SAS
}
