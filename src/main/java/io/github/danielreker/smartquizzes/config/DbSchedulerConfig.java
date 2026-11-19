package io.github.danielreker.smartquizzes.config;

import com.github.kagkarlsson.scheduler.SchedulerName;
import com.github.kagkarlsson.scheduler.boot.config.DbSchedulerOverrides;
import com.github.kagkarlsson.scheduler.serializer.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DbSchedulerConfig {

    @Bean
    DbSchedulerOverrides dbSchedulerOverrides() {
        return DbSchedulerOverrides
                .builder()
                .schedulerName(new SchedulerName.Fixed("scheduler"))
                .serializer(new JacksonSerializer())
                .build();
    }

}
