package models;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.mongojack.JacksonDBCollection;
import play.Logger;
import play.Play;

import java.net.UnknownHostException;

public class MongoDB {

    public static MongoDB instance = new MongoDB();

    public static DB theDB() {
        return instance.db;
    }

    public static JacksonDBCollection<User, String> theUsersCollection() {
        return instance.usersCollection;
    }


    protected DB db;

    protected JacksonDBCollection<User, String> usersCollection;


    public MongoDB() {
        Logger.info("MongoDB init");
        init();
    }

    public void init() {
        MongoClient mongo = null;
        try {
            mongo = new MongoClient();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        db = mongo.getDB(Play.application().configuration().getString("mongodb.database"));

        usersCollection = JacksonDBCollection.wrap(db.getCollection("users"), User.class, String.class);

    }
}
