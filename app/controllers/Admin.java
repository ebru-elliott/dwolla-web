package controllers;

import com.dwolla.java.sdk.DwollaServiceAsync;
import com.dwolla.java.sdk.DwollaServiceSync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;
import com.dwolla.java.sdk.responses.SendResponse;
import com.google.gson.Gson;
import models.User;

import play.data.Form;
import play.data.validation.Constraints.*;
import play.libs.Crypto;
import play.mvc.Result;
import play.mvc.Security;
import retrofit.RestAdapter;
import retrofit.Server;


import views.html.admin.*;


import static play.data.Form.form;

@Security.Authenticated(AdminSecured.class)
public class Admin extends BaseController {


    public static class ChargeForm {
        @Required
        public Double amount;
    }

    public static class Info {
        @Required
        public String username;
        @Required
        public boolean isAdmin;
    }


    public static Result menu() {
        return ok(adminMenu.render(User.all()));
    }

    public static Result editAmount(String id) {
        Form<ChargeForm> form = form(ChargeForm.class);
        User user = User.byId(id);
        return ok(charge.render(user.username, user.id, form));
    }


    public static Result chargeUser(String id) {
        Form<ChargeForm> form = form(ChargeForm.class).bindFromRequest();

        User user = User.byId(id);

        if (form.hasErrors()) {
            return badRequest(charge.render(user.username, user.id, form));
        } else {
            DwollaServiceSync service = new RestAdapter.Builder().setServer(
                    new Server(Application.DWOLLA_API_URL)).build().create(DwollaServiceSync.class);

            String pin = user.fetchPin();

            SendResponse response = service.send(new DwollaTypedBytes(new Gson(),
                    new SendRequest(user.token, pin, Application.DWOLLA_DESTINATION_ID, form.get().amount)));

            if (response.Success) {
                flash(SUCCESS, response.Message);
            } else {
                flash(ERROR, response.Message);
            }
            return goAdminMenu();
        }
    }

    public static Result deleteUser(String id) {
        User.delete(id);
        flash(SUCCESS, "user deleted");
        return goAdminMenu();
    }

    public static Result editInfo(String id) {
        User u = User.byId(id);

        Info info = new Info();
        info.username = u.username;
        info.isAdmin = u.isAdmin;

        Form<Info> form = form(Info.class).fill(info);

        if (form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            return badRequest(adminEditInfo.render(id, form));
        }

        return ok(adminEditInfo.render(id, form));
    }


    public static Result updateInfo(String id) {
        Form<Info> form = form(Info.class).bindFromRequest();

        if (form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            return badRequest(adminEditInfo.render(id, form));
        }

        User u = User.byId(id);
        u.username = form.get().username;
        u.isAdmin = form.get().isAdmin;
        u.update();

        return goAdminMenu();
    }

    protected static Result goAdminMenu() {
        return redirect(routes.Admin.menu());
    }


    public static void bootstrapAdmin() {
        User u = new User();
        u.username = "admin";
        u.assignPassword("admin");
        u.isAdmin = true;
        u.save();
    }

}
