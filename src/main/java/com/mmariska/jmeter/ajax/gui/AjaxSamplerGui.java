package com.mmariska.jmeter.ajax.gui;

import com.mmariska.jmeter.ajax.AjaxSampler;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

import javax.swing.*;
import java.awt.*;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class AjaxSamplerGui extends AbstractSamplerGui {

    private static final Logger logger = LoggingManager.getLoggerForClass();
    private ArgumentsPanel argsPanel;
    private ObjectTableModel objectTableModel;

    public AjaxSamplerGui() {
        super();
        init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "Ajax sampler"; // $NON-NLS-1$
    }

    @Override
    public String getStaticLabel() {
        return "Ajax Sampler";
    }

    @Override
    public TestElement createTestElement() {
        AjaxSampler sampler = new AjaxSampler();
        objectTableModel.clearData();
//        logger.info("createTestElement sampler " + sampler + " tableData.rowcount=" + objectTableModel.getRowCount());
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);
        AjaxSampler ajaxSampler = (AjaxSampler) sampler;
        final Arguments modifiedArgs = (Arguments) argsPanel.createTestElement();
//        logger.info("modifyTestElement sampler " + sampler + " store args= " + modifiedArgs);
        ajaxSampler.setArguments(modifiedArgs);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof AjaxSampler) {
            objectTableModel.clearData();
            final Arguments arguments = ((AjaxSampler) el).getArguments();
            if (arguments != null) {
//                logger.info("configure el=" + el + " args=" + arguments);
                PropertyIterator iter = arguments.iterator();
                while (iter.hasNext()) {
                    objectTableModel.addRow((Argument) iter.next().getObjectValue());
                }
            }
        }
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(makeArgumentsPanel(), BorderLayout.CENTER);

        JPanel streamsCodePane = new JPanel(new BorderLayout());
        add(streamsCodePane, BorderLayout.SOUTH);
    }

    private JPanel makeArgumentsPanel() {
        objectTableModel = new ObjectTableModel(new String[]{"Name", "Value"},
                Argument.class,
                new Functor[]{
                    new Functor("getName"), // $NON-NLS-1$
                    new Functor("getValue"), // $NON-NLS-1$
                }, // $NON-NLS-1$
                new Functor[]{
                    new Functor("setName"), // $NON-NLS-1$
                    new Functor("setValue"), // $NON-NLS-1$
                }, // $NON-NLS-1$
                new Class[]{String.class, String.class});
        //this is mapping values from GUI to Argument Class
        argsPanel = new ArgumentsPanel("AJAX Request Details", null, true, false, objectTableModel);
        return argsPanel;
    }

}
