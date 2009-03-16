/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * A 32-bit implementation of PhysicalVolumeConfiguration to be used
 * when all three extents fields fit into 32 bits.  This will be the case
 * until the logical or phyiscal volumes reach 64 TB.
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
        this.firstLogicalExtent = firstLogicalExtent;
        this.firstPhysicalExtent = firstPhysicalExtent;
        this.extents = extents;
    }

    @Override
    public int hashCode() {
        return
            + 127*physicalVolume.hashCode()
            + 31*firstLogicalExtent
            + 7*firstPhysicalExtent
            + extents
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
