package com.tcn.vera.pagination;

import com.tcn.vera.eventHandlers.ButtonHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that allows for pagination of an array of embeds. This very simple version of the Vera paginator is
 * incapable of dynamically generating pages. If you need a paginator that has dynamic page loading or other advanced feature,
 * please see
 * //TODO: Add a paginator with advanced features like the one that this very javadoc talks about.
 */
public class EmbedPaginator extends PaginatorBase {

    private final ArrayList<MessageEmbed> embedList;


    public EmbedPaginator(Message message, SlashCommandInteractionEvent commandEvent, int numberOfPages, boolean shouldWrap, long userID, ArrayList<MessageEmbed> embedList, ButtonHandler buttonHandler) {
        super(message, commandEvent, numberOfPages, shouldWrap, userID, buttonHandler);
        this.embedList = embedList;

        if (numberOfPages > 1) {
            //left, stop, right buttons
            addButton(Button.primary(getFullButtonID("previous"), "◀️ Go Left"));
            addButton(Button.danger(getFullButtonID("stop"), Emoji.fromUnicode("\uD83D\uDDD1️")));
            addButton(Button.primary(getFullButtonID("next"), "Go Right ▶️️"));
        }


    }

    @Override
    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getUser().getIdLong() != userID) {
            event.reply("You are not the user who created this menu!").setEphemeral(true).queue();
            return;
        }

        String pressedButton = event.getComponentId();

        if (getFullButtonID("previous").equals(pressedButton)) {
            decPageNum();
        } else if (getFullButtonID("next").equals(pressedButton)) {
            incPageNum();
        } else if (getFullButtonID("stop").equals(pressedButton)) {
            destroyMenu(false);
            return;
        } else {
            return;
        }
        event.deferEdit().queue();
        showPage();
    }

    @Override
    protected void showPage() {
        if (isCommand) {
            commandEvent.getHook().editOriginalComponents().setEmbeds(embedList.get(currentPage)).setActionRow(buttonList).queue();
        } else {
            message.editMessageEmbeds(embedList.get(currentPage)).setActionRow(buttonList).queue();
        }
    }

    public static class Builder extends PaginatorBase.Builder<EmbedPaginator.Builder, EmbedPaginator> {
        private final List<MessageEmbed> embedList = new ArrayList<>();

        /**
         * Builds the paginator. This method will throw an IllegalArgumentException if the paginator is not valid.
         *
         * @return A new EmbedPaginator object.
         */
        @Override
        public EmbedPaginator build() {
            //validate stuff
            if (!runChecks()) {
                throw new IllegalArgumentException("Cannot build, invalid arguments!");
            }

            //calculate the number of pages
            return new EmbedPaginator(message, commandEvent, embedList.size(), shouldWrap, userID, (ArrayList<MessageEmbed>) embedList, buttonHandler);
        }

        @Override
        protected boolean runAdditionalChecks() {
            if (embedList.isEmpty()) {
                throw new IllegalArgumentException("An embed paginator must have at least one embed set.");
            }
            return true;
        }


        /**
         * Allows for a List to be added to the list of embeds to be paginated. Any previously added lists or embeds will
         * be preserved so this method can be called multiple times to add different lists of embeds.
         *
         * @param embedList The List of MessageEmbed objects that you wish to add to the paginator.
         * @return This builder.
         */
        public EmbedPaginator.Builder setEmbeds(List<MessageEmbed> embedList) {
            this.embedList.addAll(embedList);
            return this;
        }

        /**
         * Allows for an embed to be added to the list of embeds to be paginated. Any previously added lists or embeds will
         * be preserved so this method can be called multiple times to add many embeds. Please consider adding a large number
         * of embeds via a List via {@link #setEmbeds(List)}
         *
         * @param embed A {@link MessageEmbed} object to add to the paginator
         * @return This Builder.
         */
        public EmbedPaginator.Builder setEmbeds(MessageEmbed embed) {
            this.embedList.add(embed);
            return this;
        }

    }
}
