package com.mysema.rdfbean.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author tiwe
 * 
 */
public class NODEComparator implements Comparator<NODE>, Serializable {

    private static final long serialVersionUID = -7774408184956942211L;

    @Override
    public int compare(NODE o1, NODE o2) {
        if (o1 == null) {
            return o2 == null ? 0 : -1;
        } else if (o2 == null) {
            return 1;
        }

        // node type
        if (o1.getNodeType() != o2.getNodeType()) {
            return o1.getNodeType().compareTo(o2.getNodeType());
        }

        // value
        if (!o1.getValue().equals(o2.getValue())) {
            return o1.getValue().compareTo(o2.getValue());
        }
        if (o1.isLiteral() && o2.isLiteral()) {
            LIT l1 = o1.asLiteral();
            LIT l2 = o2.asLiteral();
            // datatype
            if (!l1.getDatatype().equals(l2.getDatatype())) {
                return compare(l1.getDatatype(), l2.getDatatype());
            }
            // locale
            Locale loc1 = l1.getLang() == null ? new Locale("") : l1.getLang();
            Locale loc2 = l2.getLang() == null ? new Locale("") : l2.getLang();
            return loc1.toString().compareTo(loc2.toString());
        }
        return 0;
    }

}
