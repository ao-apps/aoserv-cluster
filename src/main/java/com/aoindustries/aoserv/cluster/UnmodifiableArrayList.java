/*
 * aoserv-cluster - Cluster optimizer for the AOServ Platform.
 * Copyright (C) 2007-2011, 2020  AO Industries, Inc.
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
 * along with aoserv-cluster.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.aoserv.cluster;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/**
 * An unmodifiable list backed by the provided array.
 *
 * @author  AO Industries, Inc.
 */
public class UnmodifiableArrayList<E> extends AbstractList<E>
	implements RandomAccess, Serializable
{
	private static final long serialVersionUID = 1L;

	private final E[] a;

	public UnmodifiableArrayList(E[] array) {
		if (array==null) throw new NullPointerException("array is null");
		a = array;
	}

	@Override
	public int size() {
		return a.length;
	}

	@Override
	public Object[] toArray() {
		return a.clone();
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T> T[] toArray(T[] a) {
		int size = size();
		int len = a.length;
		if (len < size) 
			return Arrays.copyOf(this.a, size,
								 (Class<? extends T[]>) a.getClass());
		System.arraycopy(this.a, 0, a, 0, size);
		if (len > size)
			a[size] = null;
		return a;
	}

	@Override
	public E get(int index) {
		return a[index];
	}

	@Override
	public int indexOf(Object o) {
		int len = a.length;
		if (o==null) {
			for (int i=0; i<len; i++)
				if (a[i]==null)
					return i;
		} else {
			for (int i=0; i<len; i++)
				if (o.equals(a[i]))
					return i;
		}
		return -1;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
}
