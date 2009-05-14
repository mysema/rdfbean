/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import static org.springframework.util.StringUtils.hasLength;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.*;
import com.mysema.rdfbean.model.UID;

/**
 * @author sasa
 * 
 */
public class MappedPath {

    static MappedPath getMappedPath(MappedProperty<?> property, List<MappedPredicate> path, boolean inherited) {
        if (path != null) {
            return new MappedPath(property, path, inherited);
        } else {
            if (property.isAnnotationPresent(Inject.class) 
                    || property.isAnnotationPresent(Mixin.class)
                    || property.isAnnotationPresent(Id.class)
                    || property.isAnnotationPresent(Default.class)
                    || property.isAnnotationPresent(Defaults.class)) {
                return new MappedPath(property, Collections.<MappedPredicate>emptyList(), inherited);
            } else {
            	return null;
            }
        }
    }

    static MappedPath getPathMapping(String classNs, Field field, boolean inherited) {
        FieldProperty property = new FieldProperty(field);
        List<MappedPredicate> path = getPredicatePath(classNs, property);
        return getMappedPath(property, path, inherited);
    }

    static MappedPath getPathMapping(MappedClass mappedClass, Constructor<?> constructor, int parameterIndex) {
    	ConstructorParameter constructorParameter = new ConstructorParameter(constructor, parameterIndex);
    	if (constructorParameter.isPropertyReference()) {
    		return mappedClass.getMappedPath(constructorParameter.getReferencedProperty());
    	} else {
	        List<MappedPredicate> path = getPredicatePath(mappedClass.getClassNs(), constructorParameter);
	        return getMappedPath(constructorParameter, path, false);
    	}
    }
    
    static MappedPath getPathMapping(String classNs, Method method, boolean inherited) {
        MethodProperty property = MethodProperty.getMethodPropertyOrNull(method);
        if (property != null) {
            List<MappedPredicate> path = getPredicatePath(classNs, property);
            return getMappedPath(property, path, inherited);
        } else {
            return null;
        }
    }

    private static List<MappedPredicate> getPredicatePath(String classNs, 
            MappedProperty<?> property) {
        String parentNs = classNs;
        Path path = property.getAnnotation(Path.class);
        Predicate[] predicates;
        if (path != null) {
            if (hasLength(path.ns())) {
                parentNs = path.ns();
            }
            predicates = path.value();
        } else {
            Predicate predicate = property.getAnnotation(Predicate.class);
            if (predicate != null) {
                predicates = new Predicate[] { predicate };
            } else {
                predicates = null;
            }
        }
        if (predicates != null) {
            List<MappedPredicate> predicatePath = 
                new ArrayList<MappedPredicate>(predicates.length);
            boolean first = true;
            for (Predicate predicate : predicates) {
                predicatePath.add(
                        new MappedPredicate(parentNs, predicate, 
                                first ? property.getName() : null));
                first = false;
            }
            return predicatePath;
        } else {
            return null;
        }
    }

    private boolean ignoreInvalid;

    private List<MappedPredicate> predicatePath;
    
    private MappedProperty<?> mappedProperty;
    
    private boolean constructorArgument;
    
    private boolean inherited = false;
    
    public MappedPath(MappedProperty<?> property, 
            List<MappedPredicate> predicatePath, 
            boolean inherited) {
        this.mappedProperty = property;
        this.predicatePath = predicatePath;
        this.inherited = inherited;
        Path path = property.getAnnotation(Path.class);
        if (path != null) {
            this.ignoreInvalid = path.ignoreInvalid();
        } else {
            if (predicatePath.size() > 0) {
                this.ignoreInvalid = predicatePath.get(0).ignoreInvalid();
            }
        }
        validate();
    }

    public MappedPredicate get(int i) {
        return predicatePath.get(i);
    }

	public MappedProperty<?> getMappedProperty() {
        return mappedProperty;
    }

    public List<MappedPredicate> getPredicatePath() {
        return predicatePath;
    }

    public UID getPropertyUri(int index) {
        return predicatePath.get(index).uid();
    }

    public boolean isConstructorParameter() {
		return constructorArgument;
	}
    
    public boolean isIgnoreInvalid() {
        return ignoreInvalid;
    }

    public boolean isWildcard() {
    	return isWildcard(getMappedProperty().getType());
    }
    
    public static boolean isWildcard(Class<?> type) {
    	return type == null;
    }
    
    public boolean isClassReference() {
    	return isClassReference(getMappedProperty().getType());
    }
    
    public static boolean isClassReference(Class<?> type) {
    	return type != null && Class.class.isAssignableFrom(type);
    }
    
    public boolean isReference() {
        MappedProperty<?> property = getMappedProperty();
    	return isMappedClass(property.getType()) 
    	    || property.isURI()
    	    || property.isInjection();
    }

    public static boolean isMappedClass(Class<?> type) {
    	return type != null && type.isAnnotationPresent(ClassMapping.class);
    }
    
    public boolean isInverse(int index) {
        return predicatePath.get(index).inv();
    }

    public boolean isSimpleProperty() {
        return predicatePath.size() == 1 
            && !get(0).inv() && !get(0).includeInferred();
    }

    void setConstructorArgument(boolean constructorArgument) {
		this.constructorArgument = constructorArgument;
	}

	
	boolean merge(MappedPath other) {
		if (mappedProperty.isAssignableFrom(other.mappedProperty)) {
			mappedProperty.addAnnotations(other.mappedProperty);
			return true;
		} else if (other.mappedProperty.isAssignableFrom(mappedProperty)) {
			other.mappedProperty.addAnnotations(mappedProperty);
			this.mappedProperty = other.mappedProperty;
			return true;
		} else {
			return false;
		}
	}
	
    public int size() {
        return predicatePath.size();
    }

	public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mappedProperty.toString());
        sb.append(" {");
        boolean first = true;
        for (MappedPredicate predicate : predicatePath) {
            sb.append(' ');
            if (predicate.inv()) {
                sb.append('^');
            } else if (!first) {
                sb.append('.');
            }
            sb.append(predicate.getReadableURI());
            first = false;
        }
        sb.append(" }");
        return sb.toString();
    }

	public void validate() {
    	Assert.notNull(mappedProperty);
    	mappedProperty.validate(this);
	}

	public int getOrder() {
		if (isSimpleProperty()) {
			if (!isReference()) {
				return 1;
			} else {
				return 2;
			}
		} else if (size() == 1) {
			return 3;
		} else {
			return 4;
		}
	}

	public boolean isInherited() {
		return inherited;
	}

}
