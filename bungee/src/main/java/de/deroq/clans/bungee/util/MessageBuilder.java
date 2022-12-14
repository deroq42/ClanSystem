package de.deroq.clans.bungee.util;

import de.deroq.clans.api.user.AbstractClanUser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class MessageBuilder {

    private final AbstractClanUser user;
    private final TextComponent textComponent;

    public MessageBuilder(AbstractClanUser user) {
        this.user = user;
        this.textComponent = new TextComponent();
    }

    public MessageBuilder(AbstractClanUser user, String translationKey) {
        this.user = user;
        this.textComponent = new TextComponent(user.translate(translationKey));
    }

    public MessageBuilder wrap(TextComponent temp) {
        textComponent.addExtra(temp);
        return this;
    }

    public MessageBuilder addClickEvent(String command) {
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public MessageBuilder addClickEvent(String translationKey, String command) {
        TextComponent temp = new TextComponent(user.translate(translationKey));
        temp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        textComponent.addExtra(temp);
        return this;
    }

    public TextComponent toComponent() {
        return textComponent;
    }
}
