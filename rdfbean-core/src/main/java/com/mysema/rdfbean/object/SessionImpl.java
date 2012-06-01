/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.types.EntityPath;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Blocks;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.Identifier;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QNODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQuery;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.ontology.Ontology;
import com.mysema.rdfbean.query.BeanQueryImpl;
import com.mysema.util.BeanMap;
import com.mysema.util.MultimapFactory;


/**
 * Default implementation of the Session interface
 *
 * @author sasa
 * @author tiwe
 *
 */
public final class SessionImpl implements Session {

    private static final Set<UID> CONTAINER_TYPES = new HashSet<UID>(Arrays.<UID> asList(
            RDF.Alt, RDF.Seq, RDF.Bag, RDFS.Container));

    private static final int DEFAULT_INITIAL_CAPACITY = 1024;

    private static final Logger logger = LoggerFactory.getLogger(SessionImpl.class);

    private Set<STMT> addedStatements;

    private final Configuration configuration;

    private final RDFConnection connection;

    private final DefaultErrorHandler errorHandler = new DefaultErrorHandler();

    private FlushMode flushMode = FlushMode.ALWAYS;

    private final IdentityService identityService;

    private Multimap<ID, Object> instanceCache;

    private Map<ID, Map<String, NODE>> listCache;

    private final Iterable<Locale> locales;

    private final Ontology ontology;

    private final Map<String, ObjectRepository> parentRepositories = new HashMap<String, ObjectRepository>();

    private Set<STMT> removedStatements;

    private Map<Object, ID> resourceCache;

    @Nullable
    private Set<Object> seen;

    @Nullable
    private RDFBeanTransaction transaction;

    public SessionImpl(Configuration configuration, Ontology ontology, RDFConnection connection, Iterable<Locale> locales) {
        this.configuration = configuration;
        this.ontology = ontology;
        this.connection = connection;
        this.locales = locales;
        this.identityService = new SessionIdentityService(connection);
        clear();
    }

    public SessionImpl(Configuration configuration, Ontology ontology, RDFConnection connection, Locale... locales) {
        this(configuration, ontology, connection, Arrays.asList(locales));
    }

    @Override
    public void addParent(String ns, ObjectRepository parent) {
        Assert.hasText(ns,"ns");
        Assert.notNull(parent,"parent");
        parentRepositories.put(ns, parent);
    }

    private <T> T assertHasIdProperty(T instance){
        MappedClass mappedClass = configuration.getMappedClass(instance.getClass());
        if (mappedClass.getIdProperty() == null){
            throw new IllegalArgumentException(instance.getClass().getName() + " has no id property");
        }
        return instance;
    }

    private <T> T assertMapped(T instance) {
        if (!configuration.isMapped(instance.getClass())){
            throw new IllegalArgumentException(instance.getClass().getName() + " is not mapped");
        }
        return instance;
    }

    private ID assignId(MappedClass mappedClass, BeanMap instance) {
        ID subject = createResource(mappedClass.getUID(), instance);
        setId(mappedClass, subject, instance);        
        return subject;
    }

    @Override
    public void autowire(Object instance) {
        Assert.notNull(instance,"instance");
        BeanMap beanMap = toBeanMap(instance);
        MappedClass mappedClass = configuration.getMappedClass(getClass(instance));
        bind(mappedClass, getId(mappedClass, beanMap), beanMap, new PropertiesMap());
    }

    @Override
    public RDFBeanTransaction beginTransaction() {
        return beginTransaction(false, -1, java.sql.Connection.TRANSACTION_READ_COMMITTED);
    }

    @Override
    public RDFBeanTransaction beginTransaction(boolean readOnly, int txTimeout, int isolationLevel) {
        if (transaction != null) {
            throw new IllegalStateException("Transaction exists already");
        }
        transaction = connection.beginTransaction(readOnly, txTimeout, isolationLevel);
        return transaction;
    }

    private <T> T bind(MappedClass mappedClass, ID subject, T instance, PropertiesMap properties) {
        if (instance instanceof LifeCycleAware) {
            ((LifeCycleAware) instance).beforeBinding();
        }
        // TODO: defaultContext parameter?
        UID context = getContext(instance, subject, null);
        BeanMap beanMap = toBeanMap(instance);
//        MappedClass mappedClass = configuration.getMappedClass(getClass(instance));
        setId(mappedClass, subject, beanMap);
        // loadStack.add(instance);

        if (!mappedClass.getDynamicProperties().isEmpty()){
            bindDynamicProperties(subject, properties.getDirect(), beanMap, mappedClass);
        }

        for (MappedPath path : mappedClass.getProperties()) {
            if (!path.isConstructorParameter()) {
                MappedProperty<?> property = path.getMappedProperty();
                if (!property.isVirtual()) {
                    Object convertedValue;
                    try {
                        convertedValue = getValue(path, getPathValue(path, subject, properties, context), context);
                    } catch (InstantiationException e) {
                        throw new SessionException(e);
                    } catch (IllegalAccessException e) {
                        throw new SessionException(e);
                    }
                    if (convertedValue != null) {
                        property.setValue(beanMap, convertedValue);
                    }
                }
            }
        }
        if (instance instanceof LifeCycleAware) {
            ((LifeCycleAware) instance).afterBinding();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void bindDynamicProperties(ID subject, Multimap<UID, STMT> properties, BeanMap beanMap, MappedClass mappedClass) {
        for (MappedProperty<?> property : mappedClass.getDynamicProperties()) {
            Map<UID, Object> values = new HashMap<UID, Object>();

            for (STMT stmt : properties.values()) {
                if (stmt.getPredicate().equals(CORE.localId)){
                    // skip local ids
                    continue;
                }

                if (property.isIncludeMapped() || !mappedClass.isMappedPredicate(stmt.getPredicate())) {
                    Class<?> componentType;
                    if (property.isDynamicCollection()) {
                        componentType = property.getDynamicCollectionComponentType();
                    } else {
                        componentType = property.getComponentType();
                    }

                    // for resources make sure that componentType is compatible with the resource
                    if (stmt.getObject().isResource()){
                        List<STMT> typeStmts = findStatements(stmt.getObject().asResource(), RDF.type, null, null, false);
                        boolean matched = false;
                        for (STMT typeStmt : typeStmts){
                            for (MappedClass cl : configuration.getMappedClasses(typeStmt.getObject().asURI())){
                                if (componentType.isAssignableFrom(cl.getJavaClass())){
                                    matched = true;
                                }
                            }
                        }
                        if (!matched){
                            continue;
                        }
                    // for literals, make sure that componentType is a literal type
                    }else{
                        UID dataType = configuration.getConverterRegistry().getDatatype(componentType);
                        if (dataType == null || !stmt.getObject().asLiteral().getDatatype().equals(dataType)){
                            continue;
                        }
                    }

                    Object value = convertValue(stmt.getObject(), componentType);
                    if (value != null) {
                        if (property.isDynamicCollection()) {
                            Collection<Object> collection = (Collection<Object>) values.get(stmt.getPredicate());
                            if (collection == null) {
                                try {
                                    collection = property.getDynamicCollectionType().newInstance();
                                    values.put(stmt.getPredicate(), collection);
                                } catch (InstantiationException e) {
                                    throw new SessionException(e);
                                } catch (IllegalAccessException e) {
                                    throw new SessionException(e);
                                }
                            }
                            collection.add(value);

                        } else {
                            values.put(stmt.getPredicate(), value);
                        }
                    }
                }
            }
            property.setValue(beanMap, values);
        }
    }

    @Override
    public void clear() {
//        instanceCache = LazyMap.decorate(new HashMap<ID, List<Object>>(DEFAULT_INITIAL_CAPACITY), LIST_FACTORY);
        instanceCache = HashMultimap.create();
        resourceCache = new IdentityHashMap<Object, ID>(DEFAULT_INITIAL_CAPACITY);
        addedStatements = new LinkedHashSet<STMT>(DEFAULT_INITIAL_CAPACITY);
        removedStatements = new LinkedHashSet<STMT>(DEFAULT_INITIAL_CAPACITY);
        listCache = new LinkedHashMap<ID, Map<String,NODE>>(DEFAULT_INITIAL_CAPACITY);
        seen = null;
    }

    @Override
    public void close() {
        connection.close();
    }

    @SuppressWarnings("unchecked")
    private Object convertClassReference(NODE value, MappedPath propertyPath, MappedProperty mappedProperty) {
        if (value instanceof UID) {
            return convertClassReference((UID) value, mappedProperty.getComponentType());
        } else {
            throw new BindException(propertyPath, "bnode or literal", value);
        }
    }

    @Nullable
    private Class<?> convertClassReference(UID uid, Class<?> targetClass) {
        List<MappedClass> mappedClasses = configuration.getMappedClasses(uid);
        boolean foundMatch = false;
        for (MappedClass mappedClass : mappedClasses) {
            Class<?> clazz = mappedClass.getJavaClass();
            if (targetClass.isAssignableFrom(clazz)) {
                targetClass = clazz;
                foundMatch = true;
            }
        }
        if (foundMatch) {
            return targetClass;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertCollection(MappedPath propertyPath, Collection<? extends NODE> values, UID context)
            throws InstantiationException, IllegalAccessException {
        MappedProperty<?> mappedProperty = propertyPath.getMappedProperty();
        Class<?> targetType = mappedProperty.getComponentType();
        int size = values.size();
        if (size == 1) {
            NODE node = values.iterator().next();
            if (node instanceof ID) {
                if (mappedProperty.isList()) {
                    values = convertList((ID) node, context);
                } else if (mappedProperty.isContainer()) {
                    values = convertContainer((ID) node, context, mappedProperty.isIndexed());
                }
            } // TODO else log error?
        } // TODO else log error?
        Class collectionType = mappedProperty.getCollectionType();
        Collection collection = (Collection) collectionType.newInstance();
        for (NODE value : values) {
            if (value != null){
                collection.add(convertValue(value, targetType, propertyPath));
            }else{
                collection.add(null);
            }

        }
        return collection;
    }
    
    @SuppressWarnings("unchecked")
    private Object convertArray(MappedPath propertyPath, Collection<? extends NODE> values, UID context)
            throws InstantiationException, IllegalAccessException {
        MappedProperty<?> mappedProperty = propertyPath.getMappedProperty();
        Class<?> targetType = mappedProperty.getComponentType();
        int size = values.size();
        if (size == 1) {
            NODE node = values.iterator().next();
            if (node instanceof ID) {
                if (mappedProperty.isList()) {
                    values = convertList((ID) node, context);
                } else if (mappedProperty.isContainer()) {
                    values = convertContainer((ID) node, context, mappedProperty.isIndexed());                    
                }
            } // TODO else log error?
        } // TODO else log error?
        
        Object array = Array.newInstance(targetType, values.size());
        int i = 0;
        for (NODE value : values) {
            if (value != null) {
                Array.set(array, i++, convertValue(value, targetType, propertyPath));
            } else {
                Array.set(array, i++, null);
            }
        }
        return array;
    }

    private Collection<NODE> convertContainer(ID node, UID context, boolean indexed) {
        List<STMT> stmts = findStatements(node, null, null, context, false);
        Map<Integer, NODE> values = new LinkedHashMap<Integer, NODE>();
        int maxIndex = 0;
        int i = 0;
        for (STMT stmt : stmts) {
            i++;
            UID predicate = stmt.getPredicate();
            if (RDF.NS.equals(predicate.ns())) {
                String ln = predicate.ln();
                int index = 0;
                if ("li".equals(ln)) {
                    index = i;
                } else if (RDF.isContainerMembershipPropertyLocalName(ln)) {
                    index = Integer.valueOf(ln.substring(1));
                }
                if (index > 0) {
                    maxIndex = Math.max(maxIndex, index);
                    values.put(Integer.valueOf(index), stmt.getObject());
                }
            }
        }
        if (indexed) {
            NODE[] nodes = new NODE[maxIndex];
            for (Map.Entry<Integer, NODE> entry : values.entrySet()) {
                nodes[entry.getKey() - 1] = entry.getValue();
            }
            return Arrays.asList(nodes);
        } else {
            return values.values();
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertEnum(NODE value, Class<?> targetClass) {
        if (value instanceof UID) {
            return Enum.valueOf((Class<? extends Enum>) targetClass, ((UID) value).ln());
        } else if (value instanceof LIT) {
            return Enum.valueOf((Class<? extends Enum>) targetClass, value.getValue());
        } else {
            throw new BindException("Cannot bind BNode into enum");
        }
    }

    private Object convertIDReference(NODE value, MappedPath propertyPath) {
        if (value instanceof ID) {
            return value;
        } else {
            throw new BindException(propertyPath, value);
        }
    }

    private Collection<NODE> convertList(ID subject, UID context) {
        List<NODE> list = new ArrayList<NODE>();
        while (subject != null && !subject.equals(RDF.nil)) {
            if (logger.isDebugEnabled()){
                logger.debug("query for list elements of " +  subject);
            }
            Map<String,NODE> nodes = listCache.get(subject);
            if (nodes == null){
                nodes = new RDFQueryImpl(connection)
                    .where(Blocks.S_REST, Blocks.optional(Blocks.S_FIRST))
                    .set(QNODE.s, subject)
                    .selectSingle(QNODE.first, QNODE.rest);
            }

            if (nodes == null){
                break;
            }
            NODE value = nodes.get(QNODE.first.getName());
            if (value != null){
                list.add(value);
            }else{
                list.add(null);
            }
            subject = (ID) nodes.get(QNODE.rest.getName());
        }
        return list;
    }

    private String convertLocalized(MappedPath propertyPath, Set<? extends NODE> values) {
        return LocaleUtil.getLocalized(convertLocalizedMap(propertyPath, values), locales, null);
    }

    private Map<Locale, String> convertLocalizedMap(MappedPath propertyPath, Set<? extends NODE> values) {
        Map<Locale, String> result = new HashMap<Locale, String>();
        for (NODE value : values) {
            if (value.isLiteral()) {
                LIT literal = (LIT) value;
                Locale lang = literal.getLang();
                if (lang == null) {
                    lang = Locale.ROOT;
                }
                result.put(lang, literal.getValue());
            } else {
                throw new IllegalArgumentException("Expected Literal, got " + value.getNodeType());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object convertMap(MappedPath propertyPath, Set<? extends NODE> values) {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Class<?> componentType = propertyDefinition.getComponentType();
        Class<?> keyType = propertyDefinition.getKeyType();
        Map map = new HashMap();
        for (NODE value : values) {
            // Map key
            Object key = convertValue(
                    getFunctionalValue((ID) value, propertyDefinition.getKeyPredicate(), false, null), keyType, propertyPath);
            // Map Value
            Object mapValue;
            UID valuePredicate = propertyDefinition.getValuePredicate();
            if (valuePredicate != null) {
                mapValue = convertValue(getFunctionalValue((ID) value, valuePredicate, false, null), componentType, propertyPath);
            } else {
                mapValue = convertValue(value, componentType, propertyPath);
            }
            map.put(key, mapValue);
        }
        convertedValue = map;
        return convertedValue;
    }

    @SuppressWarnings("unchecked")
    private Object convertMappedClass(NODE value, Class<?> targetClass, MappedPath propertyPath,
            MappedProperty mappedProperty) {
        if (value instanceof ID) {
            return convertMappedObject((ID) value, targetClass, isPolymorphic(mappedProperty), mappedProperty.isInjection());
        } else {
            throw new BindException(propertyPath, value);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T convertMappedObject(ID subject, Class<T> requiredClass, boolean polymorphic, boolean injection) {
        UID context = getContext(requiredClass, subject, null);
        Object instance = getCached(subject, requiredClass);
        if (instance == null) {
            if (injection) {
                if (subject instanceof UID && requiredClass != null) {
                    UID uri = (UID) subject;
                    ObjectRepository orepo = parentRepositories.get(uri.ns());
                    if (orepo != null) {
                        return orepo.getBean(requiredClass, uri);
                    } else {
                        throw new IllegalArgumentException("No such parent repository: " + uri.ns());
                    }
                }

            }else if (requiredClass.isEnum() && subject.isURI()){
                return (T)Enum.valueOf((Class)requiredClass, subject.asURI().ln());
            }

            MappedClass mappedClass = configuration.getMappedClass(requiredClass);
            Multimap<UID, STMT> direct = getProperties(subject, mappedClass, polymorphic);

            if (!direct.isEmpty()){
                Multimap<UID, STMT> inverse = null;
                if (mappedClass.getInvMappedPredicates().isEmpty()){
                    inverse = MultimapFactory.<UID, STMT>create();
                }else{
                    inverse = getInvProperties(subject, mappedClass);
                }
                instance = getMappedObject(mappedClass, subject, requiredClass, new PropertiesMap(direct, inverse), polymorphic, context, true);
            }

        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Object convertSingleValue(MappedPath propertyPath, Set<? extends NODE> values) {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Class targetType = propertyDefinition.getType();
        if (!values.isEmpty()){
            return convertValue(values.iterator().next(), targetType, propertyPath);
        }else{
            return null;
        }
    }

    private Object convertValue(NODE node, Class<?> targetClass) {
        UID targetType = configuration.getConverterRegistry().getDatatype(targetClass);
        if (targetClass.isAssignableFrom(node.getClass())){
            return node;
        } else if (targetType != null && node.isLiteral()) {
            // TODO : make sure this works also with untyped literals etc
            if (((LIT) node).getDatatype().equals(targetType)) {
                return configuration.getConverterRegistry().fromString(node.getValue(), targetClass);
            }else{
                throw new IllegalArgumentException("Literal " + node + " is not of type " + targetType);
            }
        } else if (targetType == null && node.isURI()) {
            return get(targetClass, node.asURI());
        }else{
            throw new IllegalArgumentException("Node " + node + " could not be converted to " + targetClass.getName());
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Object convertValue(NODE value, Class<?> targetClass, MappedPath propertyPath) {
        Object convertedValue;
        MappedProperty mappedProperty = propertyPath.getMappedProperty();
        try {
            if (targetClass.isAssignableFrom(value.getClass())){
                convertedValue = value;
            }
            // "Wildcard" type
            else if (MappedPath.isWildcard(targetClass) && value.isResource()) {
                convertedValue = convertMappedObject((ID) value, Object.class, true, mappedProperty.isInjection());
            }
            // Enumerations
            else if (targetClass.isEnum()) {
                convertedValue = convertEnum(value, targetClass);
            }
            // Class reference
            else if (mappedProperty.isClassReference()) {
                convertedValue = convertClassReference(value, propertyPath, mappedProperty);
            }
            // Mapped class
            else if (configuration.isMapped(targetClass) || mappedProperty.isInjection()) {
                convertedValue = convertMappedClass(value, targetClass, propertyPath, mappedProperty);
            }
            // ID reference
            else if (ID.class.isAssignableFrom(targetClass)) {
                convertedValue = convertIDReference(value, propertyPath);
            }
            // Use standard property editors for others
            else {
                // UID datatype = null;
                // if (value instanceof LIT) {
                // datatype = ((LIT) value).getDatatype();
                // }
                convertedValue = configuration.getConverterRegistry().fromString(value.getValue(), targetClass);
            }
        } catch (IllegalArgumentException e) {
            if (propertyPath.isIgnoreInvalid()) {
                logger.debug(e.getMessage(), e);
                convertedValue = null;
            } else {
                logger.error(e.getMessage(), e);
                convertedValue = errorHandler.conversionError(value, targetClass, propertyPath, e);
            }
        }
        return convertedValue;
    }

    @Nullable
    private <T> T createInstance(ID subject, Class<T> requiredType, Collection<ID> mappedTypes, PropertiesMap properties) {
        T instance;
        Class<? extends T> actualType = matchType(mappedTypes, requiredType);
        if (actualType != null) {
            if (!configuration.allowCreate(actualType)) {
                instance = null;
            } else {
                try {
                    MappedClass mappedClass = configuration.getMappedClass(actualType);
                    MappedConstructor mappedConstructor = mappedClass.getConstructor();
                    if (mappedConstructor == null) {
                        instance = actualType.newInstance();
                    } else {
                        List<Object> constructorArguments = getConstructorArguments(mappedClass, subject, properties, mappedConstructor);
                        @SuppressWarnings("unchecked")
                        Constructor<T> constructor = (Constructor<T>) mappedConstructor.getConstructor();
                        instance = constructor.newInstance(constructorArguments.toArray());
                    }
                } catch (InstantiationException e) {
                    logger.error(e.getMessage(), e);
                    instance = errorHandler.createInstanceError(subject, mappedTypes, requiredType, e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    instance = errorHandler.createInstanceError(subject, mappedTypes, requiredType, e);
                } catch (SecurityException e) {
                    logger.error(e.getMessage(), e);
                    instance = errorHandler.createInstanceError(subject, mappedTypes, requiredType, e);
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage(), e);
                    instance = errorHandler.createInstanceError(subject, mappedTypes, requiredType, e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    instance = errorHandler.createInstanceError(subject, mappedTypes, requiredType, e);
                }
            }
        } else {
            instance = errorHandler.typeMismatchError(subject, mappedTypes, requiredType);
        }
        return instance;
    }

    private <T> Map<ID, T> createInstances(MappedClass mappedClass, Class<T> clazz, boolean polymorphic, UID context,
            Map<ID, Multimap<UID, STMT>> directProps, Map<ID, Multimap<UID, STMT>> inverseProps) {
        Map<ID, T> idToInstance = new HashMap<ID, T>(directProps.size());
        for (Map.Entry<ID, Multimap<UID, STMT>> entry : directProps.entrySet()){
            T instance = getCached(entry.getKey(), clazz);
            if (instance == null){
                PropertiesMap properties = new PropertiesMap(entry.getValue(), inverseProps.get(entry.getKey()));
                instance = getMappedObject(mappedClass, entry.getKey(), clazz, properties, polymorphic, context, false);
                if (instance != null){
                    idToInstance.put(entry.getKey(), instance);
                }
            }
        }
        return idToInstance;
    }

    private RDFQuery createQuery(MappedClass mappedClass, @Nullable UID type, boolean polymorphic) {
        RDFQuery query = new RDFQueryImpl(connection);

        if (type != null){
            query.where(Blocks.S_TYPE);
            if (mappedClass.getContext() != null){
                query.set(QNODE.typeContext, mappedClass.getContext());
            }

            if (polymorphic){
                Collection<UID> types = ontology.getSubtypes(type);
                if (types.size() > 1 && connection.getInferenceOptions().subClassOf()){
                    query.where(QNODE.type.in(types));
                }else{
                    query.set(QNODE.type, type);
                }
            }else{
                query.set(QNODE.type, type);
            }
        }

        query.where(Blocks.SPOC);
        if (!polymorphic
                && mappedClass.getDynamicProperties().isEmpty()
                && mappedClass.getMappedPredicates().size() < 5){
            query.where(QNODE.p.in(mappedClass.getMappedPredicates()));
        }

        return query;
    }

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        return connection.createQuery(queryLanguage, definition);
    }

    @Override
    public <Q> Q createQuery(QueryLanguage<Void, Q> queryLanguage) {
        return connection.createQuery(queryLanguage, null);
    }

    private ID createResource(@Nullable UID type, BeanMap instance) {
        ID id = configuration.createURI(instance.getBean());
        if (id == null) {
//            String base = type != null ? type.getLocalName() : "Resource"; 
//            String local = UUID.randomUUID().toString().replace("-", "");
//            id = new UID("resource://" + base + "/" + local);
            id = connection.createBNode();
        }
        return id;
    }

    @Override
    public void delete(Object instance) {
        deleteInternal(assertMapped(instance));
        if (flushMode == FlushMode.ALWAYS) {
            flush();
        }
    }

    @Override
    public void deleteAll(Object... objects) {
        for (Object object : objects) {
            deleteInternal(assertMapped(object));
        }
        if (flushMode == FlushMode.ALWAYS) {
            flush();
        }
    }

    private void deleteInternal(Object instance) {
        BeanMap beanMap = toBeanMap(instance);
        ID subject = resourceCache.get(instance);
        Class<?> clazz = getClass(instance);
        MappedClass mappedClass = configuration.getMappedClass(clazz);
        UID context = getContext(instance, subject, null);
        if (subject == null) {
            subject = getId(mappedClass, beanMap);
        }
        if (subject != null) {
            // Delete own properties
            for (STMT statement : findStatements(subject, null, null, context, false)) {
                recordRemoveStatement(statement);
                NODE object = statement.getObject();
                if (object.isResource() && !statement.getPredicate().equals(RDF.type)) {
                    removeList((ID) object, context);
                    removeContainer((ID) object, context);
                }
            }
            // Delete references
            for (STMT statement : findStatements(null, null, subject, context, false)) {
                recordRemoveStatement(statement);
            }
            // Remove from primary cache
            Collection<Object> instances = instanceCache.removeAll(subject);
            if (instances != null) {
                for (Object obj : instances) {
                    resourceCache.remove(obj);
                }
            }
        }
    }

    private boolean exists(ID subject, MappedClass mappedClass, UID context) {
        UID type = mappedClass.getUID();
        if (type != null) {
            return connection.exists(subject, RDF.type, type, context, true);
        }
        return false;
    }

    private Set<NODE> filterObjects(List<STMT> statements) {
        Set<NODE> objects = new LinkedHashSet<NODE>();
        for (STMT statement : statements){
            objects.add(statement.getObject());
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T extends NODE> Set<T> filterSubject(List<STMT> statements) {
        Set<T> subjects = new LinkedHashSet<T>();
        for (STMT statement : statements){
            subjects.add((T) statement.getSubject());
        }
        return subjects;
    }

    @Override
    public <T> List<T> findInstances(Class<T> clazz) {
        UID type = configuration.getMappedClass(clazz).getUID();
        if (type != null){
            Set<T> instances = new LinkedHashSet<T>();
            findInstances(clazz, type, instances);
            return new ArrayList<T>(instances);
        }else{
            throw new IllegalArgumentException("No RDF type specified for " + clazz.getName());
        }

    }

    @Override
    public <T> List<T> findInstances(Class<T> clazz, LID lid) {
        ID id = identityService.getID(lid);
        if (id instanceof UID) {
            return findInstances(clazz, (UID) id);
        } else {
            throw new IllegalArgumentException("Blank nodes not supported");
        }
    }

    @Override
    public <T> List<T> findInstances(Class<T> clazz, UID uri) {
        final Set<T> instances = new LinkedHashSet<T>();
        findInstances(clazz, uri, instances);
        return new ArrayList<T>(instances);
    }

    private <T> void findInstances(Class<T> clazz, UID type, final Set<T> instances) {
        MappedClass mappedClass = configuration.getMappedClass(clazz);
        boolean polymorphic = isPolymorphic(mappedClass);
        UID context = mappedClass.getContext();

        if (logger.isDebugEnabled()){
            logger.debug("query for " + clazz.getSimpleName() + " instance data");
        }
        RDFQuery query = createQuery(mappedClass, type, polymorphic);
        CloseableIterator<STMT> stmts = query.construct(Blocks.SPOC);
        Map<ID, Multimap<UID, STMT>> directProps = getPropertiesMap(stmts, false);

        // no results
        if (directProps.isEmpty()){
            return;
        }

        Map<ID, Multimap<UID, STMT>> inverseProps = Collections.emptyMap();
        if (!polymorphic && !mappedClass.getInvMappedPredicates().isEmpty()){
            inverseProps = getInvProperties(mappedClass, directProps.keySet());
        }

        // create
        Map<ID, T> idToInstance = createInstances(mappedClass, clazz, polymorphic, context, directProps, inverseProps);

        // load references
        loadReferences(mappedClass, directProps, directProps.keySet());

        // bind
        for (Map.Entry<ID, Multimap<UID, STMT>> entry : directProps.entrySet()){
            T instance = getCached(entry.getKey(), clazz);
            if (idToInstance.containsKey(entry.getKey())){
                PropertiesMap properties = new PropertiesMap(entry.getValue(), inverseProps.get(entry.getKey()));
                MappedClass mc = resolveMappedClass(mappedClass, properties);
                bind(mc, entry.getKey(), instance, properties);
            }
            instances.add(instance);
        }
    }

    private MappedClass resolveMappedClass(MappedClass mappedClass, PropertiesMap properties) {
        for (STMT stmt : properties.getDirect().get(RDF.type)){
            if (stmt.getObject().isURI()){
                List<MappedClass> mappedClasses = configuration.getMappedClasses(stmt.getObject().asURI());
                for (MappedClass mc : mappedClasses){
                    if (!mc.equals(mappedClass) && mappedClass.getJavaClass().isAssignableFrom(mc.getJavaClass())){
                        return mc;
                    }
                }
            }
        }
        return mappedClass;
    }

    private List<ID> findMappedTypes(ID subject, UID context, Multimap<UID, STMT> properties) {
        List<ID> types = new ArrayList<ID>();
        if (properties.containsKey(RDF.type)){
            for (STMT stmt : properties.get(RDF.type)){
                NODE type = stmt.getObject();
                if (type instanceof UID && configuration.getMappedClasses((UID) type) != null) {
                    types.add((UID) type);
                }
            }
        }
        return types;
    }



    private Set<NODE> findPathValues(ID resource, MappedPath path, int index, PropertiesMap properties, UID context) {
        MappedPredicate predicate = path.get(index);
        if (predicate.getContext() != null) {
            context = predicate.getContext();
        }
        Set<NODE> values;
        if (!predicate.inv() && properties.getDirect() != null){
            values = findValues(predicate.getUID(), properties.getDirect(), context, predicate.inv());
        }else if (predicate.inv() && properties.getInverse() != null){
            values = findValues(predicate.getUID(), properties.getInverse(), null, predicate.inv());
        }else{
            values = findValues(resource, predicate.getUID(), predicate.inv(), predicate.includeInferred(), context);
        }
        if (path.size() > index + 1) {
            Set<NODE> nestedValues = new LinkedHashSet<NODE>();
            for (NODE value : values) {
                if (value.isResource()) {
                    nestedValues.addAll(findPathValues((ID) value, path, index + 1, new PropertiesMap(null, null), context));
                }
            }
            return nestedValues;
        }
        return values;
    }

    private List<STMT> findStatements(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object,
            @Nullable UID context, boolean includeInferred) {
        if (logger.isDebugEnabled()){
            logger.debug("findStatements " + subject + " " + predicate + " " + object + " " + context);
        }
        // rdf type inference
        if (RDF.type.equals(predicate)
                && subject == null && object != null
                && connection.getInferenceOptions().subClassOf()){
            Collection<UID> types = ontology.getSubtypes(object.asURI());
            if (types.size() > 1){
                RDFQuery query = new RDFQueryImpl(connection);
                CloseableIterator<STMT> stmts = query.where(
                        Blocks.SPOC, QNODE.o.in(types))
                      .set(QNODE.p, predicate)
                      .construct(Blocks.SPOC);
                return IteratorAdapter.asList(stmts);
            }
        }
        return IteratorAdapter.asList(connection.findStatements(subject, predicate, object, context, includeInferred));
    }

    private List<ID> findTypes(ID subject, UID context) {
        List<ID> types = new ArrayList<ID>();
        List<STMT> statements = findStatements(subject, RDF.type, null, context, true);
        for (STMT stmt : statements) {
            types.add((ID) stmt.getObject());
        }
        return types;
    }

    private Set<NODE> findValues(ID resource, UID predicate, boolean inverse, boolean includeInferred, UID context) {
        if (inverse) {
            return this.<NODE>filterSubject(findStatements(null, predicate, resource, context, includeInferred));
        } else {
            return filterObjects(findStatements(resource, predicate, null, context, includeInferred));
        }
    }

    private Set<NODE> findValues(UID predicate, Multimap<UID, STMT> properties, @Nullable UID context, boolean inv) {
        Set<NODE> nodes = new HashSet<NODE>();
        if (properties.containsKey(predicate)){
            for (STMT stmt : properties.get(predicate)){
                if (context == null || context.equals(stmt.getContext())){
                    nodes.add(inv ? stmt.getSubject() : stmt.getObject());
                }
            }
        }
        return nodes;
    }

    public void flush() {
        if (!removedStatements.isEmpty() || !addedStatements.isEmpty()){
            connection.update(removedStatements, addedStatements);    
        }        
        removedStatements = new LinkedHashSet<STMT>();
        addedStatements = new LinkedHashSet<STMT>();
    }

    @Override
    public BeanQuery from(EntityPath<?>... expr) {
        return new BeanQueryImpl(this, ontology, connection).from(expr);
    }

    @Override
    public <T> T get(Class<T> clazz, ID subject) {
        Assert.notNull(subject, "subject");
        boolean polymorphic = true;
        MappedClass mappedClass = configuration.getMappedClass(clazz);
        polymorphic = isPolymorphic(mappedClass);
        return convertMappedObject(subject, clazz, polymorphic, false);
    }

    @Override
    public <T> T get(Class<T> clazz, LID subject) {
        ID id = identityService.getID(subject);
        return id != null ? get(clazz, id) : null;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz, ID... subjects) {
        List<T> instances = new ArrayList<T>(subjects.length);
        if (!clazz.isEnum()){
            Set<ID> ids = new HashSet<ID>(subjects.length);
            for (ID id : subjects){
                if (id != null && getCached(id, clazz) == null){
                    ids.add(id);
                }
            }

            // return from cache
            if (ids.isEmpty()){
                return getFromCache(clazz, instances, subjects);
            }

            MappedClass mappedClass = configuration.getMappedClass(clazz);
            boolean polymorphic = isPolymorphic(mappedClass);
            UID context = mappedClass.getContext();

            if (logger.isDebugEnabled()){
                logger.debug("query for " + clazz.getSimpleName() + " instance data");
            }
            RDFQuery query = createQuery(mappedClass, null, polymorphic);
            query.where(QNODE.s.in(ids));
            CloseableIterator<STMT> stmts = query.construct(Blocks.SPOC);
            Map<ID, Multimap<UID, STMT>> directProps = getPropertiesMap(stmts, false);

            // no results, return from cache
            if (directProps.isEmpty()){
                return getFromCache(clazz, instances, subjects);
            }

            Map<ID, Multimap<UID, STMT>> inverseProps = Collections.emptyMap();
            if (!polymorphic && !mappedClass.getInvMappedPredicates().isEmpty()){
                inverseProps = getInvProperties(mappedClass, directProps.keySet());
            }

            // create
            Map<ID, T> idToInstance = createInstances(mappedClass, clazz, polymorphic, context, directProps, inverseProps);

            // load references
            loadReferences(mappedClass, directProps, directProps.keySet());

            // bind
            for (ID subject : subjects){
                T instance = null;
                if (subject != null && directProps.containsKey(subject)){
                    instance = getCached(subject, clazz);
                    if (idToInstance.containsKey(subject)){
                        PropertiesMap properties = new PropertiesMap(directProps.get(subject), inverseProps.get(subject));
                        bind(mappedClass, subject, instance, properties);
                    }
                }
                instances.add(instance);
            }

        }else{
            for (ID subject : subjects) {
                if (subject != null){
                    instances.add(get(clazz, subject));
                }else{
                    instances.add(null);
                }
            }
        }
        return instances;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz, LID... subjects) {
        ID[] ids = new ID[subjects.length];
        // TODO : bulk fetch
        for (int i=0; i < ids.length; i++){
            if (subjects[i] != null){
                ids[i] = identityService.getID(subjects[i]);
            }
        }
        return getAll(clazz, ids);
    }

    @Override
    public <T> T getBean(Class<T> clazz, UID subject) {
        return get(clazz, subject);
    }

    @Override
    public <T> T getByExample(T entity) {
        return new ExampleQuery<T>(configuration,this, entity).uniqueResult();
    }

    @Override
    public <T> T getById(String id, Class<T> clazz) {
        return get(clazz, new LID(id));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T getCached(ID resource, Class<T> clazz) {
        for (Object instance : instanceCache.get(resource)) {
            if (clazz == null || clazz.isInstance(instance)) {
                return (T)instance;
            }
        }
        return null;
    }

    private Class<?> getClass(Object object) {
        return object instanceof BeanMap ? ((BeanMap) object).getBean().getClass() : object.getClass();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    public RDFConnection getConnection() {
        return connection;
    }

    private List<Object> getConstructorArguments(MappedClass mappedClass, ID subject,
            PropertiesMap properties, MappedConstructor mappedConstructor) throws InstantiationException, IllegalAccessException {
        List<Object> constructorArguments = new ArrayList<Object>(mappedConstructor.getArgumentCount());
        // TODO parentContext?
        UID context = getContext(mappedConstructor.getDeclaringClass(), subject, null);
        for (MappedPath path : mappedConstructor.getMappedArguments()) {
            constructorArguments.add(getValue(path, getPathValue(path, subject, properties, context), context));
        }
        return constructorArguments;
    }

    private UID getContext(Class<?> clazz, @Nullable ID subject, @Nullable UID defaultContext) {
        UID contextUID = configuration.getMappedClass(clazz).getContext();
        if (contextUID != null) {
            return contextUID;
        } else {
            return defaultContext;
        }
    }

    private UID getContext(Object instance, @Nullable ID subject, @Nullable UID defaultContext) {
        return getContext(instance.getClass(), subject, defaultContext);
    }

    @Override
    public Locale getCurrentLocale() {
        if (locales != null) {
            Iterator<Locale> liter = locales.iterator();
            if (liter.hasNext()) {
                return liter.next();
            }
        }
        return Locale.ROOT;
    }

    @Override
    public FlushMode getFlushMode() {
        return flushMode;
    }

    private <T> List<T> getFromCache(Class<T> clazz, List<T> instances, ID... subjects) {
        for (ID id : subjects){
            instances.add(id != null ? getCached(id, clazz) : null);
        }
        return instances;
    }

    @Nullable
    private NODE getFunctionalValue(ID subject, UID predicate, boolean includeInferred, @Nullable UID context) {
        List<STMT> statements = findStatements(subject, predicate, null, context, includeInferred);
        if (statements.size() > 1) {
            errorHandler.functionalValueError(subject, predicate, includeInferred, context);
            return statements.get(0).getObject();
        }
        if (statements.size() > 0) {
            return statements.get(0).getObject();
        } else {
            return null;
        }
    }

    @Nullable
    private ID getId(MappedClass mappedClass, Object instance) {
        MappedProperty<?> idProperty = mappedClass.getIdProperty();
        if (idProperty != null) {
            // Assigned id
            Object id = idProperty.getValue(toBeanMap(instance));
            if (id != null) {
                if (idProperty.getIDType() == IDType.LOCAL) {
                    LID lid;
                    if (id instanceof LID) {
                        lid = (LID) id;
                    } else {
                        lid = new LID(id.toString());
                    }
                    return identityService.getID(lid);
                } else {
                    ID rid = null;
                    if (id instanceof UID) {
                        rid = (UID) id;
                    } else if (idProperty.getIDType() == IDType.URI) {
                        rid = new UID(id.toString());
                    } else {
                        rid = (ID) id;
                    }
                    return rid;
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID getId(Object instance) {
        if (instance instanceof LID) {
            return identityService.getID((LID) instance);
        } else {
            MappedClass mappedClass = configuration.getMappedClass(getClass(assertMapped(instance)));
            if (instance.getClass().isEnum()) {
                return new UID(mappedClass.getUID().ns(), ((Enum) instance).name());
            } else {
                BeanMap beanMap = toBeanMap(instance);
                return getId(mappedClass, beanMap);
            }
        }
    }

    private Multimap<UID, STMT> getInvProperties(ID object, MappedClass mappedClass) {
        Multimap<UID, STMT> properties = MultimapFactory.<UID, STMT>create();
        if (logger.isDebugEnabled()){
            logger.debug("query for inverse properties of " +  object);
        }
        RDFQuery query = new RDFQueryImpl(connection);
        CloseableIterator<STMT> stmts = query.where(
                Blocks.SPOC,
                QNODE.p.in(mappedClass.getInvMappedPredicates()))
                .set(QNODE.o, object)
                .construct(Blocks.SPOC);
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                properties.put(stmt.getPredicate(), stmt);
            }
        }finally{
            stmts.close();
        }
        return properties;
    }

    private Map<ID, Multimap<UID, STMT>> getInvProperties(MappedClass mappedClass, Collection<ID> objects){
        RDFQuery query = new RDFQueryImpl(connection);
        query.where(
                Blocks.SPOC,
                QNODE.p.in(mappedClass.getInvMappedPredicates()));

        if (objects.size() == 1){
            query.set(QNODE.o, objects.iterator().next());
        }else{
            query.where(QNODE.o.in(objects));
        }

        CloseableIterator<STMT> stmts = query.construct(Blocks.SPOC);
        Map<ID, Multimap<UID, STMT>> invProperties = getPropertiesMap(stmts, true);

        for (ID id : objects){
            if (!invProperties.containsKey(id)){
                invProperties.put(id, MultimapFactory.<UID, STMT>create());
            }
        }

        return invProperties;
    }

    @Override
    public LID getLID(ID id) {
        return identityService.getLID(id);
    }

    @Nullable
    private <T> T getMappedObject(MappedClass mappedClass, ID subject, Class<T> requiredClass, PropertiesMap properties, boolean polymorphic, UID context, boolean bind) {
        T instance = null;
        if (polymorphic) {
            Collection<ID> mappedTypes = findMappedTypes(subject, context, properties.getDirect());
            if (!mappedTypes.isEmpty()) {
                instance = createInstance(subject, requiredClass, mappedTypes, properties);
            }else if (properties.getDirect().containsKey(RDF.rest)){
                Map<String,NODE> values = new HashMap<String,NODE>();
                values.put(RDF.rest.ln(), properties.getDirect().get(RDF.rest).iterator().next().getObject());
                if (properties.getDirect().containsKey(RDF.first)){
                    values.put(RDF.first.ln(), properties.getDirect().get(RDF.first).iterator().next().getObject());
                }
                listCache.put(subject, values);
            }else{
                logger.error("got no type for " + subject.getId());
            }

        } else {
            instance = createInstance(subject, requiredClass, Collections.<ID> emptyList(), properties);
        }
        if (instance != null){
            put(subject, instance);
            if (bind){
                bind(mappedClass, subject, instance, properties);
            }
        }
        return instance;
    }

    private Set<NODE> getPathValue(MappedPath path, ID subject, PropertiesMap properties, UID context) {
        if (configuration.allowRead(path)) {
            Set<NODE> values;
            MappedProperty<?> property = path.getMappedProperty();
            if (property.isMixin()) {
                values = Collections.<NODE> singleton(subject);
            } else if (path.size() > 0) {
                values = findPathValues(subject, path, 0, properties, context);
            } else {
                values = new LinkedHashSet<NODE>();
            }
            if (values.isEmpty()) {
                for (UID uri : property.getDefaults()) {
                    values.add(uri);
                }
            }
            return values;
        }
        return Collections.emptySet();
    }

    private Multimap<UID, STMT> getProperties(ID subject, MappedClass mappedClass, boolean polymorphic) {
        Multimap<UID, STMT> properties = MultimapFactory.<UID, STMT>create();
        if (mappedClass.getDynamicProperties().isEmpty()
                && !polymorphic
                && mappedClass.getMappedPredicates().size() < 5){
            if (logger.isDebugEnabled()){
                logger.debug("query for properties of " +  subject);
            }
            RDFQuery query = new RDFQueryImpl(connection);
            CloseableIterator<STMT> stmts = query.where(
                    Blocks.SPOC,
                    QNODE.p.in(mappedClass.getMappedPredicates()))
                    .set(QNODE.s, subject)
                    .construct(Blocks.SPOC);
            try{
                while (stmts.hasNext()){
                    STMT stmt = stmts.next();
                    properties.put(stmt.getPredicate(), stmt);
                }
            }finally{
                stmts.close();
            }

        }else{
            for (STMT stmt : findStatements(subject, null, null, null, true)){
                properties.put(stmt.getPredicate(), stmt);
            }
        }
        return properties;
    }

    private Map<ID, Multimap<UID, STMT>> getPropertiesMap(CloseableIterator<STMT> stmts, boolean inv) {
        Map<ID, Multimap<UID, STMT>> propertiesMap = new HashMap<ID, Multimap<UID, STMT>>();
        try{
            while (stmts.hasNext()){
                STMT stmt = stmts.next();
                ID key = inv ? stmt.getObject().asResource() : stmt.getSubject();
                Multimap<UID, STMT> properties = propertiesMap.get(key);
                if (properties == null){
                    properties = MultimapFactory.<UID, STMT>create();
                    propertiesMap.put(key, properties);
                }
                properties.put(stmt.getPredicate(), stmt);
            }
        }finally{
            stmts.close();
        }
        return propertiesMap;
    }

    @Override
    public RDFBeanTransaction getTransaction() {
        return transaction;
    }

    private Object getValue(MappedPath propertyPath, Set<? extends NODE> values, UID context)
            throws InstantiationException, IllegalAccessException {
        MappedProperty<?> mappedProperty = propertyPath.getMappedProperty();
        Object convertedValue;

        // Collection
        if (mappedProperty.isCollection()) {
            convertedValue = convertCollection(propertyPath, values, context);
        } 
        // Array
        else if (mappedProperty.isArray()) {
            convertedValue = convertArray(propertyPath, values, context);
        }        
        // Localized
        else if (mappedProperty.isLocalized()) {
            if (mappedProperty.isMap()) {
                convertedValue = convertLocalizedMap(propertyPath, values);
            } else if (mappedProperty.getType().equals(String.class)) {
                convertedValue = convertLocalized(propertyPath, values);
            } else {
                throw new SessionException("Illegal use of @Localized with " + mappedProperty.getType() + " at "
                        + propertyPath);
            }
        }
        // Map
        else if (mappedProperty.isMap()) {
            convertedValue = convertMap(propertyPath, values);
        }
        // Literal or *-to-one relation
        else if (values.size() <= 1 || propertyPath.isIgnoreInvalid()) {
            convertedValue = convertSingleValue(propertyPath, values);
        }
        // Unsupported type
        else {
            errorHandler.cardinalityError(propertyPath, values);
            convertedValue = convertSingleValue(propertyPath, values);
        }
        return convertedValue;
    }

    private boolean isContainer(ID node, UID context) {
        for (ID type : findTypes(node, context)) {
            if (CONTAINER_TYPES.contains(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPolymorphic(MappedClass mappedClass){
        return configuration.isPolymorphic(mappedClass.getJavaClass());
    }


    private boolean isPolymorphic(MappedProperty<?> mappedProperty) {
        if (mappedProperty.isCollection() || mappedProperty.isMap()){
            return configuration.isPolymorphic(mappedProperty.getComponentType());
        }else{
            return configuration.isPolymorphic(mappedProperty.getType());
        }
    }

    private <T> void loadAll(Class<T> clazz, Collection<ID> ids, Set<ID> handled) {
        MappedClass mappedClass = configuration.getMappedClass(clazz);
        boolean polymorphic = isPolymorphic(mappedClass);
        UID context = mappedClass.getContext();

        if (logger.isDebugEnabled()){
            logger.debug("query for " + clazz.getSimpleName() + " instance data");
        }
        RDFQuery query = createQuery(mappedClass, null, polymorphic);
        query.where(QNODE.s.in(ids));
        CloseableIterator<STMT> stmts = query.construct(Blocks.SPOC);
        Map<ID, Multimap<UID, STMT>> directProps = getPropertiesMap(stmts, false);

        if (directProps.isEmpty()){
            return;
        }

        Map<ID, Multimap<UID, STMT>> inverseProps = Collections.emptyMap();
        if (!polymorphic && !mappedClass.getInvMappedPredicates().isEmpty()){
            inverseProps = getInvProperties(mappedClass, directProps.keySet());
        }

        // create
        Map<ID, T> idToInstance = createInstances(mappedClass, clazz, polymorphic, context, directProps, inverseProps);

        // load references
        loadReferences(mappedClass, directProps, handled);

        for (Map.Entry<ID, Multimap<UID, STMT>> entry : directProps.entrySet()){
            T instance = getCached(entry.getKey(), clazz);
            if (idToInstance.containsKey(entry.getKey())){
                PropertiesMap properties = new PropertiesMap(entry.getValue(), inverseProps.get(entry.getKey()));
                MappedClass mc = resolveMappedClass(mappedClass, properties);
                bind(mc, entry.getKey(), instance, properties);
            }
        }
    }

    private void loadReferences(MappedClass mappedClass, Map<ID, Multimap<UID, STMT>> directProps, Set<ID> handled) {
        Map<UID, Class<?>> directToType = new HashMap<UID, Class<?>>();
        for (MappedPath mappedPath : mappedClass.getProperties()){
            if (mappedPath.isReference() && !mappedPath.getPredicatePath().isEmpty()){
                MappedProperty<?> property = mappedPath.getMappedProperty();
                if (!property.isList()){
                    Class<?> type = property.getType();
                    if (property.isCollection() || property.isMap()){
                        type = property.getComponentType();
                    }
                    if (!type.isEnum() && !mappedPath.isInverse(0)){
                        directToType.put(mappedPath.get(0).getUID(), type);
                    }
                }
            }
        }

        Map<Class<?>, Set<ID>> typeToIds = new HashMap<Class<?>, Set<ID>>();
        Set<ID> newHandled = new HashSet<ID>(handled);

        for (Multimap<UID, STMT> properties : directProps.values()){
            for (STMT stmt : properties.values()){
                if (stmt.getObject().isResource() && directToType.containsKey(stmt.getPredicate())){
                    if (!instanceCache.containsKey(stmt.getObject()) && !handled.contains(stmt.getObject())){
                        Class<?> cl = directToType.get(stmt.getPredicate());
                        Set<ID> ids = typeToIds.get(cl);
                        if (ids == null){
                            ids = new HashSet<ID>();
                            typeToIds.put(cl, ids);
                        }
                        newHandled.add(stmt.getObject().asResource());
                        ids.add(stmt.getObject().asResource());
                    }
                }
            }
        }

        // load
        for (Map.Entry<Class<?>, Set<ID>> entry : typeToIds.entrySet()){
            loadAll(entry.getKey(), entry.getValue(), newHandled);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> matchType(Collection<ID> types, Class<T> targetType) {
        if (types.isEmpty()) {
            return targetType;
        } else {
            Class<? extends T> result = targetType;
            boolean foundMatch = false;
            for (ID type : types) {
                if (type instanceof UID) {
                    UID uid = (UID) type;
                    List<MappedClass> classes = configuration.getMappedClasses(uid);
                    if (classes != null) {
                        for (MappedClass mappedClass : classes) {
                            Class<?> clazz = mappedClass.getJavaClass();
                            if ((result == null || result.isAssignableFrom(clazz)) && !clazz.isInterface()) {
                                foundMatch = true;
                                result = (Class<? extends T>) clazz;
                            }
                        }
                    }
                }
            }
            if (foundMatch) {
                return result;
            } else {
                return null;
            }
        }
    }

    private void put(ID resource, Object value) {
        instanceCache.get(resource).add(value);
        resourceCache.put(value, resource);
    }

    private void recordAddStatement(ID subject, UID predicate, NODE object, UID context) {
        STMT statement = new STMT(subject, predicate, object, context, true);
        if (!removedStatements.remove(statement)) {
            addedStatements.add(statement);
        }
    }

    private void recordRemoveStatement(STMT statement) {
        if (!addedStatements.remove(statement)) {
            removedStatements.add(statement);
        }
    }

    private void removeContainer(ID node, UID context) {
        if (isContainer(node, context)) {
            for (STMT stmt : findStatements(node, null, null, context, false)) {
                recordRemoveStatement(stmt);
            }
        }
    }

    private void removeList(ID node, UID context) {
        if (findStatements(node, RDF.type, RDF.List, context, true).size() > 0) {
            removeListInternal(node, context);
        }
    }

    private void removeListInternal(ID node, UID context) {
        for (STMT statement : findStatements(node, null, null, context, false)) {
            recordRemoveStatement(statement);
            NODE object = statement.getObject();
            // Remove rdf:rest
            if (RDF.rest.equals(statement.getPredicate()) && object.isResource()) {
                removeListInternal((ID) object, context);
            }
        }
    }

    @Override
    public LID save(Object instance) {
        boolean flush = false;
        if (seen == null) {
            seen = new HashSet<Object>();
            flush = true;
        }
        assertMapped(instance);
        assertHasIdProperty(instance);
        ID subject = toRDF(instance, null);
        if (flush) {
            seen = null;
            if (flushMode == FlushMode.ALWAYS) {
                flush();
            }
        }
        return getLID(subject);
    }

    @Override
    public List<LID> saveAll(Object... instances) {
        List<LID> ids = new ArrayList<LID>(instances.length);
        seen = new HashSet<Object>(instances.length * 3);
        for (Object instance : instances) {
            ids.add(save(assertMapped(instance)));
        }
        seen = null;
        if (flushMode == FlushMode.ALWAYS) {
            flush();
        }
        return ids;
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        this.flushMode = flushMode;
    }

    private <T> void setId(MappedClass mappedClass, ID subject, BeanMap instance) {
        MappedProperty<?> idProperty = mappedClass.getIdProperty();
        if (idProperty != null && !mappedClass.isEnum() && !idProperty.isVirtual()) {
            Object id = null;
            Identifier identifier;
            Class<?> type = idProperty.getType();
            IDType idType = idProperty.getIDType();
            if (idType == IDType.LOCAL) {
                identifier = getLID(subject);
            } else if (idType == IDType.URI) {
                if (subject.isURI()) {
                    identifier = subject;
                } else {
                    identifier = null;
                }
            } else {
                identifier = subject;
            }
            if (identifier != null) {
                if (String.class.isAssignableFrom(type)) {
                    id = identifier.getId();
                } else if (Identifier.class.isAssignableFrom(type)) {
                    id = identifier;
                } else {
                    throw new BindException("Cannot assign id of " + mappedClass + " into " + type);
                }
            }
            idProperty.setValue(instance, id);
        }
        
        for (MappedProperty<?> mixinProperty : mappedClass.getMixinProperties()) {
            Object mixinValue = mixinProperty.getValue(instance);
            if (mixinValue != null) {
                MappedClass mixinClass = configuration.getMappedClass(mixinProperty.getTargetType());
//                if (mixinClass == null) {
//                    throw new IllegalStateException("Got no mapped class for " + mixinProperty.getTargetType().getName());
//                }
                setId(mixinClass, subject, new BeanMap(mixinValue));
            }
        }
    }

    private BeanMap toBeanMap(Object instance) {
        return instance instanceof BeanMap ? (BeanMap) instance : new BeanMap(instance);
    }

    @SuppressWarnings("unchecked")
    private void toRDF(Object instance, ID subject, UID context, MappedClass mappedClass, boolean update) {
        UID uri = mappedClass.getUID();
        if (!update && uri != null) {
            recordAddStatement(subject, RDF.type, uri, context);
        }
        BeanMap beanMap = toBeanMap(instance);

        Multimap<UID, STMT> statements = getProperties(subject, mappedClass, true);

        for (MappedPath path : mappedClass.getProperties()) {
            MappedProperty<?> property = path.getMappedProperty();
            if (path.isSimpleProperty()) {
                MappedPredicate mappedPredicate = path.get(0);
                UID predicate = mappedPredicate.getUID();
                if (mappedPredicate.getContext() != null){
                    context = mappedPredicate.getContext();
                }

                if (update) {
                    if (statements.containsKey(predicate)) {
                        for (STMT statement : statements.get(predicate)) {
                            if (property.isLocalized() && String.class.equals(property.getType())) {
                                LIT lit = (LIT) statement.getObject();
                                if (ObjectUtils.equals(getCurrentLocale(), lit.getLang())) {
                                    recordRemoveStatement(statement);
                                }
                            } else {
                                recordRemoveStatement(statement);
                                NODE object = statement.getObject();
                                if (object.isResource()) {
                                    if (property.isList()) {
                                        removeList((ID) object, context);
                                    } else if (property.isContainer()) {
                                        removeContainer((ID) object, context);
                                    }
                                }
                            }
                        }
                    }
                }

                Object object = property.getValue(beanMap);
                if (object != null) {
                    if (property.isArray()) {
                        if (object.getClass().getComponentType().isPrimitive()) {
                            int size = Array.getLength(object);
                            List<Object> list = new ArrayList<Object>(size);
                            for (int i = 0; i < size; i++) {
                                list.add(Array.get(object, i));
                            }
                            object = list;
                        } else {
                            object = Arrays.asList((Object[])object);    
                        }                        
                    }
                    
                    if (property.isList()) {
                        ID first = toRDFList((List<?>) object, context);
                        if (first != null) {
                            recordAddStatement(subject, predicate, first, context);
                        }
                                                
                    } else if (property.isContainer()) {
                        ID container = toRDFContainer((Collection<?>) object, context, property.getContainerType());
                        if (container != null) {
                            recordAddStatement(subject, predicate, container, context);
                        }
                        
                    } else if (property.isCollection()) {
                        for (Object o : (Collection<?>) object) {
                            NODE value = toRDFValue(o, context);
                            if (value != null) {
                                recordAddStatement(subject, predicate, value, context);
                            }
                        }
                        
                    } else if (property.isArray()) { // array, but not List or Container   
                        for (Object o : (Collection<?>) object) {
                            NODE value = toRDFValue(o, context);
                            if (value != null) {
                                recordAddStatement(subject, predicate, value, context);
                            }
                        }                        
                        
                    } else if (property.isLocalized()) {
                        if (property.isMap()) {
                            for (Map.Entry<Locale, String> entry : ((Map<Locale, String>) object).entrySet()) {
                                if (entry.getValue() != null) {
                                    LIT literal = new LIT(entry.getValue(), entry.getKey());
                                    recordAddStatement(subject, predicate, literal, context);
                                }
                            }
                        } else {
                            LIT literal = new LIT(object.toString(), getCurrentLocale());
                            recordAddStatement(subject, predicate, literal, context);
                        }
                    } else if (!property.isMap()) {
                        NODE value = toRDFValue(object, context);
                        if (value != null) {
                            recordAddStatement(subject, predicate, value, context);
                        }
                    }
                }

            } else if (property.isMixin()) {
                Object object = property.getValue(beanMap);
                if (object != null) {
                    UID subContext = getContext(object, subject, context);
                    toRDF(object, subject, subContext, configuration.getMappedClass(getClass(object)), update);
                }
            }
        }

        for (MappedProperty<?> property : mappedClass.getDynamicProperties()){
            Map<?,?> properties = (Map) property.getValue(beanMap);
            if (properties != null){
                for (Map.Entry<?,?> entry : properties.entrySet()){
                    UID predicate = toRDF(entry.getKey(), context).asURI();
                    if (entry.getValue() instanceof Collection){
                        for (Object value : ((Collection)entry.getValue())){
                            NODE object = toRDFValue(value, context);
                            recordAddStatement(subject, predicate, object, context);
                        }
                    }else{
                        NODE object = toRDFValue(entry.getValue(), context);
                        recordAddStatement(subject, predicate, object, context);
                    }
                }
            }
        }
    }

    private ID toRDF(Object instance, @Nullable UID parentContext) {
        if (instance instanceof ID){
            return (ID)instance;
        }
        BeanMap beanMap = toBeanMap(Assert.notNull(instance,"instance"));
        Class<?> clazz = getClass(instance);
        MappedClass mappedClass = configuration.getMappedClass(clazz);
        ID subject = resourceCache.get(instance);
        if (subject == null) {
            subject = getId(mappedClass, beanMap);
        }
        if (mappedClass.isEnum()) {
            subject = new UID(mappedClass.getClassNs(), ((Enum<?>) instance).name());
            put(subject, instance);
        } else if (seen.add(instance)) {
            UID context = getContext(clazz, subject, parentContext);
            // Update
            boolean update = subject != null && exists(subject, mappedClass, context);

            // Create
            if (subject == null) {
                subject = assignId(mappedClass, beanMap);
                context = getContext(clazz, subject, parentContext);
            }
            put(subject, instance);

            // Build-in namespaces are read-only
            if (subject.isURI() && configuration.isRestricted((UID) subject)) {
                return subject;
            }

            toRDF(beanMap, subject, context, mappedClass, update);
        }
        return subject;
    }

    private ID toRDFContainer(Collection<?> collection, UID context, ContainerType containerType) {
        int i = 0;
        ID container = connection.createBNode();
        recordAddStatement(container, RDF.type, containerType.getUID(), context);
        for (Object o : collection) {
            i++;
            if (o != null) {
                NODE value = toRDFValue(o, context);
                recordAddStatement(container, RDF.getContainerMembershipProperty(i), value, context);
            }
        }
        return container;
    }

    private ID toRDFList(List<?> list, UID context) {
        ID firstNode = null;
        ID currentNode = null;
        for (Object value : list) {
            if (currentNode == null) {
                currentNode = connection.createBNode();
                firstNode = currentNode;
            } else {
                BID nextNode = connection.createBNode();
                recordAddStatement(currentNode, RDF.rest, nextNode, context);
                currentNode = nextNode;
            }
            recordAddStatement(currentNode, RDF.type, RDF.List, context);
            recordAddStatement(currentNode, RDF.first, toRDFValue(value, context), context);
        }
        if (currentNode != null) {
            recordAddStatement(currentNode, RDF.rest, RDF.nil, context);
        }
        return firstNode;
    }

    private LIT toRDFLiteral(Object o) {
        if (o instanceof LIT){
            return (LIT)o;
        }
        UID dataType = configuration.getConverterRegistry().getDatatype(o.getClass());
        return new LIT(configuration.getConverterRegistry().toString(o), dataType);
    }

    private NODE toRDFValue(Object o, @Nullable UID context) {
        if (o instanceof NODE){
            return (NODE)o;
        }
        Class<?> type = getClass(o);
        if (configuration.isMapped(type)){
            return toRDF(o, context);
        } else if (o instanceof UID) {
            return (UID) o;
        } else {
            return toRDFLiteral(o);
        }
    }

}
