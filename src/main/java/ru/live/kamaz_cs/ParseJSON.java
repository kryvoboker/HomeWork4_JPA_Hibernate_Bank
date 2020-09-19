//package ru.live.kamaz_cs;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//
//public class ParseJSON {
//
//    private double usdBuy;
//    private double usdSale;
//    private double eurBuy;
//    private double eurSale;
//
//    public void parse() {
//        File file = new File("json.txt");
//        StringBuilder sb = new StringBuilder();
//        String result = performRequest(file);
//
//        Gson gson = new GsonBuilder().create();
//        PrivatbankJson[] jsonArray = gson.fromJson(result, PrivatbankJson[].class);
//        for (PrivatbankJson jsonObj : jsonArray) {
//            sb.append(jsonObj.ccy + " ").append(" " + jsonObj.base_ccy + " ").append("Buy: " + jsonObj.buy + " ").append("Sale: " + jsonObj.sale).append(System.lineSeparator());
//
//            if (jsonObj.ccy.equals("USD")) {
//                usdBuy = Double.valueOf(jsonObj.buy);
//            } else if (jsonObj.ccy.equals("USD")) {
//                usdSale = Double.valueOf(jsonObj.sale);
//            } else if (jsonObj.ccy.equals("EUR")) {
//                eurSale = Double.valueOf(jsonObj.sale);
//            } else if (jsonObj.ccy.equals("EUR")) {
//                eurBuy = Double.valueOf(jsonObj.buy);
//            }
//        }
//
////        System.out.println(sb.toString());
////        System.out.println(usdBuy);
//    }
//
//    private String performRequest(File file) {
//
//        StringBuilder sb = new StringBuilder();
//        String st = null;
//        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//            for (; (st = br.readLine()) != null; ) {
//                sb.append(st).append(System.lineSeparator());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }
//
//    public double getUsdBuy() {
//        return usdBuy;
//    }
//
//    public void setUsdBuy(double usdBuy) {
//        this.usdBuy = usdBuy;
//    }
//
//    public double getUsdSale() {
//        return usdSale;
//    }
//
//    public void setUsdSale(double usdSale) {
//        this.usdSale = usdSale;
//    }
//
//    public double getEurBuy() {
//        return eurBuy;
//    }
//
//    public void setEurBuy(double eurBuy) {
//        this.eurBuy = eurBuy;
//    }
//
//    public double getEurSale() {
//        return eurSale;
//    }
//
//    public void setEurSale(double eurSale) {
//        this.eurSale = eurSale;
//    }
//}
