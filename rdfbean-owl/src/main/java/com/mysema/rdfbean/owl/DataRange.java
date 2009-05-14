/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.owl;

import com.mysema.rdfbean.annotations.ClassMapping;
import com.mysema.rdfbean.annotations.Predicate;
import com.mysema.rdfbean.model.XSD;
import com.mysema.rdfbean.rdfs.RDFSClass;
import com.mysema.rdfbean.rdfs.RDFSDatatype;

/**
 * @author sasa
 *
 */
// TODO: What is the relation between owl:DataRange and rdfs:Datatype?!?
@ClassMapping(ns=OWL.NS)
public class DataRange extends RDFSClass<Object> {
    
    @ClassMapping(ns=XSD.NS)
    public static enum WhiteSpaceOption {
        collapse,
        preserve,
        replace
    }
    
    @Predicate(ns=XSD.NS)
    private Integer fractionDigits;
    
    @Predicate(ns=XSD.NS)
    private Integer length;
    
    @Predicate(ns=XSD.NS)
    private Long maxExclusive;
    
    // NOTE: applies also on owl:dateTime
    @Predicate(ns=XSD.NS)
    private Long maxInclusive;
    
    @Predicate(ns=XSD.NS)
    private Integer maxLength;

	@Predicate(ns=XSD.NS)
    private Long minExclusive;

	// NOTE: applies also on owl:dateTime
    @Predicate(ns=XSD.NS)
    private Long minInclusive;

	@Predicate(ns=XSD.NS)
    private Integer minLength;

	@Predicate(ns=OWL.NS)
    private RDFSDatatype onDataRange;

	@Predicate(ns=XSD.NS)
    private String pattern;

	@Predicate(ns=XSD.NS)
    private Integer totalDigits;

	@Predicate(ns=XSD.NS)
    private WhiteSpaceOption whiteSpace;

	public DataRange() {}

	public Integer getFractionDigits() {
        return fractionDigits;
    }

	public Integer getLength() {
        return length;
    }

	public Long getMaxExclusive() {
        return maxExclusive;
    }

	public Long getMaxInclusive() {
        return maxInclusive;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public Long getMinExclusive() {
        return minExclusive;
    }

    public Long getMinInclusive() {
        return minInclusive;
    }

    public Integer getMinLength() {
        return minLength;
    }
    
    public RDFSDatatype getOnDataRange() {
        return onDataRange;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public Integer getTotalDigits() {
        return totalDigits;
    }
    
    public WhiteSpaceOption getWhiteSpace() {
        return whiteSpace;
    }
    
    public void setFractionDigits(Integer fractionDigits) {
		this.fractionDigits = fractionDigits;
	}
    
    public void setLength(Integer length) {
		this.length = length;
	}
    
    
    public void setMaxExclusive(Long maxExclusive) {
		this.maxExclusive = maxExclusive;
	}
    
    public void setMaxInclusive(Long maxInclusive) {
		this.maxInclusive = maxInclusive;
	}

    public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

    public void setMinExclusive(Long minExclusive) {
		this.minExclusive = minExclusive;
	}

    public void setMinInclusive(Long minInclusive) {
		this.minInclusive = minInclusive;
	}

    public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

    public void setOnDataRange(RDFSDatatype onDataRange) {
		this.onDataRange = onDataRange;
	}

    public void setPattern(String pattern) {
		this.pattern = pattern;
	}

    public void setTotalDigits(Integer totalDigits) {
		this.totalDigits = totalDigits;
	}

    public void setWhiteSpace(WhiteSpaceOption whiteSpace) {
		this.whiteSpace = whiteSpace;
	}
    
}
