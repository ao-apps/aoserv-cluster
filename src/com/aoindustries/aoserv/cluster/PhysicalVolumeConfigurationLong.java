/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * A 64-bit implementation of PhysicalVolumeConfiguration to be used
 * when any of the three extents fields do not fit into 32 bits.  This will be
 * the case when the logical or phyiscal volumes reach 64 TB.
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
    public int hashCode() {
        return
            + 127*physicalVolume.hashCode()
            + 31*(int)firstLogicalExtent
            + 7*(int)firstPhysicalExtent
            + (int)extents
        ;
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
