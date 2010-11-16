package blog;
@ClassMapping(ns="http://www.mysema.com/semantics/blog/#")
public class Blogger extends User {
    @Predicate
    private String description;
}
