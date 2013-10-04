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
    @Constraints.Required
    public String username;

    @Constraints.Required
    public String pin;

    @Constraints.Required
    public String password;

    public String token;


    public static Finder<String, User> finder = new Finder<String, User>(String.class, User.class);

    public User() {
    }

    public static List<String> listUsers() {
        List<String> names = new ArrayList<String>();

        for (User user : finder.select("username").findList()) {   //todo ee: finder another way
            names.add(user.username);
        }
        return names;
    }


    public static User findByUsername(String username) {
        return finder.byId(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

}
