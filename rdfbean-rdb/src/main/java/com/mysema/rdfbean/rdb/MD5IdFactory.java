/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.rdb;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.owl.OWL;

/**
 * MD5IdFactory provides
 *
 * @author tiwe
 * @version $Id$
 */
public class MD5IdFactory implements IdFactory {
    
    private final Map<String, String> ns2prefix = new HashMap<String, String>();
    
    public MD5IdFactory() {
        ns2prefix.put(RDF.NS,  "rdf");
        ns2prefix.put(RDFS.NS, "rdfs");
        ns2prefix.put(OWL.NS,  "owl");
        ns2prefix.put(XSD.NS,  "xsd");
    }
    
    private String getReadableURI(String ns, @Nullable String ln) {
        if (ln == null) {
            ln = "";
        }
        if (StringUtils.isNotEmpty(ns)) {
            String prefix = ns2prefix.get(ns);
            if (prefix != null) {
                return prefix+":"+ln;
            } else {
                return "<"+ns+ln+">";
            }
        } else {
            return ln;
        }
    }
    
    @Override
    public Long getId(NODE node) {        
        StringBuilder builder = new StringBuilder();
        builder.append(node.getNodeType().name().charAt(0));
        if (node.isURI()){
            UID uid = node.asURI();
            builder.append(getReadableURI(uid.ns(), uid.ln()));
            
        }else if (node.isBNode()){    
            builder.append(node.getValue());
            
        }else if (node.isLiteral()){
            builder.append(node.getValue());
            LIT literal = node.asLiteral();
            if (literal.getDatatype() != null){
                builder.append("^");
                UID type = literal.getDatatype();
                builder.append(getReadableURI(type.ns(), type.ln()));                
            }
            if (literal.getLang() != null){
                builder.append("@");
                builder.append(literal.getLang());
            }
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(builder.toString().getBytes("UTF-8"));
            byte[] hash = digest.digest();
            byte[] longBytes = new byte[8];
            System.arraycopy(hash, 0, longBytes, 0, longBytes.length);
            return new BigInteger(longBytes).longValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RepositoryException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        }
        
    }

    @Override
    public Integer getId(Locale locale) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(locale.toString().getBytes("UTF-8"));
            byte[] hash = digest.digest();
            byte[] intBytes = new byte[4];
            System.arraycopy(hash, 0, intBytes, 0, intBytes.length);
            return new BigInteger(intBytes).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RepositoryException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        }
    }

}
