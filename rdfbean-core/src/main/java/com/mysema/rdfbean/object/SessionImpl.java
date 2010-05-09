/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.IOException;
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

import org.apache.commons.collections15.BeanMap;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.IteratorAdapter;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.ContainerType;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.FetchOptimizer;
import com.mysema.rdfbean.model.FetchStrategy;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.Identifier;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.xsd.ConverterRegistry;

/**
 * Default implementation of the Session interface
 * 
 * @author sasa
 * @author tiwe
 * 
 */
public final class SessionImpl implements Session {

    private static final Factory<List<Object>> listFactory = new Factory<List<Object>>() {
        @Override
        public List<Object> create() {
            return new ArrayList<Object>();
        }
    };

    public static final Set<UID> CONTAINER_TYPES = Collections.unmodifiableSet(new HashSet<UID>(Arrays.<UID> asList(
            RDF.Alt, RDF.Seq, RDF.Bag, RDFS.Container)));

    static final int DEFAULT_INITIAL_CAPACITY = 1024;

    private static final Logger logger = LoggerFactory.getLogger(SessionImpl.class);

    private Set<STMT> addedStatements;

    private final Configuration conf;

    private RDFConnection connection;

    private DefaultErrorHandler errorHandler = new DefaultErrorHandler();

    private FlushMode flushMode = FlushMode.ALWAYS;

    private final IdentityService identityService;

    private Map<ID, List<Object>> instanceCache;

    private Iterable<Locale> locales;

    private Map<String, ObjectRepository> parentRepositories = new HashMap<String, ObjectRepository>();

    private Set<STMT> removedStatements;

    private Map<Object, ID> resourceCache;

    @Nullable
    private Set<Object> seen;

    @Nullable
    private RDFBeanTransaction transaction;

    public SessionImpl(Configuration conf, RDFConnection connection, Iterable<Locale> locales) {
        this.conf = Assert.notNull(conf,"conf");
        this.connection = Assert.notNull(connection,"connection");
        this.locales = locales;

        this.identityService = new SessionIdentityService(connection);

        // TODO : do this in SessionFactory ?!?
        List<FetchStrategy> fetchStrategies = conf.getFetchStrategies();
        if (fetchStrategies != null) {
            if (this.connection instanceof FetchOptimizer) {
                ((FetchOptimizer) this.connection).addFetchStrategies(fetchStrategies);
            } else {
                this.connection = new FetchOptimizer(this.connection, fetchStrategies);
            }
        }

        clear();
    }

    public SessionImpl(Configuration configuration, RDFConnection connection, @Nullable Locale locale) {
        this(configuration, connection, locale != null ? Arrays.asList(locale) : Collections.<Locale> emptySet());
    }

    @Override
    public void addParent(String ns, ObjectRepository parent) {
        Assert.hasText(ns,"ns");
        Assert.notNull(parent,"parent");
        parentRepositories.put(ns, parent);
    }

    private ID assignId(MappedClass mappedClass, BeanMap instance) {
        ID subject = createResource(instance);
        setId(mappedClass, subject, instance);
        return subject;
    }

    @Override
    public void autowire(Object instance) {
        Assert.notNull(instance,"instance");
        BeanMap beanMap = toBeanMap(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(getClass(instance));
        bind(getId(mappedClass, beanMap), beanMap);
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

    @SuppressWarnings("unchecked")
    private void bindDynamicProperties(ID subject, BeanMap beanMap, MappedClass mappedClass) {        
        List<STMT> statements = findStatements(subject, null, null, null, false);        
        for (MappedProperty<?> property : mappedClass.getDynamicProperties()) {
            Map<UID, Object> values = new HashMap<UID, Object>();

            for (STMT stmt : statements) {
                if (property.isIncludeMapped() || !mappedClass.isMappedPredicate(stmt.getPredicate())) {
                    
                    Class<?> componentType;
                    
                    if (property.isDynamicCollection()) {
                        componentType = property.getDynamicCollectionComponentType();
                    }
                    else {
                        componentType = property.getComponentType();
                    }
                    
                    Object value = convertValue(stmt.getObject(), componentType);
                    
                    if (value != null) {
                        if (property.isDynamicCollection()) {

                            Collection<Object> collection = (Collection<Object>) values.get(stmt.getPredicate());
                            if (collection == null) {
                                try {
                                    collection = (Collection<Object>) property.getDynamicCollectionType().newInstance();
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

    @Nullable
    private Object convertValue(NODE node, @Nullable Class<?> targetClass) {
        if (targetClass != null) {
            UID targetType = conf.getConverterRegistry().getDatatype(targetClass);
    
            if (node.getClass().equals(targetClass)) {
                return node;
            } else if (targetType != null && node.isLiteral()) {
                if (((LIT) node).getDatatype().equals(targetType)) {
    
                    return conf.getConverterRegistry().fromString(node.getValue(), targetClass);
                }
            } else if (targetType == null && node.isURI()) {
                return get(targetClass, node.asURI());
            }
        }
        return null;
    }

    @Nullable
    protected <T> T bind(ID subject, @Nullable T instance) {
        if (instance != null) {
            if (instance instanceof LifeCycleAware) {
                ((LifeCycleAware) instance).beforeBinding();
            }
            // TODO: defaultContext parameter?
            UID context = getContext(instance, subject, null);
            BeanMap beanMap = toBeanMap(instance);
            MappedClass mappedClass = MappedClass.getMappedClass(getClass(instance));
            setId(mappedClass, subject, beanMap);
            // loadStack.add(instance);

            bindDynamicProperties(subject, beanMap, mappedClass);

            for (MappedPath path : mappedClass.getProperties()) {
                if (!path.isConstructorParameter()) {
                    MappedProperty<?> property = path.getMappedProperty();
                    if (!property.isVirtual()) {
                        Object convertedValue;
                        try {
                            convertedValue = getValue(path, getPathValue(path, subject, context), context);
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
        }
        return instance;
    }

    @Override
    public void clear() {
        instanceCache = LazyMap.decorate(new HashMap<ID, List<Object>>(DEFAULT_INITIAL_CAPACITY), listFactory);
        resourceCache = new IdentityHashMap<Object, ID>(DEFAULT_INITIAL_CAPACITY);
        addedStatements = new LinkedHashSet<STMT>(DEFAULT_INITIAL_CAPACITY);
        removedStatements = new LinkedHashSet<STMT>(DEFAULT_INITIAL_CAPACITY);
        seen = null;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Nullable
    private Class<?> convertClassReference(UID uid, Class<?> targetClass) {
        List<Class<?>> mappedClasses = conf.getMappedClasses(uid);
        boolean foundMatch = false;
        for (Class<?> mappedClass : mappedClasses) {
            if (targetClass.isAssignableFrom(mappedClass)) {
                targetClass = mappedClass;
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
        Object convertedValue;
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
            collection.add(convertValue(value, targetType, propertyPath));
        }
        convertedValue = collection;
        return convertedValue;
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

    private Collection<NODE> convertList(ID subject, UID context) {
        List<NODE> list = new ArrayList<NODE>();
        while (subject != null && !subject.equals(RDF.nil)) {
            list.add(getFunctionalValue(subject, RDF.first, false, context));
            subject = (ID) getFunctionalValue(subject, RDF.rest, false, context);
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
                    getFunctionalValue((ID) value, propertyDefinition.getKeyPredicate(), false, null), keyType,
                    propertyPath);
            // Map Value
            Object mapValue;
            UID valuePredicate = propertyDefinition.getValuePredicate();
            if (valuePredicate != null) {
                mapValue = convertValue(getFunctionalValue((ID) value, valuePredicate, false, null), componentType,
                        propertyPath);
            } else {
                mapValue = convertValue(value, componentType, propertyPath);
            }
            map.put(key, mapValue);
        }
        convertedValue = map;
        return convertedValue;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> T convertMappedObject(ID subject, Class<?> requiredClass, boolean polymorphic, boolean injection) {
        // XXX defaultContext?
        UID context = getContext(requiredClass, subject, null);
        Object instance = get(subject, requiredClass);
        if (instance == null) {
            if (injection) {
                if (subject instanceof UID && requiredClass != null) {
                    UID uri = (UID) subject;
                    ObjectRepository orepo = parentRepositories.get(uri.ns());
                    if (orepo != null) {
                        return (T) orepo.getBean(requiredClass, uri);
                    } else {
                        throw new IllegalArgumentException("No such parent repository: " + uri.ns());
                    }
                }
            }
            if (polymorphic) {
                Collection mappedTypes = findMappedTypes(subject, context);
                if (!mappedTypes.isEmpty()) {
                    instance = createInstance(subject, requiredClass, mappedTypes);
                }

            } else {
                instance = createInstance(subject, requiredClass, Collections.<ID> emptyList());
            }
            put(subject, instance);
            bind(subject, instance);
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private Object convertSingleValue(MappedPath propertyPath, Set<? extends NODE> values) {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Class targetType = propertyDefinition.getType();
        NODE value = values.isEmpty() ? null : values.iterator().next();
        return convertValue(value, targetType, propertyPath);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Object convertValue(@Nullable NODE value, Class<?> targetClass, MappedPath propertyPath) {
	if (value == null){
	    return null;
	}	
        Object convertedValue;
        MappedProperty mappedProperty = propertyPath.getMappedProperty();
        try {
            // "Wildcard" type
            if (MappedPath.isWildcard(targetClass) && value.isResource()) {
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
            else if (MappedPath.isMappedClass(targetClass) || mappedProperty.isInjection()) {
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
                convertedValue = conf.getConverterRegistry().fromString(value.getValue(), targetClass);
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

    private Object convertIDReference(NODE value, MappedPath propertyPath) {
        if (value instanceof ID) {
            return value;
        } else {
            throw new BindException(propertyPath, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertMappedClass(NODE value, Class<?> targetClass, MappedPath propertyPath,
            MappedProperty mappedProperty) {
        if (value instanceof ID) {
            return convertMappedObject((ID) value, targetClass, mappedProperty.isPolymorphic(), mappedProperty
                    .isInjection());
        } else {
            throw new BindException(propertyPath, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertClassReference(NODE value, MappedPath propertyPath, MappedProperty mappedProperty) {
        if (value instanceof UID) {
            return convertClassReference((UID) value, mappedProperty.getComponentType());
        } else {
            throw new BindException(propertyPath, "bnode or literal", value);
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

    @Nullable
    protected <T> T createInstance(ID subject, Class<T> requiredType, Collection<ID> mappedTypes) {
        T instance;
        Class<? extends T> actualType = matchType(mappedTypes, requiredType);
        if (actualType != null) {
            if (!conf.allowCreate(actualType)) {
                instance = null;
            } else {
                try {
                    MappedClass mappedClass = MappedClass.getMappedClass(actualType);
                    MappedConstructor mappedConstructor = mappedClass.getConstructor();
                    if (mappedConstructor == null) {
                        instance = actualType.newInstance();
                    } else {
                        List<Object> constructorArguments = getConstructorArguments(mappedClass, subject,
                                mappedConstructor);
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

    @Override
    public <D, Q> Q createQuery(QueryLanguage<D, Q> queryLanguage, D definition) {
        return connection.createQuery(this, queryLanguage, definition);
    }

    @Override
    public <Q> Q createQuery(QueryLanguage<Void, Q> queryLanguage) {
        return connection.createQuery(this, queryLanguage, null);
    }

    protected ID createResource(BeanMap instance) {
        ID id = conf.createURI(instance.getBean());
        if (id == null) {
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
        MappedClass mappedClass = MappedClass.getMappedClass(clazz);
        UID context = getContext(instance, subject, null);
        if (subject == null) {
            subject = getId(mappedClass, beanMap);
        }
        if (subject != null) {
            // Delete own properties
            for (STMT statement : findStatements(subject, null, null, context, false)) {
                recordRemoveStatement(statement);
                NODE object = statement.getObject();
                if (object.isResource()) {
                    removeList((ID) object, context);
                    removeContainer((ID) object, context);
                }
            }
            // Delete references
            for (STMT statement : findStatements(null, null, subject, context, false)) {
                recordRemoveStatement(statement);
            }
            // Remove from primary cache
            List<Object> instances = instanceCache.remove(subject);
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
            CloseableIterator<STMT> stmts = connection.findStatements(subject, RDF.type, type, context, true);
            try {
                if (stmts.hasNext()) {
                    return true;
                }
            } finally {
                close(stmts);
            }
        }
        return false;
    }

    private Set<NODE> filterObjects(CloseableIterator<STMT> statements) {
        Set<NODE> objects = new LinkedHashSet<NODE>();
        try {
            while (statements.hasNext()) {
                STMT statement = statements.next();
                objects.add(statement.getObject());
            }
        } finally {
            close(statements);
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    private <T extends NODE> Set<T> filterSubject(CloseableIterator<STMT> statements) {
        Set<T> subjects = new LinkedHashSet<T>();
        try {
            while (statements.hasNext()) {
                STMT statement = statements.next();
                subjects.add((T) statement.getSubject());
            }
        } finally {
            close(statements);
        }
        return subjects;
    }

    private void close(CloseableIterator<STMT> statements) {
        try {
            statements.close();
        } catch (IOException e) {
            throw new SessionException(e);
        }
    }

    @Override
    public <T> List<T> findInstances(Class<T> clazz) {
        UID type = MappedClass.getMappedClass(clazz).getUID();
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

    private <T> void findInstances(Class<T> clazz, UID uri, final Set<T> instances) {
        UID context = getContext(clazz, null, null);
        // try {
        Set<ID> resources = new LinkedHashSet<ID>();
        resources.addAll(this.<ID> filterSubject(connection.findStatements(null, RDF.type, uri, context, true)));
        for (ID subject : resources) {
            T instance = getBean(clazz, subject);
            if (instance != null) {
                instances.add(instance);
            }
        }
        // } catch (Exception e) {
        // throw new SessionException(e);
        // }
    }

    private List<ID> findMappedTypes(ID subject, UID context) {
        List<ID> types = new ArrayList<ID>();
        List<STMT> statements = findStatements(subject, RDF.type, null, context, true);
        for (STMT stmt : statements) {
            NODE type = stmt.getObject();
            if (type instanceof UID && conf.getMappedClasses((UID) type) != null) {
                types.add((UID) type);
            }
        }
        return types;
    }

    private Set<NODE> findPathValues(ID resource, MappedPath path, int index, UID context) {
        MappedPredicate predicate = path.get(index);
        if (predicate.context() != null) {
            context = new UID(predicate.context());
        }
        Set<NODE> values = findValues(resource, predicate.getUID(), predicate.inv(), predicate.includeInferred(),
                context);

        if (path.size() > index + 1) {
            Set<NODE> nestedValues = new LinkedHashSet<NODE>();
            for (NODE value : values) {
                if (value.isResource()) {
                    nestedValues.addAll(findPathValues((ID) value, path, index + 1, context));
                }
            }
            return nestedValues;
        }
        return values;
    }

    protected List<STMT> findStatements(@Nullable ID subject, @Nullable UID predicate, @Nullable NODE object,
            @Nullable UID context, boolean includeInferred) {
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
            return (Set<NODE>) filterSubject(connection.findStatements(null, predicate, resource, context,
                    includeInferred));
        } else {
            return filterObjects(connection.findStatements(resource, predicate, null, context, includeInferred));
        }
    }

    public void flush() {
        connection.update(removedStatements, addedStatements);
        removedStatements = new LinkedHashSet<STMT>();
        addedStatements = new LinkedHashSet<STMT>();
    }

    @Override
    public BeanQuery from(PEntity<?>... expr) {
        return connection.createQuery(this, QueryLanguage.QUERYDSL, null).from(expr);
    }

    @Override
    public <T> T get(Class<T> clazz, ID subject) {
        Assert.notNull(subject, "subject");
        return getBean(clazz, subject);
    }

    @Override
    public <T> T get(Class<T> clazz, LID subject) {
        ID id = identityService.getID(subject);
        return id != null ? get(clazz, id) : null;
    }

    @Nullable
    private Object get(ID resource, Class<?> clazz) {
        for (Object instance : instanceCache.get(resource)) {
            if (clazz == null || clazz.isInstance(instance)) {
                return instance;
            }
        }
        return null;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz, ID... subjects) {
        List<T> instances = new ArrayList<T>(subjects.length);
        for (ID subject : subjects) {
            instances.add(get(clazz, subject));
        }
        return instances;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz, LID... subjects) {
        List<T> instances = new ArrayList<T>(subjects.length);
        for (LID subject : subjects) {
            instances.add(get(clazz, subject));
        }
        return instances;
    }

    private <T> T getBean(Class<T> clazz, ID subject) {
        boolean polymorphic = true;
        MappedClass mappedClass = MappedClass.getMappedClass(clazz);
        polymorphic = mappedClass.isPolymorphic();
        return this.<T> convertMappedObject(subject, clazz, polymorphic, false);
        
    }

    @Override
    public <T> T getBean(Class<T> clazz, UID subject) {
        return (T) get(clazz, subject);
    }

    public <T> T getByExample(T entity) {
        return new ExampleQuery<T>(this, entity).uniqueResult();
    }

    @Override
    public <T> T getById(String id, Class<T> clazz) {
        return get(clazz, new LID(id));
    }

    protected Class<?> getClass(Object object) {
        return object instanceof BeanMap ? ((BeanMap) object).getBean().getClass() : object.getClass();
    }

    @Override
    public Configuration getConfiguration() {
        return conf;
    }

    public RDFConnection getConnection() {
        return connection;
    }

    private List<Object> getConstructorArguments(MappedClass mappedClass, ID subject,
            MappedConstructor mappedConstructor) throws InstantiationException, IllegalAccessException {
        List<Object> constructorArguments = new ArrayList<Object>(mappedConstructor.getArgumentCount());
        // TODO parentContext?
        UID context = getContext(mappedConstructor.getDeclaringClass(), subject, null);
        for (MappedPath path : mappedConstructor.getMappedArguments()) {
            constructorArguments.add(getValue(path, getPathValue(path, subject, context), context));
        }
        return constructorArguments;
    }

    public UID getContext(Class<?> clazz, @Nullable ID subject, @Nullable UID defaultContext) {
        UID contextUID = conf.getContext(clazz, subject);
        if (contextUID != null) {
            return contextUID;
        } else {
            return defaultContext;
        }
    }

    public UID getContext(Object instance, @Nullable ID subject, @Nullable UID defaultContext) {
        return getContext(instance.getClass(), subject, defaultContext);
    }

    public ConverterRegistry getConverterRegistry() {
        return conf.getConverterRegistry();
    }

    public Locale getCurrentLocale() {
        if (locales != null) {
            Iterator<Locale> liter = locales.iterator();
            if (liter.hasNext()) {
                return liter.next();
            }
        }
        return Locale.ROOT;
    }

    public FlushMode getFlushMode() {
        return flushMode;
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

    @SuppressWarnings("unchecked")
    public ID getId(Object instance) {
        if (instance instanceof LID) {
            return identityService.getID((LID) instance);
        } else {
            MappedClass mappedClass = MappedClass.getMappedClass(getClass(assertMapped(instance)));
            if (instance.getClass().isEnum()) {
                return new UID(mappedClass.getUID().ns(), ((Enum) instance).name());
            } else {
                BeanMap beanMap = toBeanMap(instance);
                return getId(mappedClass, beanMap);
            }
        }
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public LID getLID(ID id) {
        return identityService.getLID(id);
    }

    private Set<NODE> getPathValue(MappedPath path, ID subject, UID context) {
        if (conf.allowRead(path)) {
            Set<NODE> values;
            MappedProperty<?> property = path.getMappedProperty();
            if (property.isMixin()) {
                values = Collections.<NODE> singleton(subject);
            } else if (path.size() > 0) {
                values = findPathValues(subject, path, 0, context);
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

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> Class<? extends T> matchType(Collection<ID> types, Class<T> targetType) {
        if (types.isEmpty()) {
            return targetType;
        } else {
            Class<? extends T> result = targetType;
            boolean foundMatch = false;
            for (ID type : types) {
                if (type instanceof UID) {
                    UID uid = (UID) type;
                    List<Class<?>> classes = conf.getMappedClasses(uid);
                    if (classes != null) {
                        for (Class<?> clazz : classes) {
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

    private void put(ID resource, @Nullable Object value) {
        if (resource != null) {
            instanceCache.get(resource).add(value);
            resourceCache.put(value, resource);
        }
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

    protected void removeStatements(ID subject, UID predicate, NODE object, UID context) {
        for (STMT statement : findStatements(subject, predicate, object, context, false)) {
            recordRemoveStatement(statement);
        }
    }

    private <T> T assertMapped(T instance) {
        if (instance.getClass().getAnnotation(ClassMapping.class) == null) {
            throw new IllegalArgumentException(instance.getClass().getName() + " is not mapped");
        }
        return instance;
    }
    
    private <T> T assertHasIdProperty(T instance){
        MappedClass mappedClass = MappedClass.getMappedClass(instance.getClass());
        if (mappedClass.getIdProperty() == null){
            throw new IllegalArgumentException(instance.getClass().getName() + " as no id property");
        }
        return instance;
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
    }

    protected BeanMap toBeanMap(Object instance) {
        return instance instanceof BeanMap ? (BeanMap) instance : new BeanMap(instance);
    }

    @SuppressWarnings("unchecked")
    private void toRDF(Object instance, ID subject, UID context, MappedClass mappedClass, boolean update) {
        UID uri = mappedClass.getUID();
        if (!update && uri != null) {
            recordAddStatement(subject, RDF.type, uri, context);
        }
        BeanMap beanMap = toBeanMap(instance);
        MappedProperty<?> property;
        for (MappedPath path : mappedClass.getProperties()) {
            property = path.getMappedProperty();
            if (path.isSimpleProperty()) {
                MappedPredicate mappedPredicate = path.get(0);
                UID predicate = mappedPredicate.getUID();

                if (update) {
                    for (STMT statement : findStatements(subject, predicate, null, context, false)) {
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

                Object object = property.getValue(beanMap);
                if (object != null) {
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
                    toRDF(object, subject, subContext, MappedClass.getMappedClass(getClass(object)), update);
                }
            }
        }
    }

    private ID toRDF(Object instance, @Nullable UID parentContext) {
        BeanMap beanMap = toBeanMap(Assert.notNull(instance,"instance"));
        Class<?> clazz = getClass(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(clazz);
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
            if (subject.isURI() && conf.isRestricted((UID) subject)) {
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
            NODE value = toRDFValue(o, context);
            if (value != null) {
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
        UID dataType = conf.getConverterRegistry().getDatatype(o.getClass());
        return new LIT(conf.getConverterRegistry().toString(o), dataType);
    }

    @Nullable
    private NODE toRDFValue(@Nullable Object object, @Nullable UID context) {
        if (object == null) {
            return null;
        } else {
            Class<?> type = getClass(object);
            if (type.isAnnotationPresent(ClassMapping.class)) {
                return toRDF(object, context);
            } else if (object instanceof UID) {
                return (UID) object;
            } else {
                return toRDFLiteral(object);
            }
        }
    }

}
