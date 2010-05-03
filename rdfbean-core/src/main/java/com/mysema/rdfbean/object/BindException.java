package com.mysema.rdfbean.object;

/**
 * @author tiwe
 *
 */
public class BindException extends SessionException{

    private static final long serialVersionUID = 9148794817279302066L;
    
    BindException(String msg){
	super(msg);
    }

    BindException(MappedPath propertyPath, Object value){
	super("Cannot assign bnode or literal " + value + " into " + propertyPath);
    }
    
    BindException(MappedPath propertyPath, String specifier, Object value){
	super("Cannot assign " + specifier + " " + value + " into " + propertyPath);
    }
    
}
