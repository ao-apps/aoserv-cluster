/*
 * Copyright 2007-2010 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;

/**
 * One Xen domU disk to physical volume configuration.  This equates to a LVM logical volume to physical volume mapping segment.
 * See lvdisplay -m for segments.
 *
 * @author  AO Industries, Inc.
 */
abstract public class PhysicalVolumeConfiguration implements Comparable<PhysicalVolumeConfiguration>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PhysicalVolume of the appropriate type for the provided extents.  Will use
     * 16-bit and 32-bit representation when possible to save heap.
     * 
     * If heap space is every an issue, can use even more specialized versions like:
     *     PhysicalVolumeConfiguration896 for multiples of 896 that can store into byte
     *     PhysicalVolumeConfiguration_0_0_896 for newInstance(0,0,896) - would need to measure to know which would save heap
     */
    public static PhysicalVolumeConfiguration newInstance(
        PhysicalVolume physicalVolume,
        long firstLogicalExtent,
        long firstPhysicalExtent,
        long extents
    ) {
        assert firstLogicalExtent>=0 : "firstLogicalExtent<0: "+firstLogicalExtent;
        assert firstPhysicalExtent>=0 : "firstPhysicalExtent<0: "+firstPhysicalExtent;
        assert extents>0 : "extents<=0: "+extents;
        // 16-bit
        if(
            firstLogicalExtent<=Short.MAX_VALUE
            && firstPhysicalExtent<=Short.MAX_VALUE
            && extents<=Short.MAX_VALUE
        ) return new PhysicalVolumeConfigurationShort(physicalVolume, (short)firstLogicalExtent, (short)firstPhysicalExtent, (short)extents);
        // 32-bit
        if(
            firstLogicalExtent<=Integer.MAX_VALUE
            && firstPhysicalExtent<=Integer.MAX_VALUE
            && extents<=Integer.MAX_VALUE
        ) return new PhysicalVolumeConfigurationInt(physicalVolume, (int)firstLogicalExtent, (int)firstPhysicalExtent, (int)extents);
        // 64-bit
        return new PhysicalVolumeConfigurationLong(physicalVolume, firstLogicalExtent, firstPhysicalExtent, extents);
    }

    final PhysicalVolume physicalVolume;

    PhysicalVolumeConfiguration(PhysicalVolume physicalVolume) {
        this.physicalVolume = physicalVolume;
    }

    @Override
    final public String toString() {
        return physicalVolume.toString()+"("+getFirstLogicalExtent()+","+getFirstPhysicalExtent()+","+getExtents()+")";
    }

    final public PhysicalVolume getPhysicalVolume() {
        return physicalVolume;
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(DomUDiskConfiguration)
     */
    @Override
    final public boolean equals(Object O) {
        return O!=null && (O instanceof PhysicalVolumeConfiguration) && equals((PhysicalVolumeConfiguration)O);
    }

    /**
     * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
     * 
     * @see  #equals(Object)
     */
    final public boolean equals(PhysicalVolumeConfiguration other) {
        if(this==other) return true;
        if(other==null) return false;
        return
            physicalVolume==other.physicalVolume
            && getFirstLogicalExtent()==other.getFirstLogicalExtent()
            && getFirstPhysicalExtent()==other.getFirstPhysicalExtent()
            && getExtents()==other.getExtents()
        ;
    }

    @Override
    final public int hashCode() {
        return
            + 127*physicalVolume.hashCode()
            + 31*(int)getFirstLogicalExtent()
            + 7*(int)getFirstPhysicalExtent()
            + (int)getExtents()
        ;
    }

    abstract public long getFirstLogicalExtent();

    abstract public long getFirstPhysicalExtent();

    abstract public long getExtents();

    /**
     * Sorted ascending by:
     * <ol>
     *   <li>physicalVolume</li>
     *   <li>firstLogicalExtent</li>
     *   <li>firstPhysicalExtent</li>
     *   <li>extents</li>
     * </ol>
     */
    @Override
    final public int compareTo(PhysicalVolumeConfiguration other) {
        // Identity
        if(this==other) return 0;
        // physicalVolume
        int diff = physicalVolume.compareTo(other.physicalVolume);
        if(diff!=0) return diff;
        // firstLogicalExtent
        long mine = getFirstLogicalExtent();
        long others = other.getFirstLogicalExtent();
        if(mine<others) return -1;
        if(mine>others) return 1;
        // secondLogicalExtent
        mine = getFirstPhysicalExtent();
        others = other.getFirstPhysicalExtent();
        if(mine<others) return -1;
        if(mine>others) return 1;
        // extents
        mine = getExtents();
        others = other.getExtents();
        if(mine<others) return -1;
        if(mine>others) return 1;
        // Otherwise equal
        return 0;
    }
    
    private static boolean overlaps(long start1, long extents1, long start2, long extents2) {
        return
            (start2+extents2)>start1
            && (start1+extents1)>start2
        ;
    }

    /*
    public static void main(String[] args) {
        Random random = new SecureRandom();
        for(int c=1; c<100000; c++) {
            int start1 = random.nextInt(c);
            int extents1 = random.nextInt(c)+1;
            int start2 = random.nextInt(c);
            int extents2 = random.nextInt(c)+1;
            boolean overlaps = overlaps(start1, extents1, start2, extents2);
            Rectangle R1 = new Rectangle(start1, 0, extents1, 1);
            Rectangle R2 = new Rectangle(start2, 0, extents2, 1);
            boolean overlaps2 = R1.intersects(R2);
            if(overlaps!=overlaps2) {
                System.out.println("Not equal:");
                System.out.println("    start1 = "+start1);
                System.out.println("    extents1 = "+extents1);
                System.out.println("    start2 = "+start2);
                System.out.println("    extents2 = "+extents2);
            }
        }
        System.out.println("All OK");
    }
     */

    /**
     * Returns true if either the logical or the physical extents overlap.
     */
    final public boolean overlaps(PhysicalVolumeConfiguration other) {
        long myExtents = getExtents();
        long otherExtents = other.getExtents();
        return
            overlaps(getFirstLogicalExtent(), myExtents, other.getFirstLogicalExtent(), otherExtents)
            || (
                physicalVolume==other.physicalVolume
                && overlaps(getFirstPhysicalExtent(), myExtents, other.getFirstPhysicalExtent(), otherExtents)
            )
        ;
    }
}
