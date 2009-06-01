/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

import org.apache.commons.collections15.BeanMap;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.types.path.PEntity;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.*;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.object.identity.MemoryIdentityService;

/**
 * @author sasa
 * 
 */
public class SessionImpl implements Session {
  
    private static final Logger logger = LoggerFactory.getLogger(SessionImpl.class);
    
    private Configuration conf;

    private IdentityService identityService;
    
    private FlushMode flushMode = FlushMode.ALWAYS;

    private boolean initialized = false;
    
    Map<ID, List<Object>> instanceCache;

    private Iterable<Locale> locales;
    
    private BID model;
    
    private Map<String,ObjectRepository> parentRepositories = new HashMap<String, ObjectRepository>();
    
    private Map<Object, ID> resourceCache = new IdentityHashMap<Object, ID>();
    
    private Set<STMT> addedStatements;
    
    private Set<STMT> removedStatements;
    
    private Set<Object> seen;
    
    private RDFBeanTransaction transaction;
    
    private RDFConnection connection;
    
    public SessionImpl(Configuration conf, RDFConnection connection, Iterable<Locale> locales) {
        this.conf = Assert.notNull(conf);
        this.connection = Assert.notNull(connection);
        this.locales = locales;
        
        this.identityService = conf.getIdentityService();
    }

    public SessionImpl(Configuration configuration, RDFConnection connection, Locale locale) {
        this(configuration, connection, locale != null ? Arrays.asList(locale) : null);
    }

    @Override
    public LID save(Object instance) {
        initialize();
        boolean flush = false;
        if (seen == null) {
            seen = new HashSet<Object>();
            flush = true;
        }
    	ID subject = toRDF(instance, null);
    	if (flush) {
    	    seen = null;
    	    if (flushMode == FlushMode.ALWAYS) {
    	        flush();
    	    }
    	}
    	return toLID(subject);
    }
    
    @Override
    public List<LID> saveAll(Object... instances) {
        List<LID> ids = new ArrayList<LID>(instances.length);
        seen = new HashSet<Object>(instances.length*3);
        for (Object instance : instances) {
            ids.add(save(instance));
        }
        seen = null;
        if (flushMode == FlushMode.ALWAYS) {
            flush();
        }
        return ids;
    }

    private void addCheckNumber(BID model, BID bid) {
        String lid = identityService.getLID(model, bid).getId();
        recordAddStatement(bid, CORE.localId, new LIT(lid), null);
    }

    @Override
    public void addParent(String ns, ObjectRepository parent) {
        Assert.hasText(ns);
        Assert.notNull(parent);
        parentRepositories.put(ns, parent);
    }

    private ID assignId(MappedClass mappedClass, BeanMap instance) {
        ID subject = createResource(instance);
        setId(mappedClass, subject, instance);
        return subject;
    }
    
	private void assignModel() {
        CloseableIterator<STMT> statements = connection.findStatements(null, CORE.modelId, null, null, false);
        if (statements.hasNext()) {
            STMT statement = statements.next();
            if (!statements.hasNext()) {
                BID subject = (BID) statement.getSubject();
                BID object = (BID) statement.getObject();
                model = (BID) object;
                
                if (verifyLocalId((BID) model, subject) && verifyLocalId(model, object)) {
                    // OK
                    return;
                }
            }
        }
        cleanupModel();
        if (model == null) {
            // modelId
            BID subject = connection.createBNode();
            model = connection.createBNode();
            recordAddStatement(subject, CORE.modelId, model, null);

            addCheckNumber(model, subject);
            addCheckNumber(model, model);
        }
    }

    @Override
    public void autowire(Object instance) {
        initialize();
        Assert.notNull(instance);
        BeanMap beanMap = toBeanMap(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(getClass(instance));
        bind(getId(mappedClass, beanMap), beanMap);
    }
        
    protected <T> T bind(ID subject, T instance) {
        if (instance != null) {
            if (instance instanceof LifeCycleAware) {
                ((LifeCycleAware) instance).beforeBinding();
            }
            // TODO: defaultContext parameter?
            UID context = getContext(instance, null);
            BeanMap beanMap = toBeanMap(instance);
            MappedClass mappedClass = MappedClass.getMappedClass(getClass(instance));
            setId(mappedClass, subject, beanMap);
//            loadStack.add(instance);
            for (MappedPath path : mappedClass.getProperties()) {
                if (!path.isConstructorParameter()) {
                    MappedProperty<?> property = path.getMappedProperty();
                    if (!property.isVirtual()) {
                        Object convertedValue = getPathValue(mappedClass, path, subject, context);
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
    
    private void cleanupModel(){
        removeStatements(null, CORE.modelId, null, null);
        removeStatements(null, CORE.localId, null, null);
    }
    
    @Override
    public void clear() {
        instanceCache = LazyMap.decorate(
                new HashMap<ID, List<Object>>(), 
                new Factory<List<Object>>() {
                    @Override
                    public List<Object> create() {
                        return new ArrayList<Object>();
                    }
                }
        );
        resourceCache = new IdentityHashMap<Object, ID>();
        addedStatements = new LinkedHashSet<STMT>();
        removedStatements = new LinkedHashSet<STMT>();
        seen = null;
    }
    
    private Class<?> convertClassReference(UID uid, Type targetType) {
        List<Class<?>> mappedClasses = conf.getMappedClasses(uid);
        Class<?> targetClass = MappedProperty.getGenericClass(targetType, 0);
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
            throws InstantiationException, IllegalAccessException, Exception {
        MappedProperty<?> mappedProperty = propertyPath.getMappedProperty();
        Object convertedValue;
        Class<?> targetType = mappedProperty.getComponentType();
        int size = values.size();
        if (mappedProperty.isList() && size > 0) {
            if (size == 1) {
                NODE node = values.iterator().next();
                if (node instanceof ID) {
                    values = convertList((ID) node, context, targetType);
                } 
                // TODO log error?
            } 
            // TODO log error?
        } else {
            // TODO support containers?
        }
        Class collectionType = mappedProperty.getCollectionType();
        Collection collection = (Collection) collectionType.newInstance();
        for (NODE value : values) {
            collection.add(convertValue(value, targetType, propertyPath));
        }
        convertedValue = collection;
        return convertedValue;
    }
    
    private String convertLocalized(MappedPath propertyPath, Set<? extends NODE> values) {
        return LocaleUtil.getLocalized(convertLocalizedMap(propertyPath, values), 
                locales, null);
    }

	private Map<Locale, String> convertLocalizedMap(MappedPath propertyPath, Set<? extends NODE> values) {
        Map<Locale, String> result = new HashMap<Locale, String>();
        for (NODE value : values) {
            // XXX what if node is a resource?
            LIT literal = (LIT) value;
            Locale lang = literal.getLang();
            if (lang == null) {
            	lang = Locale.ROOT;
            }
            result.put(lang, literal.getValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object convertMap(MappedPath propertyPath, Set<? extends NODE> values)
            throws Exception {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Class<?> componentType = propertyDefinition.getComponentType();
        Class<?> keyType = propertyDefinition.getKeyType();
        Map map = new HashMap();
        for (NODE value : values) {
            // Map key
            Object key = convertValue(getFunctionalValue((ID) value, 
                    propertyDefinition.getKeyPredicate(), false, null), 
                    keyType, propertyPath);
            // Map Value
            Object mapValue;
            UID valuePredicate = propertyDefinition.getValuePredicate();
            if (valuePredicate != null) {
                mapValue = convertValue(getFunctionalValue((ID) value, 
                        valuePredicate, false, null), componentType, propertyPath);
            } else {
                mapValue = convertValue(value, componentType, propertyPath);
            }
            map.put(key, mapValue);
        }
        convertedValue = map;
        return convertedValue;
    }

    // FIXME add ID parameter
    @SuppressWarnings("unchecked")
	private <T> T convertMappedObject(ID subject, Type requiredType, boolean polymorphic, boolean injection) {
        Class<T> requiredClass = (Class<T>) MappedProperty.getGenericClass(requiredType, 0);
        // FIXME defaultContext?
        UID context = getContext(requiredClass, null);
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
                instance = createInstance(subject, 
                        findTypes(subject, context), 
                        requiredClass);
            } else {
                Collection<ID> types = Collections.emptyList();
                instance = createInstance(subject, 
                        types, 
                        requiredClass);
            }
            put(subject, instance);
            bind(subject, instance);
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    protected <T> Class<? extends T> matchType(Collection<ID> types, Class<T> targetType) {
        Class<? extends T> result = targetType;
        boolean foundMatch = types.isEmpty();
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
    
    protected <T> T createInstance(ID subject, Collection<ID> types, Class<T> requiredType) {
        T instance;
        Class<? extends T> actualType = matchType(types, requiredType);
        if (actualType != null) {
            if (!conf.allowCreate(actualType)) {
                instance = null;
            } else {
                try {
                    MappedClass mappedClass = MappedClass.getMappedClass(actualType);
                    MappedConstructor mappedConstructor = 
                        mappedClass.getConstructor();
                    if (mappedConstructor == null) {
                        instance = actualType.newInstance();
                    } else {
                        List<Object> constructorArguments = 
                            getConstructorArguments(mappedClass, subject, mappedConstructor);
                        @SuppressWarnings("unchecked")
                        Constructor<T> constructor = (Constructor<T>) mappedConstructor.getConstructor(); 
                        instance = constructor.newInstance(constructorArguments.toArray());
                    }
                } catch (InstantiationException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (SecurityException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot convert instance " + subject
                    + " with types " + types + " into required type " + requiredType);
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Object convertSingleValue(MappedPath propertyPath, Set<? extends NODE> values) throws Exception {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Class targetType = propertyDefinition.getType();
        NODE value = values.isEmpty() ? null : values.iterator().next();
        convertedValue = convertValue(value, targetType, propertyPath);
        return convertedValue;
    }
    
    @SuppressWarnings("unchecked")
    private Object convertValue(NODE value, Class<?> targetType, MappedPath propertyPath) throws Exception {
        Class targetClass = MappedProperty.getGenericClass(targetType, 0);
        MappedProperty mappedProperty = propertyPath.getMappedProperty();
        Object convertedValue;
        if (value != null) {
            try {
                // "Wildcard" type
                if (MappedPath.isWildcard(targetClass) && value.isResource()) {
                    convertedValue = convertMappedObject((ID) value,
                            Object.class, true, mappedProperty.isInjection());
                }
                // Enumerations
                else if (targetClass.isEnum()) {
                    if (value instanceof UID) {
                        convertedValue = Enum.valueOf((Class<? extends Enum>) targetType, 
                                ((UID) value).ln());
                    } else if (value instanceof LIT) {
                        convertedValue = Enum.valueOf((Class<? extends Enum>) targetType, value.getValue());
                    } else {
                        throw new IllegalArgumentException("Cannot bind BNode into enum");
                    }
                } 
                // Class reference
                else if (MappedPath.isClassReference(targetClass)) {
                    if (value instanceof UID) {
                        convertedValue =  convertClassReference((UID) value, 
                                propertyPath.getMappedProperty().getParametrizedType());
                    } else {
                        throw new IllegalArgumentException("Cannot assign bnode or literal " + value
                                + " into " + propertyPath);
                    }
                }
                // Mapped class
                else if (MappedPath.isMappedClass(targetClass) || mappedProperty.isInjection()) {
                    if (value instanceof ID) {
                        convertedValue = convertMappedObject((ID) value,
                                targetType, mappedProperty.isPolymorphic(), 
                                mappedProperty.isInjection());
                    } else {
                        throw new IllegalArgumentException("Cannot assign " + value
                                + " into " + propertyPath);
                    }
                }
                // ID reference
                else if (ID.class.isAssignableFrom(targetType)) {
                    if (value instanceof ID) {
                        convertedValue = value;
                    } else {
                        throw new IllegalArgumentException("Cannot assign " + value
                                + " into " + propertyPath);
                    }
                }
                // Use standard property editors for others
                else {
                    UID datatype = null;
                    if (value instanceof LIT) {
                        datatype = ((LIT) value).getDatatype();
                    }
                    convertedValue = conf.getConverterRegistry().fromString(value.getValue(), datatype, targetClass);
                }
            } catch (IllegalArgumentException e) {
                if (propertyPath.isIgnoreInvalid()) {
                    convertedValue = null;
                } else {
                    logger.error(e.getMessage(), e);
                    throw new IllegalArgumentException("Error assigning " + propertyPath, e);
                }
            }
        } else {
            convertedValue = null;
        }
        return convertedValue;
    }
    
    protected ID createResource(Object instance) {
        ID id = conf.createURI(toBeanMap(instance).getBean());
        if (id == null) {
            id = connection.createBNode();
        }
        return id;
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
                try {
                    stmts.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }
    
    private Collection<NODE> convertList(ID subject, UID context, Class<?> targetType) {
        List<NODE> list = new ArrayList<NODE>();
        while (subject != null && !subject.equals(RDF.nil)) {
            list.add(getFunctionalValue(subject, RDF.first, false, context));
            subject = (ID) getFunctionalValue(subject, RDF.rest, false, context);
        }
        return list;
    }
    
    private Set<NODE> filterObjects(CloseableIterator<STMT> statements) {
        Set<NODE> objects = new LinkedHashSet<NODE>();
        try {
            while (statements.hasNext()) {
                STMT statement = statements.next();
                objects.add(statement.getObject());
            }
        } finally {
            try {
                statements.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            try {
                statements.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return subjects;
    }

    @Override
    public <T> List<T> findInstances(Class<T> clazz) {
        final Set<T> instances = new LinkedHashSet<T>();
        UID type = MappedClass.getMappedClass(clazz).getUID();
        findInstances(clazz, type, instances);
        return new ArrayList<T>(instances);
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
        initialize();
        UID context = getContext(clazz, null);
        try {
            Set<ID> resources = new LinkedHashSet<ID>();
            resources.addAll(this.<ID>filterSubject(connection.findStatements(null, RDF.type, uri, context, true)));
            for (ID subject : resources) {
                instances.add(getBean(clazz, subject));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<NODE> findPathValues(ID resource, MappedPath path, int index, UID context) {
        MappedPredicate predicate = path.get(index);
        if (predicate.context() != null) {
            context = new UID(predicate.context());
        }
        Set<NODE> values = findValues(resource, predicate.uid(), 
                predicate.inv(), predicate.includeInferred(), context);
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

    // FIXME: This should return a closable iterator
    protected List<STMT> findStatements(ID subject, UID predicate, NODE object, 
            boolean includeInferred, UID context) {
        List<STMT> statements = new ArrayList<STMT>();
        CloseableIterator<STMT> iter = connection.findStatements(subject, predicate, object, context, includeInferred);
        try {
            while (iter.hasNext()) {
                statements.add(iter.next());
            }
        } finally {
            try {
                iter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return statements;
    }
    
    private List<ID> findTypes(ID subject, UID context) {
        List<ID> types = new ArrayList<ID>();
        List<STMT> statements = findStatements(subject, RDF.type, null, true, context);
        for (STMT stmt : statements) {
            types.add((ID) stmt.getObject());
        }
        return types;
    }
    
    private Set<NODE> findValues(ID resource, UID predicate, boolean inverse, boolean includeInferred, 
            UID context) {
        if (inverse) {
            return (Set<NODE>) filterSubject(connection.findStatements(null, predicate, resource, context, includeInferred));
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
        return connection.createQuery(this).from(expr);
    }

    @Override
    public <T> T get(Class<T> clazz, ID subject) {
        initialize();
        Assert.notNull(subject, "subject was null");
        return getBean(clazz, subject);
    }
    
    @Override
    public <T> T getById(String id, Class<T> clazz) {
        return get(clazz, new LID(id));
    }
    
    @Override
    public <T> T get(Class<T> clazz, LID subject) {
        return get(clazz, identityService.getID(subject));
    }

    
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
        if (clazz != null) {
            MappedClass mappedClass = MappedClass.getMappedClass(clazz);
            if (mappedClass != null) {
                polymorphic = mappedClass.isPolymorphic();
            }
        }
        return this.<T>convertMappedObject(subject, clazz, polymorphic, false);
    }

    @Override
    public <T> T getBean(Class<T> clazz, UID subject) {
        return (T) get(clazz, subject);
    }

	private List<Object> getConstructorArguments(MappedClass mappedClass, ID subject, MappedConstructor mappedConstructor) {
        List<Object> constructorArguments = new ArrayList<Object>(mappedConstructor.getArgumentCount());
        // TODO parentContext?
        UID context = getContext(mappedConstructor.getDeclaringClass(), null);
        for (MappedPath path : mappedConstructor.getMappedArguments()) {
        	constructorArguments.add(getPathValue(mappedClass, path, subject, context));
        }
        return constructorArguments;
	}
    
    public UID getContext(Object instance, UID defaultContext) {
        return getContext(instance.getClass(), defaultContext);
    }
    
    public UID getContext(Class<?> clazz, UID defaultContext) {
        UID contextUID = conf.getContext(clazz);
        if (contextUID != null) {
            return contextUID;
        } else {
            return defaultContext;
        }
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
    
    public ConverterRegistry getConverterRegistry(){
        return conf.getConverterRegistry();
    }

    private NODE getFunctionalValue(ID subject, UID predicate, boolean includeInferred, UID context) {
        List<STMT> statements = findStatements(subject, predicate, null, includeInferred, context);
        if (statements.size() > 1) {
            throw new RuntimeException(
                    "Found multiple values for a functional predicate: "
                            + predicate + " of resource" + subject);
        }
        if (statements.size() > 0) {
            return statements.get(0).getObject();
        } else {
            return null;
        }
    }
    
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
                    if (rid instanceof UID) {
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
    
    public ID getId(Object instance){
        BeanMap beanMap = toBeanMap(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(getClass(instance));
        return getId(mappedClass, beanMap);    
    }
    
    public IdentityService getIdentityService() {
        return identityService;
    }

    public LID getLID(ID id) {
        initialize();
        return identityService.getLID(getModel(), id);
    }
        
    protected BID getModel() {
        return model;
    }

    private Object getPathValue(MappedClass mappedClass, MappedPath path, ID subject, UID context) {
    	Object convertedValue = null;
		if (conf.allowRead(path)) {
		    try {
		        Set<NODE> values;
                MappedProperty<?> property = path.getMappedProperty();
                if (property.isMixin()) {
                    values = Collections.<NODE>singleton(subject);
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
		        convertedValue = getValue(path, values, context);
		    } catch (IllegalArgumentException e) {
		        if (!path.isIgnoreInvalid()) {
		            throw e;
		        }
		        return null;
		    } catch (Exception e) {
		        // TODO: ExecutionContext
		        throw new RuntimeException(path.toString(), e);
		    }
		}
		return convertedValue;
	}
    
    @Override
    public RDFBeanTransaction getTransaction() {
        return transaction;
    }
    
    private Object getValue(MappedPath propertyPath, Set<? extends NODE> values, UID context) throws Exception {
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
                throw new RuntimeException("Illegal use of @Localized with " 
                        + mappedProperty.getType() + " at " + propertyPath);
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
            throw new IllegalArgumentException(
                    "Cannot assign multiple values into singleton property "
                            + propertyPath + ": " + values);
        }
        return convertedValue;
    }

    protected void initialize() {
        if (!initialized) {
            clear();
            if (identityService == null) {
                identityService = MemoryIdentityService.instance();
            }
            assignModel();
            initialized = true;
        }
    }

    private void put(ID resource, Object value) {
        if (resource != null) {
            instanceCache.get(resource).add(value);
            resourceCache.put(value, resource);
        }
    }
    
    private void recordRemoveStatement(STMT statement) {
        if (!addedStatements.remove(statement)) {
            removedStatements.add(statement);
        }
    }
    
    private void recordAddStatement(ID subject, UID predicate, NODE object, UID context) {
        STMT statement = new STMT(subject, predicate, object, context);
        if (!removedStatements.remove(statement)) {
            addedStatements.add(statement);
        }
    }

    protected void removeStatements(ID subject, UID predicate, NODE object, UID context) {
        for (STMT statement : findStatements(subject, predicate, object, false, context)) {
            recordRemoveStatement(statement);
        }
    }
    
    private <T> void setId(MappedClass mappedClass, ID subject, BeanMap instance) {
        MappedProperty<?> idProperty = mappedClass.getIdProperty();
        if (idProperty != null && !mappedClass.isEnum() && !idProperty.isVirtual()) {
        	Object id = null;
        	Identifier identifier;
            Class<?> type = idProperty.getType();
            IDType idType = idProperty.getIDType();
			if (idType == IDType.LOCAL) {
			    identifier = toLID(subject);
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
                    throw new IllegalArgumentException(
                            "Cannot assign id of " + mappedClass + " into " + type);
                }
			}
            idProperty.setValue(instance, id);
        }
    }

    public void setIdentityService(IdentityService identityService) {
        if (initialized) {
            throw new IllegalStateException("Session already initialized");
        }
        this.identityService = identityService;
    }

    protected LID toLID(ID resource) {
        return identityService.getLID(getModel(), resource);
    }

    protected BeanMap toBeanMap(Object instance) {
        return instance instanceof BeanMap ? (BeanMap) instance : new BeanMap(instance);
    }
    
    @SuppressWarnings("unchecked")
    private void toRDF(Object instance, ID subject, UID context, MappedClass mappedClass, boolean update) {
        UID uri = mappedClass.getUID();
        if (uri != null) {
            recordAddStatement(subject, RDF.type, uri, context);
        }
        BeanMap beanMap = toBeanMap(instance);
        MappedProperty<?> property;
        for (MappedPath path : mappedClass.getProperties()) {
            property = path.getMappedProperty();
            if (path.isSimpleProperty()) {
                MappedPredicate mappedPredicate = path.get(0);
                UID predicate = mappedPredicate.uid();

                if (update) {
                    for (STMT statement : findStatements(subject, predicate, null, false, context)) {
                        if (property.isLocalized() && String.class.equals(property.getType())) {
                            LIT lit = (LIT) statement.getObject();
                            if (ObjectUtils.equals(getCurrentLocale(), lit.getLang())) {
                                recordRemoveStatement(statement);
                            }
                        } else {
                            recordRemoveStatement(statement);

                            if (property.isList()) {
                                removeList(statement.getObject(), context);
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
                                    LIT literal = new LIT(entry.getValue().toString(), entry.getKey());
                                    recordAddStatement(subject, predicate, literal, context);
                                }
                            }
                        } else {
                            LIT literal = new LIT(object.toString(), getCurrentLocale());
                            recordAddStatement(subject, predicate, literal, context);
                        }
                    } else {
                        NODE value = toRDFValue(object, context);
                        if (value != null) {
                            recordAddStatement(subject, predicate, value, context);
                        }
                    }
                }
            } else if (property.isMixin()) {
                Object object = property.getValue(beanMap);
                if (object != null) {
                    UID subContext = getContext(object, context);
                    toRDF(object, subject, subContext, MappedClass.getMappedClass(getClass(object)), update);
                }
            }
        }
    }

    protected Class<?> getClass(Object object) {
        return object instanceof BeanMap ? ((BeanMap) object).getBean().getClass() : object.getClass();
    }
    
    private ID toRDF(Object instance, UID parentContext) {
        BeanMap beanMap = toBeanMap(Assert.notNull(instance));
        Class<?> clazz = getClass(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(clazz);
        UID context = getContext(clazz, parentContext);
        ID subject = resourceCache.get(instance);
        if (subject == null) {
            subject = getId(mappedClass, beanMap);
        }
        if (mappedClass.isEnum()) {
            subject = new UID(mappedClass.getClassNs(), ((Enum<?>) instance).name());
            put(subject, instance);
        } else if (seen.add(instance)) {
            // Update
            boolean update = subject != null && exists(subject, mappedClass, context);

            // Create
            if (subject == null) {
                subject = assignId(mappedClass, beanMap);
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

    private void removeList(NODE node, UID context) {
        // XXX What if the same list is referred elsewhere? 
        // Is blank node...
        if (node.isBNode()) {
            BID bnode = (BID) node;
            // ...of type rdf:List
            if (findStatements(bnode, RDF.type, RDF.List, true, context).size() > 0) {
                for (STMT statement : findStatements(bnode, null, null, false, context)) {
                    recordRemoveStatement(statement);
                    removeList(statement.getObject(), context);
                }
            }
        }
    }

    private ID toRDFList(List<?> list, UID context) {
        ID firstNode = null;
        ID currentNode = null;
        for (Object value : list) {
            if (currentNode == null) {
                firstNode = currentNode = connection.createBNode();
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
        UID dataType = conf.getConverterRegistry().getDatatype(o);
        return new LIT(conf.getConverterRegistry().toString(o), dataType);
    }

    private NODE toRDFValue(Object object, UID context) {
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

    private boolean verifyLocalId(BID model, BID bnode) {
        String lid = identityService.getLID(model, bnode).getId();
        List<STMT> statements = findStatements(bnode, CORE.localId, new LIT(lid), true, null);
        return statements.size() == 1;
    }

    public FlushMode getFlushMode() {
        return flushMode;
    }

    public void setFlushMode(FlushMode flushMode) {
        this.flushMode = flushMode;
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
        transaction = connection.beginTransaction(this, readOnly, txTimeout, isolationLevel);
        return transaction;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }

    @Override
    public Configuration getConfiguration() {
        return conf;
    }

    public RDFConnection getConnection() {
        return connection;
    }

}
