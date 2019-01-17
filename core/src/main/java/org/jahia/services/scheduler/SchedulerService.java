/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.scheduler;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.JahiaService;
import org.jahia.settings.readonlymode.ReadOnlyModeCapable;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.*;
import java.util.regex.Pattern;

import static org.jahia.services.scheduler.BackgroundJob.*;

/**
 * Jahia background task scheduling and management service.
 *
 * @author Sergiy Shyrkov
 */
public class SchedulerService extends JahiaService implements ReadOnlyModeCapable {

    /**
     * Jahia Spring factory bean that creates, but does not start Quartz scheduler instance. So the instance remain in standby mode until
     * the scheduler is explicitly started.
     *
     * @author Sergiy Shyrkov
     */
    public static class JahiaSchedulerFactoryBean extends SchedulerFactoryBean {

        @Override
        public void start() {
            // do nothing
        }
    }

    public static final String INSTANT_TRIGGER_GROUP = "instant";

    static Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private static final Map<Pattern, Long> PURGE_ALL_STRATEGY = Collections.singletonMap(
            Pattern.compile(".*"), Long.valueOf(0));

    public static final String SYSTEM_JOB_GROUP = "system";

    private Scheduler ramScheduler = null;

    private ReadOnlyModeAwareScheduler scheduler = null;

    private long timeoutSwitchingToReadOnlyMode;

    private ThreadLocal<List<JobDetail>> scheduledAtEndOfRequest = new ThreadLocal<List<JobDetail>>();

    private ThreadLocal<List<JobDetail>> ramScheduledAtEndOfRequest = new ThreadLocal<List<JobDetail>>();

    private JahiaJobListener jahiaGlobalJobListener = new JahiaJobListener(false);
    private JahiaJobListener jahiaGlobalRamJobListener = new JahiaJobListener(true);

    public Integer deleteAllCompletedJobs() throws SchedulerException {
        return deleteAllCompletedJobs(PURGE_ALL_STRATEGY, true);
    }

    public Integer deleteAllCompletedRAMJobs() throws SchedulerException {
        return deleteAllCompletedJobs(PURGE_ALL_STRATEGY, true, true);
    }

    public Integer deleteAllCompletedJobs(Map<Pattern, Long> purgeStrategy,
                                          boolean purgeWithNoEndDate) throws SchedulerException {
        return deleteAllCompletedJobs(purgeStrategy, purgeWithNoEndDate, false);
    }

    public Integer deleteAllCompletedJobs(Map<Pattern, Long> purgeStrategy,
                                          boolean purgeWithNoEndDate, boolean isRAMScheduler) throws SchedulerException {
        logger.info("Start looking for completed jobs in {} scheduler", isRAMScheduler ? "RAM" : "persistent");
        int deletedCount = 0;

        Scheduler schedulerInstance = isRAMScheduler ? getRAMScheduler() : getScheduler();
        for (String jobGroup : schedulerInstance.getJobGroupNames()) {
            String[] jobNames = schedulerInstance.getJobNames(jobGroup);
            logger.info("Processing job group {} with {} jobs", jobGroup, jobNames.length);
            for (String jobName : jobNames) {
                logger.debug("Checking job {}.{}", jobGroup, jobName);
                if (ArrayUtils.isEmpty(schedulerInstance.getTriggersOfJob(jobName, jobGroup))) {
                    Long age = getAge(jobName, jobGroup, purgeStrategy);
                    if (age != null && age.longValue() >= 0) {
                        JobDetail job = schedulerInstance.getJobDetail(jobName, jobGroup);
                        if (job == null) {
                            logger.warn("Unable to find job {}.{}", jobGroup, jobName);
                            continue;
                        }
                        String status = job.getJobDataMap().getString(JOB_STATUS);
                        if (STATUS_SUCCESSFUL.equals(status) || STATUS_FAILED.equals(status)
                                || STATUS_CANCELED.equals(status)) {
                            Long ended = job.getJobDataMap().containsKey(JOB_END) ? job
                                    .getJobDataMap().getLongFromString(JOB_END) : null;
                            if (ended != null && (System.currentTimeMillis() - ended > age)
                                    || ended == null && purgeWithNoEndDate) {
                                logger.debug("Job {} matches purge policy. Deleting it.",
                                        job.getFullName());
                                try {
                                    schedulerInstance.deleteJob(jobName, jobGroup);
                                    deletedCount++;
                                } catch (SchedulerException e) {
                                    logger.warn("Error deleting job " + jobGroup + "." + jobName, e);
                                }
                            }
                        }
                    }
                }
            }

        }

        logger.info("Removed {} completed jobs", deletedCount);

        return deletedCount;
    }

    protected Long getAge(String jobName, String jobGroup, Map<Pattern, Long> purgeStrategy) {
        Long expiration = null;
        String key = jobGroup + "." + jobName;
        for (Map.Entry<Pattern, Long> purgeEntry : purgeStrategy.entrySet()) {
            if (purgeEntry.getKey().matcher(key).matches()) {
                expiration = purgeEntry.getValue();
                break;
            }
        }

        return expiration;
    }

    public List<JobDetail> getAllActiveJobs() throws SchedulerException {
        List<JobDetail> l = new LinkedList<JobDetail>();
        for (String group : scheduler.getTriggerGroupNames()) {
            l.addAll(getAllActiveJobs(group));
        }
        return l;
    }

    public List<JobDetail> getAllActiveJobs(String triggerGroup) throws SchedulerException {

        String group = StringUtils.isNotEmpty(triggerGroup) ? triggerGroup
                : Scheduler.DEFAULT_GROUP;

        String[] trigs = scheduler.getTriggerNames(group);
        if (trigs == null || trigs.length == 0) {
            return Collections.emptyList();
        }
        List<JobDetail> all = new LinkedList<JobDetail>();

        for (String name : trigs) {
            Trigger t = scheduler.getTrigger(name, group);
            if (t != null && !SYSTEM_JOB_GROUP.equals(t.getJobGroup())) {
                JobDetail jd = scheduler.getJobDetail(t.getJobName(), t.getJobGroup());
                if (jd != null) {
                    if (STATUS_EXECUTING.equals(jd.getJobDataMap().getString(JOB_STATUS))) {
                        all.add(jd);
                    }
                }
            }
        }

        return all;
    }

    public List<JobDetail> getAllJobs() throws SchedulerException {
        List<JobDetail> l = new LinkedList<JobDetail>();
        for (String group : scheduler.getJobGroupNames()) {
            l.addAll(getAllJobs(group));
        }
        return l;
    }

    public List<JobDetail> getAllRAMJobs() throws SchedulerException {
        List<JobDetail> l = new LinkedList<JobDetail>();
        for (String group : getRAMScheduler().getJobGroupNames()) {
            l.addAll(getAllJobs(group, true));
        }
        return l;
    }

    public List<JobDetail> getAllJobs(String groupname) throws SchedulerException {
        return getAllJobs(groupname, false);
    }

    public List<JobDetail> getAllJobs(String groupname, boolean useRamScheduler)
            throws SchedulerException {

        String group = StringUtils.isNotEmpty(groupname) ? groupname : Scheduler.DEFAULT_GROUP;

        List<JobDetail> all = new LinkedList<JobDetail>();
        for (String process : getScheduler(useRamScheduler).getJobNames(group)) {
            all.add(getScheduler(useRamScheduler).getJobDetail(process, group));
        }
        return all;
    }

    public Scheduler getRAMScheduler() {
        return ramScheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    protected Scheduler getScheduler(boolean isRamScheduler) {
        return isRamScheduler ? ramScheduler : scheduler;
    }

    public synchronized void startSchedulers() throws JahiaInitializationException {
        try {
            ramScheduler.start();

            if (settingsBean.isProcessingServer()) {
                if (logger.isDebugEnabled()) {
                    SchedulerMetaData schedulerMetadata = scheduler.getMetaData();
                    logger.debug("Starting scheduler...\n"
                               + " instanceId:{} instanceName:{}\n"
                               + "{}",
                        new Object[] {
                            schedulerMetadata.getSchedulerInstanceId(),
                            schedulerMetadata.getSchedulerName(),
                            schedulerMetadata.getSummary()
                        }
                    );
                }

                scheduler.start();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            throw new JahiaInitializationException(e.getMessage(), e);
        }
    }

    public void scheduleJobNow(JobDetail jobDetail) throws SchedulerException {
        scheduleJobNow(jobDetail, false);
    }

    public void scheduleJobNow(JobDetail jobDetail, boolean useRamScheduler) throws SchedulerException {
        JobDataMap data = jobDetail.getJobDataMap();
        SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName() + "_Trigger",
                INSTANT_TRIGGER_GROUP);
        trigger.setVolatility(jobDetail.isVolatile());// volatility of trigger
                                                      // depending of the
                                                      // jobdetail's volatility

        data.put(JOB_STATUS, STATUS_ADDED);
        logger.debug("schedule job {} volatile({}) @ {}", new Object[] {jobDetail.getName(), jobDetail.isVolatile(), new Date(System.currentTimeMillis())});
        if (useRamScheduler) {
            ramScheduler.scheduleJob(jobDetail, trigger);
        } else {
            scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    public void scheduleJobAtEndOfRequest(JobDetail jobDetail) throws SchedulerException {
        scheduleJobAtEndOfRequest(jobDetail, false);
    }

    public void scheduleJobAtEndOfRequest(JobDetail jobDetail, boolean useRamScheduler) throws SchedulerException {
        List<JobDetail> jobList = useRamScheduler?ramScheduledAtEndOfRequest.get():scheduledAtEndOfRequest.get();

        if (jobList == null) {
            jobList = new ArrayList<JobDetail>();
            if (useRamScheduler) {
                ramScheduledAtEndOfRequest.set(jobList);
            } else {
                scheduledAtEndOfRequest.set(jobList);
            }
        }
        jobList.add(jobDetail);
    }

    public void triggerEndOfRequest() {
        if (ramScheduledAtEndOfRequest.get() != null) {
            triggerJob(true);
        }
        if (scheduledAtEndOfRequest.get() != null) {
            triggerJob(false);
        }
    }

    private void triggerJob(boolean useRamScheduler) {
        List<JobDetail> jobList;
        if (useRamScheduler) {
            jobList = ramScheduledAtEndOfRequest.get();
            ramScheduledAtEndOfRequest.set(null);
        } else {
            jobList = scheduledAtEndOfRequest.get();
            scheduledAtEndOfRequest.set(null);
        }
        if (jobList != null) {
            for (JobDetail detail : jobList) {
                try {
                    scheduleJobNow(detail, useRamScheduler);
                } catch (SchedulerException e) {
                    logger.error("Cannot schedule job", e);
                }
            }
        }

    }

    public void setRamScheduler(Scheduler ramscheduler) {
        this.ramScheduler = ramscheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = new ReadOnlyModeAwareScheduler(scheduler);
    }

    public void setTimeoutSwitchingToReadOnlyMode(long timeoutSwitchingToReadOnlyMode) {
        this.timeoutSwitchingToReadOnlyMode = timeoutSwitchingToReadOnlyMode;
    }

    /**
     * Add a global job listener
     * @param jobListener the listener
     * @param useRamScheduler add listener on ramScheduler or scheduler
     */
    public void addJobListener(JobListener jobListener, boolean useRamScheduler) {
        if(useRamScheduler) {
            jahiaGlobalRamJobListener.addJobListener(jobListener);
        } else {
            jahiaGlobalJobListener.addJobListener(jobListener);
        }
    }

    /**
     * Remove a global job listener
     * @param name the listener name
     * @param useRamScheduler remove listener from ramScheduler or scheduler
     */
    public void removeJobListener(String name, boolean useRamScheduler) {
        if(useRamScheduler) {
            jahiaGlobalRamJobListener.removeJobListener(name);
        } else {
            jahiaGlobalJobListener.removeJobListener(name);
        }
    }

    @Override
    public synchronized void start() throws JahiaInitializationException {

        try {
            ramScheduler.addSchedulerListener(new JahiaSchedulerListener(ramScheduler));
            scheduler.addSchedulerListener(new JahiaSchedulerListener(scheduler));

            ramScheduler.addGlobalJobListener(jahiaGlobalRamJobListener);
            scheduler.addGlobalJobListener(jahiaGlobalJobListener);

            // new LoggingJobHistoryPlugin().initialize("LoggingJobListener", scheduler);
            // new LoggingJobHistoryPlugin().initialize("RAMLoggingJobListener", ramScheduler);
        } catch (SchedulerException se) {
            if (se.getUnderlyingException() != null) {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service", se.getUnderlyingException());
            } else {
                throw new JahiaInitializationException(
                        "Error while initializing scheduler service", se);
            }
        }
    }

    @Override
    public synchronized void stop() {
        if (scheduler == null || ramScheduler == null) {
            return;
        }
        try {
            scheduler.shutdown(true);
            ramScheduler.shutdown(true);
            scheduler = null;
            ramScheduler = null;
        } catch (Exception se) {
            logger.error(se.getMessage(), se);
        }
    }

    @Override
    public int getReadOnlyModePriority() {
        return 800;
    }

    @Override
    public synchronized void switchReadOnlyMode(boolean enable) {

        // switch db persisted scheduler read only mode flag
        scheduler.setReadOnly(enable);

        if (enable) {
            logger.info("Entering read-only mode...");
            try {
                logger.info("Putting schedulers to standby...");
                standbySchedulers();
                logger.info("Done putting schedulers to standby");
                logger.info("Waiting for running jobs to complete...");
                long start = System.currentTimeMillis();
                for (int count = getRunningJobsCount(); count > 0; count = getRunningJobsCount()) {
                    logger.info("{} job(s) are still running...", count);
                    if (System.currentTimeMillis() - start > timeoutSwitchingToReadOnlyMode) {
                        logger.error("Timed out waiting for running jobs to complete.");
                        throw new JahiaRuntimeException("Wait timeout elapsed, jobs are still running");
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
                logger.info("All running jobs have completed.");
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("Exiting read-only mode...");
            try {
                logger.info("Starting schedulers...");
                startSchedulers();
                logger.info("Done starting schedulers");
            } catch (JahiaInitializationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void standbySchedulers() throws SchedulerException {
        ramScheduler.standby();
        if (settingsBean.isProcessingServer()) {
            scheduler.standby();
        }
    }

    private int getRunningJobsCount() throws SchedulerException {
        return (scheduler.getCurrentlyExecutingJobs().size() + ramScheduler.getCurrentlyExecutingJobs().size());
    }
}
