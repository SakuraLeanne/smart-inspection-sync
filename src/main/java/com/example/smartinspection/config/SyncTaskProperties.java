package com.example.smartinspection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sync")
public class SyncTaskProperties {
    private FullInit fullInit = new FullInit();
    public FullInit getFullInit() { return fullInit; }
    public void setFullInit(FullInit fullInit) { this.fullInit = fullInit; }

    public static class FullInit {
        private boolean enabled = false;
        private boolean truncateBeforeRun = false;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isTruncateBeforeRun() { return truncateBeforeRun; }
        public void setTruncateBeforeRun(boolean truncateBeforeRun) { this.truncateBeforeRun = truncateBeforeRun; }
    }
}
