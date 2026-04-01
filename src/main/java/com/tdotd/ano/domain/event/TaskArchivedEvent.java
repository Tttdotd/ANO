package com.tdotd.ano.domain.event;

/**
 * 任务已成功标记为「已归档」后发布的事件载荷。
 * <p>
 * 仅携带业务所需的最小数据（taskId）。监听器在
 * {@link org.springframework.transaction.event.TransactionPhase#AFTER_COMMIT} 阶段执行，
 * 保证此时数据库里任务状态已是 ARCHIVED，异步线程读库不会遇到「未提交」的竞态。
 */
public record TaskArchivedEvent(String taskId) {
}
