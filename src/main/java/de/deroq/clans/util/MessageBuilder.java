package de.deroq.clans.util;

import de.deroq.clans.ClanSystem;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class MessageBuilder {

    private final TextComponent textComponent;

    public MessageBuilder() {
        this.textComponent = new TextComponent(ClanSystem.PREFIX);
    }

    public MessageBuilder(String message) {
        this.textComponent = new TextComponent(ClanSystem.PREFIX + message);
    }

    public MessageBuilder wrap(TextComponent temp) {
        textComponent.addExtra(temp);
        return this;
    }

    public MessageBuilder addClickEvent(String command) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public MessageBuilder addClickEvent(String message, String command) {
        TextComponent temp = new TextComponent(message);
        temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        textComponent.addExtra(temp);
        return this;
    }

    public TextComponent toComponent() {
        return textComponent;
    }
}
