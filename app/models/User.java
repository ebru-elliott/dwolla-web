package models;

import com.avaje.ebean.Ebean;
import org.apache.commons.codec.binary.Base64;
import org.mindrot.jbcrypt.BCrypt;
import play.api.libs.Crypto;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "USER_ACCOUNT")
public class User extends Model {


    @Id
    public Integer id;

    @Constraints.Required
    public String username;

    public String pin;

    @Constraints.Required
    @Column(name = "password")
    public String passwordHash;

    public boolean isAdmin = false;

    public String token;

    protected static final String CRYPTO_SECRET = System.getenv("APP_SECRET").substring(0, 16);


    public static Finder<Integer, User> finder = new Finder<Integer, User>(Integer.class, User.class);

    public User() {
    }

    public static List<User> all() {
        return finder.all();
    }

    public static User byId(Integer id) {
        return finder.byId(id);
    }

    public static void delete(Integer id) {
        finder.ref(id).delete();
    }


    public static User findByUsername(String username) {
        return finder.where().eq("username", username).findUnique();
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
