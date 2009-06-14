/*
 * Copyright 2007-2009 by AO Industries, Inc.,
 * 7262 Bull Pen Cir, Mobile, Alabama, 36695, U.S.A.
 * All rights reserved.
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
