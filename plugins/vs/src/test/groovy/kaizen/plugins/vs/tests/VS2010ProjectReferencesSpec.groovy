package kaizen.plugins.vs.tests

import kaizen.testing.DirectoryBuilder
import org.gradle.testfixtures.ProjectBuilder

class VS2010ProjectReferencesSpec extends VSProjectSpecification {

	def bundleDir = DirectoryBuilder.tempDirWith {
		dir('src') {
			dir('a')
			dir('b')
			dir('c')
		}
	}

	def bundle = ProjectBuilder.builder().withProjectDir(bundleDir).withName('bundle').build()
	def a = subProjectWithName('a')
	def b = subProjectWithName('b')
	def c = subProjectWithName('c')

	@Override
	def setup() {
		configure(bundle) {
			subprojects {
				apply plugin: 'assembly'
				apply plugin: 'vs2010'
			}
			project(':a') {
			}
			project(':b') {
				dependencies {
					'default' project(':a')
				}
			}
			project(':c') {
				dependencies {
					'default' project(':a')
					'default' project(':b')
				}
			}
		}
		bundle.allprojects.each {
			it.evaluate()
		}
	}

	def 'project reference paths are relative'() {
		expect:
		def aProjectXml = loadProjectFileOf(a)
		aProjectXml.ItemGroup.ProjectReference == []

		def bProjectXml = loadProjectFileOf(b)
		bProjectXml.ItemGroup.ProjectReference.collect { [it.Project.text(), it.@Include] } == [
				[a.extensions.vs.project.guid, '..\\a\\a.csproj']
		]

		def cProjectXml = loadProjectFileOf(c)
		cProjectXml.ItemGroup.ProjectReference.collect { [it.Project.text(), it.@Include] } == [
				[a.extensions.vs.project.guid, '..\\a\\a.csproj'],
				[b.extensions.vs.project.guid, '..\\b\\b.csproj'],
		]
	}

	private subProjectWithName(String name) {
		ProjectBuilder.builder().withParent(bundle).withProjectDir(new File(bundleDir, 'src/' + name)).withName(name).build()
	}
}
