package controllers;

import com.dwolla.java.sdk.DwollaServiceSync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;
import com.dwolla.java.sdk.responses.SendResponse;
import com.google.gson.Gson;
import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import play.mvc.Security;
import retrofit.RestAdapter;
import retrofit.Server;
import views.html.admin.adminEditInfo;
import views.html.admin.adminMenu;
import views.html.admin.charge;

import static play.data.Form.form;

/**
 * Controller for all the admin related actions (delete user, charge user, etc..)
 */
@Security.Authenticated(AdminSecured.class)
public class Admin extends BaseController {

    public static Logger logger = LoggerFactory.getLogger(Admin.class);

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

    /**
     * Renders the admin menu with a list of registered users.
     *
     * @return admin menu view
     */
    public static Result menu() {
        logger.info("render the user menu");
        return ok(adminMenu.render(User.all()));
    }

    /**
     * Renders the page where the user can be charged with a specified amount.
     *
     * @param id user id
     * @return charge amount page
     */
    public static Result editAmount(Integer id) {
        Form<ChargeForm> form = form(ChargeForm.class);
        User user = User.byId(id);
        logger.info("render the charge page for username:{}" + user.username);
        return ok(charge.render(user.username, user.id, form));
    }

    /**
     * Charges the user's Dwolla account by the specified amount.
     *
     * @param id user id
     * @return admin menu if successful
     */
    public static Result chargeUser(Integer id) {
        Form<ChargeForm> form = form(ChargeForm.class).bindFromRequest();

        User user = User.byId(id);

        if (form.hasErrors()) {
            logger.info("chargeForm has errors:{}", form.errors());
            return badRequest(charge.render(user.username, user.id, form));
        } else {
            DwollaServiceSync service = new RestAdapter.Builder().setServer(
                    new Server(Application.DWOLLA_API_URL)).build().create(DwollaServiceSync.class);

            String pin = user.fetchPin();
            Double amount = form.get().amount;

            SendResponse response = null;
            try {
                response = service.send(new DwollaTypedBytes(new Gson(),
                        new SendRequest(user.token, pin, Application.DWOLLA_DESTINATION_ID, amount)));
            } catch (RuntimeException re) {
                logger.error("send request failed {}", re.getMessage());
                flash(ERROR, re.getMessage());
            }


            String msg = response.Message;

            logger.info("send request username:{} amount:{} message:{}", user.username, amount, msg);
            if (response.Success) {
                flash(SUCCESS, msg);
            } else {
                flash(ERROR, msg);
            }
            return goAdminMenu();
        }
    }

    /**
     * Deletes the user from the database.
     *
     * @param id user id
     * @return admin menu
     */
    public static Result deleteUser(Integer id) {
        User.delete(id);
        logger.info("user id:{} deleted", id);
        flash(SUCCESS, "user deleted");
        return goAdminMenu();
    }

    /**
     * Renders the admin edit info for the specified user.
     *
     * @param id user id
     * @return admin edit info page
     */
    public static Result editInfo(Integer id) {
        User u = User.byId(id);

        Info info = new Info();
        info.username = u.username;
        info.isAdmin = u.isAdmin;

        Form<Info> form = form(Info.class).fill(info);

        if (form.hasErrors()) {
            logger.info("infoForm has errors:{}", form.errors());
            return badRequest(adminEditInfo.render(id, form));
        }

        logger.info("render the admin edit info page");
        return ok(adminEditInfo.render(id, form));
    }

    /**
     * Updates the user's username and/or admin status.
     *
     * @param id user id
     * @return admin menu if successful
     */
    public static Result updateInfo(Integer id) {
        Form<Info> form = form(Info.class).bindFromRequest();

        if (form.hasErrors()) {
            logger.info("infoForm has errors:{}", form.errors());
            return badRequest(adminEditInfo.render(id, form));
        }

        User u = User.byId(id);
        u.username = form.get().username;
        u.isAdmin = form.get().isAdmin;
        u.update();

        logger.info("username:{} updated", u.username);

        return goAdminMenu();
    }

    /**
     * Redirects to the admin menu.
     *
     * @return admin menu
     */
    protected static Result goAdminMenu() {
        logger.info("redirecting to admin menu");
        return redirect(routes.Admin.menu());
    }


}
