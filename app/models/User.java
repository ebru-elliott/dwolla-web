package models;

import org.mindrot.jbcrypt.BCrypt;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.ObjectId;
import org.mongojack.Id;


import play.Logger;
import play.api.libs.Crypto;
import play.data.validation.Constraints;

import javax.persistence.Column;
import java.util.List;

public class User {

    @Id
    @ObjectId
    public String id;

    @Constraints.Required
    public String username;

    public String pin;

    @Constraints.Required
    @Column(name = "password")
    public String passwordHash;

    public boolean isAdmin = false;

    public String token;

    protected static final String CRYPTO_SECRET = System.getenv("APP_SECRET").substring(0, 16);


    public User() {
    }

    public static List<User> all() {
        return MongoDB.theUsersCollection().find().toArray();
    }

    public static User byId(String id) {
        return MongoDB.theUsersCollection().findOneById(id);
    }

    public static void delete(String id) {
        MongoDB.theUsersCollection().removeById(id);
    }

    public void save() {
        MongoDB.theUsersCollection().save(this);
    }

    public void update() {
        MongoDB.theUsersCollection().save(this);
    }


    public static User findByUsername(String username) {
        List<User> users = MongoDB.theUsersCollection().find().is("username", username).toArray();
        if (users == null || users.isEmpty()) return null;
        else return users.get(0);
    }

    public void assignPassword(String password) {
        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean checkPassword(String candidate) {
        return BCrypt.checkpw(candidate, passwordHash);
    }

    public void assignPin(String pin) {
        this.pin = Crypto.encryptAES(pin, CRYPTO_SECRET);
    }

    public String fetchPin() {
        return Crypto.decryptAES(pin, CRYPTO_SECRET);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

}
