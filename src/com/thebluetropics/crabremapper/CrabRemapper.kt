package com.thebluetropics.crabremapper


import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.commons.ClassRemapper
import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.useLines
import kotlin.system.exitProcess as exit

object CrabRemapper {
	@JvmStatic
	fun main(argv: Array<String>) {
		val args = mutableListOf<String>()
		val flags = mutableListOf<String>()
		val opts = mutableMapOf<String, String>()

		var optName: String? = null

		for (arg in argv) {
			if (optName != null) {
				opts[optName] = arg
				optName = null
				continue
			}

			if (arg.startsWith('-') and setOf("h", "m").contains(arg.substring(1))) {
				optName = arg.substring(1)
				continue
			}

			if (arg.startsWith('-') and setOf("v").contains(arg.substring(1))) {
				flags.add(arg.substring(1))
			} else {
				args.add(arg)
			}
		}

		if (optName != null) {
			System.err.println("Err: invalid args.")
			exit(1)
		}

		if (flags.contains("v")) {
			println("0.0.1-dev")
			exit(0)
		}

		if (args.isEmpty()) {
			System.err.println("Err: mode of operation required.")
			exit(1)
		}

		if (!setOf("remap").contains(args[0])) {
			System.err.println("Err: unsupported mode of operation.")
			exit(1)
		}

		if (args.size < 2) {
			System.err.println("Err: target is not specified.")
			exit(1)
		}

		var targetType = "class_file"
		val targetPath = Paths.get(args[1])

		if (targetPath.isDirectory()) {
			targetType = "directory"
		}

		val mappingsPath = Paths.get(opts["m"] as String)

		if (targetType.equals("class_file")) {
			val hmap = loadHint(Paths.get(opts["h"] as String))
			val mChz = loadMappings(mappingsPath)
			val ifStream = Files.newInputStream(targetPath)
			val reader = ClassReader(ifStream)
			val writer = ClassWriter(reader, 0)
			val remapper = ClassRemapper(writer, Remapper(hmap, mChz))
			reader.accept(remapper, 0)
			val ofStream = Files.newOutputStream(targetPath)
			ofStream.write(writer.toByteArray())
			ifStream.close()
			ofStream.close()
		}

		if (targetType.equals("directory")) {
			val hmap = loadHintFromDirectory(targetPath)
			val mChz = loadMappings(mappingsPath)

			val files = targetPath.toFile().walkTopDown().filter(File::isFile).toList()

			for (file in files) {
				val ifStream = Files.newInputStream(file.toPath())
				val reader = ClassReader(ifStream)
				val writer = ClassWriter(reader, 0)
				val remapper = ClassRemapper(writer, Remapper(hmap, mChz))
				reader.accept(remapper, 0)
				val ofStream = Files.newOutputStream(file.toPath())
				ofStream.write(writer.toByteArray())
				ifStream.close()
				ofStream.close()
			}

			for (mc in mChz.values) {
				if (mc.mappedName != null) {
					val destination = targetPath.resolve(mc.mappedName + ".class")
					Files.createDirectories(destination.parent)
					Files.move(targetPath.resolve(mc.name + ".class"), destination)
				}
			}
		}
	}

	private fun loadHint(path: Path): Map<String, Hc> {
		val hMap = mutableMapOf<String, Hc>()

		path.useLines(Charsets.UTF_8, fun(lines) {
			var hc: Hc? = null

			for (line in lines) {
				val line = line.trimEnd()

				if (line.isEmpty()) {
					continue
				}

				val (k, values) = line.split(Regex("\\s+")).run { this.first() to this.drop(1) }

				if (k.equals("c")) {
					var infs = emptyArray<String>()

					if (values.size > 3) {
						infs = values.drop(3).toTypedArray()
					}

					val (acc, name, sup) = values.take(3)
					hc = Hc(acc.toInt(16), name, sup, infs)
					hMap[name] = hc
				}

				if (k.equals("f")) {
					if (hc != null) {
						val (acc, name, desc) = values.take(3)
						hc.f.add(Hf(acc.toInt(16), name, desc))
					} else {
						exit(1)
					}
				}

				if (k.equals("m")) {
					if (hc != null) {
						val (acc, name, desc) = values.take(3)
						hc.m.add(Hm(acc.toInt(16), name, desc))
					} else {
						exit(1)
					}
				}
			}
		})

		return hMap
	}

	private fun loadHintFromDirectory(path: Path): Map<String, Hc> {
		val hMap = mutableMapOf<String, Hc>()

		File(path.toString()).walkTopDown().forEach(fun(file) {
			if (file.isFile and file.extension.equals("class")) {
				FileInputStream(file).use(fun(input) {
					val classReader = ClassReader(input)
					val c = ClassNode()
					classReader.accept(c, 0)

					val hc = Hc(c.access, c.name, c.superName, c.interfaces.toTypedArray())

					for (f in c.fields) {
						val hf = Hf(f.access, f.name, f.desc)
						hc.f.add(hf)
					}

					for (m in c.methods) {
						val hm = Hm(m.access, m.name, m.desc)
						hc.m.add(hm)
					}

					hMap[hc.name] = hc
				})
			}
		})

		return hMap
	}

	private fun loadMappings(path: Path): Map<String, Mc> {
		val mChz = mutableMapOf<String, Mc>()

		path.useLines(Charsets.UTF_8, fun(lines) {
			var mc: Mc? = null
			var mm: Mm? = null

			for (line in lines) {
				val line = line.trimEnd()

				if (line.isEmpty()) {
					continue
				}

				val (k, values) = line.split(Regex("\\s+")).run { this.first() to this.drop(1) }

				if (k.equals("c")) {
					if (mm != null) {
						mm = null
					}

					if (values.size.equals(2)) {
						val (name, mappedName) = values
						mc = Mc(name, mappedName)
					} else {
						val (name) = values
						mc = Mc(name)
					}

					mChz[mc.name] = mc
					continue
				}

				if (k.equals("f")) {
					if (mm != null) {
						mm = null
					}

					val (name, mappedName, desc) = values

					if (mc != null) mc.f.add(Mf(name, mappedName, desc)) else {
						exit(1)
					}

					continue
				}

				if (k.equals("m")) {
					if (values.size.equals(3)) {
						val (name, mappedName, desc) = values
						mm = Mm(name, mappedName, desc)
					} else {
						val (name, desc) = values
						mm = Mm(name, null, desc)
					}

					if (mc != null) mc.m.add(mm) else {
						exit(1)
					}

					continue
				}
			}
		})

		return mChz
	}
}
