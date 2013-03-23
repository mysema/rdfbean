/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.mysema.commons.lang.Assert;
import com.mysema.rdfbean.annotations.ComponentType;
import com.mysema.rdfbean.annotations.Container;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.annotations.Default;
import com.mysema.rdfbean.annotations.Defaults;
import com.mysema.rdfbean.annotations.Id;
import com.mysema.rdfbean.annotations.InjectService;
import com.mysema.rdfbean.annotations.Localized;
import com.mysema.rdfbean.annotations.MapElements;
import com.mysema.rdfbean.annotations.Mixin;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.annotations.Properties;
import com.mysema.rdfbean.annotations.Required;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.UID;
import com.mysema.util.BeanMap;

/**
 * @author sasa
 * 
 */
public abstract class MappedProperty<M extends Member & AnnotatedElement> implements Cloneable {

    @SuppressWarnings("unchecked")
    public static final List<Class<? extends Annotation>> MAPPING_ANNOTATIONS =
            Collections.unmodifiableList(Arrays.<Class<? extends Annotation>> asList(
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

    @Nullable
    private final String name;

    @Nullable
    private Class<?> type;

    @Nullable
    private Class<?> componentType;

    private Class<?> keyType;

    private boolean collection;

    private MappedClass declaringClass;

    private TypeVariable<?>[] typeVariables = new TypeVariable<?>[4];

    private Map<Class<? extends Annotation>, Annotation> annotations =
            new HashMap<Class<? extends Annotation>, Annotation>();

    private boolean includeMapped;

    @SuppressWarnings("unchecked")
    MappedProperty(@Nullable String name, Annotation[] annotations, MappedClass declaringClass) {
        this.name = name;
        this.declaringClass = declaringClass;
        for (Annotation annotation : Assert.notNull(annotations, "annotations")) {
            Class<? extends Annotation> aclass = (Class<? extends Annotation>) annotation.getClass().getInterfaces()[0];
            this.annotations.put(aclass, annotation);
        }
    }

    public MappedClass getDeclaringClass() {
        return declaringClass;
    }

    static Class<?> getUpper(@Nullable Class<?> clazz, Class<?> other) {
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
        } else if (type.isArray()) {
            this.componentType = type.getComponentType();
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

        Properties propertiesAnno = getAnnotation(Properties.class);
        if (propertiesAnno != null) {
            includeMapped = propertiesAnno.includeMapped();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Class<? extends Collection> getCollectionType() {
        if (isCollection()) {
            return getConcreteCollectionType(getType());
        } else {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Class<? extends Collection> getConcreteCollectionType(Class<?> collectionType) {

        if (collectionType.isInterface()) {
            if (List.class.isAssignableFrom(collectionType)) {
                return ArrayList.class;
            } else if (SortedSet.class.isAssignableFrom(collectionType)) {
                return TreeSet.class;
            } else if (Set.class.isAssignableFrom(collectionType)) {
                return LinkedHashSet.class;
            } else if (Collection.class.equals(collectionType)) {
                return HashSet.class;
            } else {
                throw new IllegalArgumentException("Unsupported Collection interface type: " + collectionType);
            }
        }
        else if (Collection.class.isAssignableFrom(collectionType)) {
            return (Class<? extends Collection>) collectionType;
        }

        return null;
    }

    @Nullable
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
    Class getGenericClass(@Nullable final Type t, int index, MappedClass owner, int typeVariableIndex) {
        Type gtype = t;
        if (gtype != null) {
            if (gtype instanceof Class) {
                return (Class) gtype;
            } else if (gtype instanceof ParameterizedType && index >= 0) {
                gtype = ((ParameterizedType) gtype).getActualTypeArguments()[index];
            }
            if (gtype instanceof Class) {
                return (Class) gtype;
            } else if (gtype instanceof WildcardType) {
                return getGenericClass((WildcardType) gtype);
            } else if (gtype instanceof TypeVariable) {
                return getGenericClass(owner, typeVariableIndex, (TypeVariable) gtype);
            } else if (gtype instanceof ParameterizedType) {
                return (Class) ((ParameterizedType) gtype).getRawType();
            } else {
                throw new SessionException("Unable to get generic type [" + index + "] of " + t + " from " + owner);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<?> getGenericClass(MappedClass owner, int typeVariableIndex, TypeVariable<?> type) {
        Type upperBound = null;
        if (owner == null || declaringClass.equals(owner)) {
            typeVariables[typeVariableIndex] = type;
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
    }

    @SuppressWarnings("unchecked")
    private Class<?> getGenericClass(WildcardType wildcardType) {
        if (wildcardType.getUpperBounds()[0] instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) wildcardType.getUpperBounds()[0]).getRawType();
        } else if (wildcardType.getUpperBounds()[0] instanceof Class) {
            return (Class) wildcardType.getUpperBounds()[0];
        } else {
            // System.err.println("Unable to find out actual type of " + gtype);
            return Object.class;
        }
    }

    public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> atype) {
        return (T) annotations.get(atype);
    }

    @Nullable
    public UID getKeyPredicate() {
        MapElements mapKey = getAnnotation(MapElements.class);
        if (mapKey != null) {
            Predicate predicate = mapKey.key();
            // String parentNs = getParentNs(mapKey, getMember());
            return UID.create(declaringClass.getClassNs(), predicate.ns(), predicate.ln(), null);
        } else {
            return null;
        }
    }

    public Class<?> getKeyType() {
        return keyType;
    }

    protected abstract M getMember();

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
        if (mapKey != null) {
            Predicate predicate = mapKey.value();
            try {
                // String parentNs = getParentNs(mapKey, getMember());
                return UID.create(declaringClass.getClassNs(), predicate.ns(), predicate.ln(), null);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public IDType getIDType() {
        Id annotation = getAnnotation(Id.class);
        return annotation != null ? annotation.value() : null;
    }

    @Nullable
    public String getIDNamespace() {
        Id annotation = getAnnotation(Id.class);
        return annotation != null ? annotation.ns() : null;
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
        // refers to List RDF mapping, not the java.util.List type
        Container container = getAnnotation(Container.class);
        if (container != null) {
            return ContainerType.LIST == container.value();
        } else {
            return List.class.isAssignableFrom(getType()) || type.isArray();
        }
    }

    public boolean isArray() {
        return type.isArray();
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

    public boolean isDynamic() {
        return isAnnotationPresent(Properties.class);
    }

    // public boolean isPolymorphic() {
    // return MappedClass.isPolymorphic(getTargetType());
    // }

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

    @Override
    public String toString() {
        return getMember().toString();
    }

    public boolean isAssignableFrom(MappedProperty<?> other) {
        // Only methods may override...
        if (MethodProperty.class.isInstance(other) && Objects.equal(name, other.name)) {
            Class<?> domain = getMember().getDeclaringClass();
            Class<?> otherDomain = other.getMember().getDeclaringClass();
            return domain.isAssignableFrom(otherDomain);
        } else {
            return false;
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
        return container != null
                && container.value() != ContainerType.LIST
                && container.value() != ContainerType.NONE;
    }

    @Nullable
    public ContainerType getContainerType() {
        Container container = this.getAnnotation(Container.class);
        return container != null ? container.value() : null;
    }

    public boolean isIndexed() {
        return isList() || ContainerType.SEQ.equals(getContainerType());
    }

    @Override
    public Object clone() {
        try {
            MappedProperty<?> clone = (MappedProperty<?>) super.clone();
            clone.annotations = new HashMap<Class<? extends Annotation>, Annotation>(annotations);
            clone.typeVariables = new TypeVariable<?>[4];
            System.arraycopy(typeVariables, 0, clone.typeVariables, 0, 4);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new SessionException(e);
        }
    }

    public boolean isClassReference() {
        return Class.class.isAssignableFrom(type);
    }

    public boolean isIncludeMapped() {
        return includeMapped;
    }

    public boolean isDynamicCollection() {
        return Collection.class.isAssignableFrom(componentType);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Class<? extends Collection> getDynamicCollectionType() {
        if (isDynamicCollection()) {
            return getConcreteCollectionType(componentType);
        }
        else {
            return null;
        }
    }

    @Nullable
    public Class<?> getDynamicCollectionComponentType() {
        Type genericType = getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedComponentType = (ParameterizedType) ((ParameterizedType) genericType).getActualTypeArguments()[1];
            return (Class<?>) parameterizedComponentType.getActualTypeArguments()[0];
        }
        else {
            return null;
        }
    }
}