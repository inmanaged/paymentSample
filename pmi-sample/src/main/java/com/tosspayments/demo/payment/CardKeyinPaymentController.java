package com.tosspayments.demo.payment;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Controller
@RequestMapping(value="/keyin")
public class CardKeyinPaymentController {

    @GetMapping(value = "payRequest")
    public String cardKeyinControllerSuccessResult(
            Model model,
            @RequestParam(value = "orderId", required=false) String orderId,
            @RequestParam(value = "amount", required=false) String amount,
            @RequestParam(value = "cardNumber", required=false) String cardNumber,
            @RequestParam(value = "cardExpirationYear", required=false) String cardExpirationYear,
            @RequestParam(value = "cardExpirationMonth", required=false) String cardExpirationMonth,
            @RequestParam(value = "cardPassword", required=false) String cardPassword,
            @RequestParam(value = "customerIdentityNumber", required=false) String customerIdentityNumber
            ) throws Exception {

        //script 에서 orderId set 이 안될때 사용
        if(orderId == null || orderId.equals(""))
            orderId = "atech-test-"+Math.round(Math.random()*1000000);

        System.out.println("orderId["+orderId+"]");
        System.out.println("amount["+amount+"]");
        System.out.println("cardNumber["+cardNumber+"]");
        System.out.println("cardExpirationYear["+cardExpirationYear+"]");
        System.out.println("cardExpirationMonth["+cardExpirationMonth+"]");
        System.out.println("cardPassword["+cardPassword+"]");
        System.out.println("customerIdentityNumber["+customerIdentityNumber+"]");

        String secretKey = "test_sk_kZLKGPx4M3MRo7W9DJwVBaWypv1o:";   //atech005 testKey
//        String secretKey = "live_sk_N5OWRapdA8dWB5v7LBPro1zEqZKL:";   //atech005 live

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(secretKey.getBytes("UTF-8"));
        String authorizations = "Basic " + new String(encodedBytes, 0, encodedBytes.length);
        System.out.println("authorizations["+authorizations+"]");

        URL url = new URL("https://api.tosspayments.com/v1/payments/key-in"); //px live

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("cardNumber", cardNumber);
        obj.put("cardExpirationYear", cardExpirationYear);
        obj.put("cardExpirationMonth", cardExpirationMonth);
        obj.put("cardPassword", cardPassword);
        obj.put("customerIdentityNumber", customerIdentityNumber);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(obj.toString().getBytes("UTF-8"));

        int code = connection.getResponseCode();
        boolean isSuccess = code == 200 ? true : false;
        model.addAttribute("isSuccess", isSuccess);

        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();

        Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(reader);
        responseStream.close();
        model.addAttribute("responseStr", jsonObject.toJSONString());
        System.out.println(jsonObject.toJSONString());

        model.addAttribute("method", (String) jsonObject.get("method"));
        model.addAttribute("orderName", (String) jsonObject.get("orderName"));
        model.addAttribute("paymentKey", (String) jsonObject.get("paymentKey"));

        if (((String) jsonObject.get("method")) != null) {
            if (((String) jsonObject.get("method")).equals("카드")) {
                model.addAttribute("cardNumber", (String) ((JSONObject) jsonObject.get("card")).get("number"));
            } else if (((String) jsonObject.get("method")).equals("가상계좌")) {
                model.addAttribute("accountNumber", (String) ((JSONObject) jsonObject.get("virtualAccount")).get("accountNumber"));
            } else if (((String) jsonObject.get("method")).equals("계좌이체")) {
                model.addAttribute("bank", (String) ((JSONObject) jsonObject.get("transfer")).get("bank"));
            } else if (((String) jsonObject.get("method")).equals("휴대폰")) {
                model.addAttribute("customerMobilePhone", (String) ((JSONObject) jsonObject.get("mobilePhone")).get("customerMobilePhone"));
            }
        } else {
            model.addAttribute("code", (String) jsonObject.get("code"));
            model.addAttribute("message", (String) jsonObject.get("message"));
        }
        return "success";
    }

    @GetMapping(value = "fail")
    public String cardKeyinControllerFailResult(
            Model model,
            @RequestParam(value = "message") String message,
            @RequestParam(value = "code") Integer code
    ) throws Exception {

        model.addAttribute("code", code);
        model.addAttribute("message", message);

        return "fail";
    }

}

