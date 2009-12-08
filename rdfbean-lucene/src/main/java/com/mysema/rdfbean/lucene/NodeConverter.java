/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.lucene;

import java.util.HashMap;
import java.util.Map;

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
    
//  uri = <uri>|u
//  bnode = <bnode>|b
//  typed literal = <value>|l^^<datatype>
//  untyped literal : <value>|l
//  localized string : <value>|l@<lang>
    
    public static final NodeConverter DEFAULT = new NodeConverter();
    
    private static final char SEPARATOR_CHAR = '|';
    
    private static final char URI = 'u';
    
    private static final char BNODE = 'b';
    
    private static final char LITERAL = 'l';
    
    private final Map<UID,String> uidToStr = new HashMap<UID,String>();
    
    private final Map<String,UID> strToUid = new HashMap<String,UID>();
    
    public NodeConverter(){
        for (UID uid : XSD.allTypes){
            String str = "xsd:" + uid.getLocalName();
            uidToStr.put(uid, str);
            strToUid.put(str, uid);
        }
    }

    @Override
    public NODE fromString(String str) {
        int i = str.lastIndexOf(SEPARATOR_CHAR);
        if (i > -1){
            String value = str.substring(0, i);
            String md = str.substring(i+1);    
            if (md.charAt(0) == URI){
                return uidFromString(value);
            }else if (md.charAt(0) == BNODE){
                return new BID(value);
            }else if (md.charAt(0) == LITERAL){
                if (md.length() == 1){
                    return new LIT(value);
                }else if (md.charAt(1) == '@'){
                    return new LIT(value, md.substring(2));
                }else if (md.charAt(1) == '^'){
                    return new LIT(value, uidFromString(md.substring(3)));
                }
            }                
        }
        throw new IllegalArgumentException("Invalid Node string : '" + str + "'");
    }

    @Override
    public String toString(NODE node) {
        StringBuilder builder = new StringBuilder();
        if (node.isURI()){
            builder.append(uidToString((UID)node)).append(SEPARATOR_CHAR);
            return builder.append(URI).toString();
        }else if (node.isBNode()){
            builder.append(node.getValue()).append(SEPARATOR_CHAR);
            return builder.append(BNODE).toString();                       
        }else if (node.isLiteral()){
            LIT lit = (LIT)node;
            builder.append(node.getValue()).append(SEPARATOR_CHAR);
            builder.append(LITERAL);
            if (lit.getLang() != null){
                builder.append("@");
                return builder.append(LocaleUtil.toLang(lit.getLang())).toString();
            }else if (lit.getDatatype() != null){
                builder.append("^^");
                return builder.append(uidToString(lit.getDatatype())).toString();
            }else{
                return builder.toString();
            }
        }
        throw new IllegalArgumentException("Invalid Node " + node); 
    }

    private String uidToString(UID uid) {
        if (uidToStr.containsKey(uid)){
            return uidToStr.get(uid);
        }else{
            return uid.getValue();
        }
    }    

    private UID uidFromString(String str) {
        if (strToUid.containsKey(str)){
            return strToUid.get(str);
        }else{
            return new UID(str);
        }
    }

}
