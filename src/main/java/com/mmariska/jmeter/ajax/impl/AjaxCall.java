package com.mmariska.jmeter.ajax.impl;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.Callable;

public class AjaxCall implements Callable<AjaxResult> {

    private static final Logger logger = LoggingManager.getLoggerForClass();

    private static final int TIMEOUT = 10000;
    private String data;
    private JMeterContext ctx;

    public AjaxCall(String name, String data, JMeterContext ctx, Entry e) {
        this.data = data;
        this.ctx = ctx;
    }

    private AjaxResult singleErrorResult(Date start) {
        AjaxResult singleResult = new AjaxResult();
        singleResult.setResult(AjaxResult.ERROR_RESULT);
        singleResult.finish(start);
        return singleResult;
    }

    private AjaxResult execute(String method, String url, String postData, String headers, JMeterContext ctx) throws IOException {
        AjaxResult result = new AjaxResult();
        if(logger.isDebugEnabled()) logger.debug("Executing " + method + " request to URL: " + url + " (headers: " + headers + ") (data: " + postData + ")");
        result.setUrl(url);
        Date start = new Date();
        final long s = System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) (new URL(url))
                .openConnection();
//        connection.setReadTimeout(TIMEOUT); //for long request from pricemart it is failing on timeout
        connection.setConnectTimeout(TIMEOUT);
//        CookieManager cookieManager = (CookieManager) ctx.getCurrentSampler().getProperty("HTTPSampler.cookie_manager").getObjectValue();
//        if (cookieManager != null) {
//            for (int i = 0; i < cookieManager.getCookieCount(); i++) {
//                connection.addRequestProperty("Cookie", cookieManager.get(i).getName() + "=" + cookieManager.get(i).getValue());
//            }
//        }

//        final JMeterProperty headersProp = ctx.getProperty("HeaderManager.headers");
//        if(headersProp != null) {
//            List o = (List) headersProp.getObjectValue();
//            logger.info(o.get(0).getClass().getName());
//            if (o.get(0) instanceof JMeterProperty) {
//                logger.info("is jp");
//            }
////            while (iterator.hasNext()) {
////                JMeterProperty header = iterator.next();
////                connection.setRequestProperty(header.getName(), header.getStringValue());
////                logger.info("added headers from Manager. " + header.getName() + ": " + header.getStringValue());
////            }
//        }


        if (method.equals("GET")) {
            connection.connect();
        } else {
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(method);

            if (headers != null && !headers.isEmpty()) {
                final String[] splitted = headers.split(";");
                for (int i = 0; i < splitted.length; i = i + 2) {
                    connection.setRequestProperty(splitted[i], splitted[i + 1]);
                }
            }

            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(postData.getBytes().length));
            connection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();
        }

        result.setResult(connection.getResponseCode());
        connection.disconnect();
        result.finish(start);
        logger.info("Executed " + method + " request to URL: " + url + " with result: " + result.getResult() + " elapsed [ms] = " + (System.currentTimeMillis() - s));

        return result;
    }

    public AjaxResult call() throws Exception {
        AjaxResult singleResult = null;
        final Date start = new Date();
        try {
            final String[] split = data.split(";;;");
            String method = split[0];
            String url =  split.length > 1 ? split[1] : null;
            String postData = split.length > 2 ? split[2] : null;
            String headers = split.length > 3 ? split[3] : null;
            
            singleResult = execute(method, url, postData, headers, ctx);
        } catch (IOException e) {
            singleResult = singleErrorResult(start);
        }
        return singleResult;
    }

}
