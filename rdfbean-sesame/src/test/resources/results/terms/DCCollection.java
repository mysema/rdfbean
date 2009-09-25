package terms;
@ClassMapping(ns="http://purl.org/dc/terms/",ln="Collection")
public class DCCollection {
    @Predicate
    private DCMethodOfAccrual accrualMethod;
    @Predicate
    private DCFrequency accrualPeriodicity;
    @Predicate
    private DCPolicy accrualPolicy;
}
