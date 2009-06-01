/**
 * 
 */
package com.mysema.rdfbean.spring;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.mysema.rdfbean.object.SessionFactoryImpl;

/**
 * @author sasa
 *
 */
public class SpringSessionFactory extends SessionFactoryImpl {

    public Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

}
