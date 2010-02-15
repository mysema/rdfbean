/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import javax.annotation.Nullable;

import org.apache.commons.collections15.BeanMap;

import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.*;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.StringUtils;

/**
 * @author sasa
 *
 */
public abstract class MappedProperty<M extends Member & AnnotatedElement> implements Cloneable {
    
    @SuppressWarnings("unchecked")
    public static final List<Class<? extends Annotation>> MAPPING_ANNOTATIONS = 
        Collections.unmodifiableList(Arrays.<Class<? extends Annotation>>asList(
            ComponentType.class,
            Container.class,
            Default.class, 
            Defaults.class,
            Id.class,
            InjectService.class, 
            Localized.class,
            MapElements.class,
            Mixin.class,
            Required.class
        ));

    private static String getParentNs(MapElements mapElements, Member member) {
        String ns = mapElements.ns();
        if (!StringUtils.hasLength(ns)) {
            ns = MappedClass.getClassNs(member.getDeclaringClass());
        }
        return ns;
    }

    @Nullable
    private String name;
    
    private Class<?> type;
    
    private Class<?> componentType;
    
    private Class<?> keyType;
    
    private boolean collection;
    
    private MappedClass declaringClass;
    
    private TypeVariable<?>[] typeVariables = new TypeVariable<?>[4];
    
    private Map<Class<? extends Annotation>, Annotation> annotations =
        new HashMap<Class<? extends Annotation>, Annotation>();

    @SuppressWarnings("unchecked")
    MappedProperty(@Nullable String name, Annotation[] annotations, MappedClass declaringClass) {
        this.name = name;
        this.declaringClass = declaringClass;
        for (Annotation annotation : Assert.notNull(annotations)) {
            Class<? extends Annotation> aclass = (Class<? extends Annotation>) annotation.getClass().getInterfaces()[0];
            this.annotations.put(aclass, annotation);
        }
    }
    
    public MappedClass getDeclaringClass() {
        return declaringClass;
    }
    
    static Class<?> getUpper(Class<?> clazz, Class<?> other) {
        if (clazz == null) {
            return other;
        } else if (other != null && !clazz.equals(other)) {
            if (clazz.isAssignableFrom(other)) {
                return other;
            }
        }
        return clazz;
    }
    
    @SuppressWarnings("unchecked")
    void resolve(@Nullable MappedClass owner) {
        if (this.type == null) {
            this.type = getTypeInternal();
        }
        Type genericType = getGenericType();
        if (genericType instanceof TypeVariable) {
            this.type = getUpper(this.type, getGenericClass(genericType, 0, owner, 0));
        }

        this.collection = Collection.class.isAssignableFrom(type);
        
        ComponentType ctypeAnno = getAnnotation(ComponentType.class);
        if (ctypeAnno != null) {
            this.componentType = ctypeAnno.value();
        } else if (collection || isClassReference()) {
            this.componentType = getUpper(this.componentType, getGenericClass(genericType, 0, owner, 1));
        } else if (isMap()) {
            MapElements mapKey = getAnnotation(MapElements.class);
            if (mapKey != null && !Void.class.equals(mapKey.keyType())) {
                keyType = mapKey.keyType();
            } else {
                keyType = getUpper(keyType, getGenericClass(genericType, 0, owner, 2));
            }
            this.componentType = getUpper(componentType, getGenericClass(genericType, 1, owner, 3));
        } else {
            this.componentType = null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
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
        return componentType;
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
            rs.add(UID.create(null, def.ns(), def.ln(), name));
        }
        return rs;
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> atype) {
        return annotations.containsKey(atype);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    Class getGenericClass(@Nullable final Type gtype, int index, MappedClass owner, int typeVariableIndex) {
        Type type = gtype;
//        Type gtype = getParametrizedType(member);
        if (type != null) {
            if (type instanceof Class) {
                return (Class) type;
            } else if (type instanceof ParameterizedType && index >= 0) {
                type = ((ParameterizedType) type).getActualTypeArguments()[index];
            }
            if (type instanceof Class) {
                return (Class) type;
            } else if (type instanceof WildcardType) {
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
                Type upperBound = null;
                if (owner == null || declaringClass.equals(owner)) {
                    typeVariables[typeVariableIndex] = (TypeVariable) type;
                    upperBound = typeVariables[typeVariableIndex].getBounds()[0]; 
                } else if (typeVariables[typeVariableIndex] != null) {
                    Type genericType = owner.resolveTypeVariable(typeVariables[typeVariableIndex].getName(), declaringClass);
                    if (genericType instanceof TypeVariable) {
                        // Nested TypeVariable in a sub class
                        typeVariables[typeVariableIndex] = (TypeVariable<?>) genericType;
                        upperBound = typeVariables[typeVariableIndex].getBounds()[0];
                    } else {
                        typeVariables[typeVariableIndex] = null;
                        upperBound = genericType;
                    } 
                    declaringClass = owner;
                }
                return getGenericClass(upperBound, -1, owner, -1);
//                return (Class) ((TypeVariable) type).getGenericDeclaration();
            } else if (type instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) type).getRawType();
            } else {
                throw new RuntimeException("Unable to get generic type [" + index + "] of " + gtype
                        + " from " + owner);
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
            return UID.create(parentNs, predicate.ns(), predicate.ln(), null);
        } else {
            return null;
        }
    }
    
    public Class<?> getKeyType() {
        return keyType;
    }
    
    protected abstract  M getMember();
    
    public String getName() {
        return name;
    }
    
    public Class<?> getType() {
        return type;
    }
    
    protected abstract Class<?> getTypeInternal();
    
    protected abstract Type getGenericType();
    
    public abstract Object getValue(BeanMap instance);
    
    @Nullable
    public UID getValuePredicate() {
        MapElements mapKey = getAnnotation(MapElements.class);
        String parentNs = getParentNs(mapKey, getMember());
        if (mapKey != null) {
            Predicate predicate = mapKey.value();
            try {
                return UID.create(parentNs, predicate.ns(), predicate.ln(), null);
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
    
    public boolean isAnnotatedProperty() {
        if (!annotations.isEmpty()) {
            for (Class<? extends Annotation> anno : MAPPING_ANNOTATIONS) {
                if (annotations.containsKey(anno)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isCollection() {
        return collection;
    }
    
    public boolean isIdReference() {
        return isAnnotationPresent(Id.class);
    }
    
    public boolean isList() {
        Container container = (Container) getAnnotation(Container.class);
        if (container != null) {
            return ContainerType.LIST == container.value();
        } else {
            return List.class.isAssignableFrom(getType());
        }
    }
    
    public boolean isLocalized() {
        return isAnnotationPresent(Localized.class);
    }
    
    public boolean isInjection() {
        return isAnnotationPresent(InjectService.class);
    }
        
    public boolean isMixin() {
        return isAnnotationPresent(Mixin.class);
    }
    
    public boolean isMap() {
        return Map.class.isAssignableFrom(getType());
    }

    public boolean isPolymorphic() {
        return MappedClass.isPolymorphic(getTargetType());
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

    public abstract void setValue(BeanMap beanWrapper, @Nullable Object value);

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

    public boolean isContainer() {
        Container container = this.getAnnotation(Container.class);
        return container != null && container.value() != ContainerType.LIST;
    }

    public ContainerType getContainerType() {
        Container container = this.getAnnotation(Container.class);
        return container != null ? container.value() : null;
    }

    public boolean isIndexed() {
        return isList() || ContainerType.SEQ.equals(getContainerType());
    }

    public Object clone() {
        try {
            MappedProperty<?> clone = (MappedProperty<?>) super.clone();
            clone.annotations = new HashMap<Class<? extends Annotation>, Annotation>(annotations);
            clone.typeVariables = new TypeVariable<?>[4];
            System.arraycopy(typeVariables, 0, clone.typeVariables, 0, 4);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isClassReference() {
        return Class.class.isAssignableFrom(type);
    }
    
}
