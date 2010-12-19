package com.mysema.rdfbean.model.io;

import com.mysema.commons.l10n.support.LocaleUtil;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.NODE;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;

public final class NTriplesUtil {
    
    public static String toString(STMT stmt){
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
    
    public static String toString(UID uid){
        return "<" + escapeString(uid.getValue()) + ">";
    }
    
    public static String toString(LIT lit){
        String value = "\"" + escapeString(lit.getValue()) + "\"";
        if (lit.getLang() != null) {
            return value + "@" + LocaleUtil.toLang(lit.getLang());
        } else {
            return value + "^^" + toString(lit.getDatatype());
        }
    }
    
    public static String toString(BID bid){
        return "_:" + bid.getValue();
    }

    public static String escapeString(String label) {
        int labelLength = label.length();
        StringBuilder sb = new StringBuilder(2 * labelLength);

        for (int i = 0; i < labelLength; i++) {
            char c = label.charAt(i);
            int cInt = c;

            if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '"') {
                sb.append("\\\"");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else if (cInt >= 0x0 && cInt <= 0x8 || cInt == 0xB || cInt == 0xC
                    || cInt >= 0xE && cInt <= 0x1F || cInt >= 0x7F
                    && cInt <= 0xFFFF) {
                sb.append("\\u");
                sb.append(toHexString(cInt, 4));
            } else if (cInt >= 0x10000 && cInt <= 0x10FFFF) {
                sb.append("\\U");
                sb.append(toHexString(cInt, 8));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static String toHexString(int decimal, int stringLength) {
        StringBuilder sb = new StringBuilder(stringLength);
        String hexVal = Integer.toHexString(decimal).toUpperCase();
        // insert zeros if hexVal has less than stringLength characters:
        int nofZeros = stringLength - hexVal.length();
        for (int i = 0; i < nofZeros; i++) {
            sb.append('0');
        }
        sb.append(hexVal);
        return sb.toString();
    }    

    private NTriplesUtil() {
    }


}
