package com.nownews.mobile.Json;

/**
 * Created by cindy on 2017/8/14.
 */

public class LiveListErrorJson {

//    {
//        "status": 403,
//        "message": "Forbidden，APP 版本不符合"
//    }

    public int status;
    public String message;

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

}
