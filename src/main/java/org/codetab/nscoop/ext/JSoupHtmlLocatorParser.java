package org.codetab.nscoop.ext;

import java.util.List;

import org.codetab.nscoop.exception.FieldNotFoundException;
import org.codetab.nscoop.model.AxisName;
import org.codetab.nscoop.model.FieldsBase;
import org.codetab.nscoop.model.Locator;
import org.codetab.nscoop.model.Member;
import org.codetab.nscoop.shared.BeanService;
import org.codetab.nscoop.step.IStep;
import org.codetab.nscoop.util.FieldsUtil;
import org.codetab.nscoop.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JSoupHtmlLocatorParser extends JSoupHtmlParser {

    static final Logger LOGGER = LoggerFactory.getLogger(JSoupHtmlLocatorParser.class);

    @Override
    public IStep instance() {
        return new JSoupHtmlLocatorParser();
    }

    @Override
    public void handover() throws Exception {
        final long sleepMillis = 1000;
        for (Member member : getData().getMembers()) {
            Locator locator = createLocator(member);
            String stepType = getStepType();
            setStepType("seeder");
            pushTask(locator, locator.getFields());
            setStepType(stepType);
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
            }
        }
    }

    private Locator createLocator(final Member member) throws FieldNotFoundException {
        Locator locator = new Locator();
        locator.setName(FieldsUtil.getValue(getFields(), "locatorName"));
        locator.setUrl(member.getValue(AxisName.FACT));
        if (member.getGroup() == null) {
            String message = Util.buildString(
                    "unable to create new locator. define group for member ",
                    "in datadef of locator type ", member.getName());
            throw new FieldNotFoundException(message);
        } else {
            locator.setGroup(member.getGroup());
            List<FieldsBase> groupFields = getGroupFields(locator.getGroup());
            locator.getFields().addAll(groupFields);
            List<FieldsBase> stepFields = getGroupFields("steps");
            locator.getFields().addAll(stepFields);
            if (member.getFields() != null) {
                locator.getFields().addAll(member.getFields());
            }
        }
        LOGGER.trace("created new {} {}", locator, locator.getUrl());
        return locator;
    }

    private List<FieldsBase> getGroupFields(final String group)
            throws FieldNotFoundException {
        List<FieldsBase> fieldsBeans = BeanService.INSTANCE.getBeans(FieldsBase.class);
        FieldsBase classFields = FieldsUtil.getFieldsByValue(fieldsBeans, "class",
                Locator.class.getName());
        if (classFields != null) {
            List<FieldsBase> fields = FieldsUtil.getGroupFields(classFields, group);
            return fields;
        }
        return null;
    }

    @Override
    public void store() throws Exception {
        // not required - don't throw illegal oper
    }
}
