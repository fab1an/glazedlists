/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.query;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// for collections of results
import java.util.*;
// glazed tasks are simple background jobs
import com.odellengineeringltd.glazedtasks.*;

/**
 * A query list that creates a task for each query that requires running,
 * and runs such tasks using a Glazed Tasks' TaskManager. A Task Manager
 * provides thread pooling and progress monitoring to background tasks.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskQueryList extends QueryList {
    
    /** the task manager to run the task on */
    public TaskManager taskManager;
    
    /** the previously executing query task */
    public TaskContext queryTaskContext;
    
    /**
     * Create a new TaskQueryList with no query.
     */
    public TaskQueryList(TaskManager taskManager) {
        this.taskManager = taskManager;
    }
    
    /**
     * Sets the query list to display the specified query. The list
     * will not change immediately, instead it will only change when
     * the specified query has loaded. As a woraround, consider
     * setting the query to an empty query to clear the list and then
     * to the desired query.
     */
    public synchronized void setQuery(Query query) {
        // interrupt the previous query!
        queryTaskContext.cancelTask();
        
        // execute this query on a task
        queryTaskContext = taskManager.runTask(new QueryTask(query));
    }
    
    /**
     * Simple task that executes a query and sets the results of that
     * query to the body of this list.
     */
    class QueryTask implements Task {
        
        /** the context of this task */
        private TaskContext taskContext;
        
        /** the query to execute */
        private Query query;
        
        /**
         * Create a new query task that executes the specified query.
         */
        public QueryTask(Query query) {
            this.query = query;
        }
        
        /**
         * Manage the task context.
         */
        public void setTaskContext(TaskContext taskContext) {
            this.taskContext = taskContext;
        }
        public void unsetTaskContext() { 
            this.taskContext = null;
        }

        /**
         * When this task is executed it sets it's context as busy
         * and performs the query on its background thread. It then
         * sets the query results and sets itself as completed.
         */
        public int doTask() throws InterruptedException, Exception {
            // do the query
            taskContext.setBusy(true);
            SortedSet results = query.doQuery();
            
            // don't set the results unless this request has not been cancelled
            if(Thread.interrupted()) throw new InterruptedException();
            setQueryResults(results);
            
            // we're done
            taskContext.setBusy(false);
            return Task.COMPLETE;
        }
        
        /**
         * Gets this task's name.
         */
        public String toString() {
            return "Running query: " + query;
        }
    }
}
