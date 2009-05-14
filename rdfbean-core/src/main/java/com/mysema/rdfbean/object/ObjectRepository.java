/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import com.mysema.rdfbean.model.UID;


/**
 * @author sasa
 *
 */
public interface ObjectRepository {

    <T> T getBean(Class<T> clazz, UID subject);

}
