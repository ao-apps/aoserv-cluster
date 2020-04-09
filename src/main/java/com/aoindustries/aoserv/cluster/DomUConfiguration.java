/*
 * Copyright 2007-2011, 2020 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * One Xen domU configuration.
 *
 * @author  AO Industries, Inc.
 */
public class DomUConfiguration implements Comparable<DomUConfiguration>, Serializable {

	private static final long serialVersionUID = 1L;

	final DomU domU;
	final Dom0 primaryDom0;
	final Dom0 secondaryDom0;
	final List<DomUDiskConfiguration> unmodifiableDomUDiskConfigurations;

	/**
	 * unmodifiableDomUDiskConfigurations MUST BE UNMODIFIABLE
	 */
	DomUConfiguration(
		DomU domU,
		Dom0 primaryDom0,
		Dom0 secondaryDom0,
		List<DomUDiskConfiguration> unmodifiableDomUDiskConfigurations
	) {
		this.domU = domU;

		assert primaryDom0!=null : "primaryDom0 is null";
		assert primaryDom0.clusterName.equals(domU.clusterName) : "primaryDom0.clusterName!=domU.clusterName";
		this.primaryDom0 = primaryDom0;

		assert secondaryDom0!=null : "secondaryDom0 is null";
		assert primaryDom0!=secondaryDom0 : "primaryDom0==secondaryDom0: "+primaryDom0;
		assert secondaryDom0.clusterName.equals(domU.clusterName) : "secondaryDom0.clusterName!=domU.clusterName";
		this.secondaryDom0 = secondaryDom0;

		this.unmodifiableDomUDiskConfigurations = unmodifiableDomUDiskConfigurations;
	}

	@Override
	public String toString() {
		return domU.toString();
	}

	public DomU getDomU() {
		return domU;
	}

	/**
	 * Gets the current primary Dom0 for this machine.
	 */
	public Dom0 getPrimaryDom0() {
		return primaryDom0;
	}

	/**
	 * Gets the current secondary Dom0 for this machine.
	 */
	public Dom0 getSecondaryDom0() {
		return secondaryDom0;
	}

	/**
	 * Gets the unmodifiable list of disk configurations currently set on this domu.
	 */
	public List<DomUDiskConfiguration> getDomUDiskConfigurations() {
		return unmodifiableDomUDiskConfigurations;
	}

	/**
	 * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
	 * 
	 * @see  #equals(DomUConfiguration)
	 */
	@Override
	public boolean equals(Object O) {
		return O!=null && (O instanceof DomUConfiguration) && equals((DomUConfiguration)O);
	}

	/**
	 * Performs a deep field-by-field comparison to see if two configurations are identical in every way.
	 * 
	 * @see  #equals(Object)
	 */
	public boolean equals(DomUConfiguration other) {
		if(this==other) return true;
		if(other==null) return false;
		if(domU!=other.domU) return false;
		if(primaryDom0!=other.primaryDom0) return false;
		if(secondaryDom0!=other.secondaryDom0) return false;
		{
			int size = unmodifiableDomUDiskConfigurations.size();
			if(size!=other.unmodifiableDomUDiskConfigurations.size()) return false;
			Iterator<DomUDiskConfiguration> myIter = unmodifiableDomUDiskConfigurations.iterator();
			Iterator<DomUDiskConfiguration> otherIter = other.unmodifiableDomUDiskConfigurations.iterator();
			while(myIter.hasNext()) {
				if(!myIter.next().equals(otherIter.next())) return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return
			73*domU.hashCode()
			+ 37*primaryDom0.hashCode()
			+ 31*secondaryDom0.hashCode()
			+ unmodifiableDomUDiskConfigurations.hashCode()
		;
	}

	public int compareTo(DomUConfiguration other) {
		if(this==other) return 0;
		return domU.compareTo(other.domU);
	}
}
