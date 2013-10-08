package models;

import com.avaje.ebean.Ebean;
import org.apache.commons.codec.binary.Base64;
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

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

}
