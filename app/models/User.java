package models;

import org.mindrot.jbcrypt.BCrypt;
import play.api.libs.Crypto;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
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

    /**
     * Returns all the users in the database.
     *
     * @return all users
     */
    public static List<User> all() {
        return finder.all();
    }

    /**
     * Returns the user with the specified id.
     *
     * @param id user id
     * @return user
     */
    public static User byId(Integer id) {
        return finder.byId(id);
    }

    /**
     * Deletes the user with the specified id from the database.
     *
     * @param id user id
     */
    public static void delete(Integer id) {
        finder.ref(id).delete();
    }

    /**
     * Finds the user with the username.
     *
     * @param username username
     * @return the user
     */
    public static User findByUsername(String username) {
        return finder.where().eq("username", username).findUnique();
    }

    /**
     * Returns the hash of the user password using the OpenBSD bcrypt scheme.
     *
     * @param password user password
     */
    public void assignPassword(String password) {
        passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Checks that a plaintext password matches a previously hashed one.
     *
     * @param candidate plaintext password
     * @return true ff the plaintext password matches the previously hashed password
     */
    public boolean checkPassword(String candidate) {
        return BCrypt.checkpw(candidate, passwordHash);
    }

    /**
     * Encrypts the pin with the AES encryption standard using the application secret.
     *
     * @param pin user Dwolla pin
     */
    public void assignPin(String pin) {
        this.pin = Crypto.encryptAES(pin, CRYPTO_SECRET);
    }

    /**
     * Decrypts the pin with the AES encryption standard using the application secret.
     *
     * @return user Dwolla pin
     */
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
