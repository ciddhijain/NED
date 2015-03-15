import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DBUtils {

    public DB db;

    public DBCollection orgsColl;
    public DBCollection articlesColl;
    public DBCollection peopleColl;
    public DBCollection locsColl;
    public DBCollection manCurColl;

    public DBUtils(String dbName) throws Exception {
        MongoClient client = new MongoClient();
        db = client.getDB(dbName);
        System.out.println("Connected to DB: " + db.getName());

        // Collections Connections
        articlesColl = db.getCollection("articles");
        orgsColl = db.getCollection("orgs");
        peopleColl = db.getCollection("people");
        locsColl = db.getCollection("locs");
        manCurColl = db.getCollection("manucur");

        // Initialize the Abbr. Dictionary
        //LocsUtils.create_dictionary();
    }

    public void updateOrgs(String name, double ocscr, Object articleId) {
        Object orgId = getOrgId(name);

        BasicDBObject articleQuery = new BasicDBObject("_id", articleId);
        BasicDBObject articleUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("orgs", new BasicDBObject("_id", orgId).append("ocscr", ocscr)));

        BasicDBObject orgQuery = new BasicDBObject("_id", orgId);
        BasicDBObject orgUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("articles", new BasicDBObject("_id", articleId).append("ocscr", ocscr)));

        orgsColl.update(orgQuery, orgUpdate);
        articlesColl.update(articleQuery, articleUpdate);
    }

    /**
     * The function updates freebase parent of a company and adds it as a
     * subsidiary for the same parent. It ensures that the parent name is not
     * same as the company name.
     */
    public void updateOrgs_usingFreeBase(String name, String parent) {

    	String nameMatch = "";
        String parentMatch = "";
        StringTokenizer st1 = new StringTokenizer(name,
                OrgsUtils.DELIMITERS);
        while (st1.hasMoreTokens()) {
            String next = st1.nextToken();
            nameMatch = nameMatch.concat(next);
        }

        StringTokenizer st2 = new StringTokenizer(parent,
                OrgsUtils.DELIMITERS);
        while (st2.hasMoreTokens()) {
            String next = st2.nextToken();
            parentMatch = parentMatch.concat(next);
        }

        // To ensure that company does not get mapped to itself
        if (!(nameMatch.equalsIgnoreCase(parentMatch))) {
        	System.out.println("Updating record for company : " + name);
        	System.out.println("Adding to parent : " + parent);
            Object orgId = getOrgId(name);
            Object parentId = getOrgId(parent);

            int exists = 0;

            ArrayList<Object> remove = new ArrayList<Object>();
            BasicDBObject orgQuery = new BasicDBObject("_id", orgId);
            DBObject o = orgsColl.findOne(orgQuery);
            BasicDBList orgs = (BasicDBList) (o.get("orgs"));
            if (!(orgs == null)) {
                for (Object e : orgs) {
                    String rtype = (String) ((DBObject) e).get("rtype");
                    if (rtype.equalsIgnoreCase("freebase_parent")) {
                        exists = 1;
                        remove.add(e);
                        // System.out.println("Removing existing entry for freebase_parent");
                        // break;
                    }
                }
                if (exists == 1) {
                    for (int i = 0; i < remove.size(); i++) {
                        String remove_name = (String) ((DBObject) remove.get(i))
                                .get("name");
                        BasicDBObject p_org = new BasicDBObject("$pull",
                                new BasicDBObject("orgs", new BasicDBObject(
                                        "name", remove_name).append("rtype",
                                        "freebase_parent")));
                        orgsColl.update(orgQuery, p_org);
                        // orgs.remove(remove.get(i));
                    }
                }
            }

            BasicDBObject orgUpdate = new BasicDBObject("$addToSet",
                    new BasicDBObject("orgs",
                            new BasicDBObject("_id", parentId).append("name",
                                    parent).append("rtype", "freebase_parent")));

            BasicDBObject parentQuery = new BasicDBObject("_id", parentId);
            BasicDBObject parentUpdate = new BasicDBObject("$addToSet",
                    new BasicDBObject("orgs", new BasicDBObject("_id", orgId)
                            .append("name", name).append("rtype",
                                    "freebase_subsidiary")));

            orgsColl.update(orgQuery, orgUpdate);
            orgsColl.update(parentQuery, parentUpdate);
        }
    }

    public Object getOrgId(String name) {
        BasicDBObject query = new BasicDBObject("iname", name.toLowerCase());
        BasicDBObject fields = new BasicDBObject("_id", 1);

        DBObject doc = orgsColl.findOne(query, fields);

        Object orgId = new Object();

        if (doc == null) {
            orgsColl.insert(query.append("isChecked", false).append("name", name));
            fields = new BasicDBObject("name", 1).append("orgs", 1);
            DBObject orgObj = orgsColl.findOne(query, fields);
            try {
                OrgsUtils.processOrg(orgObj, this);   // TODO
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            orgId = orgObj.get("_id");
        } else {
            orgId = doc.get("_id");
        }
        return orgId;
    }

    /**
     * The function removes a given subsidiary from parent
     */
    public void updateParent(String name, String parent) {
        Object parentId = getOrgId(parent);
        BasicDBObject parentQuery = new BasicDBObject("_id", parentId);

        DBObject o_parent = orgsColl.findOne(parentQuery);
        ArrayList<Object> remove_p = new ArrayList<Object>();
        BasicDBList orgs_parent = (BasicDBList) (o_parent.get("orgs"));
        if (!(orgs_parent == null)) {
            for (Object e : orgs_parent) {
                String rtype = (String) ((DBObject) e).get("rtype");
                String subsidiary = (String) ((DBObject) e).get("name");
                if (rtype.equalsIgnoreCase("freebase_subsidiary")
                        && subsidiary.equalsIgnoreCase(name)) {
                    remove_p.add(e);
                }
            }
        }
        for (int i = 0; i < remove_p.size(); i++) {
            String remove_name = (String) ((DBObject) remove_p.get(i))
                    .get("name");
            System.out.println("Removing Company : " + remove_name + " from previous parent : " + parent);
            BasicDBObject s_org = new BasicDBObject("$pull", new BasicDBObject(
                    "orgs", new BasicDBObject("name", remove_name).append(
                    "rtype", "freebase_subsidiary")));
            orgsColl.update(parentQuery, s_org);
        }
    }

    /**
     * The function returns documents that have the maximum prefix match with
     * the given company name
     */
    public MatchDocDetails getMatchDocs(String name) {
        StringTokenizer st = new StringTokenizer(name,
                OrgsUtils.DELIMITERS);
        String Delimiters = "([ .,-\\/']+|$)";
        String regex = "^";
        DBCursor c = null;
        DBCursor c_old = null;
        String maxMatch = "";
        String maxMatch_old = "";
        int l = 0;
        int first = 0;
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (first == 0) {
                maxMatch = next;
                first = 1;
            } else {
                maxMatch = maxMatch.concat(" ".concat(next));
            }
            regex = regex.concat(next.concat(Delimiters));
            BasicDBObject query = new BasicDBObject("name", new BasicDBObject(
                    "$regex", regex.concat(".*")).append("$options", "i"));
            c = orgsColl.find(query);
            l = c.count();
            if (l > 1) {
                c_old = c;
                maxMatch_old = maxMatch;
            } else {
                break;
            }
        }
        if (c != null) {
            c.close();
        }

        MatchDocDetails matchDocDetails = new MatchDocDetails(c_old, maxMatch_old);
        return matchDocDetails;
    }

    /**
     * The function returns an existing freebase parent, which forms the longest
     * prefix of the given company name
     */
    public DBObject getExistingMatch(String name) {
        StringTokenizer st = new StringTokenizer(name,
                OrgsUtils.DELIMITERS);
        String Delimiters = "([ .,-\\/']+|$)";
        String regex = "^";
        String compare = "";
        DBCursor c;
        DBObject parent = null;
        int first = 0;

        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            regex = regex.concat(next.concat(Delimiters));
            if (first == 0) {
                first = 1;
                compare = next;
            } else {
                compare = compare.concat(" ".concat(next));
            }
            BasicDBObject query = new BasicDBObject("name", new BasicDBObject(
                    "$regex", regex.concat(".*")).append("$options", "i"));
            c = orgsColl.find(query);
            if (!(c == null) && c.count() > 1) {
                while (c.hasNext()) {
                    DBObject o = c.next();
                    String s = (String) o.get("name");

                    // It should not be the same as supplied
                    if (!(name.equalsIgnoreCase(s))) {
                        s = OrgsUtils.cleanOrganization(s); // May be the one added
                        // from freebase
                        StringTokenizer st1 = new StringTokenizer(s,
                                OrgsUtils.DELIMITERS);
                        String s_compare = "";
                        int f = 0;
                        while (st1.hasMoreTokens()) {
                            String n = st1.nextToken();
                            if (f == 0) {
                                f = 1;
                                s_compare = n;
                            } else {
                                s_compare = s_compare.concat(" ".concat(n));
                            }
                        }
                        if (compare.equalsIgnoreCase(s_compare)) {
                            BasicDBList orgs = (BasicDBList) o.get("orgs");
                            if (!(orgs == null)) {
                                for (Object e : orgs) {
                                    String rtype = (String) ((DBObject) e)
                                            .get("rtype");
                                    if (rtype
                                            .equalsIgnoreCase("freebase_subsidiary")) {
                                        parent = o;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (c != null) {
                c.close();
            }
        }

        return parent;
    }
    
    public void markOrgChecked(String name){
    	System.out.println("Marking as done: " + name);
    	Object orgId = getOrgId(name);
		BasicDBObject orgQuery = new BasicDBObject("_id", orgId);
		BasicDBObject orgUpdate = new BasicDBObject("$set", new BasicDBObject("isChecked", true));
		orgsColl.update(orgQuery, orgUpdate);
	}

    public void updatePeople(String name, double ocscr, Object articleId) {
        Object persId = getPersId(name);

        BasicDBObject articleQuery = new BasicDBObject("_id", articleId);
        BasicDBObject articleUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("people", new BasicDBObject("_id", persId).append("ocscr", ocscr)));

        BasicDBObject peopleQuery = new BasicDBObject("_id", persId);
        BasicDBObject peopleUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("articles", new BasicDBObject("_id", articleId).append("ocscr", ocscr)));

        peopleColl.update(peopleQuery, peopleUpdate);
        articlesColl.update(articleQuery, articleUpdate);
    }

    private Object getPersId(String name) {
        BasicDBObject query = new BasicDBObject("name", name);
        BasicDBObject fields = new BasicDBObject("_id", 1);

        DBObject doc = peopleColl.findOne(query, fields);

        Object persId;

        if (doc == null) {
            peopleColl.insert(query);
            persId = peopleColl.findOne(query, fields).get("_id");
        } else {
            persId = doc.get("_id");
        }

        return persId;
    }

    public void updateLocs(String name, double ocscr, Object articleId) {
        /*name = LocsUtils.expand(name);  // To handle Abbreviations
        Object locId = getLocId(name);


        BasicDBObject articleQuery = new BasicDBObject("_id", articleId);
        BasicDBObject articleUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("locs", new BasicDBObject("_id", locId).append("ocscr", ocscr)));

        BasicDBObject locQuery = new BasicDBObject("_id", locId);
        BasicDBObject locUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("articles", new BasicDBObject("_id", articleId).append("ocscr", ocscr)));
        locsColl.update(locQuery, locUpdate);
        articlesColl.update(articleQuery, articleUpdate);*/
    }

    public Object getLocId(String locName) {

        BasicDBObject query = new BasicDBObject("name", locName);
        BasicDBObject fields = new BasicDBObject("_id", 1);

        DBObject doc = locsColl.findOne(query, fields);

        Object locId = null;
/*
        if (doc == null) {
            locsColl.insert(query);
            locId = locsColl.findOne(query, fields).get("_id");

            String parentName = LocsUtils.getParentLoc(locName); // TODO Add abbr. handler code
            if (parentName != null) {
                Object parentId = null;
                if (!(parentName.equalsIgnoreCase("Earth"))) {
                    parentId = getLocId(parentName);
                }
                if (parentId != null) {
                    locsColl.update(new BasicDBObject("_id", locId),
                            new BasicDBObject("$addToSet", new BasicDBObject("locs", new BasicDBObject("_id", parentId).append("name", parentName).append("rtype", "parent"))));
                    locsColl.update(new BasicDBObject("_id", parentId),
                            new BasicDBObject("$addToSet", new BasicDBObject("locs", new BasicDBObject("_id", locId).append("name", locName).append("rtype", "child"))));
                }
            }
        } else {
            locId = doc.get("_id");
        }*/

        return locId;
    }

    public void addToManCuration(String content, double ocscr, Object articleId) {
        Object manId = getManId(content);

        BasicDBObject manCurQuery = new BasicDBObject("_id", manId);
        BasicDBObject manCurUpdate = new BasicDBObject("$addToSet",
                new BasicDBObject("articles", new BasicDBObject("_id", articleId).append("ocscr", ocscr)));

        manCurColl.update(manCurQuery, manCurUpdate);
    }

    private Object getManId(String name) {
        BasicDBObject query = new BasicDBObject("name", name);
        BasicDBObject fields = new BasicDBObject("_id", 1);

        DBObject doc = manCurColl.findOne(query, fields);

        Object newId;

        if (doc == null) {
            manCurColl.insert(query);
            newId = manCurColl.findOne(query, fields).get("_id");
        } else {
            newId = doc.get("_id");
        }

        return newId;
    }


}