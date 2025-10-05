package com.thebluetropics.crabremapper

internal class Remapper(private val hChz: Map<String, Hc>, private val mChz: Map<String, Mc>) : org.objectweb.asm.commons.Remapper() {
	private fun getSuperClasses(cName: String): List<Hc> {
		val chain = mutableListOf(this.hChz[cName] as Hc)

		while (this.hChz.contains(chain.last().sup)) {
			chain.add(this.hChz[chain.last().sup] as Hc)
		}

		return chain.drop(1)
	}

	private fun getSuperInterfaces(cName: String): List<Hc> {
		val seq = mutableListOf<Hc>()
		val interfaces = (this.hChz[cName] as Hc).inf.map { name -> this.hChz[name] as Hc }

		return emptyList()
	}

	override fun map(name: String): String {
		val mc = this.mChz[name]

		if (mc != null) {
			if (mc.mappedName != null) {
				return mc.mappedName
			}
		}

		return name
	}

	override fun mapFieldName(cName: String, name: String, desc: String): String {
		val hc = this.hChz.getOrElse(cName) {
			return name
		}

		val mc = this.mChz.getOrElse(cName) {
			return name
		}

		val hf = hc.getField(name, desc)
		val mf = mc.getField(name, desc)

		if (hf != null) {
			return if (mf?.mappedName != null) mf.mappedName else name
		}

		val superclasses = getSuperClasses(cName)

		for (hcSup in superclasses) {
			val hfSup = hcSup.getField(name, desc)

			if (hfSup != null) {
				val mcSup = this.mChz[hcSup.name]

				if (mcSup != null) {
					val mfSup = mcSup.getField(name, desc)

					if (mfSup?.mappedName != null) {
						return mfSup.mappedName
					}
				}

				break
			}
		}

		return name
	}

	override fun mapMethodName(cName: String, name: String, desc: String): String {
		val hc = this.hChz.getOrElse(cName) {
			return name
		}

		val mc = this.mChz.getOrElse(cName) {
			return name
		}

		val mm = mc.getMethod(name, desc)

		if (mm != null) {
			if (mm.mappedName != null) {
				return mm.mappedName
			}
		}

		val superclasses = getSuperClasses(cName)

		for (hcSup in superclasses) {
			val mcSup = this.mChz[hcSup.name]

			if (mcSup != null) {
				val mmSup = mcSup.getMethod(name, desc)

				if (mmSup?.mappedName != null) {
					return mmSup.mappedName
				}
			}
		}

		val interfaces = hc.inf

		for (inf in interfaces) {
			val hcInf = this.hChz[inf]

			if (hcInf != null) {
				val mcInf = this.mChz[inf]

				if (mcInf != null) {
					val mmInf = mcInf.getMethod(name, desc)

					if (mmInf?.mappedName != null) {
						return mmInf.mappedName
					}
				}
			}
		}

		return name
	}
}
