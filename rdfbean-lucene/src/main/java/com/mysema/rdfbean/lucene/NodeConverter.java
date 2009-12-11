/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.compass.core.util.Assert;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.xsd.Converter;

/**
 * NodeConverter provides
 *
 * @author tiwe
 * @version $Id$
 */
public class NodeConverter implements Converter<NODE>{
    
//  full uri = <uri>|U
//  short uri = <prefix>:<local>|u    
//  bnode = <bnode>|b
//  string literal : <value>|l
//  localized literal : <value>|l@<lang>    
//  typed literal = <value>|lt<prefix>:<local>
//  typed literal2 = <value>|lT<uri>     
    
    private final Map<String,String> prefixToNs;
    
    private final Map<String,String> nsToPrefix = new HashMap<String,String>();
    
    private static final char SEPARATOR_CHAR = '|';
    
    private static final char SHORT_URI = 'u';
    
    private static final char FULL_URI = 'U';
    
    private static final char BNODE = 'b';
    
    private static final char LITERAL = 'l';
    
    private final Map<UID,String> uidToStr = new HashMap<UID,String>();
    
    private final Map<String,UID> strToUid = new HashMap<String,UID>();
    
    public NodeConverter(Collection<UID> knownResources, Map<String,String> prefixToNs){
        this.prefixToNs = prefixToNs;
        for (Map.Entry<String, String> entry : prefixToNs.entrySet()){
            nsToPrefix.put(entry.getValue(), entry.getKey());
        }
        for (UID uid : knownResources){
            String prefix = nsToPrefix.get(uid.getNamespace());
            Assert.notNull(prefix, "Got no prefix for " + uid.getNamespace());
            String shortName = prefix + ":" + uid.getLocalName();
            uidToStr.put(uid, shortName);
            strToUid.put(shortName, uid);
        }
    }
    
    @Override
    public NODE fromString(String str) {
        int i = str.lastIndexOf(SEPARATOR_CHAR);
        if (i > -1){
            String value = str.substring(0, i);
            String md = str.substring(i+1);    
            if (md.charAt(0) == SHORT_URI){
                return uidFromShortString(value);
                
            }else if (md.charAt(0) == FULL_URI){
                return new UID(value);
                
            }else if (md.charAt(0) == BNODE){
                return new BID(value);
                
            }else if (md.charAt(0) == LITERAL){
                if (md.length() == 1){ // xsd:string typed
                    return new LIT(value);
                }else if (md.charAt(1) == '@'){
                    return new LIT(value, md.substring(2));
                }else if (md.charAt(1) == 't'){
                    return new LIT(value, uidFromShortString(md.substring(2)));
                }else if (md.charAt(1) == 'T'){
                    return new LIT(value, new UID(md.substring(2)));
                }
            }                
        }
        throw new IllegalArgumentException("Invalid Node string : '" + str + "'");
    }

    @Override
    public String toString(NODE node) {        
        switch(node.getNodeType()){
        case BLANK : return toString((BID)node);
        case URI :   return toString((UID)node);
        case LITERAL:return toString((LIT)node);
        default: throw new IllegalArgumentException("Invalid Node " + node); 
        }         
    }
    
    public String toString(UID uid){
        StringBuilder builder = new StringBuilder();
        if (nsToPrefix.containsKey(((UID)uid).getNamespace())){
            builder.append(uidToShortString((UID)uid)).append(SEPARATOR_CHAR);
            return builder.append(SHORT_URI).toString();    
        }else{
            builder.append(uid.getValue()).append(SEPARATOR_CHAR);
            return builder.append(FULL_URI).toString();
        }
    }
    
    public String toString(BID bid){
        StringBuilder builder = new StringBuilder();
        builder.append(bid.getValue()).append(SEPARATOR_CHAR);
        return builder.append(BNODE).toString();  
    }
    
    public String toString(LIT lit){
        StringBuilder builder = new StringBuilder();
        builder.append(lit.getValue()).append(SEPARATOR_CHAR);
        builder.append(LITERAL);
        if (lit.getLang() != null){
            builder.append("@");
            return builder.append(LocaleUtil.toLang(lit.getLang())).toString();
        }else if (!lit.getDatatype().equals(XSD.stringType)){
            if (nsToPrefix.containsKey(lit.getDatatype().getNamespace())){
                builder.append("t");
                builder.append(uidToShortString(lit.getDatatype()));    
            }else{
                builder.append("T");
                builder.append(lit.getDatatype().getValue());
            }                
            return builder.toString();
        }else{
            return builder.toString();
        }
    }

    public String uidToShortString(UID uid) {
        if (uidToStr.containsKey(uid)){
            return uidToStr.get(uid);
        }else{
            return nsToPrefix.get(uid.getNamespace()) + ":" + uid.getLocalName();
        }
    }    

    public UID uidFromShortString(String str) {
        if (strToUid.containsKey(str)){
            return strToUid.get(str);
        }else{
            int index = str.indexOf(':');
            if (index == -1) throw new IllegalArgumentException("Illegal prefixed URI : '" + str + "'");
            return new UID(prefixToNs.get(str.substring(0, index)), str.substring(index+1));                           
        }
    }

}
