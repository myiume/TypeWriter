package com.typewritermc.engine.paper.interaction

import com.typewritermc.engine.paper.entry.entries.Event
import com.typewritermc.engine.paper.entry.entries.InteractionEndTrigger

interface TriggerHandler {
    suspend fun trigger(event: Event, currentInteraction: Interaction?): TriggerContinuation
}

class InteractionTriggerHandler : TriggerHandler {
    override suspend fun trigger(event: Event, currentInteraction: Interaction?): TriggerContinuation {
        if (InteractionEndTrigger in event) {
            return TriggerContinuation.EndInteraction
        }
        return TriggerContinuation.Done
    }
}

sealed interface TriggerContinuation {
    // Signals that the trigger is completed.
    data object Done : TriggerContinuation
    // Processes the event in the current tick.
    data class Append(val event: Event) : TriggerContinuation
    data class StartInteraction(val interaction: Interaction) : TriggerContinuation
    data object EndInteraction : TriggerContinuation
    data class Multi(val continuations: List<TriggerContinuation>) : TriggerContinuation {
        constructor(vararg continuations: TriggerContinuation) : this(continuations.toList())
    }
}