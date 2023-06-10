package com.udacity.location_reminders.utils.condition_watcher;

import android.os.Bundle;

/**
 * Created by F1sherKK on 16/12/15.
 * https://github.com/AzimoLabs/ConditionWatcher/tree/master
 *
 * Imported here to resolve flak issue
 *
 */
public abstract class Instruction {

    private Bundle dataContainer = new Bundle();

    private boolean _skipException = false;

    public boolean skipException() {
        return _skipException;
    }

    public void disableThrowingExceptions() {
        _skipException = true;
    }

    public final void setData(Bundle dataContainer) {
        this.dataContainer = dataContainer;
    }

    public final Bundle getDataContainer() {
        return dataContainer;
    }

    public abstract String getDescription();

    public abstract boolean checkCondition();
}
