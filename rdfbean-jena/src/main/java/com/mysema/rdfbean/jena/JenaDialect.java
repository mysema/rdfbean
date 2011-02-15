package com.mysema.rdfbean.jena;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Quad;
import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.*;

/**
 * @author tiwe
 *
 */
public class JenaDialect extends AbstractDialect<Node, Node, Node, Node, Node, Quad>{

    private static final Map<UID, RDFDatatype> datatypes = new HashMap<UID, RDFDatatype>();

    static{
        for (UID uid : Nodes.get(XSD.NS)){
            datatypes.put(uid, new BaseDatatype(uid.getId()));
        }
    }

    @Override
    public Node createBNode() {
        return Node.createAnon();
    }

    @Override
    public Quad createStatement(Node subject, Node predicate, Node object) {
        return new Quad(Quad.defaultGraphIRI, subject, predicate, object);
    }

    @Override
    public Quad createStatement(Node subject, Node predicate, Node object, Node context) {
        return new Quad(context, subject, predicate, object);
    }

    @Override
    public BID getBID(Node bnode) {
        return new BID(bnode.getBlankNodeLabel());
    }

    @Override
    public Node getBNode(BID bid) {
        return Node.createAnon(new AnonId(bid.getId()));
    }

    @Override
    public ID getID(Node resource) {
        if (resource.isURI()){
            return new UID(resource.getURI());
        }else{
            return new BID(resource.getBlankNodeLabel());
        }
    }

    @Override
    public LIT getLIT(Node literal) {
        if (!StringUtils.isEmpty(literal.getLiteralLanguage())){
            return new LIT(literal.getLiteralLexicalForm(), literal.getLiteralLanguage());
        }else{
            String datatype = literal.getLiteralDatatypeURI();
            if (datatype == null || datatype.equals(RDF.text.getId())){
                return new LIT(literal.getLiteralLexicalForm(), RDF.text);
            }else {
                return new LIT(literal.getLiteralLexicalForm(), getDatatypeUID(datatype));
            }
        }
    }

    @Override
    public Node getLiteral(LIT lit) {
        if (lit.getLang() != null){
            return Node.createLiteral(lit.getValue(), LocaleUtil.toLang(lit.getLang()), false);
        }else if (!lit.getDatatype().equals(RDF.text)){
            if (lit.getDatatype().getNamespace().equals(XSD.NS)){
                return Node.createLiteral(lit.getValue(), null, datatypes.get(lit.getDatatype()));
            }else{
                return Node.createLiteral(lit.getValue(), null, new BaseDatatype(lit.getDatatype().getId()));
            }
        }else{
            return Node.createLiteral(lit.getValue(), null, null);
        }
    }

    @Override
    public NODE getNODE(Node node) {
        if (node.isURI()){
            return getUID(node);
        }else if (node.isBlank()){
            return getBID(node);
        }else{
            return getLIT(node);
        }
    }
    @Override
    public NodeType getNodeType(Node node) {
        if (node.isURI()){
            return NodeType.URI;
        }else if (node.isBlank()){
            return NodeType.BLANK;
        }else if (node.isLiteral()){
            return NodeType.LITERAL;
        }else{
            throw new IllegalArgumentException(node.toString());
        }
    }

    @Override
    public Node getObject(Quad statement) {
        return statement.getObject();
    }

    @Override
    public Node getPredicate(Quad statement) {
        return statement.getPredicate();
    }

    @Override
    public Node getSubject(Quad statement) {
        return statement.getSubject();
    }

    @Override
    public UID getUID(Node resource) {
        return new UID(resource.getURI());
    }

    @Override
    public Node getURI(UID uid) {
        return Node.createURI(uid.getValue());
    }

}
