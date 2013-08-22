package foaf;
@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Agent extends wordnet.Agent_3 {
    @Predicate(ln="aimChatID")
    private String foafAimChatID;
    @Predicate(ln="birthday")
    private String foafBirthday;
    @Predicate(ln="gender")
    private String foafGender;
    @Predicate(ln="holdsAccount")
    private OnlineAccount foafHoldsAccount;
    @Predicate(ln="icqChatID")
    private String foafIcqChatID;
    @Predicate(ln="jabberID")
    private String foafJabberID;
    @Predicate(ln="made")
    private owl.Thing foafMade;
    @Predicate(ln="mbox")
    private owl.Thing foafMbox;
    @Predicate(ln="mbox_sha1sum")
    private String foafMbox_sha1sum;
    @Predicate(ln="msnChatID")
    private String foafMsnChatID;
    @Predicate(ln="openid")
    private Document foafOpenid;
    @Predicate(ln="tipjar")
    private Document foafTipjar;
    @Predicate(ln="weblog")
    private Document foafWeblog;
    @Predicate(ln="yahooChatID")
    private String foafYahooChatID;
}
