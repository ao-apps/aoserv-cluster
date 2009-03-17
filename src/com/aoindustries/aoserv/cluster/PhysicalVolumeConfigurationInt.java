/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * A 32-bit implementation of PhysicalVolumeConfiguration to be used
 * when all three extents fields fit into 32 bits and at least one
 * value is greater than the 16-bit range.  This covers the range
 * from 1 TB through until the logical or phyiscal volumes reach 128 EB.
 *
 * @author  AO Industries, Inc.
 */
public class PhysicalVolumeConfigurationInt extends PhysicalVolumeConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    final int firstLogicalExtent;
    final int firstPhysicalExtent;
    final int extents;

    PhysicalVolumeConfigurationInt(
        PhysicalVolume physicalVolume,
        int firstLogicalExtent,
        int firstPhysicalExtent,
        int extents
    ) {
        super(physicalVolume);
        assert firstLogicalExtent>=0 : "firstLogicalExtent<0: "+firstLogicalExtent;
        assert firstPhysicalExtent>=0 : "firstPhysicalExtent<0: "+firstPhysicalExtent;
        assert extents>0 : "extents<=0: "+extents;
        assert
            firstLogicalExtent>Short.MAX_VALUE
            || firstPhysicalExtent>Short.MAX_VALUE
            || extents>Short.MAX_VALUE
            : "At least one of firstLogicalExtent, firstPhysicalExtent, and extents must be greater than Short.MAX_VALUE";
        this.firstLogicalExtent = firstLogicalExtent;
        this.firstPhysicalExtent = firstPhysicalExtent;
        this.extents = extents;
    }

    public long getFirstLogicalExtent() {
        return firstLogicalExtent;
    }

    public long getFirstPhysicalExtent() {
        return firstPhysicalExtent;
    }

    public long getExtents() {
        return extents;
    }
}
