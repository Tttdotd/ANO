package com.tdotd.ano.common.constant;

/**
 * 与 {@code ano_task.state} 及产品状态机一致。
 */
public final class TaskStates {

    public static final int TODO = 0;
    public static final int DOING = 1;
    public static final int NOTED = 2;
    public static final int DONE = 3;
    public static final int ARCHIVED = 4;

    private TaskStates() {
    }
}
