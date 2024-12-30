package com.typewritermc.entity.entries.activity

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.utils.point.World
import com.typewritermc.engine.paper.entry.entity.*
import com.typewritermc.engine.paper.entry.entries.EntityActivityEntry
import com.typewritermc.engine.paper.entry.entries.EntityProperty
import com.typewritermc.engine.paper.entry.entries.IndividualEntityActivityEntry
import com.typewritermc.engine.paper.utils.position

@Entry(
    "player_world_activity",
    "Spawns the entity in the world the player is in",
    Colors.PALATINATE_BLUE,
    "material-symbols:globe-asia"
)
/**
 * The `Player World Activity` makes the entity move to the world the player is in.
 * If the player moves between worlds, the entity will move with the player.
 *
 * ## How could this be used?
 * This can be used to make one entry appear in multiple worlds.
 * Like a dungeon trader that lives in every dungeon.
 */
class PlayerWorldActivityEntry(
    override val id: String = "",
    override val name: String = "",
    val child: Ref<out EntityActivityEntry> = emptyRef(),
) : IndividualEntityActivityEntry {
    override fun create(
        context: IndividualActivityContext,
        currentLocation: PositionProperty
    ): EntityActivity<IndividualActivityContext> {
        return PlayerWorldActivity(child, currentLocation)
    }
}

class PlayerWorldActivity(
    private val child: Ref<out EntityActivityEntry>,
    private val startPosition: PositionProperty,
) : IndividualEntityActivity {
    private var childActivity: EntityActivity<in IndividualActivityContext>? = null
    private var world: World = startPosition.world

    override fun initialize(context: IndividualActivityContext) {
        world = context.viewer.position.world
        childActivity = child.get()?.create(context, currentPosition)
        childActivity?.initialize(context)
    }

    override fun tick(context: IndividualActivityContext): TickResult {
        val playerWorld = context.viewer.position.world
        if (playerWorld != world) {
            childActivity?.dispose(context)
            world = playerWorld
            childActivity = child.get()?.create(context, currentPosition)
            childActivity?.initialize(context)
        }

        return childActivity?.tick(context) ?: TickResult.IGNORED
    }

    override fun dispose(context: IndividualActivityContext) {
        childActivity?.dispose(context)
        childActivity = null
    }

    override val currentPosition: PositionProperty
        get() = childActivity?.currentPosition?.withWorld(world) ?: startPosition.withWorld(world)

    override val currentProperties: List<EntityProperty>
        get() = childActivity?.currentProperties ?: emptyList()
}
