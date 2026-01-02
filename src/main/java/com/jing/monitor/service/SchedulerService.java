package com.jing.monitor.service;

import com.jing.monitor.core.CourseCrawler;
import com.jing.monitor.model.SectionInfo;
import com.jing.monitor.model.StatusMapping;
import com.jing.monitor.model.Task;
import com.jing.monitor.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Service responsible for scheduling course monitoring tasks.
 * Refactored V1.0: Implements Course-Level batch fetching to reduce API request frequency.
 */
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final CourseCrawler crawler;
    private final MailService mailService;
    private final TaskRepository taskRepository;
    private final Random random = new Random();

    // Define alert actions
    enum AlertAction { NONE, SEND_OPEN_EMAIL, SEND_WAITLIST_EMAIL }

    /**
     * Main Monitoring Loop.
     * Frequency should be set conservatively (e.g., 3-5 minutes) to avoid WAF blocking.
     */
    @Scheduled(fixedDelayString = "${monitor.poll-interval-ms}")
    public void monitorTask() {
        // 1. Aggregation: Fetch all tasks and deduplicate by Course ID
        List<Task> tasks = taskRepository.findByEnabledTrue();
        Set<String> courseSet = new HashSet<>();
        for(Task task : tasks){
            courseSet.add(task.getCourseId());
        }

        if (courseSet.isEmpty()) {
            System.out.println("[Scheduler] No active tasks. Idle.");
            return;
        }

        System.out.println("[Scheduler] Starting cycle. Monitoring " + courseSet.size() + " unique courses.");

        int cnt = courseSet.size();
        // 2. Batch Processing: Fetch data per Course (1 Request = N Sections)
        for (String courseId : courseSet) {
            processSingleCourse(courseId);
            cnt--;
            if(cnt == 0){
                break;
            }
            try {
                long sleepTime = 120000 + random.nextInt(10000);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Fetches all sections for a given course and updates local Task states.
     * @param courseId The 6-digit course identifier (e.g., "004289")
     */
    private void processSingleCourse(String courseId) {
        try {
            // Step 1: Network I/O - Fetch course data
            List<SectionInfo> infos = crawler.fetchCourseStatus(courseId);

            if (infos == null) {
                System.err.println("[Error] Fetch failed or blocked for course: " + courseId);
                return;
            }

            // Step 2: Synchronization - Update Database
            for (SectionInfo info : infos) {
                StatusMapping currentStatus = info.getStatus();
                StatusMapping previousStatus = null;
                String sectionId = info.getSection();

                // Find existing task for this specific section
                Task task = taskRepository.findBySectionId(sectionId);

                // Logic: Auto-Discovery vs Update
                if (task == null) {
                    // Scenario A: New Section Discovered (Auto-add to DB)
                    // Note: This will monitor ALL sections. If this is spammy, add filtering logic here.
                    task = new Task(info.getSubject(), info.getCatalogNumber(), sectionId, courseId, info.getStatus());
                    System.out.println("[New Section] Found " + info.getSection() + ". Adding to DB.");

                    // TODO: Optional: Send alert on discovery?
                    AlertAction action = determineAction(null, currentStatus);
                    Mail(action, info);
                } else {
                    // Scenario B: Existing Task Update
                    previousStatus = task.getLastStatus();

                    // Logging state only on changes or verbose debug can go here
                    // System.out.println("[Checking] " + task.getCourseDisplayName() + " [" + currentStatus + "]");

                    if (task.isEnabled()) {
                        AlertAction action = determineAction(previousStatus, currentStatus);
                        Mail(action, info);
                    }
                }

                // Step 3: Persistence
                if (previousStatus != currentStatus || task.getId() == null) {
                    if (task.getId() != null) {
                        System.out.println("🔄 State changed: " + previousStatus + " -> " + currentStatus + " for " + sectionId);
                    }
                    task.setLastStatus(currentStatus);
                    taskRepository.save(task);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing course " + courseId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private AlertAction determineAction(StatusMapping prev, StatusMapping curr) {
        if (prev == null) {
            // Logic for newly discovered tasks (prevent spam on restart)
            // return AlertAction.NONE; // Uncomment to silent new task alerts
            if (curr == StatusMapping.OPEN) return AlertAction.SEND_OPEN_EMAIL;
            return AlertAction.NONE;
        }

        if (prev == curr) return AlertAction.NONE;

        switch (curr) {
            case OPEN: return AlertAction.SEND_OPEN_EMAIL;
            case WAITLISTED:
                // Only alert if upgraded from CLOSED. Downgrade from OPEN is ignored.
                return (prev == StatusMapping.CLOSED) ? AlertAction.SEND_WAITLIST_EMAIL : AlertAction.NONE;
            default: return AlertAction.NONE;
        }
    }

    private void Mail(AlertAction action, SectionInfo info) {
        if (action == AlertAction.SEND_OPEN_EMAIL) {
            System.out.println("🔥 ALERT: OPEN detected for " + info.getSection());
            mailService.sendCourseOpenAlert(info.getSection(), info.getSubject() + " " + info.getCatalogNumber());
        } else if (action == AlertAction.SEND_WAITLIST_EMAIL) {
            System.out.println("⚠️ ALERT: WAITLIST detected for " + info.getSection());
            mailService.sendCourseWaitlistedAlert(info.getSection(), info.getSubject() + " " + info.getCatalogNumber());
        }
    }
}