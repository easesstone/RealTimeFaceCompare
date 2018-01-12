package com.hzgc.collect.expand.log;

public class DataReceiveLogGroupWriter extends AbstractLogGroupWrite {
    @Override
    public boolean writeEvent(String event) {
        return false;
    }

    @Override
    public void logCheck() {

    }
}
