package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Pavel Asadchiy
 * on 12:00 03.04.18.
 */
@Slf4j
@Component
public class RequestLoginFilter extends AbstractRequestLoggingFilter {

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
    }
}
