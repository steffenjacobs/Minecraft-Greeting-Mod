package me.steffenjacobs.greetingmod.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * <p>A highly efficient, thread-safe, lock-free implementation of
 * {@link com.google.common.util.concurrent.ListeningExecutorService} that uses the
 * main Minecraft thread to execute tasks.</p>
 * <p>Limited scheduling is available via the {@link #schedule(Runnable, long)} method.</p>
 * <p>This ExecutorService cannot be shut down or terminated.</p>
 * <p>If tasks are added from inside a task executed by this Scheduler, they will be executed in the same tick as the
 * task adding the new tasks. If a task is scheduled from inside another task, the current tick will count as the first
 * waiting tick.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Scheduler {
    private static final Logger LOG = LogManager.getLogger();

    private static final Scheduler INSTANCE;

    private Scheduler() {
    }

    public static Scheduler instance() {
        return INSTANCE;
    }

    /**
     * <p>Execute the given task after {@code tickDelay} ticks have passed. There are 40 game ticks per second in
     * minecraft forge client.</p>
     *
     * @param r         the task
     * @param tickDelay the delay, in ticks
     */
    public void schedule(Runnable r, long tickDelay) {
        execute(new WaitingTask(r, tickDelay));
    }

    public void execute(Runnable task) {
        execute(new WrappedRunnable(task));
    }

    public void execute(Task task) {
        inputQueue.offer(task);
    }

    static {
        INSTANCE = new Scheduler();
    }

    // this is the queue that holds new tasks until they are picked up by the main thread
    private final ConcurrentLinkedQueue<Task> inputQueue = new ConcurrentLinkedQueue<>();

    // only used by the main thread
    private Task[] activeTasks = new Task[5];
    private int size = 0; // actual number of tasks in the above array, used for adding to the end

    public void tick() {
        Task[] activeTasksLocal = this.activeTasks;
        int sizeLocal = this.size;
        handleExistingTasks(activeTasksLocal, sizeLocal);
        handleNewTasks(activeTasksLocal, sizeLocal);
    }

    private void handleNewTasks(Task[] activeTasksLocal, int sizeLocal) {
        // handle new tasks
        Task task;
        while ((task = inputQueue.poll()) != null) {
            // only add task to the active list if it wants to keep executing
            // avoids unnecessary work for one-off tasks
            if (checkedExecute(task)) {
                if (sizeLocal == activeTasksLocal.length) {
                    // we are full
                    Task[] newArr = new Task[sizeLocal << 1];
                    System.arraycopy(activeTasksLocal, 0, newArr, 0, sizeLocal);
                    activeTasksLocal = this.activeTasks = newArr;
                }
                activeTasksLocal[sizeLocal] = task;
                this.size++;
            }
        }
    }

    private void handleExistingTasks(Task[] activeTasksLocal, int sizeLocal) {
        // handle existing tasks

        // move through task list and simultaneously execute tasks and compact the list
        // by moving non-removed tasks to the new end of the list if needed
        int idx = 0;
        int free = -1;
        while (idx < sizeLocal) {
            Task t = activeTasksLocal[idx];
            if (!checkedExecute(t)) {
                // task needs to be removed, null out it's slot
                activeTasksLocal[idx] = null;
                if (free == -1) {
                    // if this is the first task to be removed, set it as the compaction target
                    free = idx;
                }
            } else if (free != -1) {
                // we had to remove one or more tasks earlier in the list,
                // move this one there to keep the list continuous
                activeTasksLocal[free++] = t;
                activeTasksLocal[idx] = null;
            }
            idx++;
        }
        // we had to remove at least one task, adjust the sizeLocal
        if (free != -1) {
            this.size = free;
        }
    }

    private static boolean checkedExecute(Task task) {
        try {
            return task.execute();
        } catch (Exception x) {
            LOG.error(String.format("Exception thrown during execution of %s", task));
            return false;
        }
    }

    public interface Task {

        /**
         * <p>Execute this task, return true to keep executing.</p>
         *
         * @return true to keep executing
         */
        boolean execute();

    }

    private static final class WaitingTask implements Task {

        private final Runnable r;
        private long ticks;

        WaitingTask(Runnable r, long ticks) {
            this.r = r;
            this.ticks = ticks;
        }

        @Override
        public boolean execute() {
            if (--ticks == 0) {
                r.run();
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String toString() {
            return String.format("Scheduled task (task=%s, remainingTicks=%s)", r, ticks);
        }
    }

    private static class WrappedRunnable implements Task {
        private final Runnable task;

        public WrappedRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public boolean execute() {
            task.run();
            return false;
        }

        @Override
        public String toString() {
            return task.toString();
        }
    }
}
