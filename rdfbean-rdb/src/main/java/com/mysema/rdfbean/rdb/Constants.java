package com.mysema.rdfbean.rdb;

import java.util.Arrays;
import java.util.List;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

/**
 * Constants provides
 *
 * @author tiwe
 * @version $Id$
 */
public interface Constants {
    
    List<UID> integerTypes = Arrays.asList(XSD.integerType, XSD.intType, XSD.byteType, XSD.longType);
    
    List<UID> decimalTypes = Arrays.asList(XSD.decimalType, XSD.doubleType, XSD.floatType);

}
