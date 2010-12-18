package com.mysema.rdfbean.model.io;

import java.io.Writer;


public class NTriplesWriterTest extends AbstractWriterTest{

//    <http://www.w3.org/2001/08/rdf-test/>  <http://purl.org/dc/elements/1.1/creator>    "Dave Beckett" .
//    <http://www.w3.org/2001/08/rdf-test/>  <http://purl.org/dc/elements/1.1/creator>    "Jan Grant" .
//    <http://www.w3.org/2001/08/rdf-test/>  <http://purl.org/dc/elements/1.1/publisher>  _:a .
//    _:a                                    <http://purl.org/dc/elements/1.1/title>      "World Wide Web Consortium" .
//    _:a                                    <http://purl.org/dc/elements/1.1/source>     <http://www.w3.org/> .

    @Override
    protected RDFWriter createWriter(Writer w) {
        return WriterUtils.createWriter(Format.NTRIPLES, w);
    }

    
}
