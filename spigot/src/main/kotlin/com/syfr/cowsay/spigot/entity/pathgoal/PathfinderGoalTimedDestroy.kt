package com.syfr.cowsay.spigot.entity.pathgoal

import net.minecraft.server.v1_12_R1.PathfinderGoal
import com.syfr.cowsay.spigot.entity.CowSayEntity

class PathfinderGoalTimedDestroy(private val entity: CowSayEntity,
                                 private var duration: Long) : PathfinderGoal() {

    override fun a(): Boolean {
        if (this.duration <= 0) {
            this.entity.destroy()
        }
        this.duration -= 4
        return false
    }
}
