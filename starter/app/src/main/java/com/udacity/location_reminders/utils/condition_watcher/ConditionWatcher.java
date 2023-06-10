package com.udacity.location_reminders.utils.condition_watcher;


/**
 * Created by F1sherKK on 08/10/15.
 * https://github.com/AzimoLabs/ConditionWatcher/tree/master
 *
 * Imported here to resolve flak issue
 *
 */
public class ConditionWatcher {

    public static final int CONDITION_NOT_MET = 0;
    public static final int CONDITION_MET = 1;
    public static final int TIMEOUT = 2;

    public static final int DEFAULT_TIMEOUT_LIMIT = 1000 * 5;
    public static final int DEFAULT_INTERVAL = 16;

    private int timeoutLimit = DEFAULT_TIMEOUT_LIMIT;
    private int watchInterval = DEFAULT_INTERVAL;

    private static ConditionWatcher conditionWatcher;

    private ConditionWatcher() {
        super();
    }

    public static ConditionWatcher getInstance() {
        if (conditionWatcher == null) {
            conditionWatcher = new ConditionWatcher();
        }
        return conditionWatcher;
    }

    public static void waitForCondition(Instruction instruction) throws Exception {
        waitForCondition(instruction, getInstance().timeoutLimit, getInstance().watchInterval);
    }

    public static void waitForCondition(Instruction instruction, int timeoutLimit) throws Exception {
        waitForCondition(instruction, timeoutLimit, getInstance().watchInterval);
    }

    public static void waitForCondition(Instruction instruction, int timeoutLimit, int watchInterval) throws Exception {
        int status = CONDITION_NOT_MET;
        int elapsedTime = 0;

        do {
            if (instruction.checkCondition()) {
                status = CONDITION_MET;
            } else {
                elapsedTime += watchInterval;
                Thread.sleep(watchInterval);
            }

            if (elapsedTime >= timeoutLimit) {
                status = TIMEOUT;
                break;
            }
        } while (status != CONDITION_MET);

        if (status == TIMEOUT && !instruction.skipException())
            throw new Exception(instruction.getDescription() + " - took more than " + timeoutLimit/1000 + " seconds. Test stopped.");
    }

    public static void setWatchInterval(int watchInterval) {
        getInstance().watchInterval = watchInterval;
    }

    public static void setTimeoutLimit(int ms) {
        getInstance().timeoutLimit = ms;
    }
}

