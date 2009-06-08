/**
 * 
 */
package com.mysema.rdfbean.model;

/**
 * @author sasa
 *
 */
public final class STMTMatcher {
    private final ID subject; 
    private final UID predicate; 
    private final NODE object; 
    private final UID context;
    private final boolean includeInferred;
    public STMTMatcher(ID subject, UID predicate, NODE object, UID context,
            boolean includeInferred) {
        super();
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.context = context;
        this.includeInferred = includeInferred;
    }
    public ID getSubject() {
        return subject;
    }
    public UID getPredicate() {
        return predicate;
    }
    public NODE getObject() {
        return object;
    }
    public UID getContext() {
        return context;
    }
    public boolean isIncludeInferred() {
        return includeInferred;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((context == null) ? 0 : context.hashCode());
        result = prime * result + (includeInferred ? 1231 : 1237);
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        STMTMatcher other = (STMTMatcher) obj;
        if (context == null) {
            if (other.context != null)
                return false;
        } else if (!context.equals(other.context))
            return false;
        if (includeInferred != other.includeInferred)
            return false;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        return true;
    }
    
    public static boolean matches(STMT stmt, ID subject, UID predicate, NODE object, UID context, boolean includeInferred) {
        return 
        // Subject match
        (subject == null || stmt.getSubject().equals(subject)) &&

        // Predicate match
        (predicate == null || predicate.equals(stmt.getPredicate())) &&
        
        // Object match
        (object == null || object.equals(stmt.getObject())) &&
        
        // Context match
        (context == null || context.equals(stmt.getContext())) &&
        
        // Asserted or includeInferred statement
        (includeInferred || stmt.isAsserted());
    }
    
    public boolean matches(STMT stmt) {
        return matches(stmt, subject, predicate, object, context, includeInferred);
    }
}
