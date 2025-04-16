package ch.sbb.polarion.extension.cucumber.helper;

import lombok.Getter;

@Getter
public enum PolarionTestRunStatus {
    PASSED("passed", 3),
    FAILED("failed", 2);

    PolarionTestRunStatus(String id, int number) {
        this.id = id;
        this.number = number;
    }

    private final String id;
    private final int number;
}
