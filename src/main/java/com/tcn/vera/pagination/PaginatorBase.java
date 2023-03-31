package com.tcn.vera.pagination;

import com.tcn.vera.eventHandlers.ButtonHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

//This paginator has support for both messages and interaction events

/**
 * The base class for all Vera paginators. This class should not be used directly, but instead extended by other classes.
 */
public abstract class PaginatorBase {

    protected final Message message;
    protected final SlashCommandInteractionEvent commandEvent;
    protected final long userID;

    protected final int numberOfPages;

    protected final boolean shouldWrap;

    protected final boolean isCommand;
    protected final ButtonHandler buttonHandler;
    protected int currentPage;
    protected List<Button> buttonList = new ArrayList<>();


    protected PaginatorBase(Message message, SlashCommandInteractionEvent commandEvent, int numberOfPages, boolean shouldWrap, long userID, ButtonHandler buttonHandler) {
        this.message = message;
        this.commandEvent = commandEvent;
        this.numberOfPages = numberOfPages;
        this.shouldWrap = shouldWrap;
        this.userID = userID;
        this.buttonHandler = buttonHandler;

        if (message == null && commandEvent != null) {
            isCommand = true;
        } else if (message != null && commandEvent == null) {
            isCommand = false;
        } else {
            throw new IllegalArgumentException("This paginator cannot be in response to both a message and a command at the same time!");
        }

        buttonHandler.registerButtonSet(getButtonID(), this::onButtonClick);
    }

    /**
     * The method called when a button is pressed. This method should be overridden by the child class.
     *
     * @param event The {@link ButtonInteractionEvent} that was fired.
     */
    public abstract void onButtonClick(ButtonInteractionEvent event);

    /**
     * The method called when a page is shown. This method should be overridden by the child class.
     */
    protected abstract void showPage();

    /**
     * Shows the paginator at a specific page number.
     *
     * @param pageNum The page number that should be first presented to the user.
     */
    public void paginate(int pageNum) {
        if (pageNum < 1) {
            currentPage = 1;
        } else if (pageNum > numberOfPages) {
            currentPage = numberOfPages;
        }
        showPage();
    }

    /**
     * Shows the paginator on the first page.
     */
    public void paginate() {
        paginate(1);
    }

    /**
     * Used to generate the ID of a button via the message ID, discord user ID, and the name of the button. While Vera
     * would be capable of matching buttons without all of these factors, we can ensure unique buttons with all of this information.
     *
     * @param buttonName The name of the button
     * @return A String representing the ID of the button. This should be used as the first parameter when creating a button.
     */
    public String getFullButtonID(String buttonName) {
        if (isCommand) {
            return commandEvent.getHook().retrieveOriginal().complete().getId() + ":" + userID + ":" + buttonName;
        }
        return message.getId() + ":" + userID + ":" + buttonName;
    }

    public String getButtonID() {
        if (isCommand) {
            return commandEvent.getHook().retrieveOriginal().complete().getId() + ":" + userID;
        }
        return message.getId() + ":" + userID;
    }

    protected void incPageNum() {
        if (currentPage == numberOfPages - 1 && shouldWrap) {
            currentPage = 0;
        } else if (currentPage < numberOfPages - 1) {
            currentPage++;
        }
    }

    protected void decPageNum() {
        if (currentPage == 0 && shouldWrap) {
            currentPage = numberOfPages - 1;
        } else if (currentPage > 0) {
            currentPage--;
        }
    }

    /**
     * Adds a button to the paginator. Please note that there is a hard limit of 5 buttons per paginator. If more are added,
     * an exception will be thrown.
     *
     * @param toAdd The button to be added.
     */
    protected void addButton(Button toAdd) {
        if (buttonList.size() < 5) {
            buttonList.add(toAdd);
        } else {
            throw new IllegalArgumentException("You cannot have more than 5 buttons on a message!");
        }
    }

    /**
     * Destroys the paginator. This method should be called when the paginator is no longer needed.
     *
     * @param deleteMessage Whether or not the message should be deleted.
     */
    protected void destroyMenu(boolean deleteMessage) {
        if (deleteMessage && isCommand) {
            commandEvent.getHook().deleteOriginal().queue();
        } else if (deleteMessage) {
            message.delete().queue();
        } else if (isCommand) {
            commandEvent.getHook().editOriginalComponents().setComponents().queue();
        } else {
            message.editMessageComponents().setComponents().queue();
        }
    }

    @SuppressWarnings("unchecked")
    protected abstract static class Builder<T extends Builder<T, V>, V extends PaginatorBase> {
        protected boolean shouldWrap = true;
        protected Message message;
        protected SlashCommandInteractionEvent commandEvent;

        protected long userID = -1;

        protected ButtonHandler buttonHandler;

        /**
         * builds the paginator object. This method should be overridden by the child class.
         *
         * @return The paginator object.
         */
        public abstract V build();

        protected boolean runChecks() {
            if (null == buttonHandler) {
                throw new IllegalStateException("A paginator MUST have a button handler!");
            }

            return runAdditionalChecks();
        }

        /**
         * This method should be overridden by the child class to add additional checks.
         *
         * @return Whether or not the additional checks passed.
         */
        protected abstract boolean runAdditionalChecks();

        /**
         * Sets whether or not the paginator should wrap around when it reaches the end of the pages.
         *
         * @param shouldWrap Whether or not the paginator should wrap around.
         * @return The builder.
         */
        public final T wrapPages(boolean shouldWrap) {
            this.shouldWrap = shouldWrap;
            return (T) this;
        }

        /**
         * Sets the user ID of the user who should be able to interact with the paginator.
         *
         * @param userID The user ID of the user who should be able to interact with the paginator.
         * @return The builder.
         */
        public final T setUserID(long userID) {
            this.userID = userID;
            return (T) this;
        }

        /**
         * Sets the event that triggered the paginator.
         *
         * @param event The event that triggered the paginator.
         * @return The builder.
         */
        public final T setEvent(SlashCommandInteractionEvent event) {
            this.commandEvent = event;
            return (T) this;
        }

        /**
         * Sets the event that triggered the paginator.
         *
         * @param event The event that triggered the paginator.
         * @return The builder.
         */
        public final T setEvent(MessageReceivedEvent event) {
            this.message = event.getMessage();
            return (T) this;
        }

        /**
         * Sets the button handler that should be used by the paginator. If this is not set, the paginator will not work.
         *
         * @param buttonHandler The button handler that should be used by the paginator.
         * @return The builder.
         */
        public final T setButtonHandler(ButtonHandler buttonHandler) {
            this.buttonHandler = buttonHandler;
            return (T) this;
        }

    }
}
