package terms;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://purl.org/dc/terms/")
public class DCCollection {

    @Predicate
    private DCMethodOfAccrual accrualMethod;

    @Predicate
    private DCFrequency accrualPeriodicity;

    @Predicate
    private DCPolicy accrualPolicy;

    public DCMethodOfAccrual getAccrualMethod(){
        return accrualMethod;
    }

    public void setAccrualMethod(DCMethodOfAccrual accrualMethod){
        this.accrualMethod = accrualMethod;
    }

    public DCFrequency getAccrualPeriodicity(){
        return accrualPeriodicity;
    }

    public void setAccrualPeriodicity(DCFrequency accrualPeriodicity){
        this.accrualPeriodicity = accrualPeriodicity;
    }

    public DCPolicy getAccrualPolicy(){
        return accrualPolicy;
    }

    public void setAccrualPolicy(DCPolicy accrualPolicy){
        this.accrualPolicy = accrualPolicy;
    }

}
