package com.syfr.cowsay.spigot.entity

import com.syfr.cowsay.spigot.entity.pathgoal.PathfinderGoalPlaySound
import com.syfr.cowsay.spigot.entity.pathgoal.PathfinderGoalTimedDestroy
import com.syfr.cowsay.spigot.entity.pathgoal.PathfinderGoalWalkAroundPlayer
import net.minecraft.server.v1_12_R1.*
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import kotlin.math.cos
import kotlin.math.sin

class CowSayEntity(private val player: WeakReference<Player>,
                   private val radius: Double,
                   private val speed: Double,
                   private val lifetime: Long,
                   private val particle: Particle,
                   cowName: String
): EntityCow((player.get()!! as CraftPlayer).handle.world) {

    constructor(world: World) : this(WeakReference(null), 0.0, 0.0, 0, Particle.entries[0], "") {
        throw UnsupportedOperationException("Default constructor is not supporter for CowSayEntity")
    }

    init {
        this.customName = cowName
        this.customNameVisible = true

        this.goalSelector.a(0, PathfinderGoalFloat(this))
        this.goalSelector.a(1, PathfinderGoalWalkAroundPlayer(
            this, this.player, this.radius, this.speed, this.particle)
        )
        this.goalSelector.a(2, PathfinderGoalPlaySound(this))
        this.goalSelector.a(3, PathfinderGoalTimedDestroy(this, this.lifetime))

        val spawnLocation = calculateLocation(0.0, radius, player.get()!!)
        this.setLocation(
            spawnLocation.x,
            spawnLocation.y,
            spawnLocation.z,
            spawnLocation.yaw,
            spawnLocation.pitch
        )
    }

    override fun r() { } // removing default path goals

    fun destroy() {
        world.removeEntity(this)
        world.world.spawnParticle(Particle.EXPLOSION_HUGE, this.lastX, this.lastY, this.lastZ, 0)
        world.world.playSound(
            Location(world.world, this.lastX, this.lastY, this.lastZ),
            Sound.ENTITY_GENERIC_EXPLODE,
            1.0F,
            1.0F
        )
    }

    companion object {
        init {
            val method = EntityTypes::class.java.getDeclaredMethod("a", // a = registerEntityType
                Int::class.java, String::class.java, Class::class.java, String::class.java)

            method.isAccessible = true
            method.invoke(null, 92, "cow_say", CowSayEntity::class.java, "CowSay")
        }

        fun spawn(player: Player, config: FileConfiguration, cowName: String): CowSayEntity {
            val entity = CowSayEntity(
                WeakReference(player),
                config.getDouble("cow.radius", 7.0),
                config.getDouble("cow.speed", 3.0),
                config.getLong("cow.lifetime", 200),
                Particle.valueOf(config.getString("cow.particle", "FLAME")),
                cowName
            )

            (player.world as CraftWorld).handle.addEntity(entity)
            return entity
        }

        fun calculateLocation(angle: Double, radius: Double, player: Player) : Location {
            val radians = Math.toRadians(angle)
            val dx = sin(radians) * radius * 1.25
            val dz = cos(radians) * radius * 1.25
            return player.location.add(dx, 0.0, dz)
        }
    }
}
