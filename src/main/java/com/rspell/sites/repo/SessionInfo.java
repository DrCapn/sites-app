package com.example.sites.repo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

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
