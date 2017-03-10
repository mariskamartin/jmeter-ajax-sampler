package com.mmariska.jmeter.ajax;

import com.mmariska.jmeter.ajax.impl.AjaxCall;
import com.mmariska.jmeter.ajax.impl.AjaxResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContext;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is custom sampler for parallel AJAX/XHR calls
 *
 * @author mmariska
 * @version 1.0.0
 */
public class AjaxSampler extends AbstractSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String MAX_POOLSIZE_VARNAME = "AjaxSampler.max-concurrent-threads";
    private static final int MAX_CONCURRENT_CHROME_POOL_SIZE = 6;
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String ARGUMENTS = "AjaxSampler.arguments";

    @Override
    public SampleResult sample(Entry entry) {
        final JavaSamplerContext jctx = new JavaSamplerContext(getArguments());
        Map<String, String> args = getArgsWithExpressions(jctx);
        Collection<AjaxResult> results = new ArrayList<AjaxResult>();
        final int concurrentThreadPoolSize = getThreadPoolSize();
        if (log.isDebugEnabled()) log.debug("Concurent thread pool size = " + concurrentThreadPoolSize);

        SampleResult rv = new SampleResult();
        rv.setSampleLabel(getName());
        rv.setDataType(SampleResult.TEXT);
        rv.sampleStart();
        try {
            results = execute(args, getThreadContext(), entry, concurrentThreadPoolSize);
            boolean hasError = hasResultsErrors(results);
            rv.setSuccessful(!hasError);
            rv.setResponseMessage("AJAX Requests Execution was" + (hasError ? "n't" : "") + " successful");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            rv.setSuccessful(false);
            rv.setResponseMessage("AJAX Requests Execution failed in java code. \n" + ex.getMessage());
        } finally {
            rv.sampleEnd();
        }

        StringBuilder datawriter = new StringBuilder();
        for (AjaxResult result : results) {
            datawriter.append(result.getUrl())
                    .append(" - elapsed [ms] = ").append(result.getElapsedTime())
                    .append(" - result = ").append(result.getResult())
                    .append(" - resp. bytes = ").append(result.getResponseByteSize())
                    .append(NEWLINE);
        }
        rv.setResponseData(datawriter.toString(), "UTF-8");
        return rv;
    }

    private int getThreadPoolSize() {
        final String poolSize = this.getThreadContext().getVariables().get(MAX_POOLSIZE_VARNAME);
        return poolSize == null ? MAX_CONCURRENT_CHROME_POOL_SIZE : Integer.valueOf(poolSize);
    }

    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    private boolean hasResultsErrors(Collection<AjaxResult> results) {
        for (Iterator<AjaxResult> iterator = results.iterator(); iterator.hasNext();) {
            if (!iterator.next().isOk()) {
                return true;
            }
        }
        return false;
    }

    private void printSamplerProperties(JavaSamplerContext jctx) {
//        final PropertyIterator pi = this.propertyIterator();
//        StringBuilder sb = new StringBuilder();
//        while (pi.hasNext()) {
//            final JMeterProperty next = pi.next();
//            sb.append(next.getName()).append(": ").append(next.getStringValue()).append(" | ");
//        }
//        logger.info("Sampler properties = " + sb.toString());
//        final JMeterProperty property = this.getProperty(ARGUMENTS);
//        logger.info("Sampler ARGUMENTS properties = " + property.getStringValue());
//        final TestElementProperty a = (TestElementProperty) this.getProperty(ARGUMENTS);
//        final PropertyIterator iterator = a.iterator();
//        while (iterator.hasNext()) {
//            final JMeterProperty prop = iterator.next();
//            logger.info("testElem > " + prop.getName() + "=" + prop.getStringValue());
//            if(prop.getName().equals("Arguments.arguments")){
//                List<TestElementProperty> l = (List<TestElementProperty>) prop.getObjectValue();
//                for (TestElementProperty argument : l) {
//                    logger.info("args > " + argument.getName() + " = " + argument.getStringValue());
//                }
//            }
//        }
//        Iterator argsIt = jctx.getParameterNamesIterator();
//        while (argsIt.hasNext()) {
//            String name = (String) argsIt.next();
//            logger.info("CtxProp > " + name + "=" + jctx.getParameter(name));
//        }
    }

    private Map<String, String> getArgsWithExpressions(JavaSamplerContext jctx) {
        final HashMap<String, String> args = new HashMap<String, String>();
        Iterator contextArguments = jctx.getParameterNamesIterator();
        while (contextArguments.hasNext()) {
            String name = (String) contextArguments.next();
            args.put(name, jctx.getParameter(name));
        }
// RAW arguments
//        final Arguments arguments = getArguments();
//        for (int i = 0; i < arguments.getArgumentCount(); i++) {
//            Argument a = arguments.getArgument(i);
//            if (jctx.containsParameter(a.getName())) {
//                args.put(a.getName(), jctx.getParameter(a.getName()));
//            } else {
//                args.put(a.getName(), a.getValue());
//            }
//        }
        if (log.isDebugEnabled()) {
            log.debug("ARGS: " + args.toString());
        }
        return args;
    }

    private Collection<AjaxResult> execute(Map<String, String> args, JMeterContext jmeterCtx, Entry e, int concurrentThreadPoolSize) throws InterruptedException, ExecutionException {
        Collection<AjaxResult> results = new LinkedList<AjaxResult>();
        final int argsSize = args.values().size();
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(argsSize, concurrentThreadPoolSize));
        List<Future<AjaxResult>> ajaxCallsFutures = new ArrayList<Future<AjaxResult>>(argsSize);
        for (Map.Entry<String, String> entry : args.entrySet()) {
            Future<AjaxResult> future = executorService.submit(new AjaxCall(entry.getKey(), entry.getValue(), jmeterCtx, e));
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
