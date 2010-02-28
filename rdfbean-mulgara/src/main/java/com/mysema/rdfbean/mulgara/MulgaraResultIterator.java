package com.mysema.rdfbean.mulgara;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.jrdf.graph.Node;
import org.mulgara.query.Answer;
import org.mulgara.query.TuplesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * MulgaraResultIterator provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MulgaraResultIterator implements CloseableIterator<STMT>{
    
    private static final Logger logger = LoggerFactory.getLogger(MulgaraResultIterator.class);
    
    private final MulgaraDialect dialect;
    
    private final Answer answer;
    
    private final ID subject;
    
    private final UID predicate, context;
    
    private final NODE object;
    
    private Boolean next = null;
    
    public MulgaraResultIterator(MulgaraDialect dialect, Answer answer, ID subject, UID predicate,
            NODE object, UID context) {
        this.dialect = Assert.notNull(dialect);
        this.answer = Assert.notNull(answer);
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
    }

    @Override
    public boolean hasNext() {
        if (next == null){
            try {
                next = answer.next();
            } catch (TuplesException e) {
                String error = "Caught " + e.getClass().getName();
                logger.error(error, e);
                throw new RepositoryException(error, e);
            }                        
        }
        return next;
    }

    @Override
    public STMT next() {
        if (hasNext()){                        
            try {
                next = null;
                ID s = (ID) (subject != null ? subject : convert(answer.getObject("s")));
                UID p = (UID) (predicate != null ? predicate : convert(answer.getObject("p")));
                NODE o = object != null ? object : convert(answer.getObject("o"));
                return new STMT(s, p, o, context);
            } catch (TuplesException e) {
                String error = "Caught " + e.getClass().getName();
                logger.error(error, e);
                throw new RepositoryException(error, e);
            }
            
        }else{
            throw new NoSuchElementException();
        }
    }

    private NODE convert(Object object) {
        if (object instanceof Node){
            return dialect.getNODE((Node) object);
        }else{
            throw new IllegalArgumentException("Unknown type : " + object.getClass().getName()); 
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void close() throws IOException {
        try {
            answer.close();
        } catch (TuplesException e) {
            String error = "Caught " + e.getClass().getName();
            logger.error(error, e);
            throw new IOException(error, e);
        }
        
    }

}
