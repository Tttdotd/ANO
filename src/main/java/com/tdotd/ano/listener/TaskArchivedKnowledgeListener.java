package com.tdotd.ano.listener;

import com.tdotd.ano.domain.dto.KnowledgeArchiveRequest;
import com.tdotd.ano.domain.event.TaskArchivedEvent;
import com.tdotd.ano.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 「做法 A」：事务提交后再异步执行知识归档。
 * <p>
 * <b>为何用 {@link TransactionalEventListener#phase()} = {@link TransactionPhase#AFTER_COMMIT}？</b>
 * 若在带 {@code @Transactional} 的 {@code archiveTask} 方法里直接 {@code @Async} 调用归档，
 * 异步线程可能在主事务 <i>尚未 commit</i> 时就开始读库，出现读不到 ARCHIVED、或主事务回滚后仍执行归档等问题。
 * AFTER_COMMIT 由 Spring 事务管理器在 commit 成功之后触发，从语义上保证：异步逻辑开始时，任务归档已持久化。
 * <p>
 * <b>为何 {@code @Async} 放在单独的 Listener Bean 里？</b>
 * {@code @Async} 依赖 Spring AOP 代理；同类内部 {@code this.xxx()} 不会走代理，异步可能不生效。
 * 独立组件通过注入的 {@link KnowledgeService} 调用，代理链完整。
 * <p>
 * 异步方法中的异常不会传回 HTTP 调用方，因此这里捕获并打日志，便于运维排查；必要时可接告警或补偿任务。
 */
@Slf4j
@Component
public class TaskArchivedKnowledgeListener {

    private final KnowledgeService knowledgeService;

    public TaskArchivedKnowledgeListener(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onTaskArchived(TaskArchivedEvent event) {
        String taskId = event.taskId();
        try {
            String nodeId = knowledgeService.archiveKnowledge(new KnowledgeArchiveRequest(taskId));
            log.info("async knowledge archive done: taskId={}, nodeId={}", taskId, nodeId);
        } catch (Exception e) {
            log.warn("async knowledge archive failed: taskId={}", taskId, e);
        }
    }
}
