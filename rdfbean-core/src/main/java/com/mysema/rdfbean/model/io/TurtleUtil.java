package com.mysema.rdfbean.model.io;

public final class TurtleUtil {
    
    public static boolean isPrefixChar(int c) {
        return isNameChar(c);
    }
    
    public static boolean isPrefixStartChar(int c) {
        return
        Character.isLetter(c) ||
        c >= 0x00C0 && c <= 0x00D6 ||
        c >= 0x00D8 && c <= 0x00F6 ||
        c >= 0x00F8 && c <= 0x02FF ||
        c >= 0x0370 && c <= 0x037D ||
        c >= 0x037F && c <= 0x1FFF ||
        c >= 0x200C && c <= 0x200D ||
        c >= 0x2070 && c <= 0x218F ||
        c >= 0x2C00 && c <= 0x2FEF ||
        c >= 0x3001 && c <= 0xD7FF ||
        c >= 0xF900 && c <= 0xFDCF ||
        c >= 0xFDF0 && c <= 0xFFFD ||
        c >= 0x10000 && c <= 0xEFFFF;
    }

    public static boolean isNameStartChar(int c) {
        return c == '_' || isPrefixStartChar(c);
    }

    public static boolean isNameChar(int c) {
        return
        isNameStartChar(c) ||
        Character.isDigit(c) ||
        c == '-' ||
        c == 0x00B7 ||
        c >= 0x0300 && c <= 0x036F ||
        c >= 0x203F && c <= 0x2040;
    }

}