package controllers;

import com.dwolla.java.sdk.DwollaServiceAsync;
import com.dwolla.java.sdk.DwollaServiceSync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;

import com.google.gson.Gson;
import models.User;
import play.*;
import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import play.data.validation.Constraints.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import views.html.*;
import views.html.helper.form;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.Server;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

public class Application extends Controller {

    private static final String TOKEN = "token";
    private static final String AUTHORIZATION_CODE = "authorization_code";


    private static final String DESTINATION_ID = "812-713-9234";  /*reflector_id*/ //todo ee: change it later
    private static final String API_URL = "https://www.dwolla.com";   //todo ee: move it to app config?

    private static final String KEY = System.getenv("DWOLLA_APP_KEY");
    private static final String SECRET = System.getenv("DWOLLA_APP_SECRET");
    private static final String REDIRECT_URI = System.getenv("DWOLLA_REDIRECT_URI");


    public static class ChargeForm {
        @Required
        public String username;
        @Required
        public Double amount;
    }


    // -- Actions

    /**
     * Home page
     */
    public static Result index() throws UnsupportedEncodingException {

        return ok(index.render(encode(KEY), encode(REDIRECT_URI )));
    }

    private static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }


    static class Token {
        String access_token;
        String error;
        String error_description;
    }


    interface Dwolla {
        @GET("/oauth/v2/token")
        Token getToken(@Query("client_id") String key, @Query("client_secret") String secret, @Query("grant_type") String grantType, @Query("redirect_uri") String redirectUri, @Query("code") String code);
    }

    public static Result oauthFlow(String code) {
        Form<User> userForm = form(User.class);

        //Retrofit REST client, oauth step2
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(API_URL)
                .build();

        Dwolla dwolla = restAdapter.create(Dwolla.class);
        Token token = dwolla.getToken(KEY, SECRET, AUTHORIZATION_CODE, REDIRECT_URI, code);
        session(TOKEN, token.access_token);

        return ok(user.render(userForm));

    }

    public static Result saveUser() {
        Form<User> form = form(User.class).bindFromRequest();

        if (form.hasErrors()) {
            System.out.println("ERROR!" + form.errorsAsJson());
            return badRequest(user.render(form));

        } else {
            User user = form.get();
            user.token = session(TOKEN);
            user.save();
            return ok("Thanks for registering with Oasis!");
        }
    }

    public static Result listUsers() {
        Form<ChargeForm> chargeForm = form(ChargeForm.class);

        List<String> names = User.listUsers();
        return ok(users.render(names, chargeForm));
    }

    public static Result chargeUser() {
        Form<ChargeForm> chargeForm = form(ChargeForm.class).bindFromRequest();
        ChargeForm form = chargeForm.get();

        String username = form.username;
        Double amount = form.amount;

        User user = User.find.byId(username);


        if (chargeForm.hasErrors()) {
            System.out.println("ERROR!" + chargeForm.errorsAsJson());
            return badRequest(users.render(User.listUsers(), chargeForm));  //todo ee: anyway to avoid hitting db again

        } else {

            //todo ee: async vs sync
            DwollaServiceAsync service = new RestAdapter.Builder().setServer(
                    new Server(API_URL + "/oauth/rest")).build().create(DwollaServiceAsync.class);

            //todo ee: update this with error handling
            service.send(new DwollaTypedBytes(new Gson(),
                    new SendRequest(user.token, user.pin, DESTINATION_ID, amount)), new SendCallback());
            return ok("Thanks for payment!");

        }

    }

}
