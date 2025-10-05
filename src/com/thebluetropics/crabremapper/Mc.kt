package com.thebluetropics.crabremapper

internal class Mc(val name: String, val mappedName: String? = null) {
	val f = mutableListOf<Mf>()
	val m = mutableListOf<Mm>()

	fun getField(name: String, desc: String): Mf? {
		for (mf in this.f) {
			if (mf.name.equals(name).and(mf.desc.equals(desc))) {
				return mf
			}
		}

		return null
	}

	fun getMethod(name: String, desc: String): Mm? {
		for (mm in this.m) {
			if (mm.name.equals(name).and(mm.desc.equals(desc))) {
				return mm
			}
		}

		return null
	}
}
