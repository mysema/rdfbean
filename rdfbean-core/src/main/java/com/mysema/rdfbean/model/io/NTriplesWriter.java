package com.mysema.rdfbean.model.io;

import java.io.IOException;
import java.io.Writer;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.RepositoryException;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

/**
 * @author tiwe
 * 
 */
public class NTriplesWriter implements RDFWriter {

    private final Writer writer;

    public NTriplesWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void handle(STMT stmt) {
        try {
            writer.append(toString(stmt.getSubject()));
            writer.append(" ");
            writer.append(toString(stmt.getPredicate()));
            writer.append(" ");
            writer.append(toString(stmt.getObject()));
            writer.append(" .\n");
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public static String toString(STMT stmt) {
        return toString(stmt.getSubject()) + " "
                + toString(stmt.getPredicate()) + " "
                + toString(stmt.getObject()) + " . ";
    }

    public static String toString(NODE node) {
        if (node.isURI()) {
            return toString(node.asURI());
        } else if (node.isLiteral()) {
            return toString(node.asLiteral());
        } else {
            return toString(node.asBNode());
        }
    }

    public static String toString(UID uid) {
        return "<" + NTriplesUtil.escapeString(uid.getValue()) + ">";
    }

    public static String toString(LIT lit) {
        String value = "\"" + NTriplesUtil.escapeString(lit.getValue()) + "\"";
        if (lit.getLang() != null) {
            return value + "@" + LocaleUtil.toLang(lit.getLang());
        } else {
            return value + "^^" + toString(lit.getDatatype());
        }
    }

    public static String toString(BID bid) {
        return "_:" + bid.getValue();
    }
}
