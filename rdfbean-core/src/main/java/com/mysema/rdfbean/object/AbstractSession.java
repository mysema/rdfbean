/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.object;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;
import org.springframework.beans.BeanWrapperImpl;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.commons.lang.Assert;
import com.mysema.query.grammar.types.Expr.EEntity;
import com.mysema.rdfbean.CORE;
import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.Dialect;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.IDType;
import com.mysema.rdfbean.model.Identifier;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.NodeType;
import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.object.identity.MemoryIdentityService;

/**
 * @author sasa
 * 
 */
public abstract class AbstractSession<N, 
        R extends N, 
        B extends R, 
        U extends R, 
        L extends N, 
        S> implements Session, RDFBinder<R> {
    
    private Converter converter = new Converter();
    
    private U coreLocalId;
    
	private U coreModelId;
    
	private Configuration conf;

    private IdentityService identityService;

    private boolean initialized = false;
    
    Map<R, List<Object>> instanceCache = LazyMap.decorate(
        new HashMap<R, List<Object>>(), 
            new Factory<List<Object>>() {
                @Override
                public List<Object> create() {
                    return new ArrayList<Object>();
                }
            }
    );

    private List<Locale> locales;
    
    private BID model;
    
    private Map<String,ObjectRepository> parentRepositories = new HashMap<String, ObjectRepository>();
    
    private U rdfFirst;
    
    private U rdfList;
    
    private U rdfNil;

    private U rdfRest;
    
    private U rdfType;

    private Map<Object, R> resourceCache = new IdentityHashMap<Object, R>();
    
    private Set<Object> seen;
    
    // TODO make configurable: !conf.isReadOnly() 
//    private Map<Object, Set<S>> objectStatements = new IdentityHashMap<Object, Set<S>>();
    
//    private Deque<Object> loadStack = new LinkedList<Object>();
    
    public AbstractSession(Class<?>... classes) {
        this(Collections.<Locale>emptyList(), new DefaultConfiguration(classes));
    }
    
    public AbstractSession(Configuration ctx) {
        this(Collections.<Locale>emptyList(), ctx);
    }

    public AbstractSession(List<Locale> locales, Class<?>... classes) {
        this(locales, new DefaultConfiguration(classes));
    }
    
    public AbstractSession(List<Locale> locales, Configuration defaultCtx) {
        this.locales = locales;
        this.conf = defaultCtx;
    }

    public AbstractSession(List<Locale> locales, Package... packages) throws ClassNotFoundException {
        this(locales, new DefaultConfiguration(packages));
    }
    
    public AbstractSession(Locale locale, Class<?>... classes) {
        this(Collections.singletonList(locale), new DefaultConfiguration(classes));
    }

    public AbstractSession(Locale locale, Configuration ctx) {
        this(Collections.singletonList(locale), ctx);
    }
    
    public AbstractSession(Locale locale, Package... packages) throws ClassNotFoundException {
        this(Collections.singletonList(locale), new DefaultConfiguration(packages));
    }

    public AbstractSession(Package... packages) throws ClassNotFoundException {
        this(Collections.<Locale>emptyList(), new DefaultConfiguration(packages));
    }

    @Override
    public LID save(Object instance) {
        initialize();
        if (seen == null) {
            seen = new HashSet<Object>();
        }
    	R subject = toRDF(instance, null);
    	return toLID(subject);
    }
    
    @Override
    public List<LID> saveAll(Object... instances) {
        List<LID> ids = new ArrayList<LID>(instances.length);
        seen = new HashSet<Object>(instances.length*3);
        for (Object instance : instances) {
            ids.add(save(instance));
        }
        return ids;
    }

    private void addCheckNumber(BID model, B bnode) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        BID bid = dialect.getBID(bnode);
        String lid = identityService.getLID(model, bid).getId();
        addStatement(bnode, coreLocalId, dialect.getLiteral(new LIT(lid)), null);
    }

    @Override
    public void addParent(String ns, ObjectRepository parent) {
        Assert.hasText(ns);
        Assert.notNull(parent);
        parentRepositories.put(ns, parent);
    }

    protected abstract void addStatement(R subject, U predicate, N object, U contexts);

    private R assignId(MappedClass mappedClass, Object instance) {
        R subject = createResource(instance);
        setId(mappedClass, subject, instance);
        return subject;
    }
    
    @SuppressWarnings("unchecked")
	private void assignModel() {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        List<S> statements = findStatements(null, coreModelId, null, false, null);
        if (statements.size() > 1) {
            //TODO log.error(found multiple modelIds)
            cleanupModel();
        }
        // Verify check numbers
        else if (statements.size() == 1) {
            S statement = statements.get(0);
            //XXX type check before cast? Thought only bNodes allowed here, error could be nicer
            B subject = (B) dialect.getSubject(statement);
            //XXX type check before cast? Thought only bNodes allowed here, error could be nicer
            B modelId = (B) dialect.getObject(statement);

            model = dialect.getBID(modelId);
            
            if (!(verifyLocalId(dialect, model, subject) && verifyLocalId(dialect, model, modelId))) {
                model = null;
                cleanupModel();
            }
        }
        if (model == null) {
            // modelId
            B subject = dialect.createBNode();
            B modelId = dialect.createBNode();
            addStatement(subject, coreModelId, modelId, null);

            model = dialect.getBID(modelId);

            addCheckNumber(model, subject);
            addCheckNumber(model, modelId);
        }
    }

    @Override
    public void autowire(Object instance) {
        Assert.notNull(instance);
        MappedClass mappedClass = MappedClass.getMappedClass(instance.getClass());
        bind(getId(mappedClass, instance), instance);
    }
        
    @Override
    public <T> void bind(R subject, T instance) {
        put(subject, instance);
        if (instance != null) {
            if (instance instanceof LifeCycleAware) {
                ((LifeCycleAware) instance).beforeBinding();
            }
            // TODO: default context
            U context = getContext(instance, null);
            MappedClass mappedClass = MappedClass.getMappedClass(instance.getClass());
            setId(mappedClass, subject, instance);
//            loadStack.add(instance);
            for (MappedPath path : mappedClass.getProperties()) {
                if (!path.isConstructorParameter()) {
                    MappedProperty<?> property = path.getMappedProperty();
                    if (!property.isVirtual()) {
                        Object convertedValue = getPathValue(mappedClass, path, subject, context);
                        if (convertedValue != null) {
                            property.setValue(new BeanWrapperImpl(instance), convertedValue);
                        }
                    }
                }
            }
            if (instance instanceof LifeCycleAware) {
                ((LifeCycleAware) instance).afterBinding();
            }
        }
    }
    
    private void cleanupModel(){
        removeStatements(null, coreModelId, null, null);
        removeStatements(null, coreLocalId, null, null);
    }
    
    @Override
    public void clear() {
        instanceCache.clear();
    }

    protected Collection<R> convert(Collection<UID> uids) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        List<R> uris = new ArrayList<R>(uids.size());
        for (UID uid : uids) {
            uris.add(dialect.getURI(uid));
        }
        return uris;
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
    private Object convertCollection(MappedPath propertyPath, Set<N> values, U context)
            throws InstantiationException, IllegalAccessException, Exception {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Class collectionType = propertyDefinition.getCollectionType();
        Type targetType = propertyDefinition.getComponentType();
        Collection collection = (Collection) collectionType.newInstance();
        for (N value : expandListOrContainer(values, context)) {
            collection.add(convertValue(value, targetType, propertyPath));
        }
        convertedValue = collection;
        return convertedValue;
    }
    
    private String convertLocalized(MappedPath propertyPath, Set<N> values) {
        return LocaleUtil.getLocalized(convertLocalizedMap(propertyPath, values), 
                locales, null);
    }

    @SuppressWarnings("unchecked")
	private Map<Locale, String> convertLocalizedMap(MappedPath propertyPath, Set<N> values) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        Map<Locale, String> result = new HashMap<Locale, String>();
        for (N value : values) {
            LIT literal = dialect.getLIT((L) value);
            Locale lang = literal.getLang();
            if (lang == null) {
            	lang = Locale.ROOT;
            }
            result.put(lang, literal.getValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object convertMap(MappedPath propertyPath, Set<N> values)
            throws Exception {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Type componentType = propertyDefinition.getComponentType();
        Type keyType = propertyDefinition.getKeyType();
        Map map = new HashMap();
        for (N value : values) {
            // Map key
            Object key = convertValue(getFunctionalValue((R) value, 
                    propertyDefinition.getKeyPredicate(), false, null), 
                    keyType, propertyPath);
            // Map Value
            Object mapValue;
            UID valuePredicate = propertyDefinition.getValuePredicate();
            if (valuePredicate != null) {
                mapValue = convertValue(getFunctionalValue((R) value, 
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
	private <T> T convertMappedObject(R subject, Type requiredType, boolean polymorphic, boolean injection) {
        Class<T> requiredClass = (Class<T>) MappedProperty.getGenericClass(requiredType, 0);
        // FIXME defaultContext?
        U context = getContext(requiredClass, null);
        Object instance = get(subject, requiredClass);
        if (instance == null) {
            Dialect<N,R,B,U,L,S> dialect = getDialect();
            if (injection) {
                if (dialect.getNodeType(subject) == NodeType.URI && requiredClass != null) {
                    UID uri = dialect.getUID((U) subject);
                    ObjectRepository orepo = parentRepositories.get(uri.ns());
                    if (orepo != null) {
                        return (T) orepo.getBean(requiredClass, 
                        		ID.uriRef(uri.ns(), uri.ln()));
                    }
                }
            }
            if (polymorphic) {
                instance = conf.createInstance(subject, 
                        findTypes(subject, context), 
                        requiredClass,
                        this,
                        getDialect());
            } else {
                Collection<R> types;
                UID uid = MappedClass.getUID(requiredClass);
                if (uid != null) {
                    types = Collections.singleton(dialect.getResource(uid));
                } else {
                    types = Collections.emptyList();
                }
                instance = conf.createInstance(subject, 
                        types, 
                        requiredClass, 
                        this,
                        getDialect());
            }
            // Ensure that instance gets cached even if not binded
            put(subject, instance);
        }
        return (T) instance;
    }

    @SuppressWarnings("unchecked")
    private Object convertSingleValue(MappedPath propertyPath, Set<N> values) throws Exception {
        MappedProperty<?> propertyDefinition = propertyPath.getMappedProperty();
        Object convertedValue;
        Class targetType = propertyDefinition.getType();
        N value = values.isEmpty() ? null : values.iterator().next();
        convertedValue = convertValue(value, targetType, propertyPath);
        return convertedValue;
    }
    
    @SuppressWarnings("unchecked")
    private Object convertValue(N value, Type targetType, MappedPath propertyPath) throws Exception {
        Class targetClass = MappedProperty.getGenericClass(targetType, 0);
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        MappedProperty mappedProperty = propertyPath.getMappedProperty();
        Object convertedValue;
        if (value != null) {
            try {
                NODE node = dialect.getNode(value);
                // "Wildcard" type
                if (MappedPath.isWildcard(targetClass) && node.isResource()) {
                    convertedValue = convertMappedObject((R) value,
                            Object.class, true, mappedProperty.isInjection());
                }
                // Enumerations
                else if (targetClass.isEnum()) {
                    if (node instanceof UID) {
                        convertedValue = Enum.valueOf((Class<? extends Enum>) targetType, 
                                ((UID) node).ln());
                    } else if (node instanceof LIT) {
                        convertedValue = Enum.valueOf((Class<? extends Enum>) targetType, node.getValue());
                    } else {
                        throw new IllegalArgumentException("Cannot bind BNode into enum");
                    }
                } 
                // Class reference
                else if (MappedPath.isClassReference(targetClass)) {
                    if (node instanceof UID) {
                        convertedValue =  convertClassReference((UID) node, 
                                propertyPath.getMappedProperty().getParametrizedType());
                    } else {
                        throw new IllegalArgumentException("Cannot assign bnode or literal " + value
                                + " into " + propertyPath);
                    }
                }
                // Mapped class
                else if (MappedPath.isMappedClass(targetClass) || mappedProperty.isInjection()) {
                    if (node instanceof ID) {
                        convertedValue = convertMappedObject((R) value,
                                targetType, mappedProperty.isPolymorphic(), 
                                mappedProperty.isInjection());
                    } else {
                        throw new IllegalArgumentException("Cannot assign " + value
                                + " into " + propertyPath);
                    }
                }
                // Use standard property editors for others
                else {
                    UID datatype = null;
                    if (node instanceof LIT) {
                        datatype = ((LIT) node).getDatatype();
                    }
                    convertedValue = converter.convert(node.getValue(), datatype, targetClass);
                }
            } catch (IllegalArgumentException e) {
                if (propertyPath.isIgnoreInvalid()) {
                    convertedValue = null;
                } else {
                    throw new IllegalArgumentException("Error assigning " + propertyPath, e);
                }
            }
        } else {
            convertedValue = null;
        }
        return convertedValue;
    }
    protected R createResource(Object instance) {
        UID uri = conf.createURI(instance);
        if (uri != null) {
            return getDialect().getURI(uri);
        } else {
            return getDialect().createBNode();
        }
    }
    
    private boolean exists(R subject, MappedClass mappedClass, U context) {
        UID type = mappedClass.getUID();
        if (type != null) {
            if (findStatements(subject, rdfType, getDialect().getURI(type), true, context).size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<N> expandList(R subject, U context) {
        List<N> list = new ArrayList<N>();
//        TODO: RDFList rdfList = 
        while (subject != null && !subject.equals(rdfNil)) {
            list.add(getFunctionalValue(subject, rdfFirst, false, context));
            subject = (R) getFunctionalValue(subject, rdfRest, false, context);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
	private Collection<N> expandListOrContainer(Set<N> values, U context) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        if (values.size() == 1) {
            N value = values.iterator().next();
            if (dialect.isResource(value)) {
                R subject = (R) value;
                List<R> types = findTypes(subject, context);
                if (types.contains(rdfList)) {
                    return expandList(subject, context);
                } else {
                    // TODO: Containers
                    return values;
                }
            } else {
                return values;
            }
        } else {
            return values;
        }
    }

    private Collection<R> filterSubject(List<S> statements) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        List<R> subjects = new ArrayList<R>();
        for (S statement : statements) {
            subjects.add(dialect.getSubject(statement));
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
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        U context = getContext(clazz, null);
        try {
            boolean foundMatch = false;
            for (Class<?> mappedClass : conf.getMappedClasses(uri)) {
                if (clazz.isAssignableFrom(mappedClass)) {
                    foundMatch = true;
                    break;
                }
            }
            if (foundMatch) {
                Set<R> resources = new LinkedHashSet<R>();
                resources.addAll(filterSubject(findStatements(null, rdfType, dialect.getURI(uri), true, context)));
                for (R subject : resources) {
                    instances.add(getBean(clazz, subject));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<N> findPathValues(R resource, MappedPath path, int index, U context) {
        U ctx = context;
        MappedPredicate predicate = path.get(index);
        if (predicate.context() != null) {
            ctx = getDialect().getURI(predicate.context());
        }
        Set<N> values = findValues(resource, predicate.uid(), 
                predicate.inv(), predicate.includeInferred(), ctx);
        if (path.size() > index + 1) {
            Set<N> nestedValues = new LinkedHashSet<N>();
            for (N value : values) {
                if (getDialect().isResource(value)) {
                    nestedValues.addAll(findPathValues((R) value, path, index + 1, ctx));
                }
            }
            return nestedValues;
        }
        return values;
    }

    // FIXME: This should return a closable iterator
    protected abstract List<S> findStatements(R subject, U predicate, N object, 
            boolean includeInferred, U context);
    
    @SuppressWarnings("unchecked")
    private List<R> findTypes(R subject, U context) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        List<R> types = new ArrayList<R>();
        List<S> statements = findStatements(subject, rdfType, null, true, context);
        for (S stmt : statements) {
            N object = dialect.getObject(stmt);
            types.add((R) object);
        }
        return types;
    }
    
    private Set<N> findValues(R resource, UID predicate, boolean inverse, boolean includeInferred, 
            U context) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        U puri = dialect.getURI(predicate);
        Set<N> values = new LinkedHashSet<N>();
        if (inverse) {
            List<S> statements = findStatements(null, puri, resource, includeInferred, context);
//            Set<S> statementCache = getStatementCache();
            for (S stmt : statements) {
//                if (statementCache != null) {
//                    statementCache.add(stmt);
//                }
                values.add(dialect.getSubject(stmt));
            }
        } else {
            List<S> statements = findStatements(resource, puri, null, includeInferred, context);
            for (S stmt : statements) {
                values.add(dialect.getObject(stmt));
            }
        }
        return values;
    }
    
//    private Set<S> getStatementCache() {
//        Object currentObject = getCurrentObject();
//        if (currentObject != null) {
//            Set<S> statements = objectStatements.get(currentObject);
//            if (statements == null) {
//                statements = new LinkedHashSet<S>();
//                objectStatements.put(currentObject, statements);
//            }
//            return statements;
//        } else {
//            return null;
//        }
//    }
    
//    private void pushCurrentObject(Object object) {
//        loadStack.push(object);
//    }
    
//    private void popCurrentObject() {
//        loadStack.pop();
//    }
    
//    private Object getCurrentObject() {
//        return loadStack.peek();
//    }

    @Override
    public BeanQuery from(EEntity<?>... expr) {
        return new SimpleBeanQuery(this).from(expr);
    }

    @Override
    public <T> T get(Class<T> clazz, ID subject) {
        initialize();
        Assert.notNull(subject, "subject was null");
        return getBean(clazz, getDialect().getResource(subject));
    }
    
    @Override
    public <T> T getById(String id, Class<T> clazz) {
        return get(clazz, new LID(id));
    }
    
    @Override
    public <T> T get(Class<T> clazz, LID subject) {
        return get(clazz, identityService.getID(subject));
    }

    
    private Object get(R resource, Class<?> clazz) {
        for (Object instance : instanceCache.get(resource)) {
            if (clazz == null || clazz.isInstance(instance)) {
                return instance;
            }
        }
        return null;
    }
    
    @Override
    public <T, I extends ID> List<T> getAll(Class<T> clazz, I... subjects) {
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

    private <T> T getBean(Class<T> clazz, R subject) {
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

    @Override
	public List<Object> getConstructorArguments(MappedClass mappedClass, R subject, MappedConstructor mappedConstructor) {
        List<Object> constructorArguments = new ArrayList<Object>(mappedConstructor.getArgumentCount());
        // TODO parentContext?
        U context = getContext(mappedConstructor.getDeclaringClass(), null);
        for (MappedPath path : mappedConstructor.getMappedArguments()) {
        	constructorArguments.add(getPathValue(mappedClass, path, subject, context));
        }
        return constructorArguments;
	}
    
    public U getContext(Class<?> clazz, U defaultContext) {
        UID contextUID = conf.getContext(clazz);
        if (contextUID != null) {
            return getDialect().getURI(contextUID);
        } else {
            return defaultContext;
        }
    }
    
    public U getContext(Object object, U defaultContext) {
        return getContext(object.getClass(), defaultContext);
    }

    public Converter getConverter(){
        return converter;
    }
    
    public Locale getCurrentLocale() {
        if (locales != null && !locales.isEmpty()) {
            return locales.get(0);
        } else {
            // TODO default locale, null or empty locale?
            return Locale.getDefault();
        }
    }
    
    public abstract Dialect<N,R,B,U,L,S> getDialect();

    private N getFunctionalValue(R subject, U predicate, boolean includeInferred, U context) {
        List<S> statements = findStatements(subject, predicate, null, includeInferred, context);
        if (statements.size() > 1) {
            throw new RuntimeException(
                    "Found multiple values for a functional predicate: "
                            + predicate + " of resource" + subject);
        }
        if (statements.size() > 0) {
            return getDialect().getObject(statements.get(0));
        } else {
            return null;
        }
    }
    
    private N getFunctionalValue(R subject, UID predicate, boolean includeInferred, U context) {
        return getFunctionalValue(subject, getDialect().getURI(predicate), includeInferred, context);
    }
    
    private R getId(MappedClass mappedClass, Object instance) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        MappedProperty<?> idProperty = mappedClass.getIdProperty();
        if (idProperty != null) {
            // Assigned id
            Object id = idProperty.getValue(instance);
            if (id != null) {
                if (idProperty.getIDType() == IDType.LOCAL) {
                    LID lid;
                    if (id instanceof LID) {
                        lid = (LID) id;
                    } else {
                        lid = new LID(id.toString());
                    }
                    return dialect.getResource(identityService.getID(lid));
                } else {
                    UID uid = null;
                    if (id instanceof UID) {
                        uid = (UID) id;
                    } else {
                        uid = new UID(id.toString());
                    }
                    return dialect.getResource(uid);
                }
            }
        }
        return null;
    }
    
    public R getId(Object instance){
        MappedClass mappedClass = MappedClass.getMappedClass(instance.getClass());
        if (mappedClass != null){
            return getId(mappedClass, instance);    
        }else{
            throw new IllegalArgumentException("No mapped class for " + instance.getClass().getName());
        }        
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

    private Object getPathValue(MappedClass mappedClass, MappedPath path, R subject, U context) {
    	Object convertedValue = null;
		if (conf.allowRead(path)) {
		    try {
		        Set<N> values;
                MappedProperty<?> property = path.getMappedProperty();
                if (property.isMixin()) {
                    values = Collections.<N>singleton(subject);
                } else if (path.size() > 0) {
		            values = findPathValues(subject, path, 0, context);
		        } else {
		            values = new LinkedHashSet<N>();
		        }
		        if (values.isEmpty()) {
	                for (UID uri : property.getDefaults()) {
	                    values.add(getDialect().getURI(uri));
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
    public RDFBeanTransaction getTransaction(){
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private Object getValue(MappedPath propertyPath, Set<N> values, U context) throws Exception {
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

    private void initialize() {
        if (!initialized) {
            if (identityService == null) {
                identityService = MemoryIdentityService.instance();
            }
            initURIs();
            assignModel();
            initialized = true;
        }
    }
    
    private void initURIs() {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        this.coreLocalId = dialect.getURI(CORE.localId);
        coreModelId = dialect.getURI(CORE.modelId);
        rdfRest = dialect.getURI(RDF.rest);
        rdfFirst = dialect.getURI(RDF.first);
        rdfType = dialect.getURI(RDF.type);
        rdfList = dialect.getURI(RDF.List);
        rdfNil = dialect.getURI(RDF.nil);
    }

    private void put(R resource, Object value) {
        if (resource != null) {
            instanceCache.get(resource).add(value);
            resourceCache.put(value, resource);
        }
    }
    
    protected abstract void removeStatement(S statement);

    protected void removeStatements(R subject, U predicate, N object, U context) {
        for (S statement : findStatements(subject, predicate, object, false, context)) {
            removeStatement(statement);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> void setId(MappedClass mappedClass, R subject, T instance) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        MappedProperty<?> idProperty = mappedClass.getIdProperty();
        if (idProperty != null && !mappedClass.isEnum()) {
        	Object id = null;
        	Identifier identifier;
            Class<?> type = idProperty.getType();
            IDType idType = idProperty.getIDType();
			if (idType == IDType.LOCAL) {
			    identifier = toLID(subject);
			} else if (idType == IDType.URI) {
                if (dialect.getNodeType(subject) == NodeType.URI) {
                    identifier = dialect.getUID((U) subject);
                } else {
                    identifier = null;
                }
			} else {
			    identifier = dialect.getID((U) subject);
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
            idProperty.setValue(new BeanWrapperImpl(instance), id);
        }
    }

    public void setIdentityService(IdentityService identityService) {
        if (initialized) {
            throw new IllegalStateException("Session already initialized");
        }
        this.identityService = identityService;
    }

    protected LID toLID(R resource) {
        return identityService.getLID(getModel(), getDialect().getID(resource));
    }

    @SuppressWarnings("unchecked")
    private void toRDF(Object instance, R subject, U context, MappedClass mappedClass, boolean update) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        UID uri = mappedClass.getUID();
        if (uri != null) {
            addStatement(subject, rdfType, dialect.getURI(uri), context);
        }
        
        MappedProperty<?> property;
        for (MappedPath path : mappedClass.getProperties()) {
            if (path.isSimpleProperty()) {
                MappedPredicate mappedPredicate = path.get(0);
                U predicate = dialect.getURI(mappedPredicate.uid());

                if (update) {
                    for (S statement : findStatements(subject, predicate, null, false, context)) {
                        removeStatement(statement);
                        // Check if it's a list
                        removeList(dialect.getObject(statement), context, dialect);
                    }
                }
                
                Object object = path.getMappedProperty().getValue(instance);
                if (object != null) {
                    property = path.getMappedProperty();
                    if (property.isList()) {
                        R first = toRDFList((List<?>) object, context);
                        if (first != null) {
                            addStatement(subject, predicate, first, context);
                        }
                    } else if (property.isCollection()) {
                        for (Object o : (Collection<?>) object) {
                            N value = toRDFValue(o, context);
                            if (value != null) {
                                addStatement(subject, predicate, value, context);
                            }
                        }
                    } else if (property.isLocalized()) {
                        if (property.isMap()) {
                            for (Map.Entry<Locale, String> entry : ((Map<Locale, String>) object).entrySet()) {
                                if (entry.getValue() != null) {
                                    LIT lit = new LIT(entry.getValue().toString(), entry.getKey());
                                    L literal = dialect.getLiteral(lit);
                                    addStatement(subject, predicate, literal, context);
                                }
                            }
                        } else {
                            LIT lit = new LIT(object.toString(), getCurrentLocale());
                            L literal = dialect.getLiteral(lit);
                            addStatement(subject, predicate, literal, context);
                        }
                    } else {
                        N value = toRDFValue(object, context);
                        if (value != null) {
                            addStatement(subject, predicate, value, context);
                        }
                    }
                }
            } else if (path.getMappedProperty().isMixin()) {
                Object object = path.getMappedProperty().getValue(instance);
                if (object != null) {
                    U subContext = getContext(object, context);
                    toRDF(object, subject, subContext, MappedClass.getMappedClass(object.getClass()), update);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private R toRDF(Object instance, U parentContext) {
        MappedClass mappedClass = MappedClass.getMappedClass(Assert.notNull(instance).getClass());
        U context = getContext(instance.getClass(), parentContext);
        if (context == null) {
            // TODO ???
        }
        R subject = resourceCache.get(instance);
        if (subject == null) {
            subject = getId(mappedClass, instance);
        }
        if (mappedClass.isEnum()) {
            subject = getDialect().getURI(mappedClass.getClassNs() + ((Enum<?>) instance).name());
            put(subject, instance);
        } else if (seen.add(instance)) {
            // Update
            boolean update = subject != null && exists(subject, mappedClass, context);

            // Create
            if (subject == null) {
                subject = assignId(mappedClass, instance);
            }
            put(subject, instance);

            // Build-in namespaces are read-only
            if (getDialect().getNodeType(subject) == NodeType.URI 
                    && conf.isRestricted(getDialect().getUID((U) subject))) {
                return subject;
            }
            
            toRDF(instance, subject, context, mappedClass, update);
        }
        return subject;
    }

    private void removeList(N node, U context, Dialect<N, R, B, U, L, S> dialect) {
        // XXX What if the same list is referred elsewhere? 
        // Is blank node...
        if (dialect.getNodeType(node) == NodeType.BLANK) {
            @SuppressWarnings("unchecked")
            B bnode = (B) node;
            // ...of type rdf:List
            if (findStatements(bnode, rdfType, rdfList, true, context).size() > 0) {
                for (S statement : findStatements(bnode, null, null, false, context)) {
                    removeStatement(statement);
                    removeList(dialect.getObject(statement), context, dialect);
                }
            }
        }
    }

    private R toRDFList(List<?> list, U context) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        R firstNode = null;
        R currentNode = null;
        for (Object value : list) {
            if (currentNode == null) {
                firstNode = currentNode = dialect.createBNode();
            } else {
                B nextNode = dialect.createBNode();
                addStatement(currentNode, rdfRest, nextNode, context);
                currentNode = nextNode;
            }
            addStatement(currentNode, rdfType, rdfList, context);
            addStatement(currentNode, rdfFirst, toRDFValue(value, context), context);
        }
        if (currentNode != null) {
            addStatement(currentNode, rdfRest, rdfNil, context);
        }
        return firstNode;
    }

    private L toRDFLiteral(Object o) {
        Dialect<N,R,B,U,L,S> dialect = getDialect();
        UID dataType = converter.getDatatype(o);
        return dialect.getLiteral(new LIT(converter.toString(o), dataType));
    }

    private N toRDFValue(Object object, U context) {
        if (object == null) {
            return null;
        } else {
            Class<?> type = object.getClass();
            if (type.isAnnotationPresent(ClassMapping.class)) {
                return toRDF(object, context);
            } else if (object instanceof UID) {
                return getDialect().getURI((UID) object);
            } else {
                return toRDFLiteral(object);
            }
        }
    }

    private boolean verifyLocalId(Dialect<N,R,B,U,L,S> dialect, BID model, B bnode) {
        String lid = identityService.getLID(model, dialect.getBID(bnode)).getId();
        List<S> statements = findStatements(bnode, coreLocalId, 
                dialect.getLiteral(new LIT(lid)), true, null);
        return statements.size() == 1;
    }

}
