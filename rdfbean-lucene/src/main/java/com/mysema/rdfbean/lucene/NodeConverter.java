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
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;
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
//  typed literal = <value>|lt<prefix>:<local>
//  typed literal2 = <value>|lT<uri>     
//  untyped literal : <value>|l
//  localized string : <value>|l@<lang>
    
    private final Map<String,String> prefixToNs;
    
    private final Map<String,String> nsToPrefix;
    
    private static final char SEPARATOR_CHAR = '|';
    
    private static final char SHORT_URI = 'u';
    
    private static final char FULL_URI = 'U';
    
    private static final char BNODE = 'b';
    
    private static final char LITERAL = 'l';
    
    private final Map<UID,String> uidToStr = new HashMap<UID,String>();
    
    private final Map<String,UID> strToUid = new HashMap<String,UID>();
    
    public NodeConverter(Map<String,String> prefixToNs, Map<String,String> nsToPrefix){
        this.prefixToNs = prefixToNs;
        this.nsToPrefix = nsToPrefix;        
        for (UID uid : OWL.all) register(uid, "owl:" + uid.getLocalName());
        for (UID uid : RDF.all) register(uid, "rdf:" + uid.getLocalName());
        for (UID uid : RDFS.all)register(uid, "rdfs:" + uid.getLocalName());
        for (UID uid : XSD.all) register(uid, "xsd:" + uid.getLocalName());
    }
    
    private void register(UID uid, String str){
        uidToStr.put(uid, str);
        strToUid.put(str, uid);
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
                if (md.length() == 1){
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
        StringBuilder builder = new StringBuilder();
        if (node.isURI()){
            if (nsToPrefix.containsKey(((UID)node).getNamespace())){
                builder.append(uidToShortString((UID)node)).append(SEPARATOR_CHAR);
                return builder.append(SHORT_URI).toString();    
            }else{
                builder.append(node.getValue()).append(SEPARATOR_CHAR);
                return builder.append(FULL_URI).toString();
            }
            
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
        throw new IllegalArgumentException("Invalid Node " + node); 
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
