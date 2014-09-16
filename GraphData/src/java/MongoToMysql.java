
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.types.ObjectId;
import org.json.JSONObject;

public class MongoToMysql {

    Statement statement;
    ResultSet resultSet;
    static Connection connect;
    MongoClient mongoClient;
    DB db;
    DBCollection coll;

    public MongoToMysql() {
        try {
            connect = getConnection();
            mongoClient = new MongoClient();
            db = mongoClient.getDB("mydb1");
            coll = db.getCollection("Ubuntu1");
        } catch (Exception e) {
            System.out.println(e);
        }

    }
    static MongoToMysql ubuntu1;

    public static void main(String args[]) {
        ubuntu1 = new MongoToMysql();

        Thread a = new Thread(new Runnable() {

            @Override
            public void run() {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                while (true) {
                    HashMap<String, String> a = new HashMap<>();
                    ArrayList<String> list = (ArrayList<String>) ubuntu1.coll.distinct("path");
                    for (String ab : list) {
                        String file = null;
                        if (ab.indexOf("/") != -1) {
                            // System.out.println(ab.lastIndexOf("/") + 1);
                            file = ab.substring(ab.lastIndexOf("/") + 1);
                        } else {
                            file = ab.substring(ab.lastIndexOf("\\") + 1);
                        }
                        String machine = (file.replace("MyFile", "").replace(".txt", ""));
                        if (validate(machine)) {
                            machine = "Host" + machine.substring(machine.lastIndexOf(".") + 1);
                        }
                        a.put(machine, file);
                    }
                    for (Entry<String, String> entry : a.entrySet()) {
                        System.out.println(entry.getValue()+"\t"+entry.getKey());
                        ubuntu1.updateMysqlTable(entry.getValue(), entry.getKey());
                    }

                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MongoToMysql.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        a.start();

    }

    static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            // setup the connection with the DB.
            connect = DriverManager.getConnection("jdbc:mysql://localhost/lab3?"
                    + "user=root&password=root");
        } catch (Exception e) {
            //System.out.println(e);
        }
        return connect;
    }
    private static Pattern pattern;
    private static Matcher matcher;

    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    /**
     * Validate ip address with regular expression
     *
     * @param ip ip address for validation
     * @return true valid ip address, false invalid ip address
     */
    public static boolean validate(final String ip) {
        pattern = Pattern.compile(IPADDRESS_PATTERN);
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private void updateMysqlTable(String file, String machine) {
        try {
            statement = connect.createStatement();
            // resultSet gets the result of the SQL query
            resultSet = statement.executeQuery("select max(id) from lab3.logs where machine='" + machine + "'");
            String id = null;
            if (resultSet.next()) {
                id = (resultSet.getString(1));
            }
            //535c5c85b19fd067c23cf04a
            BasicDBObject query = new BasicDBObject();

            HashMap f = new HashMap();
            f.put("message", 1);
            BasicDBObject fields = new BasicDBObject(f);

            if (id != null) {
                List<BasicDBObject> obj = new ArrayList<>();
                obj.add(new BasicDBObject("path", new BasicDBObject("$regex", "\\w*" + file + "\\b")));
                obj.add(new BasicDBObject("_id", new BasicDBObject("$gte", new ObjectId(id))));
                query.put("$and", obj);
            } else {
                query.put("path", new BasicDBObject("$regex", "\\w*" + file + "\\b"));
            }
            // System.out.println(query);
            DBCursor cursor = coll.find(query, fields);
            System.out.println(cursor.count());
            HashMap<String, ArrayList<Integer>> hm = new HashMap();
            HashMap<String, Date> date = new HashMap();
            HashMap<String, String> idmp = new HashMap();
            while (cursor.hasNext()) {
                DBObject ob = cursor.next();
                System.out.println(ob.get("message").toString());
                JSONObject jsonobject = new JSONObject(ob.get("message").toString());
                // System.out.println(ob.get("message"));
                try {
                    String key;
                    Integer value;
                    String Sample_time = jsonobject.optString("Sample time");
                    if (Sample_time.isEmpty()) {
                        continue;
                    } else {
                        key = jsonobject.optString("Key");
                        value = jsonobject.optInt("Value");
                        // System.out.println(Sample_time+"\t"+key+"\t"+value);
                    }
                    SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
                    Date samplevalue = (formatter.parse(Sample_time));
                    if (date.get(key) != null) {
                        if (samplevalue.getTime() - date.get(key).getTime() > 300000) {
                            System.out.println(ob.get("_id") + "\t" + key + "\t" + samplevalue + "\t" + calculateAverage(hm.get(key)));
                            try {
                                statement.executeUpdate("INSERT INTO `lab3`.`logs` "
                                        + "(`id`, "
                                        + "`key`, "
                                        + "`value`,  "
                                        + "`time`, "
                                        + "`machine`) "
                                        + "VALUES ('" + ob.get("_id").toString()
                                        + "' , '" + key
                                        + "' , '" + calculateAverage(hm.get(key))
                                        + "' , '" + new Timestamp(samplevalue.getTime())
                                        + "' , '" + machine
                                        + "');");
                            } catch (Exception e) {
                            }
                            hm.get(key).clear();
                            date.get(key).setTime(samplevalue.getTime());
                            idmp.remove(key);
                        }

                    }

                    if (hm.get(key) != null) {
                        hm.get(key).add(value);
                    } else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(value);
                        hm.put(key, list);
                        date.put(key, samplevalue);

                    }
                    idmp.put(key, ob.get("_id").toString());
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            Iterator it = hm.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (!((ArrayList<Integer>) pairs.getValue()).isEmpty()) {
                    System.out.println(idmp.get(pairs.getKey().toString()) + "\t" + date.get(pairs.getKey().toString()) + "\t" + pairs.getKey() + "\t" + Arrays.asList((ArrayList<Integer>) pairs.getValue()));

                    try {
                        statement.executeUpdate("INSERT INTO `lab3`.`logs` " + "(`id`, "
                                + "`key`, "
                                + "`value`,  "
                                + "`time`, "
                                + "`machine`) "
                                + "VALUES ('" + idmp.get(pairs.getKey().toString())
                                + "' , '" + pairs.getKey().toString()
                                + "' , '" + calculateAverage((ArrayList<Integer>) pairs.getValue())
                                + "' , '" + new Timestamp(((Date) date.get(pairs.getKey())).getTime())
                                + "' , '" + machine
                                + "')");
                    } catch (Exception e) {
                    }
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (Exception r) {
            r.printStackTrace();
        }

    }

    private double calculateAverage(List<Integer> marks) {
        Integer sum = 0;
        if (!marks.isEmpty()) {
            for (Integer mark : marks) {
                sum += mark;
            }
            return sum.doubleValue() / marks.size();
        }
        return sum;
    }
}
