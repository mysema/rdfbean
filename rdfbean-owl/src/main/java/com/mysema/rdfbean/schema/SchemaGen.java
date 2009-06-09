/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.schema;

import java.util.*;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.*;
import com.mysema.rdfbean.owl.*;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSDatatype;
import com.mysema.rdfbean.rdfs.RDFSResource;


/**
 * @author sasa
 *
 */
public class SchemaGen {
    
    private Configuration configuration;
    
    private SessionFactory sessionFactory;
    
    private ConverterRegistry converterRegistry = new ConverterRegistry();
    
    private Set<String> exportNamespaces = new HashSet<String>();
    
    private boolean useTypedLists = true;
    
    private String ontology;
    
    private String[] ontologyImports;
    
    public void exportConfiguration() {
        Configuration cnf = new DefaultConfiguration(RDFSClass.class.getPackage(), OWLClass.class.getPackage());
        Session session = sessionFactory.openSession(cnf);
        if (ontology != null) {
            Ontology ont = new Ontology(new UID(ontology));
            if (ontologyImports != null) {
                for (String oimport : ontologyImports) {
                    ont.addImport(new Ontology(new UID(oimport)));
                }
            }
            session.save(ont);
        }
        Map<UID, RDFSResource> resources = new HashMap<UID, RDFSResource>();
        resources.put(RDF.List, new RDFSClass<RDFSResource>(RDF.List));
        resources.put(RDF.first, new RDFProperty(RDF.first));
        resources.put(RDF.rest, new RDFProperty(RDF.rest));
        resources.put(RDF.type, new RDFProperty(RDF.type));
        resources.put(RDFS.label, new RDFProperty(RDFS.label));
        resources.put(RDFS.comment, new RDFProperty(RDFS.comment));
        resources.put(RDFS.Resource, new RDFSClass<Object>(RDFS.Resource));
        for (Class<?> clazz : configuration.getMappedClasses()) {
            processClass(clazz, session, resources);
        }
        session.saveAll(resources.values().toArray());
    }

    @SuppressWarnings("unchecked")
    private RDFSClass<RDFSResource> processClass(Class<?> clazz, Session session, 
            Map<UID, RDFSResource> resources) {
        if (clazz == null) {
            return null;
        }
        MappedClass mappedClass = MappedClass.getMappedClass(clazz);
        OWLClass owlClass = null;
        UID cuid = mappedClass.getUID();
        if (cuid != null) {
            if (!(exportNamespaces.isEmpty() || exportNamespaces.contains(cuid.ns()))) {
                owlClass = new ReferenceClass(cuid);
            } else {
                owlClass = (OWLClass) resources.get(cuid);
                
                if (owlClass == null) {
                    owlClass = new OWLClass(cuid);
                    resources.put(cuid, owlClass);
                    // label
                    owlClass.setLabel(Locale.ROOT, cuid.getLocalName());
                } else {
                    return owlClass;
                }
    
                // super class
                addParent(clazz.getSuperclass(), owlClass, session, resources);
                // interfaces
                for (Class<?> iface : clazz.getInterfaces()) {
                    addParent(iface, owlClass, session, resources);
                }
                
                if (mappedClass.isEnum()) {
                    List<RDFSResource> oneOf = new ArrayList<RDFSResource>();
                    for (Object enumValue: clazz.getEnumConstants()) {
                        oneOf.add(new AnyThing(new UID(cuid.ns(), ((Enum) enumValue).name()),
                                owlClass));
                    }
                    owlClass.setOneOf(oneOf);
                }

                // properties
                for (MappedPath mappedPath : mappedClass.getProperties()) {
                    if (!mappedPath.isInherited() && mappedPath.isSimpleProperty()) {
                        MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
                        MappedPredicate mappedPredicate = mappedPath.get(0);
                        UID puid = mappedPredicate.uid();
                        final RDFProperty property;
                        boolean seenProperty = resources.containsKey(puid);
                        if (seenProperty) {
                            property = (RDFProperty) resources.get(puid);
                        } else {
                            if (mappedProperty.isAnyResource()) {
                                property = new RDFProperty(puid);
                            } else if (mappedPath.isReference() || mappedProperty.isCollection()) {
                                property = new ObjectProperty(puid);
                            } else {
                                property = new DatatypeProperty(puid);
                            }
                            property.setLabel(Locale.ROOT, puid.getLocalName());
                            resources.put(puid, property);
                            // label
                        }
                        
                        if (mappedProperty.isLocalized()) {
                            //TODO
                        } else {
                            final Restriction restriction = new Restriction();
                            restriction.setOnProperty(property);
                            
                            // TODO range mismatches!
                            if (mappedProperty.isCollection()) {
                                if (mappedProperty.isList()) {
                                    if (property.getRange().isEmpty()) {
                                        RDFSClass<?> componentType = 
                                            processClass(mappedProperty.getComponentType(), session, 
                                                    resources);
                                        if (useTypedLists && componentType != null) {
                                            property.addRange(new TypedList(cuid.ns(), componentType));
                                        } else {
                                            property.addRange((RDFSClass<RDFSResource>) resources.get(RDF.List));
                                        }
                                        restriction.setMaxCardinality(1);
                                    }
                                } else {
                                    RDFSClass<?> componentType = 
                                        processClass(mappedProperty.getComponentType(), session, 
                                                resources);
                                    if (componentType != null) {
                                        restriction.setAllValuesFrom(componentType);
                                    }
                                }
                            } else if (mappedPath.isReference()) {
                                if (property.getRange().isEmpty()) {
                                    RDFSClass<?> range = processClass(mappedProperty.getType(), 
                                            session, resources);
                                    if (range != null) {
                                        property.addRange(range);
                                    } else if (mappedProperty.isAnyResource()) {
                                        property.addRange((RDFSClass<?>) resources.get(RDFS.Resource));
                                    }
                                }
                                if (!RDF.type.equals(puid.ns())) {
                                    restriction.setMaxCardinality(1);
                                }
                            } else if (!RDF.type.equals(puid.ns())) {
                                if (property.getRange().isEmpty()) {
                                    UID range;
                                    if (mappedProperty.isAnyResource()) {
                                        range = RDFS.Resource;
                                    } else {
                                        range = converterRegistry.getDatatype(mappedProperty.getType());
                                    }
                                    if (range != null) {
                                        property.addRange(getDatatype(range, resources));
                                    }
                                }
                                restriction.setMaxCardinality(1);
                            }
                            if (restriction.isDefined()) {
                                owlClass.addSuperClass(restriction);
                            }
                        }
                        
                        if (mappedProperty.isRequired()) {
                            final Restriction minCardinality = new Restriction();
                            minCardinality.setOnProperty(property);
                            minCardinality.setMinCardinality(1);
                            owlClass.addSuperClass(minCardinality);
                        }
//                        if (!seenProperty) {
//                            session.save(property);
//                        }
                    }
                }
            }
        }
        return owlClass;
    }
    
    private RDFSDatatype getDatatype(UID uid, Map<UID, RDFSResource> resources) {
        if (resources.containsKey(uid)) {
            return (RDFSDatatype) resources.get(uid);
        } else {
            RDFSDatatype datatype = new RDFSDatatype(uid);
            resources.put(uid, datatype);
            return datatype;
        }
    }

    private void addParent(Class<?> clazz, OWLClass owlClass, Session session, 
            Map<UID, RDFSResource> resources) {
        if (clazz != null) {
            RDFSClass<RDFSResource> parent = processClass(clazz, session, resources);
            if (parent != null) {
                owlClass.addSuperClass(parent);
            }
        }
    }
    
    public SchemaGen addExportNamespaces(Set<String> namespaces) {
        exportNamespaces.addAll(namespaces);
        return this;
    }
    
    public SchemaGen addExportNamespace(String ns) {
        exportNamespaces.add(ns);
        return this;
    }
    
    public SchemaGen setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public SchemaGen setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        return this;
    }

    public SchemaGen setOntology(String ontology) {
        this.ontology = ontology;
        return this;
    }

    public SchemaGen setUseTypedLists(boolean useTypedLists) {
        this.useTypedLists = useTypedLists;
        return this;
    }

    public SchemaGen setOntologyImports(String... ontologyImports) {
        this.ontologyImports = ontologyImports;
        return this;
    }

    public String getOntology() {
        return ontology;
    }

}