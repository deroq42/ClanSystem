package de.deroq.clans.command;

import de.deroq.clans.user.ClanUser;

/**
 * @author Miles
 * @since 10.12.2022
 */
public abstract class ClanSubCommand {
    protected abstract void run(ClanUser user, String[] args);
}
