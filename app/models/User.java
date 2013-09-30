package models;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: ebru
 * Date: 9/26/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
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

    public static Finder<String,User> find = new Finder<String,User>(String.class, User.class);

    public static List<String> listUsers() {
        List<String> names = new ArrayList<String>();

        for(User user: User.find.select("username").findList()) {   //todo ee: find another way
            names.add(user.username);
        }
        return names;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }
}
