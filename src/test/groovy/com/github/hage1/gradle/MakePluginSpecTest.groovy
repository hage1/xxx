package com.github.hage1.gradle

import com.github.hage1.gradle.plugins.PluginPlugin
import org.apache.commons.io.IOUtils
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import spock.lang.Specification

class MakePluginSpecTest extends Specification {
    def toByteArray(Class c) {
        def path = c.name.replace('.', '/') + '.class'
        IOUtils.toByteArray(c.getClassLoader().getResourceAsStream(path))
    }

    def makeStubInstance() {
        def resourceDir = File.createTempFile(this.class.simpleName, 'resources').toPath()
        def impl = new MakePluginSpec(new HashSet<File>(), resourceDir, Logging.getLogger(this.class))
        impl
    }

    def "getAlias from File"() {
        setup:
        def cls = PluginPlugin.class
        def impl = makeStubInstance()
        def file = File.createTempFile(this.class.simpleName, cls.simpleName + '.class')

        def fos = new FileOutputStream(file)
        fos.write(toByteArray(cls))
        fos.close()

        when:
        file.exists()
        then:
        def result = impl.getAlias(file)
        result.key == 'plugin-dev'
        result.value == cls.name
    }

    def "getAlias"() {
        def alias = sampleClassNode().visibleAnnotations.collect { MakePluginSpec.getAlias(it as AnnotationNode) }.find()

        when: alias
        then: alias == 'plugin-dev'
    }

    def "isPlugin"() {
        setup: ClassNode cn = sampleClassNode()
        expect: MakePluginSpec.isPlugin(cn)
    }

    def ClassNode sampleClassNode() {
        def cr = new ClassReader(toByteArray(PluginPlugin.class))
        def cn = new ClassNode()
        cr.accept cn, 0
        cn
    }
}
