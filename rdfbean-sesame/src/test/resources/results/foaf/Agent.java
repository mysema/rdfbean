package foaf;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Agent extends wordnet.Agent-3 {

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

    public String getFoafAimChatID(){
        return foafAimChatID;
    }

    public void setFoafAimChatID(String foafAimChatID){
        this.foafAimChatID = foafAimChatID;
    }

    public String getFoafBirthday(){
        return foafBirthday;
    }

    public void setFoafBirthday(String foafBirthday){
        this.foafBirthday = foafBirthday;
    }

    public String getFoafGender(){
        return foafGender;
    }

    public void setFoafGender(String foafGender){
        this.foafGender = foafGender;
    }

    public OnlineAccount getFoafHoldsAccount(){
        return foafHoldsAccount;
    }

    public void setFoafHoldsAccount(OnlineAccount foafHoldsAccount){
        this.foafHoldsAccount = foafHoldsAccount;
    }

    public String getFoafIcqChatID(){
        return foafIcqChatID;
    }

    public void setFoafIcqChatID(String foafIcqChatID){
        this.foafIcqChatID = foafIcqChatID;
    }

    public String getFoafJabberID(){
        return foafJabberID;
    }

    public void setFoafJabberID(String foafJabberID){
        this.foafJabberID = foafJabberID;
    }

    public owl.Thing getFoafMade(){
        return foafMade;
    }

    public void setFoafMade(owl.Thing foafMade){
        this.foafMade = foafMade;
    }

    public owl.Thing getFoafMbox(){
        return foafMbox;
    }

    public void setFoafMbox(owl.Thing foafMbox){
        this.foafMbox = foafMbox;
    }

    public String getFoafMbox_sha1sum(){
        return foafMbox_sha1sum;
    }

    public void setFoafMbox_sha1sum(String foafMbox_sha1sum){
        this.foafMbox_sha1sum = foafMbox_sha1sum;
    }

    public String getFoafMsnChatID(){
        return foafMsnChatID;
    }

    public void setFoafMsnChatID(String foafMsnChatID){
        this.foafMsnChatID = foafMsnChatID;
    }

    public Document getFoafOpenid(){
        return foafOpenid;
    }

    public void setFoafOpenid(Document foafOpenid){
        this.foafOpenid = foafOpenid;
    }

    public Document getFoafTipjar(){
        return foafTipjar;
    }

    public void setFoafTipjar(Document foafTipjar){
        this.foafTipjar = foafTipjar;
    }

    public Document getFoafWeblog(){
        return foafWeblog;
    }

    public void setFoafWeblog(Document foafWeblog){
        this.foafWeblog = foafWeblog;
    }

    public String getFoafYahooChatID(){
        return foafYahooChatID;
    }

    public void setFoafYahooChatID(String foafYahooChatID){
        this.foafYahooChatID = foafYahooChatID;
    }

}
