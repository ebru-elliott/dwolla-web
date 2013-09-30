package controllers;

import com.dwolla.java.sdk.DwollaCallback;
import com.dwolla.java.sdk.responses.BasicAccountInformationResponse;
import com.dwolla.java.sdk.responses.SendResponse;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created with IntelliJ IDEA.
 * User: ebru
 * Date: 9/25/13
 * Time: 11:50 AM
 * To change this template use File | Settings | File Templates.
 */
class SendCallback extends DwollaCallback<SendResponse> {


    //todo ee: revisit this later
    @Override
    public void success(SendResponse response, Response r) {

        if (response.Success)
        {
            System.out.println("SUCCESS:" + response.Message);
        }
        // Handle response...
        else
            System.out.println("NOT SUCCESS:" + response.Message);
            //super.failure(response.Message);
    }

    @Override
    public void failure(RetrofitError error) {
        System.out.println("FAILURE:" + error.getLocalizedMessage());
        //super.failure(error);
    }
}
