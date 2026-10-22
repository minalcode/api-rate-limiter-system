
package com.ratelimiter.api_rate_limiter_system.Repository;

import com.ratelimiter.api_rate_limiter_system.Model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository
        extends JpaRepository<RequestLog, Long> {
}
