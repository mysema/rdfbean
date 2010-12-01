package com.mysema.rdfbean.xsd;

import java.sql.Blob;

import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

public class BlobConverter extends AbstractConverter<Blob>{

    @Override
    public Blob fromString(String str) {
        return null;
    }

    @Override
    public Class<Blob> getJavaType() {
        return Blob.class;
    }

    @Override
    public UID getType() {
        return XSD.hexBinary;
    }

}
