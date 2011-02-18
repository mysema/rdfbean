/**
 *
 */
package com.mysema.rdfbean.tapestry;

import org.apache.commons.collections15.BeanMap;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactory;

/**
 * @author tiwe
 *
 * @param <T>
 */
public class EntityValueEncoderFactory<T> implements ValueEncoderFactory<T> {

    private final SessionFactory sessionFactory;

    private final Class<T> cl;

    private final MappedProperty<?> idProperty;

    public EntityValueEncoderFactory(SessionFactory sessionFactory,
            Configuration rdfBeanConfiguration,
            Class<T> cl) {
        this.sessionFactory = sessionFactory;
        this.cl = cl;
        this.idProperty = rdfBeanConfiguration.getMappedClass(cl).getIdProperty();
    }

    @Override
    public ValueEncoder<T> create(Class<T> type) {
        return new ValueEncoder<T>(){
            @Override
            public String toClient(Object value) {
                return idProperty.getValue(new BeanMap(value)).toString();
            }

            @Override
            public T toValue(String id) {
                Session session = sessionFactory.getCurrentSession();
                boolean close = session == null;
                if (session == null){
                    session = sessionFactory.openSession();
                }
                try{
                    return session.getById(id, cl);
                }finally{
                    if (close){
                        session.close();
                    }
                }
            }
        };
    }
}