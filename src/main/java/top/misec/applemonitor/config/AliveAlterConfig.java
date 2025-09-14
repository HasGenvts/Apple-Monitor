package top.misec.applemonitor.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * alive alter config
 */

@Data
@Slf4j
public class AliveAlterConfig {
    private PushConfig pushConfig;
    private String cronExpressions;
    private int ignoreStartHour;
    private int ignoreEndHour;
}
