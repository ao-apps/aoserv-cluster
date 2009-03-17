/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * A 16-bit implementation of PhysicalVolumeConfiguration to be used
 * when all three extents fields fit into 16 bits.  This will be the case
 * until the logical or phyiscal volumes reach 1 TB.
 *
 * @author  AO Industries, Inc.
 */
public class PhysicalVolumeConfigurationShort extends PhysicalVolumeConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    final short firstLogicalExtent;
    final short firstPhysicalExtent;
    final short extents;

    PhysicalVolumeConfigurationShort(
        PhysicalVolume physicalVolume,
        short firstLogicalExtent,
        short firstPhysicalExtent,
        short extents
    ) {
        super(physicalVolume);
        assert firstLogicalExtent>=0 : "firstLogicalExtent<0: "+firstLogicalExtent;
        assert firstPhysicalExtent>=0 : "firstPhysicalExtent<0: "+firstPhysicalExtent;
        assert extents>0 : "extents<=0: "+extents;
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
