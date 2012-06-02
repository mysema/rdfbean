package com.mysema.rdfbean.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Strings;


/**
 * @author tiwe
 *
 */
public class UpdateClause {

    public enum Type { CLEAR, CREATE, DELETE, DROP, INSERT, LOAD, MODIFY }
    
    private final Map<String, String> prefixes;
    
    private final Type type;
    
    @Nullable
    private final UID source, target;
    
    private final List<UID> from = new ArrayList<UID>();
    
    private final List<UID> into = new ArrayList<UID>();
    
    private boolean silent;
    
    @Nullable
    private String pattern, template, delete, insert;
    
    public UpdateClause(Map<String, String> prefixes, Type type) {
        this(prefixes, type, null, null);
    }
        
    public UpdateClause(Map<String, String> prefixes, Type type, UID source) {
        this(prefixes, type, source, null);
    }
    
    public UpdateClause(Map<String, String> prefixes, Type type, UID source, boolean silent){
        this(prefixes, type, source, null);
        this.silent = silent;
    }
    
    public UpdateClause(Map<String, String> prefixes, Type type, @Nullable UID source, @Nullable UID target){
        this.prefixes = Collections.unmodifiableMap(prefixes);
        this.type = type;
        this.source = source;
        this.target = target;
    }

    public Type getType() {
        return type;
    }

    public UID getSource() {
        return source;
    }

    public UID getTarget() {
        return target;
    }

    public boolean isSilent() {
        return silent;
    }
    
    public void addFrom(UID uid){
        this.from.add(uid);
    }

    public void addFrom(List<UID> uids) {
        this.from.addAll(uids);
    }
    
    public void addInto(UID uid){
        this.into.add(uid);
    }

    public void addInto(List<UID> uids) {
        this.into.addAll(uids);
    }
    
    public List<UID> getFrom(){
        return from;
    }
    
    public List<UID> getInto(){
        return into;
    }
    
    public String getPattern(){
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = Strings.emptyToNull(pattern);        
    }

    public String getTemplate(){
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = Strings.emptyToNull(template);
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = Strings.emptyToNull(delete);
    }

    public String getInsert() {
        return insert;
    }

    public void setInsert(String insert) {
        this.insert = Strings.emptyToNull(insert);
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }
    
    
        
}
