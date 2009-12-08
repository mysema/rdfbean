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
    
    private final NodeConverter nodeConverter;
    
    private final Document document;
    
    public LuceneDocument(NodeConverter nodeConverter){
        this(nodeConverter, new Document());
    }
    
    public LuceneDocument(NodeConverter nodeConverter, Document document){
        this.nodeConverter = Assert.notNull(nodeConverter);
        this.document = Assert.notNull(document);
    }
    
    public void add(Fieldable field) {
        document.add(field);        
    }

    public void addContext(String context) {
        document.add(new Field(CONTEXT_FIELD_NAME, context, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addId(String id) {
        document.add(new Field(ID_FIELD_NAME, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
    }

    public void addProperty(STMT statement) {
        String predicateField = statement.getPredicate().getValue();
        String object = nodeConverter.toString(statement.getObject());
        document.add(new Field(predicateField, object, Field.Store.YES, Field.Index.ANALYZED));
        if (statement.getObject().isLiteral()){
            String text = statement.getObject().getValue();
            document.add(new Field(TEXT_FIELD_NAME, text, Field.Store.YES, Field.Index.ANALYZED));    
        }        
    }

    public Document getDocument(){
        return document;
    }

    public List<Fieldable> getFields() {
        return document.getFields();
    }
    
}
