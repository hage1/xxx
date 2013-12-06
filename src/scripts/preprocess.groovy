/**
 * Created by wataru on 13/12/05.
 */

import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

PluginClassName = Type.getInternalName(Plugin)
HashMap<String, File> RESULT = []

def isPlugin(ClassNode cn) {
    cn.interfaces.contains PluginClassName
}

def Object getAlias(AnnotationNode ann) {
    if (ann.values[0] == 'alias') {
        def name = ann.values[1]
        assert name
        return name
    }
}


args.each{ fileName ->
    def file = new File(fileName)
    assert(file.exists())
    def byte[] buffer = FileUtils.readFileToByteArray(file)
    def ClassReader cr = new ClassReader(buffer)
    def ClassNode cn = new ClassNode(0)
    cr.accept(cn, 0)

    if (!isPlugin(cn)) {
//        println "not groovy plugin: ${file} ${cn.name}"
        return
    }

    cn.visibleAnnotations.each{AnnotationNode ann ->
        def alias = getAlias ann
        if (alias) {
            RESULT[alias] = file
            println(RESULT)
        }
    }
}