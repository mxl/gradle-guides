package org.gradle.guides.testing

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import static org.gradle.guides.testing.fixtures.HelloWorldProjectFixture.failingHelloWorldTask
import static org.gradle.guides.testing.fixtures.HelloWorldProjectFixture.successfulHelloWorldTask
import static org.gradle.guides.testing.fixtures.JavaProjectFixture.basicTestableJavaProject
import static org.gradle.guides.testing.fixtures.JavaProjectFixture.simpleJavaClass
import static org.gradle.testkit.runner.TaskOutcome.FAILED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class SamplesFunctionalTest extends AbstractSamplesFunctionalTest {

    private static final String SAMPLE_CODE_PROJECT_DIR_NAME = 'use-case'

    @Rule
    final TemporaryFolder samplesDirFolder = new TemporaryFolder()

    private File samplesCodeDir
    private File samplesBuildFile

    def setup() {
        samplesCodeDir = samplesDirFolder.newFolder('code', SAMPLE_CODE_PROJECT_DIR_NAME)
        samplesBuildFile = new File(samplesCodeDir, 'build.gradle')
        System.properties['samplesDir'] = samplesDirFolder.root.absolutePath
    }

    def cleanup() {
        System.clearProperty('samplesDir')
    }

    def "can execute build and expect successful result"() {
        given:
        samplesBuildFile << successfulHelloWorldTask()
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = succeeds('helloWorld')

        then:
        result.task(':helloWorld').outcome == SUCCESS
        result.output.contains('Hello World!')
    }

    def "can execute build and expect failed result"() {
        setup:
        samplesBuildFile << failingHelloWorldTask()
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = fails('helloWorld')

        then:
        result.task(':helloWorld').outcome == FAILED
        result.output.contains('expected failure')
    }

    def "can copy sample directory recursively"() {
        setup:
        samplesBuildFile << basicTestableJavaProject()
        File javaSrcDir = new File(samplesCodeDir, 'src/main/java/com/company')
        javaSrcDir.mkdirs()
        new File(javaSrcDir, 'MyClass.java') << simpleJavaClass()
        copySampleCode(SAMPLE_CODE_PROJECT_DIR_NAME)

        when:
        def result = succeeds('compileJava')

        then:
        result.task(':compileJava').outcome == SUCCESS
        new File(testDirectory, 'build/classes/java/main/com/company/MyClass.class').exists()
    }
}
