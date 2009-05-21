/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.collections15.BeanMap;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.*;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.MiniDialect;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.StringUtils;

/**
 * @author sasa
 *
 */
public abstract class MappedProperty<M extends Member & AnnotatedElement> {

	private static String getParentNs(MapElements mapElements, Member member) {
        String ns = mapElements.ns();
        if (!StringUtils.hasLength(ns)) {
            ns = MappedClass.getClassNs(member.getDeclaringClass());
        }
        return ns;
	}

	private String name;
	
	private Map<Class<? extends Annotation>, Annotation> annotations =
		new HashMap<Class<? extends Annotation>, Annotation>();

	@SuppressWarnings("unchecked")
    MappedProperty(String name, Annotation[] annotations) {
		this.name = name;
		for (Annotation annotation : Assert.notNull(annotations)) {
			Class<? extends Annotation> aclass = (Class<? extends Annotation>) annotation.getClass().getInterfaces()[0];
			this.annotations.put(aclass, annotation);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class<? extends Collection> getCollectionType() {
		if (isCollection()) {
			Class collectionType = getType();
			if (collectionType.isInterface()) {
				if (List.class.isAssignableFrom(collectionType)) {
					collectionType = ArrayList.class;
				} else if (SortedSet.class.isAssignableFrom(collectionType)) {
				    collectionType = TreeSet.class;
				} else if (Set.class.isAssignableFrom(collectionType)) {
					collectionType = LinkedHashSet.class;
				} else if (Collection.class.equals(collectionType)) {
					collectionType = HashSet.class;
				} else {
					throw new IllegalArgumentException("Unsupported Collection interface type: "+collectionType);
				}
			}
			return collectionType;
		} else {
			return null;
		}
	} 
	
	public Class<?> getComponentType() {
		ComponentType ctypeAnno = getAnnotation(ComponentType.class);
		if (ctypeAnno != null) {
			return ctypeAnno.value();
		} else if (isCollection()) {
			return getGenericClass(getParametrizedType(), 0);
		} else if (isMap()) {
			return getGenericClass(getParametrizedType(), 1);
		} else {
			return null;
		}
	}
	
	public Class<?> getTargetType() {
	    Class<?> clazz = getComponentType();
	    if (clazz == null) {
	        clazz = getType();
	    }
	    return clazz;
	}

	public List<UID> getDefaults() {
	    Default[] defaults;
	    Defaults defs = getAnnotation(Defaults.class);
	    if (defs != null) {
	        defaults = defs.value();
	    } else {
	        Default def = getAnnotation(Default.class);
	        if (def != null) {
	            defaults = new Default[] { def };
	        } else {
	            defaults = new Default[0];
	        }
	    }
	    List<UID> rs = new ArrayList<UID>(defaults.length);
	    for (Default def : defaults) {
	    	// TODO: Use default ns and ln if there's only one default?
	        rs.add(MiniDialect.UID(null, def.ns(), def.ln(), name));
	    }
	    return rs;
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> atype) {
		return annotations.containsKey(atype);
	}

    static Type getGenericType(Type gtype, int index) {
        if (gtype instanceof Class) {
            return gtype;
        } else if (gtype instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) gtype;
            return ptype.getActualTypeArguments()[index];
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	static Class getGenericClass(Type gtype, int index) {
//		Type gtype = getParametrizedType(member);
		if (gtype != null) {
		    if (gtype instanceof Class) {
		        return (Class) gtype;
		    } else if (gtype instanceof ParameterizedType) {
		        Type type = getGenericType(gtype, index);
    			if (type instanceof WildcardType) {
    			    WildcardType wildcardType = (WildcardType) type; 
    			    if (wildcardType.getUpperBounds()[0] instanceof ParameterizedType) {
    			        return (Class) ((ParameterizedType) wildcardType.getUpperBounds()[0]).getRawType();
    			    } else if (wildcardType.getUpperBounds()[0] instanceof Class) {
    			        return (Class) wildcardType.getUpperBounds()[0];
    			    } else {
    			        //System.err.println("Unable to find out actual type of " + gtype);
    			        return Object.class;
    			    }
                } else if (type instanceof TypeVariable) {
                    return (Class) ((TypeVariable) type).getGenericDeclaration();
                } else if (type instanceof ParameterizedType) {
                    return (Class) ((ParameterizedType) type).getRawType();
    			} else {
    			    try {
    			        return (Class) type;
    			    } catch (Exception e) {
    			        e.printStackTrace();
    			    }
    			}
		    }
		}
		return null;
	}
    
	@SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> atype) {
    	return (T) annotations.get(atype);
    }
	
	public UID getKeyPredicate() {
		MapElements mapKey = getAnnotation(MapElements.class);
		String parentNs = getParentNs(mapKey, getMember());
		if (mapKey != null) {
			Predicate predicate = mapKey.key(); 
			return MiniDialect.UID(parentNs, predicate.ns(), predicate.ln(), null);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Class getKeyType() {
		if (isMap()) {
			MapElements mapKey = getAnnotation(MapElements.class);
			if (mapKey != null) {
				if (!Void.class.equals(mapKey.keyType())) {
					return mapKey.keyType();
				} else {
					return getGenericClass(getParametrizedType(), 0);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	protected abstract  M getMember();
	
	protected abstract Type getParametrizedType();
	
	public String getName() {
		return name;
	}
	
	public abstract Class<?> getType();
	
	public abstract Object getValue(Object instance);
    
    public UID getValuePredicate() {
		MapElements mapKey = getAnnotation(MapElements.class);
        String parentNs = getParentNs(mapKey, getMember());
        if (mapKey != null) {
            Predicate predicate = mapKey.value();
            try {
                return MiniDialect.UID(parentNs, predicate.ns(), predicate.ln(), null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return null;
        }
	}
    
    public IDType getIDType() {
    	IDType idType = null;
    	Id annotation = getAnnotation(Id.class);
    	if (annotation != null) {
    		idType = annotation.value();
    	}
    	return idType;
    }
    
    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getType());
    }
    
    public boolean isIdReference() {
        return isAnnotationPresent(Id.class);
    }
    
    public boolean isList() {
        return List.class.isAssignableFrom(getType());
    }
    
    public boolean isLocalized() {
	    return isAnnotationPresent(Localized.class);
	}
    
    public boolean isInjection() {
        return isAnnotationPresent(Inject.class);
    }
        
    public boolean isMixin() {
        return isAnnotationPresent(Mixin.class);
    }
    
    public boolean isMap() {
		return Map.class.isAssignableFrom(getType());
	}

	public boolean isPolymorphic() {
        return MappedClass.isPolymorphic(getGenericClass(getTargetType(), 0));
    }
	
	public boolean isConstructorParameter() {
		return getMember() instanceof Constructor<?>;
	}
	
	public boolean isSet() {
        return Set.class.isAssignableFrom(getType());
    }
	
	public boolean isSortedSet() {
        return SortedSet.class.isAssignableFrom(getType());
    }
	
	public boolean isRequired() {
	    return isAnnotationPresent(Required.class);
	}
	
	public abstract boolean isVirtual();

    public abstract void setValue(BeanMap beanWrapper, Object value);

	public void validate(MappedPath path) {
		if (isMixin()) {
			Member member = getMember();
			if (getType().isAssignableFrom(member.getDeclaringClass())) {
				throw new IllegalArgumentException("Illegal mixin reference to oneself: " + 
						toString());
			}
		}
	}
    
	public String toString() {
		return getMember().toString();
	}

	public boolean isAssignableFrom(MappedProperty<?> other) {
		// Only methods may override...
		if (MethodProperty.class.isInstance(other) && nullSafeEquals(name, other.name)) {
			Class<?> domain = getMember().getDeclaringClass();
			Class<?> otherDomain = other.getMember().getDeclaringClass();
			return domain.isAssignableFrom(otherDomain);
		} else {
			return false;
		}
	}


	public static boolean nullSafeEquals(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return false;
		} else {
			return o1.equals(o2);
		}
	}

	public void addAnnotations(MappedProperty<?> other) {
		this.annotations.putAll(other.annotations);
	}

    public boolean isAnyResource() {
        return UID.class == getType();
    }

    public boolean isURI() {
        return UID.class.isAssignableFrom(getTargetType());
    }
}
