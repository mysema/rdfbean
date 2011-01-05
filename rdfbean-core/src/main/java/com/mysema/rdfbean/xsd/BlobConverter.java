package com.mysema.rdfbean.xsd;

import java.sql.Blob;

public class BlobConverter extends AbstractConverter<Blob>{

    @Override
    public Blob fromString(String str) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<Blob> getJavaType() {
        return Blob.class;
    }

//    @Override
//    public UID getType() {
//        return XSD.hexBinary;
//    }

}
