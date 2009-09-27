package blog;
@ClassMapping(ns="http://www.mysema.com/semantics/blog/#")
public class Article {
    @Predicate
    private User author;
    @Predicate
    private org.joda.time.DateTime created;
    @Predicate
    private org.joda.time.DateTime edited;
    @Predicate
    private String text;
    @Predicate
    private String title;
}
