package de.deroq.clans.util;

import de.deroq.clans.ClanSystem;
import de.deroq.clans.user.AbstractUser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * @author Miles
 * @since 12.12.2022
 */
public class MessageBuilder {

    private final AbstractUser user;
    private final TextComponent textComponent;

    public MessageBuilder(AbstractUser user) {
        this.user = user;
        this.textComponent = new TextComponent();
    }

    public MessageBuilder(AbstractUser user, String translationKey) {
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
