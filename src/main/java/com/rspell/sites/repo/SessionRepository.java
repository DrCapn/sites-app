package com.rspell.sites.repo;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SessionRepository extends CrudRepository<SessionInfo, String> {

    SessionInfo findSessionBySessionInstanceId(final String sessionInstanceId);
    List<SessionInfo> findSessionBySessionInstanceIdEndingWith(final String instanceName);
    List<SessionInfo> findSessionBySessionInstanceIdContaining(final String siteName);
}
