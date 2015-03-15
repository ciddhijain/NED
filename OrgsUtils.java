import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class OrgsUtils {

    public static int max_compare = 5;
    public static int go_back = 0;
    public static String API_KEY = "AIzaSyAai4HNP4_PVIUrF_2DQuy26U9osg7242c";
    // public static String API_KEY = "AIzaSyCWCV8r8KpHSM3uZoA9nT2a64mbqQP19mg";
    // public static String API_KEY = "AIzaSyBEkKph1EGyjakxIDfORqLAsOWaJUf-pvE";
    // public static String API_KEY = "AIzaSyBqdBJPgkJCVO7AJ5LK4M1L-KS5XNzdZys";
    // public static String TAG = "SEARCH_FREEBASE_MAX_MATCH";
    // public static String TAG1 = "MAX_MATCH_SEARCH";
    // public static String TAG2 = "ADD_TO_EXISTING";
    public static String DELIMITERS = " .,-/'";
    private static int wait_seconds = 30;

    public static String cleanOrganization(String org) {
        org = org.replaceAll(
                "((?<=\\s)-(?!\\s)|(?<!\\s)-(?=\\s)|(?<!\\s)-(?!\\s))", " ");
        org = org
                .replaceAll(
                        "^[*!@#$%^&,).+=~`{}|\\:;\"'<>?/ ]|[*!@#$%^&,(.+=~`{}|\\:;\"'<>?/ ]$",
                        "");
        String org1;
        do {
            org1 = org;
            org = org.replaceAll("^[*!@#$%^&,).+=~`{}|\\:;\"'<>?/ ]|[*!@#$%^&,(.+=~`{}|\\:;\"'<>?/ ]$", "");
            org = org.replaceAll("(?i)(\\b(a[\\.]?d|A[\\.]?B|A[\\.]?G|A[\\.]?N[\\.]?S|A[\\.]?S[\\.]?A|A/S|A[\\.]?S|GmbH|K[\\.]?K|N[\\.]?V|Oy|LTD|Limited|P[\\.]?L[\\.]?C|Inc|L[\\.]?L[\\.]?C|Co|Company|T[\\.]?V|A[\\.]?G|Corp|Corporation|Cos|S[\\.]?p[\\.]?A|\\.com|\\.in|\\.net|\\.co|group|holding[s]?|enterprise[s]?|L[\\.]?P[\\.]?|SA|S\\.A|LLP|d[\\.]?d|o[\\.]?y[\\.]?j)\\b[\\.]?|PTY|PTE|B[\\.]?V[\\.]?)$", "");
            org = org.replaceAll("(?i)^(\\b(the|a's|about|actually|afterwards|again|against|ain't|already|also|although|always|am|amongst|an|and|any|anybody|anyhow|anyone|anything|anyway|anyways|anywhere|apart|are|aren't|as|asking|awfully|be|became|because|become|becomes|been|before|beforehand|both|but|by|c'mon|c's|came|can't|cannot|cant|certain|changes|com|come|comes|considering|contain|contains|could|couldn't|described|despite|did|didn't|do|does|doesn't|doing|don't|done|during|each|eg|either|else|elsewhere|enough|especially|et|etc|even|everyone|except|few|for|formerly|forth|from|further|furthermore|gives|got|gotten|had|hadn't|happens|has|hasn't|have|haven't|having|he|he'd|he'll|he's|hence|her|here|here's|hereafter|hereby|hereupon|hers|herself|him|himself|his|hither|hopefully|how|how's|howbeit|however|i|i'd|i'll|i'm|i've|if|in|inasmuch|indicated|indicates|into|is|isn't|it'd|it'll|it's|itself|lately|later|latterly|least|let's|look|looking|mainly|many|maybe|me|meanwhile|more|moreover|most|must|mustn't|my|myself|nd|neither|no|noone|nor|not|nowhere|obviously|of|off|often|okay|on|once|ones|only|onto|or|other|others|otherwise|ought|our|ours|ourselves|out|outside|overall|particular|particularly|perhaps|please|presumably|probably|provides|rather|reasonably|regarding|regardless|relatively|respectively|said|saying|says|seeing|seemed|seen|shall|shan't|she|she'd|she'll|she's|should|shouldn't|somebody|somehow|sometime|sometimes|somewhat|somewhere|sorry|such|t's|th|than|thanks|that|that's|thats|their|theirs|them|themselves|then|thence|there|there's|thereafter|thereby|therefore|theres|thereupon|these|they|they'd|they'll|they're|they've|this|thoroughly|those|though|through|throughout|thus|to|too|toward|towards|try|trying|under|until|unto|using|usually|various|very|want|was|wasn't|we|we'd|we'll|we're|we've|welcome|went|were|weren't|what|what's|whatever|when|when's|whence|whenever|where|where's|whereafter|whereas|whereby|wherein|whereupon|wherever|whether|which|who|who's|whoever|whom|whose|why|why's|will|with|without|won't|would|wouldn't|www|you|you'd|you'll|you're|you've|yourself|yourselves| )\\b)", "");
            org = org.replaceAll("(?i)(\\b(a's|about|actually|afterwards|again|against|ain't|already|also|although|always|am|amongst|an|and|any|anybody|anyhow|anyone|anything|anyway|anyways|anywhere|apart|are|aren't|as|asking|awfully|be|became|because|become|becomes|been|before|beforehand|both|but|by|c'mon|c's|came|can't|cannot|cant|certain|changes|com|come|comes|considering|contain|contains|could|couldn't|described|despite|did|didn't|do|does|doesn't|doing|don't|done|during|each|eg|either|else|elsewhere|enough|especially|et|etc|even|everyone|except|few|for|formerly|forth|from|further|furthermore|gives|got|gotten|had|hadn't|happens|has|hasn't|have|haven't|having|he|he'd|he'll|he's|hence|her|here|here's|hereafter|hereby|hereupon|hers|herself|him|himself|his|hither|hopefully|how|how's|howbeit|however|i|i'd|i'll|i'm|i've|if|in|inasmuch|indicated|indicates|into|is|isn't|it'd|it'll|it's|itself|lately|later|latterly|least|let's|look|looking|mainly|many|maybe|me|meanwhile|more|moreover|most|must|mustn't|my|myself|nd|neither|no|noone|nor|not|nowhere|obviously|of|off|often|okay|on|once|ones|only|onto|or|other|others|otherwise|ought|our|ours|ourselves|out|outside|overall|particular|particularly|perhaps|please|presumably|probably|provides|rather|reasonably|regarding|regardless|relatively|respectively|said|saying|says|seeing|seemed|seen|shall|shan't|she|she'd|she'll|she's|should|shouldn't|somebody|somehow|sometime|sometimes|somewhat|somewhere|sorry|such|t's|th|than|thanks|that|that's|thats|their|theirs|them|themselves|then|thence|there|there's|thereafter|thereby|therefore|theres|thereupon|these|they|they'd|they'll|they're|they've|this|thoroughly|those|though|through|throughout|thus|to|too|toward|towards|try|trying|under|until|unto|using|usually|various|very|want|was|wasn't|we|we'd|we'll|we're|we've|welcome|went|were|weren't|what|what's|whatever|when|when's|whence|whenever|where|where's|whereafter|whereas|whereby|wherein|whereupon|wherever|whether|which|who|who's|whoever|whom|whose|why|why's|will|with|without|won't|would|wouldn't|www|you|you'd|you'll|you're|you've|yourself|yourselves| )\\b)$", "");
            org = org.replaceAll("(?i)^[*!@#$%^&,).+=~`{}|\\:;\"'<>?/]|[*!@#$%^&,(.+=~`{}|\\:;\"'<>?/ ]$", "");
        } while (!org1.equals(org));
        return org.trim();
    }

    public static void main(String[] args) throws Exception {
        //System.out.println("no cookies here");
        DBUtils dbConnection = new DBUtils("iatest_scripttesting");
		DBCollection orgsColl = dbConnection.orgsColl;
		//DBCursor cursor = orgsColl.find(new BasicDBObject("iname", new BasicDBObject("$regex", "monster".concat(".*")).append("$options", "i")));
		//DBCursor cursor = orgsColl.find(new BasicDBObject("name", "Monster Energy"));
		DBCursor cursor = orgsColl.find(new BasicDBObject("isChecked", false)).limit(5000);
		System.out.println("Orgs to process : " + cursor.count());
		
		try{
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				processOrg(dbo, dbConnection);
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			cursor.close();
		}
    }

    /**
     * The function is called when a new company arrives in the database. It
     * proceeds with name-entity disambiguation only if a common prefix
     * (maxMatch) for the given company name is found in the database.
     * @throws Exception 
     */
    public static void processOrg(DBObject orgObj, DBUtils dbConnection) throws Exception {

        try {            
        	String companyNameDB = (String) orgObj.get("name");
            String companyName = cleanOrganization(companyNameDB);
            System.out.println("Processing Company : " + companyName);
            BasicDBList orgs = (BasicDBList) (orgObj.get("orgs"));
            ArrayList<DBObject> match_list = new ArrayList<DBObject>();

            // Get the details of common prefix:
            MatchDocDetails matchDetails = dbConnection.getMatchDocs(companyName);
            DBCursor c = matchDetails.getDBCursor();
            if (c != null) {
                match_list = (ArrayList<DBObject>) c.toArray();
                c.close();
            }
            String maxMatch = matchDetails.getMaxMatch();

            // Get the details of any possible existing parent company:
            DBObject parent_company = dbConnection.getExistingMatch(companyName);
            String mm = "";
            if (parent_company != null) {
                mm = (String) parent_company.get("name");
            }

            // In case of no possible existing parent:
            if (parent_company == null) {

                // Proceed only if prefix match exists(a single initial match
                // not considered)
                if (maxMatch.length() > 1) {
                    go_back = 0;
                    maxMatchSearch(match_list, maxMatch, dbConnection);
                }
            } else {
                // In case the match already exists in database, check for more
                // matching . If a larger match found, search for that. Else,
                // add in the existing match.

                // System.out.println(TAG + "Existing match found " + mm);
                // System.out.println(TAG + "New match " + maxMatch);
                if (!(maxMatch.length() > mm.length())) {
                    // System.out.println(TAG +
                    // "existing match is greater, Adding to existing");
                    AddToExisting(mm, companyName, orgs, dbConnection);
                } else {

                    // Check for the new match
                    // System.out.println(TAG +
                    // "New Match is bigger , checking for it");
                    go_back = 1;
                    if (maxMatch.length() > 1) {
                        maxMatchSearch(match_list, maxMatch, dbConnection);

                        // If no match is found for the common prefix, it
                        // updates for the existing match found.
                        if (go_back == 0) {
                            // System.out.println(TAG +
                            // "New Match is useless, adding to previous one");
                            AddToExisting(mm, companyName, orgs, dbConnection);
                        }
                    }
                }
            }
            dbConnection.markOrgChecked(companyNameDB);
//			System.out.println("Done with : " + companyName);
        } finally {
        	
        }
    }

    /**
     * The function searches for a matching entry (for given parameter maMatch)
     * on freebase. It updates all companies that have the given match
     * @throws Exception 
     */
    public static void maxMatchSearch(ArrayList<DBObject> match_list,
                                      String maxMatch, DBUtils dbConnection) throws Exception {
        String query = "";
        String nameMatch = "";
        int first = 0;
        int count = 0;
        String elementName2 = "";
        int added = 0;
        String parent = "";
        String parentMatch = "";
        String os;

        StringTokenizer st = new StringTokenizer(maxMatch, DELIMITERS);
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if (next.equalsIgnoreCase("&")) { // Replace all '&' with 'and'
                next = "and";
            }
            nameMatch = nameMatch.concat(next);
            if (first == 0) {
                query = next;
                first = 1;
            } else {
                query = query.concat("+" + next);
            }
        }

        // Connect to freebase and proceed further if some results are obtained
        JSONArray results = Connect(query);
        if (results.size() > 0) {
            for (int j = 0; j < results.size(); j++) {
                count = count + 1;
                JSONObject element = (JSONObject) results.get(j);
                elementName2 = element.get("name").toString();
                String elementName = cleanOrganization(elementName2);
                StringTokenizer st3 = new StringTokenizer(elementName,
                        DELIMITERS);
                String elementNameMatch = "";
                while (st3.hasMoreTokens()) {
                    String next = st3.nextToken();
                    if (next.equalsIgnoreCase("&")) { // Replace all '&' with
                        // 'and'
                        next = "and";
                    }
                    elementNameMatch = elementNameMatch.concat(next);
                }

                // In case a match is found, update database accordingly.
                if (elementNameMatch.equalsIgnoreCase(nameMatch)) {
                    added = 1;

                    // Update database for all the comapnies that have the
                    // matching prefix
                    for (int i = 0; i < match_list.size(); i++) {
                        DBObject o = match_list.get(i);
                        BasicDBList orgs = (BasicDBList) o.get("orgs");
                        os = (String) o.get("name");
                        int check = 0;
                        // If 'orgs' exists, check for existence of field
                        // freebase_parent
                        if (!(orgs == null)) {
                            for (Object e : orgs) {
                                String rtype = (String) ((DBObject) e)
                                        .get("rtype");
                                if (rtype.equalsIgnoreCase("freebase_parent")) {
                                    parent = (String) ((DBObject) e)
                                            .get("name");
                                    check = 1;
                                    break;
                                }
                            }
                        }

                        // If no existing freebase parent, update the database
                        // unconditionally
                        if (check == 0) {
                            dbConnection.updateOrgs_usingFreeBase(os, elementName);
                        } else {

                            // If a freebase parent exists, update it only if
                            // existing parent length is smaller than the new
                            // one found. Also, remove the subsidiary from
                            // existing parent and add it to the new parent.
                            StringTokenizer st2 = new StringTokenizer(parent,
                                    DELIMITERS);
                            while (st2.hasMoreTokens()) {
                                parentMatch = parentMatch.concat(st2
                                        .nextToken());
                            }
                            if (parentMatch.length() < elementNameMatch
                                    .length()) {
                                dbConnection.updateParent(os, parent);
                                dbConnection.updateOrgs_usingFreeBase(os, elementName);
                            }
                        }

                    }
                    break;
                }

                // Check for some fixed number of top results in case total
                // number is large
                if (count > max_compare - 1) {
                    break;
                }
            }
            if (added == 0) {
                // In case of no result match, start after the
                // matched part and check as done before.
                // The attribute go_back is zero when no possible parent exists
                // in database.
                if (go_back == 0) {

                    for (int i = 0; i < match_list.size(); i++) {
                        DBObject o = match_list.get(i);
                        BasicDBList orgs = (BasicDBList) o.get("orgs");
                        os = (String) o.get("name");
                        int check = 0;
                        
                        if (!(orgs == null)) {
                            for (Object e : orgs) {
                                String rtype = (String) ((DBObject) e)
                                        .get("rtype");
                                if (rtype.equalsIgnoreCase("freebase_parent")) {
                                    check = 1;
                                    break;
                                }
                            }
                        }

                        // check after the common prefix only if no
                        // freebase_parent already exists
                        if (check == 0) {
                            QueryAfterMaxMatch(maxMatch, os, dbConnection);
                        }

                    }
                } else {
                    go_back = 0;
                }
            }
        } else {
            // In case of no result for the match, start after the
            // matched part and check as done before

            if (go_back == 0) {

                for (int i = 0; i < match_list.size(); i++) {
                    DBObject o = match_list.get(i);
                    BasicDBList orgs = (BasicDBList) o.get("orgs");
                    os = (String) o.get("name");
                    int check = 0;
                    
                    if (!(orgs == null)) {
                        for (Object e : orgs) {
                            String rtype = (String) ((DBObject) e).get("rtype");
                            if (rtype.equalsIgnoreCase("freebase_parent")) {
                                check = 1;
                                break;
                            }
                        }
                    }
                    if (check == 0) {
                        QueryAfterMaxMatch(maxMatch, os, dbConnection);
                    }

                }
            } else {
                go_back = 0;
            }
        }
    }

    /**
     * The function updates database for companies that have a possible existing
     * parent in the database.
     */
    public static void AddToExisting(String mm, String os1, BasicDBList orgs, DBUtils dbConnection)
            throws UnknownHostException {

        int check = 0;
        String parent = "";
        String nameMatch = "";
        String parentMatch = "";

        StringTokenizer st1 = new StringTokenizer(mm, DELIMITERS);
        while (st1.hasMoreTokens()) {
            nameMatch = nameMatch.concat(st1.nextToken());
        }

        if (!(orgs == null)) {
            for (Object e : orgs) {
                String rtype = (String) ((DBObject) e).get("rtype");
                if (rtype.equalsIgnoreCase("freebase_parent")) {
                    parent = (String) ((DBObject) e).get("name");
                    check = 1;
                    break;
                }
            }
        }

        // If no parent already exists, update is done unconditionally.
        if (check == 0) {
        	System.out.println("Company name" + os1);
        	System.out.println("parent name" + mm);
            dbConnection.updateOrgs_usingFreeBase(os1, mm);
        } else {

            // If parent already exists, update is done only when existing
            // parent (in database) is smaller than the match found.
            StringTokenizer st2 = new StringTokenizer(parent, DELIMITERS);
            while (st2.hasMoreTokens()) {
                parentMatch = parentMatch.concat(st2.nextToken());
            }
            if (parentMatch.length() < nameMatch.length()) {
                dbConnection.updateParent(os1, parent);
                dbConnection.updateOrgs_usingFreeBase(os1, mm);
            }
        }
    }

    /**
     * The function finds a match on freebase for the given company name. It
     * starts by appending words iteratively to the already tested match (that
     * does not exist on freebase).
     * @throws Exception 
     */
    public static void QueryAfterMaxMatch(String maxMatch, String companyName, DBUtils dbConnection)
            throws Exception {
        int first = 0;
        int added = 0;
        String nameMatch = "";
        String query = "";
        String pre_query = "";
        String nameId = "";
        String elementName2 = "";

        String name = companyName;
        String nonMatch = findNonMaxMatch(maxMatch, name);

        StringTokenizer st0 = new StringTokenizer(maxMatch, DELIMITERS);
        while (st0.hasMoreTokens()) { // maxMatch already has all the '&'
            // replaced by 'and'
            String next = st0.nextToken();
            if (first == 0) {
                pre_query = next;
                nameMatch = next;
                first = 1;
            } else {
                pre_query = pre_query.concat("+" + next);
                nameMatch = nameMatch.concat(next);
            }
        }
        query = pre_query;

        StringTokenizer st1 = new StringTokenizer(name, DELIMITERS);
        while (st1.hasMoreTokens()) {
            nameId = nameId.concat(st1.nextToken());
        }
        StringTokenizer st2 = new StringTokenizer(nonMatch, DELIMITERS);

        // Keep concatenating next word and repeatedly query to freebase, until
        // an exact match is found.
        while (st2.hasMoreTokens()) {
            int count = 0;
            String next = st2.nextToken();
            query = query.concat("+" + next);
            nameMatch = nameMatch.concat(next);

            JSONArray results = Connect(query);
            if (results.size() > 0) {
                for (int k = 0; k < results.size(); k++) {
                    count = count + 1;
                    JSONObject element = (JSONObject) results.get(k);
                    elementName2 = element.get("name").toString();
                    String elementName = cleanOrganization(elementName2);
                    StringTokenizer st3 = new StringTokenizer(elementName, DELIMITERS);
                    String elementNameMatch = "";
                    while (st3.hasMoreTokens()) {
                        String next2 = st3.nextToken();
                        if (next2.equalsIgnoreCase("&")) { // Replace all '&'
                            // with 'and'
                            next2 = "and";
                        }
                        elementNameMatch = elementNameMatch.concat(next2);
                    }

                    // Database is updated only if an match is found
                    if (elementNameMatch.equalsIgnoreCase(nameMatch)) {
                        added = 1;
                        dbConnection.updateOrgs_usingFreeBase(name, elementName);
                        break;
                    }
                    if (count > max_compare - 1) {
                        break;
                    }
                }
                if (added == 1) {
                    break;
                }
            }
        }
    }

    /**
     * Function returns the non-matching suffix (words) of the strings s1 and
     * s2. s1 is the already matched part, s2 is the full string
     */
    public static String findNonMaxMatch(String s1, String s2) {
        String nonMaxMatch = "";
        int first = 0;
        StringTokenizer st1 = new StringTokenizer(s1, DELIMITERS);
        StringTokenizer st2 = new StringTokenizer(s2, DELIMITERS);

//		System.out.println(s1 + "<---->" + s2);
        while (st1.hasMoreTokens()) {
            String s11 = st1.nextToken();
            String s22 = st2.nextToken();
        }
        while (st2.hasMoreTokens()) {
            String s222 = st2.nextToken();
            if (s222.equalsIgnoreCase("&")) {
                s222 = "and";
            }
            if (first == 0) {
                nonMaxMatch = s222;
                first = 1;
            } else {
                nonMaxMatch = nonMaxMatch.concat(" " + s222);
            }
        }
        return nonMaxMatch;
    }

    /**
     * This function connects to freebase.
     * @throws IOException
     * @throws ParseException
     */
    public static JSONArray Connect(String query) throws Exception {

        boolean notSuccessful = true;

        JSONArray results = new JSONArray();
        while (notSuccessful){
            try {
            	System.getProperties().put( "proxySet", "true" );
    			System.getProperties().put("https.proxyHost", "10.10.78.62");
    			System.getProperties().put("https.proxyPort", "3128");
    			Authenticator.setDefault(new Authenticator() {
    				protected PasswordAuthentication getPasswordAuthentication() {
    					return new
    							PasswordAuthentication("ee5100011","cubit*arrow".toCharArray());
    			}});
                HttpTransport httpTransport = new NetHttpTransport();
                HttpRequestFactory requestFactory = httpTransport
                        .createRequestFactory();
                JSONParser parser = new JSONParser();
                GenericUrl url = new GenericUrl(
                        "https://www.googleapis.com/freebase/v1/search");
                url.put("query", query);
                url.put("lang", "en");
                url.put("filter",
                        "(any type:/business/brand type:/business/consumer_company type:/business/venture_funded_company type:/organization/organization type:/organization/non_profit_organization type:/organization/endowed_organization type:/organization/defunct_organization)");
                url.put("scoring", "entity");
                url.put("indent", "true");
                url.put("key", API_KEY);
                // System.out.println(TAG + url);
                HttpRequest request = requestFactory.buildGetRequest(url);
                HttpResponse httpResponse = request.execute();
                JSONObject response = (JSONObject) parser.parse(httpResponse.parseAsString());
                results = (JSONArray) response.get("result");
                // System.out.println(TAG + "Number of results : " +
                // results.size());
                notSuccessful = false;
            } catch (SocketException e) {
                //e.printStackTrace();
                try {
                    System.out.println("Network Error ----> Waiting for " + wait_seconds  + " seconds");
                    Thread.sleep(1000*wait_seconds);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (UnknownHostException e){
                //e.printStackTrace();
                try {e.printStackTrace();
                    System.out.println("Network Error ----> Waiting for " + wait_seconds  + " seconds");
                    Thread.sleep(1000*wait_seconds);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (UnknownServiceException e){
                //e.printStackTrace();
                try {
                    System.out.println("Network Error ----> Waiting for " + wait_seconds  + " seconds");
                    Thread.sleep(1000*wait_seconds);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (SocketTimeoutException e){
                //e.printStackTrace();
                try {
                    System.out.println("Network Error ----> Waiting for " + wait_seconds + " seconds");
                    Thread.sleep(1000*wait_seconds);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }/* catch (ParseException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}*/
        }

        return results;
    }
}