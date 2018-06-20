package ru.spbau.dkaznacheev.benchmark;

public class TestResult {

    public int getCalcTime() {
        return calcTime;
    }

    public int getHandleTime() {
        return handleTime;
    }

    private final int calcTime;
    private final int handleTime;

    public TestResult(int averageCalcTime, int averageHandleTime) {
        calcTime = averageCalcTime;
        handleTime = averageHandleTime;
    }
}
