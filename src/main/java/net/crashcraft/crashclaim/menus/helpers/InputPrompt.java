package net.crashcraft.crashclaim.menus.helpers;

import lombok.AllArgsConstructor;
import me.lucko.helper.text3.Text;
import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.localization.Localization;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor(staticName = "of")
public class InputPrompt extends ValidatingPrompt {
    private Player player;
    private String prompt;
    private Function<String, Boolean> validator;
    private Consumer<String> handler;

    @Override
    protected boolean isInputValid(@NotNull ConversationContext ctx, @NotNull String s) {
        return validator.apply(s);
    }

    @Override
    protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext ctx, @NotNull String s) {
        handler.accept(s);
        return END_OF_CONVERSATION;
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext ctx) {
        return Text.colorize(prompt);
    }

    public void start() {
        new ConversationFactory(CrashClaim.getPlugin())
                .withTimeout(60)
                .withLocalEcho(false)
                .withEscapeSequence("quit")
                .withEscapeSequence("exit")
                .withEscapeSequence("cancel")
                .withEscapeSequence("stop")
                .addConversationAbandonedListener(e -> {
                    if(e.getCanceller() instanceof InactivityConversationCanceller || e.getCanceller() instanceof ManuallyAbandonedConversationCanceller) {
                        handler.accept(null);
                    }
                })
                .withFirstPrompt(this)
                .buildConversation(player)
                .begin();;
    }
}
