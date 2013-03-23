package com.mysema.rdfbean.model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author tiwe
 * 
 */
public class STMTComparator implements Comparator<STMT>, Serializable {

    private static final long serialVersionUID = -2981760366315374544L;

    public static final STMTComparator DEFAULT = new STMTComparator(new NODEComparator());

    private final Comparator<NODE> nodeComparator;

    public STMTComparator(Comparator<NODE> comparator) {
        nodeComparator = comparator;
    }

    @Override
    public int compare(STMT o1, STMT o2) {
        int rv = 0;
        if ((rv = compare(o1.getContext(), o2.getContext())) == 0) {
            if ((rv = compare(o1.getSubject(), o2.getSubject())) == 0) {
                if ((rv = compare(o1.getPredicate(), o2.getPredicate())) == 0) {
                    if ((rv = compare(o1.getObject(), o2.getObject())) == 0) {
                        return 0;
                    }
                }
            }
        }
        return rv;
    }

    private int compare(NODE n1, NODE n2) {
        return nodeComparator.compare(n1, n2);
    }

}
