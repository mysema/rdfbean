package blog;
@ClassMapping(ns="http://www.mysema.com/semantics/blog/#")
public class Comment {
    @Predicate
    private Article article;
    @Predicate
    private User author;
    @Predicate
    private org.joda.time.DateTime created;
    @Predicate
    private String text;
    @Predicate
    private String title;
}
