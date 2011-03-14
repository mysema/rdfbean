/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.mysema.rdfbean.model.RDF;
import com.mysema.rdfbean.model.RDFS;
import com.mysema.rdfbean.model.Repository;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.MappedClass;
import com.mysema.rdfbean.object.MappedPath;
import com.mysema.rdfbean.object.MappedPredicate;
import com.mysema.rdfbean.object.MappedProperty;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionFactoryImpl;
import com.mysema.rdfbean.owl.DatatypeProperty;
import com.mysema.rdfbean.owl.OWL;
import com.mysema.rdfbean.owl.OWLClass;
import com.mysema.rdfbean.owl.ObjectProperty;
import com.mysema.rdfbean.owl.Ontology;
import com.mysema.rdfbean.owl.Restriction;
import com.mysema.rdfbean.owl.TypedList;
import com.mysema.rdfbean.rdfs.RDFProperty;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSDatatype;
import com.mysema.rdfbean.rdfs.RDFSResource;
import com.mysema.rdfbean.xsd.ConverterRegistry;
import com.mysema.rdfbean.xsd.ConverterRegistryImpl;


/**
 * @author sasa
 *
 */
public class SchemaGen {

    private Configuration configuration;

    private Repository repository;

    private final ConverterRegistry converterRegistry = new ConverterRegistryImpl();

    private final Set<String> exportNamespaces = new HashSet<String>();

    private boolean useTypedLists = true;

    private String ontology;

    private String[] ontologyImports;

    public void exportConfiguration() {
        SessionFactoryImpl sessionFactory = new SessionFactoryImpl();
        sessionFactory.setConfiguration(new DefaultConfiguration(RDFSClass.class.getPackage(), OWLClass.class.getPackage()));
        sessionFactory.setRepository(repository);
        sessionFactory.initialize();
        Session session = sessionFactory.openSession();
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

        // process classes
        for (MappedClass mappedClass : configuration.getMappedClasses()) {
            processClass(session, mappedClass.getJavaClass(), resources);
        }

        // save resources
        session.saveAll(resources.values().toArray());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private RDFSClass<RDFSResource> processClass(Session session, Class<?> clazz, Map<UID, RDFSResource> resources) {
        MappedClass mappedClass = configuration.getMappedClass(clazz);
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
                if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)){
                    addParent(clazz.getSuperclass(), owlClass, session, resources);
                }

                // interfaces
                for (Class<?> iface : clazz.getInterfaces()) {
                    addParent(iface, owlClass, session, resources);
                }

                // enum instances
                if (mappedClass.isEnum()) {
                    List<RDFSResource> oneOf = new ArrayList<RDFSResource>();
                    for (Object constant: clazz.getEnumConstants()) {
                        Enum enumValue = (Enum)constant;
                        oneOf.add(new AnyThing(new UID(cuid.ns(), enumValue.name()), owlClass, enumValue.ordinal()));
                    }
                    owlClass.setOneOf(oneOf);
                }

                // properties
                for (MappedPath mappedPath : mappedClass.getProperties()) {
                    if (!mappedPath.isInherited() && mappedPath.isSimpleProperty()) {
                        processProperty(session, resources, owlClass, cuid, mappedPath);
                    }
                }
            }
        }
        return owlClass;
    }

    @SuppressWarnings("unchecked")
    private void processProperty(Session session, Map<UID, RDFSResource> resources, OWLClass owlClass, UID cuid, MappedPath mappedPath) {
        MappedProperty<?> mappedProperty = mappedPath.getMappedProperty();
        MappedPredicate mappedPredicate = mappedPath.get(0);
        UID puid = mappedPredicate.getUID();
        String predicateNs = puid.ns();
        // No restrictions on predicates from built-in namespaces
        if (RDF.NS.equals(predicateNs) || RDFS.NS.equals(predicateNs) || OWL.NS.equals(predicateNs)) {
           return;
        }
        final RDFProperty property;
        boolean seenProperty = resources.containsKey(puid);

        if (seenProperty) {
            property = (RDFProperty) resources.get(puid);
            if (mappedPath.isReference()) {
                if (!(property instanceof ObjectProperty)) {
                    throw new IllegalArgumentException("Expected ObjectProperty for: " + mappedPath);
                }
            } else  if (!mappedProperty.isAnyResource()){
                if (!(property instanceof DatatypeProperty)) {
                    throw new IllegalArgumentException("Expected DatatypeProperty for: " + mappedPath);
                }
            }

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

        final Restriction restriction = new Restriction();
        restriction.setOnProperty(property);

        // TODO range mismatches!
        if (mappedProperty.isCollection()) {
            if (mappedProperty.isList()) {
                if (property.getRange().isEmpty()) {
                    RDFSClass<?> componentType =
                        processClass(session, mappedProperty.getComponentType(), resources);
                    if (useTypedLists && componentType != null) {
                        property.addRange(new TypedList(cuid.ns(), componentType));
                        // Protege doesn't support typed lists using allValuesFrom
                        //owlClass.setAllValuesFrom(property, new TypedList(cuid.ns(), componentType));
                    } else {
                        owlClass.setAllValuesFrom(property, (RDFSClass<RDFSResource>) resources.get(RDF.List));
                    }
                    restriction.setMaxCardinality(1);
                }
            } else {
                RDFSClass<?> componentType =
                    processClass(session, mappedProperty.getComponentType(),
                            resources);
                if (componentType != null) {
                    restriction.setAllValuesFrom(componentType);
                }
            }

        // reference
        } else if (mappedPath.isReference()) {
            if (property.getRange().isEmpty()) {
                RDFSClass<?> range = processClass(session,
                        mappedProperty.getType(), resources);
                if (range != null) {
                    owlClass.setAllValuesFrom(property, range);
                } else if (mappedProperty.isAnyResource()) {
                    owlClass.setAllValuesFrom(property, (RDFSClass<?>) resources.get(RDFS.Resource));
                }
            }
            restriction.setMaxCardinality(1);

        // other
        } else {
            if (property.getRange().isEmpty()) {
                UID range;
                if (mappedProperty.isAnyResource()) {
                    range = RDFS.Resource;
                }else if (mappedProperty.isLocalized()){
                    range = RDF.text;
                } else {
                    range = converterRegistry.getDatatype(mappedProperty.getType());
                }
                if (range != null) {
                    owlClass.setAllValuesFrom(property, getDatatype(range, resources));
                }
            }
            restriction.setMaxCardinality(1);
        }
        if (restriction.isDefined()) {
            owlClass.addSuperClass(restriction);
        }

        if (mappedProperty.isRequired()) {
            final Restriction minCardinality = new Restriction();
            minCardinality.setOnProperty(property);
            minCardinality.setMinCardinality(1);
            owlClass.addSuperClass(minCardinality);
        }
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

    private void addParent(Class<?> clazz, OWLClass owlClass, Session session, Map<UID, RDFSResource> resources) {
        RDFSClass<RDFSResource> parent = processClass(session, clazz, resources);
        if (parent != null) {
            owlClass.addSuperClass(parent);
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

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
