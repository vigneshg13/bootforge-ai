package com.acme.saas.tenant.domain;

public enum Role {
    OWNER(3),
    ADMIN(2),
    MEMBER(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public boolean hasAtLeast(Role required) {
        return this.level >= required.level;
    }
}
