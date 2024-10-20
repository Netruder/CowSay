package com.syfr.cowsay.spigot.entity.pathgoal

import com.syfr.cowsay.spigot.entity.CowSayEntity
import net.minecraft.server.v1_12_R1.PathEntity
import net.minecraft.server.v1_12_R1.PathfinderGoal
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.lang.ref.WeakReference

class PathfinderGoalWalkAroundPlayer(private val entity: CowSayEntity,
                                     private val player: WeakReference<Player>,
                                     private val radius: Double,
                                     private val speed: Double,
                                     private val particle: Particle?
) : PathfinderGoal() {

    private val angleDiff = calculateAngleDiff(this.radius, this.speed)

    private var path: PathEntity? = null
    private var angle = 0.0

    override fun a(): Boolean {
        val player0 = this.player.get()
        if (player0 === null || !player0.isOnline) {
            this.entity.destroy()
            return false
        }

        if (this.particle != null) {
            this.entity.getWorld().world.spawnParticle(
                this.particle,
                this.entity.lastX - this.entity.motX * 3,
                this.entity.lastY + 1,
                this.entity.lastZ - this.entity.motZ * 3,
                0)
        }

        val location = CowSayEntity.calculateLocation(this.angle, this.radius, player0)
        this.path = this.entity.navigation.a(location.x, location.y, location.z)

        this.entity.headRotation = this.entity.lastYaw

        if (this.path != null) {
            this.c()
        }

        this.angle += this.angleDiff
        this.angle %= 360

        return this.path != null
    }

    override fun c() {
        this.entity.navigation.a(this.path, this.speed)
    }

    private companion object {
        fun calculateAngleDiff(radius: Double, speed: Double) : Double {
            var alpha = 2.5
            alpha /= (radius * 0.125)
            val speedMultiplier = (speed.coerceAtLeast(1.0) - 0.8) * 1.15
            return alpha * speedMultiplier
        }
    }
}
