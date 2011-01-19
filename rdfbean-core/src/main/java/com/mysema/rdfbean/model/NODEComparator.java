package com.mysema.rdfbean.model;

import java.util.Comparator;
import java.util.Locale;

/**
 * @author tiwe
 *
 */
public class NODEComparator implements Comparator<NODE>{

    @Override
    public int compare(NODE o1, NODE o2) {
        if (o1 == null){
            return o2 == null ? 0 : -1;
        }else if (o2 == null){
            return 1;
        }
        int rv = 0;
        if ((rv = o1.getNodeType().ordinal() - o2.getNodeType().ordinal()) == 0){
            if ((rv = o1.getValue().compareTo(o2.getValue())) == 0){
                if (o1.isLiteral() && o2.isLiteral()){
                    LIT l1 = o1.asLiteral();
                    LIT l2 = o2.asLiteral();
                    if ((rv = compare(l1.getDatatype(), l2.getDatatype())) == 0){
                        Locale loc1 = l1.getLang() == null ? new Locale("") : l1.getLang();
                        Locale loc2 = l2.getLang() == null ? new Locale("") : l2.getLang();
                        return loc1.toString().compareTo(loc2.toString());
                    }    
                }                
            }
        }
        return rv;
    }

}
