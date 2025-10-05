package com.thebluetropics.crabremapper

internal class Hc(val acc: Int, val name: String, val sup: String, val inf: Array<String>) {
	val f: ArrayList<Hf> = ArrayList()
	val m: ArrayList<Hm> = ArrayList()

	fun getField(name: String, desc: String): Hf? {
		for (hf in this.f) {
			if (hf.name.equals(name).and(hf.desc.equals(desc))) {
				return hf
			}
		}

		return null
	}

	fun getMethod(name: String, desc: String): Hm? {
		for (hm in this.m) {
			if (hm.name.equals(name).and(hm.desc.equals(desc))) {
				return hm
			}
		}

		return null
	}
}
