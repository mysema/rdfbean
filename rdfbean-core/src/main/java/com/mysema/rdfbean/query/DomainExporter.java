package com.mysema.rdfbean.query;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.JavaWriter;
import com.mysema.codegen.model.ClassType;
import com.mysema.codegen.model.SimpleType;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;
import com.mysema.codegen.model.Types;
import com.mysema.query.annotations.QueryInit;
import com.mysema.query.annotations.QueryType;
import com.mysema.query.codegen.*;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedProperty;

/**
 * @author tiwe
 *
 */
public class DomainExporter {

    private static final Logger logger = LoggerFactory.getLogger(DomainExporter.class);

    private String sourceSuffix = ".java";

    private final File targetFolder;

    private final Map<Class<?>, Type> classToType = new HashMap<Class<?>, Type>();

    private final Map<String,EntityType> allTypes = new HashMap<String,EntityType>();

    private final Map<String,EntityType> entityTypes = new HashMap<String,EntityType>();

    private final Set<EntityType> serialized = new HashSet<EntityType>();

    private final Configuration configuration;

    private final QueryTypeFactory queryTypeFactory;

    private final TypeMappings typeMappings;

    private final Serializer entitySerializer;

    private final TypeFactory typeFactory = new TypeFactory();

    private final SerializerConfig serializerConfig;

    public DomainExporter(File targetFolder, Configuration configuration){
        this("Q", "", targetFolder, SimpleSerializerConfig.DEFAULT, configuration);
    }

    public DomainExporter(String namePrefix, File targetFolder, Configuration configuration){
        this(namePrefix, "", targetFolder, SimpleSerializerConfig.DEFAULT, configuration);
    }

    public DomainExporter(String namePrefix, String nameSuffix, File targetFolder, Configuration configuration){
        this(namePrefix, nameSuffix, targetFolder, SimpleSerializerConfig.DEFAULT, configuration);
    }

    public DomainExporter(String namePrefix, File targetFolder, SerializerConfig serializerConfig, Configuration configuration){
        this(namePrefix, "", targetFolder, serializerConfig, configuration);
    }

    public DomainExporter(String namePrefix, String nameSuffix, File targetFolder, SerializerConfig serializerConfig, Configuration configuration){
        this.targetFolder = targetFolder;
        this.serializerConfig = serializerConfig;
        this.configuration = configuration;
        CodegenModule module = new CodegenModule();
        module.bind(CodegenModule.PREFIX, namePrefix);
        module.bind(CodegenModule.SUFFIX, nameSuffix);
        module.bind(CodegenModule.KEYWORDS, Collections.<String>emptySet());
        this.queryTypeFactory = module.get(QueryTypeFactory.class);
        this.typeMappings = module.get(TypeMappings.class);
        this.entitySerializer = module.get(EntitySerializer.class);
        typeFactory.setUnknownAsEntity(true);
    }

    public void execute() throws IOException {
        // collect types
        collectTypes();

        // add supertypes
        for (EntityType type : allTypes.values()){
            addSupertypeFields(type, allTypes);
        }

        // serialize them
        serialize(entityTypes, entitySerializer);
    }


    private void addSupertypeFields(EntityType model, Map<String, EntityType> superTypes) {
        for (Supertype supertype : model.getSuperTypes()){
            EntityType entityType = superTypes.get(supertype.getType().getFullName());
            if (entityType != null){
                supertype.setEntityType(entityType);
                model.include(supertype);
            }
        }
    }

    private void collectTypes(){
        for (MappedClass mappedClass : configuration.getMappedClasses()){
            if (mappedClass.isEnum()){
                // skip enum types
                continue;
            }
            EntityType entityType = (EntityType) createType(mappedClass.getJavaClass());
            for (MappedPath mappedPath : mappedClass.getProperties()){
                MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                Class<?> type = mappedPath.getMappedProperty().getType();
                Type propertyType = null;
                if (type.equals(Map.class)){
                    propertyType = new SimpleType(Types.MAP, createType(mappedProperty.getKeyType()), createType(mappedProperty.getComponentType()));
                }else if (type.equals(List.class)){
                    propertyType = new SimpleType(Types.LIST, createType(mappedProperty.getComponentType()));
                }else if (type.equals(Set.class)){
                    propertyType = new SimpleType(Types.SET, createType(mappedProperty.getComponentType()));
                }else if (type.equals(Collection.class)){
                    propertyType = new SimpleType(Types.COLLECTION, createType(mappedProperty.getComponentType()));
                }else{
                    propertyType = createType(type);
                }
                entityType.addProperty(createProperty(entityType, mappedPath.getName(), propertyType, mappedProperty.getAnnotations()));
            }
        }
    }

    private Type createType(Class<?> clazz){
        Type type = classToType.get(clazz);
        if (type == null){
            if (configuration.isMapped(clazz) && !clazz.isEnum()){
                type = createEntityType(clazz);
            }else{
                type = typeFactory.create(clazz);
            }
            classToType.put(clazz, type);
        }
        return type;
    }

    private Property createProperty(EntityType entityType, String propertyName, Type propertyType, Map<Class<? extends Annotation>, Annotation> annotations) {
        String[] inits = new String[0];
        if (annotations.containsKey(QueryInit.class)){
            inits = ((QueryInit)annotations.get(QueryInit.class)).value();
        }
        if (annotations.containsKey(QueryType.class)){
            propertyType = propertyType.as(((QueryType)annotations.get(QueryType.class)).value().getCategory());
        }
        Property property = new Property(entityType, propertyName, propertyType, inits);
        return property;
    }

    private EntityType createEntityType(Class<?> cl) {
        return createEntityType(cl, entityTypes);
    }

    private EntityType createEntityType(Class<?> cl,  Map<String,EntityType> types) {
        if (types.containsKey(cl.getName())){
            return types.get(cl.getName());
        }else{
            EntityType type = new EntityType(new ClassType(TypeCategory.ENTITY, cl));
            typeMappings.register(type, queryTypeFactory.create(type));
            if (cl.getSuperclass() != null && !cl.getSuperclass().equals(Object.class)){
                type.addSupertype(new Supertype(new ClassType(cl.getSuperclass())));
            }
            types.put(cl.getName(), type);
            allTypes.put(cl.getName(), type);
            return type;
        }
    }

    private void serialize(Map<String,EntityType> types, Serializer serializer) throws IOException {
        for (EntityType entityType : types.values()){
            if (serialized.add(entityType)){
                Type type = typeMappings.getPathType(entityType, entityType, true);
                String packageName = entityType.getPackageName();
                String className = packageName.length() > 0 ? (packageName + "." + type.getSimpleName()) : type.getSimpleName();
                write(serializer, className.replace('.', '/') + sourceSuffix, entityType);
            }
        }
    }

    private void write(Serializer serializer, String path, EntityType type) throws IOException {
        File targetFile = new File(targetFolder, path);
        Writer w = writerFor(targetFile);
        try{
            CodeWriter writer = new JavaWriter(w);
            serializer.serialize(type, serializerConfig, writer);
        }finally{
            w.close();
        }
    }

    private Writer writerFor(File file) {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            logger.error("Folder " + file.getParent() + " could not be created");
        }
        try {
            return new OutputStreamWriter(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


//    /**
//     * Override the Serializer for entity types
//     *
//     * @param entitySerializer
//     */
//    public void setEntitySerializer(Serializer entitySerializer) {
//        this.entitySerializer = entitySerializer;
//    }

    /**
     * Override the source file suffix (default: .java)
     *
     * @param sourceSuffix
     */
    public void setSourceSuffix(String sourceSuffix) {
        this.sourceSuffix = sourceSuffix;
    }
}
