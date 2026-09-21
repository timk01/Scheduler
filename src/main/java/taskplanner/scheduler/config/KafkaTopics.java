package taskplanner.scheduler.config;

public final class KafkaTopics {

    public static final String SUMMARIZATION_REQUESTS = "SCHEDULER_SUMMARIZATION_REQUESTS";

    public static final String SUMMARIZATION_REPLIES = "SCHEDULER_SUMMARIZATION_REPLIES";

    public static final String SUMMARY_SENDING = "SUMMARY_SENDING_TASKS";

    private KafkaTopics() {
    }
}