/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import static com.mysema.rdfbean.lucene.Constants.CONTEXT_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.ID_FIELD_NAME;
import static com.mysema.rdfbean.lucene.Constants.TEXT_FIELD_NAME;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.model.STMT;

/**
 * LuceneDocument provides
 *
 * @author tiwe
 * @version $Id$
 */
class LuceneDocument {
    
    private final LuceneConfiguration conf;
    
    private final Document document;
    
    public LuceneDocument(LuceneConfiguration conf){
        this(conf, new Document());
    }
    
    public LuceneDocument(LuceneConfiguration conf, Document document){
        this.conf = Assert.notNull(conf);
        this.document = Assert.notNull(document);
    }
    
    public void add(Fieldable field) {
        document.add(field);        
    }

    public void addContext(String context) {
        if (conf.isContextsStored()){
            document.add(new Field(CONTEXT_FIELD_NAME, context, Field.Store.YES, Field.Index.NOT_ANALYZED));    
        }        
    }

    public void addId(String id) {
        document.add(new Field(ID_FIELD_NAME, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addProperty(STMT statement) {
        String predicateField = statement.getPredicate().getValue();
        String object = conf.getConverter().toString(statement.getObject());
        
        if (conf.isStored()){
            document.add(new Field(predicateField, object, Field.Store.YES, Field.Index.NOT_ANALYZED));    
        }        
        
        if (conf.isFullTextIndexed() && statement.getObject().isLiteral()){
            String text = statement.getObject().getValue();
            document.add(new Field(TEXT_FIELD_NAME, text, Field.Store.NO, Field.Index.ANALYZED));   
        }        
    }

    public Document getDocument(){
        return document;
    }

    public List<Fieldable> getFields() {
        return document.getFields();
    }
    
}
