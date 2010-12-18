package com.mysema.rdfbean.model.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import com.mysema.rdfbean.Namespaces;

/**
 * @author tiwe
 *
 */
public final class WriterUtils {

    public static RDFWriter createWriter(Format format, OutputStream out){
        return createWriter(format, out, Namespaces.DEFAULT);
    }
    
    public static RDFWriter createWriter(Format format, OutputStream out, Map<String,String> prefixes){        
        try {
            String encoding = format == Format.NTRIPLES ? "US-ASCII" : "UTF-8";
            return createWriter(format, new OutputStreamWriter(out, encoding), prefixes);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public static RDFWriter createWriter(Format format, Writer writer){
        return createWriter(format, writer, Namespaces.DEFAULT);
    }    

    public static RDFWriter createWriter(Format format, Writer writer, Map<String,String> prefixes){
        if (format == Format.N3 || format == Format.TURTLE){
            return new TurtleWriter(writer, prefixes);
        }else if (format == Format.NTRIPLES){
            return new NTriplesWriter(writer);
        }else if (format == Format.RDFXML){
            return new RDFXMLWriter(writer, prefixes);
        }else{
            throw new IllegalArgumentException("Unsupported format " + format);
        }
    }
    
    private WriterUtils(){}
    
}
