package org.codetab.gotz.step.load;

import java.util.List;

import org.codetab.gotz.exception.StepRunException;
import org.codetab.gotz.model.DataSet;
import org.codetab.gotz.step.IStep;
import org.codetab.gotz.step.base.BaseAppender;

public class DataSetAppender extends BaseAppender {

    @Override
    public IStep instance() {
        return this;
    }

    @Override
    public boolean process() {
        List<DataSet> dataSets = getDataSets();
        doAppend(dataSets);
        return false;
    }

    /**
     * <p>
     * If step input object is of type List<DataSet> then casts the object to
     * list and returns it. Otherwise throws exception.
     * @return step input as List of dataset.
     * @throws StepRunException
     *             if step input is not of type List<DataSet>
     */
    @SuppressWarnings("unchecked")
    private List<DataSet> getDataSets() {
        Object data = getData();
        if (data instanceof List<?>) {
            List<?> list = (List<?>) data;
            if (list.size() > 0) {
                if (list.get(0) instanceof DataSet) {
                    return (List<DataSet>) list;
                }
            } else {
                // if empty list then return it
                return (List<DataSet>) list;
            }
        }
        throw new StepRunException("data is not of type List<DataSet>");
    }

}
