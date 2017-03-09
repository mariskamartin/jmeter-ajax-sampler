package com.mmariska.jmeter.ajax;

import com.mmariska.jmeter.ajax.impl.AjaxExecutor;
import com.mmariska.jmeter.ajax.impl.AjaxResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is custom sampler for parallel AJAX/XHR calls
 *
 * @author mmariska
 * @version 1.0.0
 */
public class AjaxSampler extends AbstractSampler {

    private static final Logger logger = LoggingManager.getLoggerForClass();
    private static final String NEWLINE = System.getProperty("line.separator");
    private static final String ARGUMENTS = "AjaxSampler.arguments";

    @Override
    public SampleResult sample(Entry entry) {
        final JavaSamplerContext jctx = new JavaSamplerContext(getArguments());
        Map<String, String> args = getArgsWithExpressions(jctx);
        Collection<AjaxResult> results = new ArrayList<AjaxResult>();

        SampleResult rv = new SampleResult();
        rv.setSampleLabel(getName());
        rv.setDataType(SampleResult.TEXT);
        rv.sampleStart();
        try {
            results = AjaxExecutor.execute(args, getThreadContext(), entry);
            boolean hasError = hasResultsErrors(results);
            rv.setSuccessful(!hasError);
            rv.setResponseMessage("AJAX Requests Execution was" + (hasError ? "n't" : "") + " successful");
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            rv.setSuccessful(false);
            rv.setResponseMessage("AJAX Requests Execution failed in java code");
        } finally {
            rv.sampleEnd();
        }

        StringBuilder datawriter = new StringBuilder();
        for (AjaxResult result : results) {
            datawriter.append(result.getUrl())
                    .append(" - result = ").append(result.getResult())
                    .append(" - elapsed time [ms] = ").append(result.getElapsedTime())
                    .append(NEWLINE);
        }
        rv.setResponseData(datawriter.toString(), "UTF-8");
        return rv;
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
//        final Arguments arguments = getArguments();
//        for (int i = 0; i < arguments.getArgumentCount(); i++) {
//            Argument a = arguments.getArgument(i);
//            if (jctx.containsParameter(a.getName())) {
//                args.put(a.getName(), jctx.getParameter(a.getName()));
//            } else {
//                args.put(a.getName(), a.getValue());
//            }
//        }
        if (logger.isDebugEnabled()) {
            logger.debug("ARGS: " + args.toString());
        }
        return args;
    }
}
