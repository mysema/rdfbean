package blog;
@ClassMapping(ns="http://www.mysema.com/semantics/blog/#")
public class User {
    @Predicate
    private String email;
    @Predicate
    private String firstName;
    @Predicate
    private String lastName;
    @Predicate
    private String shortName;
}
