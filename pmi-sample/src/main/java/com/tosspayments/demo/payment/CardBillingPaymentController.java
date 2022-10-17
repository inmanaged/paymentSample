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
@RequestMapping(value="/billing")
public class CardBillingPaymentController {

    @GetMapping(value = "sender")
    public String cardBillingKeySend(
            Model model,
            @RequestParam(value = "billingKey") String billingKey,
            @RequestParam(value = "customerKey") String customerKey
    )throws Exception {

        model.addAttribute("billingKey", billingKey);
        model.addAttribute("customerKey", customerKey);

        return "/payRequest/BillingKeyPaymentRequest";
    }

    @GetMapping(value = "issuing")
    public String cardBillingKeyIssuingRequest(
            Model model,
            @RequestParam(value = "customerKey", required=false) String customerKey,
            @RequestParam(value = "cardNumber", required=false) String cardNumber,
            @RequestParam(value = "cardExpirationYear", required=false) String cardExpirationYear,
            @RequestParam(value = "cardExpirationMonth", required=false) String cardExpirationMonth,
            @RequestParam(value = "cardPassword", required=false) String cardPassword,
            @RequestParam(value = "customerIdentityNumber", required=false) String customerIdentityNumber
    ) throws Exception {

        System.out.println("customerKey["+customerKey+"]");
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

        URL url = new URL("https://api.tosspayments.com/v1/billing/authorizations/card"); //px live endpoint

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        JSONObject obj = new JSONObject();
        obj.put("cardNumber", cardNumber);
        obj.put("cardExpirationYear", cardExpirationYear);
        obj.put("cardExpirationMonth", cardExpirationMonth);
        obj.put("cardPassword", cardPassword);
        obj.put("customerIdentityNumber", customerIdentityNumber);
        obj.put("customerKey", customerKey);

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

        model.addAttribute("authenticatedAt", (String) jsonObject.get("authenticatedAt"));
        model.addAttribute("customerKey", (String) jsonObject.get("customerKey"));
        model.addAttribute("method", (String) jsonObject.get("method"));
        model.addAttribute("mId", (String) jsonObject.get("mId"));
        model.addAttribute("billingKey", (String) jsonObject.get("billingKey"));
        model.addAttribute("cardCompany", (String) jsonObject.get("cardCompany"));
        model.addAttribute("cardNumber", (String) jsonObject.get("cardNumber"));

        System.out.println("authenticatedAt["+model.getAttribute("authenticatedAt")+"]");
        System.out.println("customerKey["+model.getAttribute("customerKey")+"]");
        System.out.println("method["+model.getAttribute("method")+"]");
        System.out.println("mId["+model.getAttribute("mId")+"]");
        System.out.println("billingKey["+model.getAttribute("billingKey")+"]");
        System.out.println("cardCompany["+model.getAttribute("cardCompany")+"]");
        System.out.println("cardNumber["+model.getAttribute("cardNumber")+"]");

        if (((String) jsonObject.get("method")) != null) {
            if (((String) jsonObject.get("method")).equals("카드")) {
                model.addAttribute("card_ownerType", (String) ((JSONObject) jsonObject.get("card")).get("ownerType"));
                model.addAttribute("card_number", (String) ((JSONObject) jsonObject.get("card")).get("number"));
                model.addAttribute("card_cardType", (String) ((JSONObject) jsonObject.get("card")).get("cardType"));
                model.addAttribute("card_company", (String) ((JSONObject) jsonObject.get("card")).get("company"));
                System.out.println("card_ownerType["+model.getAttribute("card_ownerType")+"]");
                System.out.println("card_number["+model.getAttribute("card_number")+"]");
                System.out.println("card_cardType["+model.getAttribute("card_cardType")+"]");
                System.out.println("card_company["+model.getAttribute("card_company")+"]");
            }
        } else {
            model.addAttribute("code", (String) jsonObject.get("code"));
            model.addAttribute("message", (String) jsonObject.get("message"));
        }
        return "billingKeyIssuingSuccess";
    }

    @GetMapping(value = "payment")
    public String billingPayment(
            Model model,
            @RequestParam(value = "customerKey", required = false) String customerKey,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "orderName", required = false) String orderName,
            @RequestParam(value = "customerEmail", required = false) String customerEmail,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "taxFreeAmount", required = false) String taxFreeAmount,
            @RequestParam(value = "billingKey", required = false) String billingKey
    ) throws Exception {
        System.out.println("customerKey["+customerKey+"]");
        System.out.println("amount["+amount+"]");
        System.out.println("orderId["+orderId+"]");
        System.out.println("orderName["+orderName+"]");
        System.out.println("customerEmail["+customerEmail+"]");
        System.out.println("customerName["+customerName+"]");
        System.out.println("taxFreeAmount["+taxFreeAmount+"]");
        System.out.println("billingKey["+billingKey+"]");

        String secretKey = "test_sk_kZLKGPx4M3MRo7W9DJwVBaWypv1o:";   //atech005 testKey
//        String secretKey = "live_sk_N5OWRapdA8dWB5v7LBPro1zEqZKL:";   //atech005 live

        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode(secretKey.getBytes("UTF-8"));
        String authorizations = "Basic " + new String(encodedBytes, 0, encodedBytes.length);
        System.out.println("authorizations["+authorizations+"]");

        URL url = new URL("https://api.tosspayments.com/v1/billing/" + billingKey); //px live

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        JSONObject obj = new JSONObject();
        obj.put("customerKey", customerKey);
        obj.put("amount", amount);
        obj.put("orderId", orderId);
        obj.put("orderName", orderName);
        obj.put("customerEmail", customerEmail);
        obj.put("customerName", customerName);
        obj.put("taxFreeAmount", taxFreeAmount);

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
