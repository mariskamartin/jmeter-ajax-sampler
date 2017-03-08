package com.mmariska.jmeter.ajax.impl;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.threads.JMeterContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public final class AjaxExecutor {

    private static final Logger logger = LoggingManager.getLoggerForClass();

    public static Collection<AjaxResult> execute(Map<String, String> args, JMeterContext ctx, Entry e) throws InterruptedException, ExecutionException {
        Collection<AjaxResult> results = new LinkedList<AjaxResult>();
        final int argCount = args.values().size();
        ExecutorService executorService = Executors.newFixedThreadPool(argCount);
        List<Future<AjaxResult>> ajaxCallsFutures = new ArrayList<Future<AjaxResult>>(argCount);
        for (Map.Entry<String, String> entry : args.entrySet()) {
            Future<AjaxResult> future = executorService.submit(new AjaxCall(entry.getKey(), entry.getValue(), ctx, e));
            ajaxCallsFutures.add(future);
        }
        for (Future<AjaxResult> future : ajaxCallsFutures) {
            AjaxResult singleResult = future.get();
            results.add(singleResult);
        }
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.MINUTES);
        return results;

    }

}
