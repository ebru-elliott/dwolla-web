package models;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;

public class MongoDB {

    public static MongoDB instance = new MongoDB();

    public static DB theDB() {
        return instance.db;
    }

    protected DB db;

    public MongoDB() {
        init();
    }

    public void init() {
        MongoClient mongo = null;
        try {
            mongo = new MongoClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Get "logs" collection from the "websites" DB.
        db = mongo.getDB("dwolla-web"); //app.configuration().getString("mongodb.database"));
    }



}
