package com.syfr.cowsay.spigot.entity.pathgoal

import com.syfr.cowsay.spigot.entity.CowSayEntity
import net.minecraft.server.v1_12_R1.PathfinderGoal

class PathfinderGoalPlaySound(private val entity: CowSayEntity) : PathfinderGoal() {

    override fun a(): Boolean {
        this.entity.D()
        return false
    }
}
