package com.rspell.sites.repo;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
@Getter
@Setter
public class SessionInfo {

    // Id formed from <sessionId>-<siteName>-<instanceName>
    @Id
    private String sessionInstanceId;
    private String siteName;
    private String instanceName;

    @Lob
    @Column(name = "CATEGORIES JSON SET", columnDefinition="BLOB")
    private String categoriesJsonSet;
}
