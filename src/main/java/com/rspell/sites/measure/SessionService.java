package com.rspell.sites.measure;

import com.rspell.sites.domain.CategoryService;
import com.rspell.sites.repo.SessionInfo;
import com.rspell.sites.repo.SessionRepository;
import com.rspell.sites.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final SessionRepository sessionRepo;

    public SessionService(SessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }
    private String sessionIdTail(final String siteName, final String instName) {
        return siteName + "-" + instName;
    }
    private String sessionId(final String sessionId, final String siteName, final String instanceName) {
        return sessionId + "-" + sessionIdTail(siteName, instanceName);
    }
    public SessionInfo findRegisterSession(final HttpSession session, final String siteName, final String instanceName) {
        final String sessionInstanceId = sessionId(
                session.getId(), siteName, instanceName);
        SessionInfo sessionInfo = sessionRepo.findSessionBySessionInstanceId(sessionInstanceId);
        Set<String> sessionCategories;
        if (sessionInfo == null) {
            sessionInfo = new SessionInfo();
            sessionInfo.setSessionInstanceId(sessionInstanceId);
            sessionInfo.setSiteName(siteName);
            sessionInfo.setInstanceName(instanceName);
            sessionCategories = new LinkedHashSet<>();
            sessionInfo.setCategoriesJsonSet(Utils.setToJson(sessionCategories));
        }
        return sessionInfo;
    }

    public void updateSession(final SessionInfo sessionInfo,
                              final Set<String> sessionCategories) {
        sessionInfo.setCategoriesJsonSet(Utils.setToJson(sessionCategories));
        sessionRepo.save(sessionInfo);
    }
    public Iterable<SessionInfo> findAllSessions() {
        return sessionRepo.findAll();
    }
    public Iterable<SessionInfo> findAllSiteSessions(final String siteName) {
        return sessionRepo.findSessionBySessionInstanceIdContaining(siteName);
    }
    public Iterable<SessionInfo> findAllInstanceSessions(final String siteName, final String instanceName) {
        return sessionRepo.findSessionBySessionInstanceIdEndingWith(sessionIdTail(siteName, instanceName));
    }
    public void removeSessionInstance(final String sessionId, final String siteName, final String instanceName) {
        final String sessionInstanceId = sessionId(sessionId, siteName, instanceName);
        // TODO GRAB SESSION COUNT DATA AND REMOVE FROM COUNTS, EXCEPT PAGE VIEWS?
        sessionRepo.deleteById(sessionInstanceId);
    }
    public long removeAllSiteSessions(final String siteName) {
        Iterable<SessionInfo> sessions = findAllSiteSessions(siteName);
        long count = 0L;
        for (SessionInfo session : sessions) {
            sessionRepo.deleteById(session.getSessionInstanceId());
            count += 1L;
        }
        return count;
    }
    public long removeAllInstanceSessions(final String siteName, final String instanceName) {
        Iterable<SessionInfo> sessions = findAllInstanceSessions(siteName, instanceName);
        long count = 0L;
        for (SessionInfo session : sessions) {
            sessionRepo.deleteById(session.getSessionInstanceId());
            count += 1L;
        }
        return count;
    }
    public long removeAllSessions() {
        long count = 0L;
        sessionRepo.deleteAll();
        if (sessionRepo.count() == 0L) {
            return count;
        }
        return -1L;
    }
}
