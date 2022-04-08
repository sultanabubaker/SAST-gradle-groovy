package kaizen.plugins.nunit

import kaizen.plugins.clr.Clr
import kaizen.plugins.clr.ClrExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class NUnitTask extends DefaultTask {

	final Set<String> includedCategories = new HashSet<String>()
	final Set<String> excludedCategories = new HashSet<String>()

	def executable = 'lib/NUnit/nunit-console.exe'

	String targetFrameworkVersion = 'v3.5'

	def include(String... categories) {
		includedCategories.addAll(categories)
	}

	def exclude(String... categories) {
		excludedCategories.addAll(categories)
	}

	def targetFrameworkVersion(String version) {
		targetFrameworkVersion = version
	}

	@TaskAction
	def runTests() {
		def assembly = inputs.files.getSingleFile()
		def result = clr.exec {
			//environment 'MONO_EXECUTABLE': cli
			executable executableFile
			args '-nologo', '-nodots', '-domain:none'
			args "-work=${assembly.parentFile}"
			args includedCategories.collect { "-include=$it" }
			args excludedCategories.collect { "-exclude=$it" }
			args assembly
		}
	}

	Clr getClr() {
		def clr = ClrExtension.forProject(project)
		clr.runtimeForFrameworkVersion(targetFrameworkVersion)
	}

	File getExecutableFile() {
		project.rootProject.file(executable)
	}
}
