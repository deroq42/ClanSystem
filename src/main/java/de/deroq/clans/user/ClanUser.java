package de.deroq.clans.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Miles
 * @since 10.12.2022
 */
@RequiredArgsConstructor
public class ClanUser {

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    @Getter
    private final UUID clan;
}
